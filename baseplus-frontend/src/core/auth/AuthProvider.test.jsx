import React from 'react';
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthContext } from './AuthContext.js';
import { ThemeContext } from '../theme/ThemeContext.js';

const { httpAdapter } = vi.hoisted(() => ({ httpAdapter: vi.fn() }));

vi.mock('axios', async (importOriginal) => {
  const actual = await importOriginal();
  actual.default.defaults.adapter = (config) => httpAdapter(config);
  return actual;
});

function deferred() {
  let resolve;
  const promise = new Promise((resolvePromise) => { resolve = resolvePromise; });
  return { promise, resolve };
}

function ok(config, data) {
  return { config, data, headers: {}, status: 200, statusText: 'OK' };
}

function installWebLock() {
  let tail = Promise.resolve();
  Object.defineProperty(navigator, 'locks', {
    configurable: true,
    value: {
      request: async (_name, callback) => {
        const previous = tail;
        let release;
        tail = new Promise((resolve) => { release = resolve; });
        await previous;
        try {
          return await callback();
        } finally {
          release();
        }
      },
    },
  });
}

function dispatchStorageEvent(key, newValue = null) {
  window.dispatchEvent(new StorageEvent('storage', {
    key,
    newValue,
    storageArea: window.localStorage,
  }));
}

function installAuthenticatedAdapter() {
  httpAdapter.mockImplementation(async (config) => {
    if (config.url === '/auth/me') {
      const name = config.headers.Authorization === 'Bearer access-new' ? 'Usuário novo' : 'Usuário atual';
      return ok(config, { data: { id: 7, nome: name, mustChangePassword: false } });
    }
    return ok(config, { data: {} });
  });
}

async function mountAuthProvider() {
  const [{ AuthProvider }, { tokenStorage }] = await Promise.all([
    import('./AuthProvider.jsx'),
    import('../../shared/storage/tokenStorage.js'),
  ]);
  let authState;
  function AuthProbe() {
    return (
      <AuthContext.Consumer>
        {(auth) => {
          authState = auth;
          return (
            <div
              data-testid="auth-state"
              data-authenticated={String(auth.isAuthenticated)}
              data-access-token={auth.accessToken ?? ''}
              data-loading={String(auth.isLoading)}
            >
              {auth.user?.nome ?? ''}
            </div>
          );
        }}
      </AuthContext.Consumer>
    );
  }
  const theme = { setDensity: vi.fn(), setMode: vi.fn() };
  const view = render(
    <ThemeContext.Provider value={theme}>
      <AuthProvider><AuthProbe /></AuthProvider>
    </ThemeContext.Provider>,
  );
  return { ...view, getAuth: () => authState, tokenStorage };
}

beforeEach(() => {
  window.localStorage.clear();
  installWebLock();
  httpAdapter.mockReset();
});

afterEach(() => {
  cleanup();
});

