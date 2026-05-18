const defaultThemeMode = 'light';
const defaultDensity = 'regular';
const defaultUserPreferenceTheme = 'APP_DEFAULT';
const defaultUserPreferenceDensity = 'APP_DEFAULT';

let brandingRuntime = {
  density: null,
  theme: null,
};

const brandingRuntimeListeners = new Set();

/**
 * Resolves the effective theme and density for the application.
 *
 * Theme precedence:
 * 1. Authenticated user preference, when present.
 * 2. Branding default theme.
 * 3. Light fallback.
 *
 * Density precedence:
 * 1. Authenticated user preference, when present.
 * 2. Branding default density.
 * 3. Regular fallback.
 *
 * This module is the single place that decides which values reach
 * `document.documentElement`, so React effects elsewhere do not compete.
 */
export function resolveEffectiveTheme({ userPreferenceTheme, brandingDefaultTheme } = {}) {
  const normalizedUserPreference = normalizeThemePreference(userPreferenceTheme);
  if (normalizedUserPreference && normalizedUserPreference !== defaultUserPreferenceTheme) {
    return normalizedUserPreference === 'DARK' ? 'dark' : 'light';
  }

  return normalizeThemeMode(brandingDefaultTheme) ?? defaultThemeMode;
}

export function resolveEffectiveDensity({ userPreferenceDensity, brandingDefaultDensity } = {}) {
  const normalizedUserPreference = normalizeDensityPreference(userPreferenceDensity);
  if (normalizedUserPreference && normalizedUserPreference !== defaultUserPreferenceDensity) {
    return normalizedUserPreference === 'COMPACT' ? 'compact' : 'regular';
  }

  return normalizeDensity(brandingDefaultDensity) ?? defaultDensity;
}

export function resolveThemeSettings({
  brandingDensity,
  brandingTheme,
  userDensity,
  userTheme,
} = {}) {
  return {
    density: resolveEffectiveDensity({
      brandingDefaultDensity: brandingDensity,
      userPreferenceDensity: userDensity,
    }),
    mode: resolveEffectiveTheme({
      brandingDefaultTheme: brandingTheme,
      userPreferenceTheme: userTheme,
    }),
  };
}

export function getBrandingRuntimeSnapshot() {
  return brandingRuntime;
}

export function subscribeBrandingRuntime(listener) {
  brandingRuntimeListeners.add(listener);

  return () => {
    brandingRuntimeListeners.delete(listener);
  };
}

export function setBrandingRuntime(nextRuntime = {}) {
  const nextValue = {
    density: normalizeDensity(nextRuntime.density),
    theme: normalizeThemeMode(nextRuntime.theme),
  };

  if (
    brandingRuntime.theme === nextValue.theme &&
    brandingRuntime.density === nextValue.density
  ) {
    return brandingRuntime;
  }

  brandingRuntime = nextValue;
  notifyBrandingRuntimeListeners();

  return brandingRuntime;
}

export function clearBrandingRuntime() {
  return setBrandingRuntime({ density: null, theme: null });
}

export function normalizeThemeMode(value) {
  return value === 'dark' ? 'dark' : value === 'light' ? 'light' : null;
}

export function normalizeDensity(value) {
  return value === 'compact' ? 'compact' : value === 'regular' ? 'regular' : null;
}

export function normalizeThemePreference(value) {
  const normalized = typeof value === 'string' ? value.trim().toUpperCase() : '';
  if (normalized === 'APP_DEFAULT' || normalized === 'LIGHT' || normalized === 'DARK') {
    return normalized;
  }
  return null;
}

export function normalizeDensityPreference(value) {
  const normalized = typeof value === 'string' ? value.trim().toUpperCase() : '';
  if (normalized === 'APP_DEFAULT' || normalized === 'REGULAR' || normalized === 'COMPACT') {
    return normalized;
  }

  return null;
}

function notifyBrandingRuntimeListeners() {
  brandingRuntimeListeners.forEach((listener) => {
    listener();
  });
}
