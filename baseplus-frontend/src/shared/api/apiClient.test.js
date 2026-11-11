import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const { httpAdapter } = vi.hoisted(() => ({ httpAdapter: vi.fn() }));

vi.mock('axios', async (importOriginal) => {
  const actual = await importOriginal();
  actual.default.defaults.adapter = (config) => httpAdapter(config);
  return actual;
});

const TOKEN_KEY = 'baseplus.accessToken';
const REFRESH_KEY = 'baseplus.refreshToken';
const SESSION_KEY = 'baseplus.authSessionId';

function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

function ok(config, data = { success: true }) {
  return { config, data, headers: {}, status: 200, statusText: 'OK' };
}

function fail(config, status, data = {}) {
  const error = new Error(`HTTP ${status}`);
  error.config = config;
  error.response = { config, data, headers: {}, status, statusText: `HTTP ${status}` };
  throw error;
}

function responseForRefresh(config, tokens = { token: 'access-2', refreshToken: 'refresh-2' }) {
  return ok(config, { data: tokens });
}

function setLocks(available) {
  let tail = Promise.resolve();
  const locks = available
    ? {
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
      }
    : undefined;
  Object.defineProperty(navigator, 'locks', { configurable: true, value: locks });
}

function dispatchStorageEvents() {
  const original = Storage.prototype.setItem;
  vi.spyOn(Storage.prototype, 'setItem').mockImplementation(function setItem(key, value) {
    original.call(this, key, value);
    window.dispatchEvent(new StorageEvent('storage', { key, newValue: value, storageArea: this }));
  });
}

async function loadClient() {
  const [{ apiClient }, { tokenStorage }] = await Promise.all([
    import('./apiClient.js'),
    import('../storage/tokenStorage.js'),
  ]);
  return { apiClient, tokenStorage };
}

function seedSession(tokenStorage, { accessToken = 'access-1', refreshToken = 'refresh-1', sessionId = 'session-1' } = {}) {
  tokenStorage.setTokens({ accessToken, refreshToken, sessionId });
}

beforeEach(() => {
  window.localStorage.clear();
  setLocks(true);
  httpAdapter.mockReset();
  httpAdapter.mockImplementation(async (config) => ok(config));
});

afterEach(() => {
  vi.useRealTimers();
});