describe('AuthProvider refresh/logout race', () => {
  it('does not restore tokens when logout happens during a pending startup refresh', async () => {
    const refresh = deferred();
    window.localStorage.setItem('baseplus.refreshToken', 'refresh-before-logout');
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') return refresh.promise;
      if (config.url === '/auth/me') return ok(config, { data: { id: 7, nome: 'Usuário' } });
      return ok(config, { data: {} });
    });

    const [{ AuthProvider }, { tokenStorage }] = await Promise.all([
      import('./AuthProvider.jsx'),
      import('../../shared/storage/tokenStorage.js'),
    ]);
    const theme = { setDensity: vi.fn(), setMode: vi.fn() };
    function SignOutControl() {
      return (
        <AuthContext.Consumer>
          {(auth) => <button onClick={() => { window.signOutPromise = auth.signOut(); }}>Sair</button>}
        </AuthContext.Consumer>
      );
    }

    render(
      <ThemeContext.Provider value={theme}>
        <AuthProvider><SignOutControl /></AuthProvider>
      </ThemeContext.Provider>,
    );

    await waitFor(() => expect(httpAdapter).toHaveBeenCalledWith(expect.objectContaining({ url: '/auth/refresh' })));
    fireEvent.click(screen.getByRole('button', { name: 'Sair' }));
    await act(async () => { await window.signOutPromise; });
    expect(tokenStorage.getRefreshToken()).toBeNull();

    await act(async () => {
      refresh.resolve(ok({ url: '/auth/refresh' }, { data: { token: 'access-late', refreshToken: 'refresh-late' } }));
      await refresh.promise;
    });

    await waitFor(() => expect(tokenStorage.getRefreshToken()).toBeNull());
    expect(tokenStorage.getAccessToken()).toBeNull();
  });

  it('clears the authenticated React state when another tab logs out', async () => {
    installAuthenticatedAdapter();
    window.localStorage.setItem('baseplus.accessToken', 'access-1');
    window.localStorage.setItem('baseplus.refreshToken', 'refresh-1');
    window.localStorage.setItem('baseplus.authSessionId', 'session-1');
    const provider = await mountAuthProvider();

    await waitFor(() => expect(screen.getByTestId('auth-state').dataset.authenticated).toBe('true'));
    window.localStorage.clear();
    dispatchStorageEvent('baseplus.accessToken');

    await waitFor(() => {
      expect(screen.getByTestId('auth-state').dataset.authenticated).toBe('false');
      expect(screen.getByTestId('auth-state').dataset.accessToken).toBe('');
      expect(screen.getByTestId('auth-state').dataset.loading).toBe('false');
      expect(provider.getAuth().user).toBeNull();
    });
  });

  it('ignores storage events unrelated to authentication', async () => {
    installAuthenticatedAdapter();
    window.localStorage.setItem('baseplus.accessToken', 'access-1');
    window.localStorage.setItem('baseplus.refreshToken', 'refresh-1');
    window.localStorage.setItem('baseplus.authSessionId', 'session-1');
    const provider = await mountAuthProvider();

    await waitFor(() => expect(screen.getByTestId('auth-state').dataset.authenticated).toBe('true'));
    window.localStorage.setItem('unrelated.preference', 'value');
    dispatchStorageEvent('unrelated.preference', 'value');

    expect(screen.getByTestId('auth-state').dataset.authenticated).toBe('true');
    expect(provider.getAuth().user.nome).toBe('Usuário atual');
  });

  it('does not restore an old session when its pending refresh completes after cross-tab logout', async () => {
    const refresh = deferred();
    window.localStorage.setItem('baseplus.refreshToken', 'refresh-before-logout');
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') return refresh.promise;
      return ok(config, { data: { id: 7, nome: 'Usuário antigo' } });
    });
    const provider = await mountAuthProvider();

    await waitFor(() => expect(httpAdapter).toHaveBeenCalledWith(expect.objectContaining({ url: '/auth/refresh' })));
    window.localStorage.clear();
    dispatchStorageEvent('baseplus.refreshToken');
    await waitFor(() => expect(screen.getByTestId('auth-state').dataset.authenticated).toBe('false'));

    await act(async () => {
      refresh.resolve(ok({ url: '/auth/refresh' }, { data: { token: 'access-late', refreshToken: 'refresh-late' } }));
      await refresh.promise;
    });

    expect(provider.tokenStorage.getAccessToken()).toBeNull();
    expect(provider.tokenStorage.getRefreshToken()).toBeNull();
    expect(provider.getAuth().user).toBeNull();
  });

  it('hydrates the current login instead of clearing it for a queued event from an older session', async () => {
    installAuthenticatedAdapter();
    window.localStorage.setItem('baseplus.accessToken', 'access-1');
    window.localStorage.setItem('baseplus.refreshToken', 'refresh-1');
    window.localStorage.setItem('baseplus.authSessionId', 'session-1');
    const provider = await mountAuthProvider();

    await waitFor(() => expect(screen.getByTestId('auth-state').textContent).toBe('Usuário atual'));
    provider.tokenStorage.setTokens({
      accessToken: 'access-new',
      refreshToken: 'refresh-new',
      sessionId: 'session-new',
    });
    dispatchStorageEvent('baseplus.accessToken');

    await waitFor(() => {
      expect(screen.getByTestId('auth-state').dataset.authenticated).toBe('true');
      expect(screen.getByTestId('auth-state').dataset.accessToken).toBe('access-new');
      expect(screen.getByTestId('auth-state').textContent).toBe('Usuário novo');
    });
  });

  it('removes the storage listener when the provider unmounts', async () => {
    installAuthenticatedAdapter();
    window.localStorage.setItem('baseplus.accessToken', 'access-1');
    window.localStorage.setItem('baseplus.refreshToken', 'refresh-1');
    window.localStorage.setItem('baseplus.authSessionId', 'session-1');
    const addListener = vi.spyOn(window, 'addEventListener');
    const removeListener = vi.spyOn(window, 'removeEventListener');
    const provider = await mountAuthProvider();
    await waitFor(() => expect(screen.getByTestId('auth-state').dataset.authenticated).toBe('true'));

    const storageListener = addListener.mock.calls.find(([type]) => type === 'storage')?.[1];
    expect(storageListener).toBeTypeOf('function');
    provider.unmount();

    expect(removeListener).toHaveBeenCalledWith('storage', storageListener);
    addListener.mockRestore();
    removeListener.mockRestore();
  });
});
