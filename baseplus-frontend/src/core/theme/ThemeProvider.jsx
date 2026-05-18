import { useCallback, useEffect, useLayoutEffect, useMemo, useState, useSyncExternalStore } from 'react';
import { ThemeContext } from './ThemeContext.js';
import {
  getBrandingRuntimeSnapshot,
  normalizeDensityPreference,
  normalizeThemePreference,
  resolveEffectiveDensity,
  resolveEffectiveTheme,
  subscribeBrandingRuntime,
} from './themeResolution.js';

const defaultTheme = {
  mode: 'light',
  name: 'baseplus',
};

const defaultMenuPrincipal = 'sidebar';
const menuPrincipalStorageKey = 'baseplus.menuPrincipal';
const legacyMenuPrincipalStorageKey = 'baseplus.navigation.mode';

export function ThemeProvider({ children }) {
  const brandingRuntime = useSyncExternalStore(
    subscribeBrandingRuntime,
    getBrandingRuntimeSnapshot,
    getBrandingRuntimeSnapshot,
  );
  const [userThemePreference, setUserThemePreferenceState] = useState(null);
  const [userDensityPreference, setUserDensityPreferenceState] = useState(null);
  const [menuPrincipal, setMenuPrincipalState] = useState(() => readStoredValue(
    menuPrincipalStorageKey,
    legacyMenuPrincipalStorageKey,
    defaultMenuPrincipal,
    normalizeMenuPrincipal,
  ));

  const resolvedTheme = useMemo(() => ({
    density: resolveEffectiveDensity({
      brandingDefaultDensity: brandingRuntime.density,
      userPreferenceDensity: userDensityPreference,
    }),
    mode: resolveEffectiveTheme({
      brandingDefaultTheme: brandingRuntime.theme,
      userPreferenceTheme: userThemePreference,
    }),
  }), [brandingRuntime.density, brandingRuntime.theme, userDensityPreference, userThemePreference]);

  useLayoutEffect(() => {
    if (typeof document === 'undefined') {
      return;
    }

    const root = document.documentElement;
    root.dataset.theme = resolvedTheme.mode;
    root.dataset.density = resolvedTheme.density;
  }, [resolvedTheme.density, resolvedTheme.mode]);

  useEffect(() => {
    if (typeof window === 'undefined') {
      return;
    }

    window.localStorage.setItem(menuPrincipalStorageKey, menuPrincipal);
  }, [menuPrincipal]);

  const setUserDensityPreference = useCallback((nextDensity) => {
    setUserDensityPreferenceState(normalizeDensityPreference(nextDensity));
  }, []);

  const setUserThemePreference = useCallback((nextMode) => {
    setUserThemePreferenceState(normalizeThemePreference(nextMode));
  }, []);

  const setMenuPrincipal = useCallback((nextMode) => {
    const normalized = normalizeMenuPrincipal(nextMode);
    setMenuPrincipalState(normalized);
  }, []);

  const value = useMemo(
    () => ({
      density: resolvedTheme.density,
      effectiveDensity: resolvedTheme.density,
      effectiveTheme: resolvedTheme.mode,
      menuPrincipal,
      setDensity: setUserDensityPreference,
      setMenuPrincipal,
      setMode: setUserThemePreference,
      setUserDensityPreference,
      setUserThemePreference,
      theme: {
        ...defaultTheme,
        mode: resolvedTheme.mode,
      },
      userDensityPreference: userDensityPreference ?? 'APP_DEFAULT',
      userThemePreference: userThemePreference ?? 'APP_DEFAULT',
    }),
    [
      menuPrincipal,
      resolvedTheme.density,
      resolvedTheme.mode,
      setMenuPrincipal,
      setUserDensityPreference,
      setUserThemePreference,
      userDensityPreference,
      userThemePreference,
    ],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

function readStoredValue(primaryKey, legacyKey, fallbackValue, normalize) {
  if (typeof window === 'undefined') {
    return fallbackValue;
  }

  const storedValue = window.localStorage.getItem(primaryKey) ?? window.localStorage.getItem(legacyKey);
  return normalize(storedValue ?? fallbackValue);
}

function normalizeMenuPrincipal(value) {
  return value === 'topbar' ? 'topbar' : defaultMenuPrincipal;
}