describe('apiClient refresh rotation', () => {
  it('stores the new access and refresh token pair after a 401', async () => {
    const { apiClient, tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    let resourceCalls = 0;
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') return responseForRefresh(config);
      resourceCalls += 1;
      if (config.headers.Authorization !== 'Bearer access-2') return fail(config, 401);
      return ok(config, { value: 'retried' });
    });

    const response = await apiClient.get('/resource');

    expect(response.data.value).toBe('retried');
    expect(tokenStorage.getAccessToken()).toBe('access-2');
    expect(tokenStorage.getRefreshToken()).toBe('refresh-2');
    expect(resourceCalls).toBe(2);
  });

  it('shares one refresh for multiple simultaneous 401 responses in one tab', async () => {
    const { apiClient, tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    const refreshResponse = deferred();
    let refreshCalls = 0;
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1;
        return refreshResponse.promise;
      }
      if (config.headers.Authorization !== 'Bearer access-2') return fail(config, 401);
      return ok(config);
    });

    const requests = [apiClient.get('/one'), apiClient.get('/two'), apiClient.get('/three')];
    await vi.waitFor(() => expect(refreshCalls).toBe(1));
    refreshResponse.resolve(responseForRefresh({ url: '/auth/refresh' }));

    const responses = await Promise.all(requests);
    expect(responses).toHaveLength(3);
    expect(refreshCalls).toBe(1);
    expect(tokenStorage.getRefreshToken()).toBe('refresh-2');
  });

  it.each([
    ['network failure', (config) => { throw Object.assign(new Error('offline'), { config }); }],
    ['HTTP 503', (config) => fail(config, 503)],
  ])('preserves credentials after a transient %s', async (_label, refreshFailure) => {
    const { apiClient, tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') return refreshFailure(config);
      return fail(config, 401);
    });

    await expect(apiClient.get('/resource')).rejects.toBeDefined();
    expect(tokenStorage.getAccessToken()).toBe('access-1');
    expect(tokenStorage.getRefreshToken()).toBe('refresh-1');
  });

  it.each([401, 403])('clears only the corresponding session on definitive refresh HTTP %i', async (status) => {
    const { apiClient, tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    const unauthorized = vi.fn();
    const { setUnauthorizedHandler } = await import('./apiClient.js');
    setUnauthorizedHandler(unauthorized);
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') return fail(config, status);
      return fail(config, 401);
    });

    await expect(apiClient.get('/resource')).rejects.toBeDefined();
    expect(tokenStorage.getRefreshToken()).toBeNull();
    expect(tokenStorage.getAccessToken()).toBeNull();
    expect(unauthorized).toHaveBeenCalledTimes(1);
  });

  it('does not clear a session for an application-level 403', async () => {
    const { apiClient, tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    httpAdapter.mockImplementation(async (config) => fail(config, 403));
    await expect(apiClient.get('/forbidden')).rejects.toBeDefined();
    expect(tokenStorage.getRefreshToken()).toBe('refresh-1');
  });

  it('retries a delayed 401 once with the newer access token', async () => {
    const { apiClient, tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    const delayed401 = deferred();
    let slowConfig;
    let slowCalls = 0;
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') return responseForRefresh(config);
      if (config.url === '/slow' && slowCalls++ === 0) {
        slowConfig = config;
        return delayed401.promise;
      }
      if (config.headers.Authorization === 'Bearer access-2') return ok(config);
      return fail(config, 401);
    });

    const slowRequest = apiClient.get('/slow');
    await vi.waitFor(() => expect(slowCalls).toBe(1));
    await apiClient.get('/trigger');
    delayed401.reject(Object.assign(new Error('late 401'), {
      config: slowConfig,
      response: { config: slowConfig, status: 401, data: {}, headers: {} },
    }));

    await expect(slowRequest).resolves.toBeDefined();
    expect(slowCalls).toBe(2);
  });

  it('does not overwrite a newer login pair when an old refresh response arrives late', async () => {
    const { apiClient, tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    const refreshResponse = deferred();
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') return refreshResponse.promise;
      return fail(config, 401);
    });

    const request = apiClient.get('/resource');
    await vi.waitFor(() => expect(httpAdapter).toHaveBeenCalledWith(expect.objectContaining({ url: '/auth/refresh' })));
    tokenStorage.setTokens({ accessToken: 'access-login', refreshToken: 'refresh-login', sessionId: 'session-login' });
    refreshResponse.resolve(responseForRefresh({ url: '/auth/refresh' }));

    await expect(request).rejects.toBeDefined();
    expect(tokenStorage.getAccessToken()).toBe('access-login');
    expect(tokenStorage.getRefreshToken()).toBe('refresh-login');
  });

  it('does not restore a refresh response invalidated by logout in another tab', async () => {
    const firstTab = await loadClient();
    seedSession(firstTab.tokenStorage);
    vi.resetModules();
    const secondTab = await loadClient();
    const refreshResponse = deferred();
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') return refreshResponse.promise;
      return fail(config, 401);
    });

    const pendingRequest = secondTab.apiClient.get('/resource');
    await vi.waitFor(() => expect(httpAdapter).toHaveBeenCalledWith(expect.objectContaining({ url: '/auth/refresh' })));
    firstTab.tokenStorage.clear();
    refreshResponse.resolve(responseForRefresh({ url: '/auth/refresh' }));

    await expect(pendingRequest).rejects.toBeDefined();
    expect(firstTab.tokenStorage.getAccessToken()).toBeNull();
    expect(firstTab.tokenStorage.getRefreshToken()).toBeNull();
  });

  it('keeps the session identity during rotation and changes it for a new login', async () => {
    const { apiClient, tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') return responseForRefresh(config);
      if (config.headers.Authorization !== 'Bearer access-2') return fail(config, 401);
      return ok(config);
    });

    await apiClient.get('/resource');
    expect(tokenStorage.getSessionId()).toBe('session-1');
    tokenStorage.setTokens({ accessToken: 'access-login', refreshToken: 'refresh-login', sessionId: 'session-login' });
    expect(tokenStorage.getSessionId()).toBe('session-login');
  });

  it('does not refresh or clear a new session after a late remote logout rejection', async () => {
    const { tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    const logoutResponse = deferred();
    let refreshCalls = 0;
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1;
        return responseForRefresh(config);
      }
      if (config.url === '/auth/logout') return logoutResponse.promise;
      return ok(config);
    });

    const { logout } = await import('../../modules/auth/services/authService.js');
    const logoutPromise = logout('captured-old-access');
    tokenStorage.setTokens({ accessToken: 'access-new-login', refreshToken: 'refresh-new-login', sessionId: 'session-new-login' });
    await vi.waitFor(() => expect(httpAdapter).toHaveBeenCalledWith(expect.objectContaining({ url: '/auth/logout' })));
    const logoutConfig = httpAdapter.mock.calls.find(([config]) => config.url === '/auth/logout')[0];
    expect(logoutConfig.headers.Authorization).toBe('Bearer captured-old-access');
    logoutResponse.reject(Object.assign(new Error('logout rejected'), {
      config: logoutConfig,
      response: { config: logoutConfig, status: 401, data: {}, headers: {} },
    }));

    await expect(logoutPromise).rejects.toBeDefined();
    expect(refreshCalls).toBe(0);
    expect(tokenStorage.getAccessToken()).toBe('access-new-login');
    expect(tokenStorage.getRefreshToken()).toBe('refresh-new-login');
  });

  it('serializes two tabs with Web Locks and performs one rotation', async () => {
    const firstTab = await loadClient();
    seedSession(firstTab.tokenStorage);
    vi.resetModules();
    const secondTab = await loadClient();
    let refreshCalls = 0;
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1;
        return responseForRefresh(config);
      }
      if (config.headers.Authorization === 'Bearer access-2') return ok(config);
      return fail(config, 401);
    });

    const responses = await Promise.all([
      firstTab.apiClient.get('/first-tab'),
      secondTab.apiClient.get('/second-tab'),
    ]);

    expect(responses).toHaveLength(2);
    expect(refreshCalls).toBe(1);
    expect(firstTab.tokenStorage.getRefreshToken()).toBe('refresh-2');
  });

  it('recovers both requests across tabs without Web Locks when one rotation wins', async () => {
    setLocks(false);
    dispatchStorageEvents();
    const firstTab = await loadClient();
    seedSession(firstTab.tokenStorage);
    vi.resetModules();
    const secondTab = await loadClient();
    const winningResponse = deferred();
    let refreshCalls = 0;
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1;
        if (refreshCalls === 1) return fail(config, 401);
        return winningResponse.promise;
      }
      if (config.headers.Authorization === 'Bearer access-2') return ok(config);
      return fail(config, 401);
    });

    const requests = [
      firstTab.apiClient.get('/first-tab'),
      secondTab.apiClient.get('/second-tab'),
    ];
    await vi.waitFor(() => expect(refreshCalls).toBe(2));
    winningResponse.resolve(responseForRefresh({ url: '/auth/refresh' }));

    await expect(Promise.all(requests)).resolves.toHaveLength(2);
    expect(firstTab.tokenStorage.getRefreshToken()).toBe('refresh-2');
    expect(refreshCalls).toBe(2);
  });

  it('documents loss of recovery when the winning no-lock rotation exceeds the wait window', async () => {
    setLocks(false);
    dispatchStorageEvents();
    vi.useFakeTimers();
    const firstTab = await loadClient();
    seedSession(firstTab.tokenStorage);
    vi.resetModules();
    const secondTab = await loadClient();
    const winningResponse = deferred();
    const bothRefreshesStarted = deferred();
    let refreshCalls = 0;
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1;
        if (refreshCalls === 2) bothRefreshesStarted.resolve();
        if (refreshCalls === 1) return fail(config, 401);
        return winningResponse.promise;
      }
      return fail(config, 401);
    });

    const requests = [
      firstTab.apiClient.get('/first-tab'),
      secondTab.apiClient.get('/second-tab'),
    ];
    const outcomesPromise = Promise.allSettled(requests);
    await bothRefreshesStarted.promise;
    for (let index = 0; index < 10; index += 1) await Promise.resolve();
    expect(vi.getTimerCount()).toBeGreaterThan(0);
    await vi.advanceTimersByTimeAsync(2000);
    winningResponse.resolve(responseForRefresh({ url: '/auth/refresh' }));

    const outcomes = await outcomesPromise;
    expect(outcomes.every((outcome) => outcome.status === 'rejected')).toBe(true);
    expect(firstTab.tokenStorage.getRefreshToken()).toBeNull();
  });

  it('does not loop when the retried resource still returns 401', async () => {
    const { apiClient, tokenStorage } = await loadClient();
    seedSession(tokenStorage);
    let resourceCalls = 0;
    let refreshCalls = 0;
    httpAdapter.mockImplementation(async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1;
        return responseForRefresh(config);
      }
      resourceCalls += 1;
      return fail(config, 401);
    });

    await expect(apiClient.get('/resource')).rejects.toBeDefined();
    expect(resourceCalls).toBe(2);
    expect(refreshCalls).toBe(1);
  });
});
