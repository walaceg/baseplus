import { apiBaseURL } from '../api/apiClient.js';
import { hexToRgb as parseHexToRgb, normalizeHex, rgbToHex as formatRgbToHex } from '../utils/color/index.js';

let faviconUpdateSequence = 0;

export const defaultBranding = {
  nomePlataforma: 'Base+',
  subtituloInstitucional: 'Painel administrativo',
  tema: 'light',
  corPrimaria: '#2563eb',
  corSecundaria: '#0f172a',
  densidadeVisual: 'regular',
  loginBackgroundMode: 'DEFAULT',
  logoUrl: '',
  compactLogoUrl: '',
  faviconUrl: '',
  loginLogoUrl: '',
  loginBackgroundUrl: '',
  whiteLabelEnabled: false,
  whiteLabelName: '',
  whiteLabelSubtitle: '',
};

export const brandingPresets = [
  {
    id: 'classico-baseplus',
    name: 'Clássico Base+',
    tema: 'light',
    corPrimaria: '#2563eb',
    corSecundaria: '#0f172a',
  },
  {
    id: 'noite-azul',
    name: 'Noite Azul',
    tema: 'dark',
    corPrimaria: '#60a5fa',
    corSecundaria: '#e2e8f0',
  },
  {
    id: 'grafite-ciano',
    name: 'Grafite Ciano',
    tema: 'dark',
    corPrimaria: '#38bdf8',
    corSecundaria: '#cbd5e1',
  },
  {
    id: 'operacao-verde',
    name: 'Operação Verde',
    tema: 'light',
    corPrimaria: '#0f766e',
    corSecundaria: '#134e4a',
  },
];

export function normalizeBrandingSettings(settings = {}) {
  return {
    ...defaultBranding,
    nomePlataforma: normalizeText(settings.nomePlataforma, defaultBranding.nomePlataforma),
    subtituloInstitucional: normalizeText(settings.subtituloInstitucional, defaultBranding.subtituloInstitucional),
    tema: normalizeBrandingTheme(settings.tema),
    corPrimaria: normalizeHexColor(settings.corPrimaria) ?? defaultBranding.corPrimaria,
    corSecundaria: normalizeHexColor(settings.corSecundaria) ?? defaultBranding.corSecundaria,
    densidadeVisual: settings.densidadeVisual === 'compact' ? 'compact' : defaultBranding.densidadeVisual,
    loginBackgroundMode: normalizeLoginBackgroundMode(settings.loginBackgroundMode),
    logoUrl: normalizeLogoUrl(settings.logoUrl),
    compactLogoUrl: normalizeCompactLogoUrl(settings.compactLogoUrl),
    faviconUrl: normalizeFaviconUrl(settings.faviconUrl),
    loginLogoUrl: normalizeLogoUrl(settings.loginLogoUrl),
    loginBackgroundUrl: normalizeLoginBackgroundUrl(settings.loginBackgroundUrl),
    whiteLabelEnabled: Boolean(settings.whiteLabelEnabled),
    whiteLabelName: normalizeOptionalText(settings.whiteLabelName),
    whiteLabelSubtitle: normalizeOptionalText(settings.whiteLabelSubtitle),
  };
}

export function applyBrandingSettings(settings = {}) {
  if (typeof document === 'undefined') {
    return;
  }

  const branding = normalizeBrandingSettings(settings);
  const root = document.documentElement;
  const visualTokens = buildBrandingVisualTokens(branding, settings.assetVersion);

  root.dataset.loginBackgroundMode = branding.loginBackgroundMode;
  root.dataset.loginBackgroundHasImage = branding.loginBackgroundUrl ? 'true' : 'false';
  applyVisualTokens(root.style, visualTokens);
  document.title = branding.nomePlataforma;
}

export async function applyBrandingFavicon(settings = {}) {
  if (typeof document === 'undefined') {
    return;
  }

  const currentSequence = ++faviconUpdateSequence;
  const branding = normalizeBrandingSettings(settings);
  const fallbackHref = buildBrandingFaviconFallback(branding);
  const faviconLink = ensureFaviconLink();
  const sources = [
    resolveBrandingAssetUrl(branding.faviconUrl, settings.assetVersion),
    resolveBrandingAssetUrl(branding.logoUrl, settings.assetVersion),
  ].filter(Boolean);

  setFaviconHref(faviconLink, fallbackHref, 'image/svg+xml');

  for (const source of sources) {
    const resolved = await validateImageSource(source);
    if (currentSequence !== faviconUpdateSequence) {
      return;
    }

    if (resolved) {
      setFaviconHref(faviconLink, source);
      return;
    }
  }
}

export function buildBrandingPreviewStyle(settings = {}) {
  const branding = normalizeBrandingSettings(settings);
  const visualTokens = buildBrandingVisualTokens(branding, settings.assetVersion);

  return {
    ...visualTokens,
    colorScheme: branding.tema,
  };
}

export function isPresetActive(settings = {}, preset = {}) {
  const branding = normalizeBrandingSettings(settings);
  return (
    branding.tema === preset.tema &&
    branding.corPrimaria === normalizeHexColor(preset.corPrimaria) &&
    branding.corSecundaria === normalizeHexColor(preset.corSecundaria)
  );
}

export function resetBrandingSettings() {
  if (typeof document === 'undefined') {
    return;
  }

  const root = document.documentElement;
  delete root.dataset.loginBackgroundMode;
  delete root.dataset.loginBackgroundHasImage;
  root.style.removeProperty('--brand-platform-name');
  root.style.removeProperty('--brand-platform-subtitle');
  root.style.removeProperty('--color-primary');
  root.style.removeProperty('--color-primary-hover');
  root.style.removeProperty('--color-primary-muted');
  root.style.removeProperty('--color-secondary');
  root.style.removeProperty('--color-secondary-hover');
  root.style.removeProperty('--color-secondary-muted');
  root.style.removeProperty('--color-primary-text');
  root.style.removeProperty('--brand-login-background-mode');
  root.style.removeProperty('--brand-login-background-image');
  root.style.removeProperty('--brand-login-background-image-has');
  root.style.removeProperty('--brand-login-background-base');
  root.style.removeProperty('--brand-login-background-overlay');
  root.style.removeProperty('--brand-login-background-grid-opacity');
  root.style.removeProperty('--brand-login-background-panel');
  root.style.removeProperty('--brand-login-background-panel-border');
  root.style.removeProperty('--brand-login-background-panel-shadow');
  root.style.removeProperty('--brand-login-background-chip-shadow');
  root.style.removeProperty('--brand-login-background-chrome-shadow');
  document.title = defaultBranding.nomePlataforma;
}

export function resetBrandingFavicon() {
  if (typeof document === 'undefined') {
    return;
  }

  faviconUpdateSequence += 1;
  const faviconLink = ensureFaviconLink();
  setFaviconHref(faviconLink, buildBrandingFaviconFallback(defaultBranding), 'image/svg+xml');
}

export function normalizeHexColor(value) {
  return normalizeHex(value);
}

export function normalizeLogoUrl(value) {
  return typeof value === 'string' && value.trim() ? value.trim() : '';
}

export function normalizeLoginBackgroundUrl(value) {
  return typeof value === 'string' && value.trim() ? value.trim() : '';
}

export function resolveBrandingAssetUrl(value, version = 0) {
  const normalized = normalizeLogoUrl(value);
  if (!normalized) {
    return '';
  }

  const absoluteUrl = buildAbsoluteAssetUrl(normalized);
  const cacheBust = normalizeAssetVersion(version);
  if (!cacheBust) {
    return absoluteUrl;
  }

  return appendQueryParameter(absoluteUrl, 'v', cacheBust);
}

export function normalizeFaviconUrl(value) {
  return normalizeLogoUrl(value);
}

export function normalizeCompactLogoUrl(value) {
  return normalizeLogoUrl(value);
}

export function getBrandingInitials(name = '') {
  const normalized = typeof name === 'string' ? name.trim() : '';
  if (!normalized) {
    return 'B+';
  }

  const parts = normalized
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2);

  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase();
  }

  return parts.map((part) => part.charAt(0).toUpperCase()).join('');
}

function normalizeText(value, fallback) {
  return typeof value === 'string' && value.trim() ? value.trim() : fallback;
}

function normalizeOptionalText(value) {
  return typeof value === 'string' && value.trim() ? value.trim() : '';
}

function normalizeLoginBackgroundMode(value) {
  const normalized = typeof value === 'string' ? value.trim().toUpperCase() : '';
  if (normalized === 'INSTITUTIONAL_GRADIENT' || normalized === 'INSTITUTIONAL') {
    return 'INSTITUTIONAL_GRADIENT';
  }

  if (normalized === 'NEUTRAL_SURFACE' || normalized === 'NEUTRAL') {
    return 'NEUTRAL_SURFACE';
  }

  return 'DEFAULT';
}

function normalizeBrandingTheme(value) {
  const normalized = typeof value === 'string' ? value.trim().toUpperCase() : '';
  if (normalized === 'DARK') {
    return 'dark';
  }

  if (normalized === 'LIGHT') {
    return 'light';
  }

  return value === 'dark' ? 'dark' : 'light';
}

function buildAbsoluteAssetUrl(value) {
  if (isAbsoluteUrl(value) || value.startsWith('data:') || value.startsWith('blob:')) {
    return value;
  }

  if (value.startsWith('/')) {
    return `${normalizeApiBaseURL()}${value}`;
  }

  return `${normalizeApiBaseURL()}/${value.replace(/^\/+/, '')}`;
}

function normalizeApiBaseURL() {
  return apiBaseURL.replace(/\/+$/, '');
}

function isAbsoluteUrl(value) {
  return /^https?:\/\//i.test(value);
}

function normalizeAssetVersion(value) {
  if (value === null || value === undefined || value === '') {
    return '';
  }

  return String(value).trim();
}

function appendQueryParameter(url, key, value) {
  const separator = url.includes('?') ? '&' : '?';
  return `${url}${separator}${encodeURIComponent(key)}=${encodeURIComponent(value)}`;
}

function buildBrandingFaviconFallback(branding) {
  const initials = getBrandingInitials(branding.nomePlataforma);
  const primary = normalizeHexColor(branding.corPrimaria) ?? defaultBranding.corPrimaria;
  const secondary = normalizeHexColor(branding.corSecundaria) ?? defaultBranding.corSecundaria;
  const textColor = branding.tema === 'dark' ? '#f8fafc' : '#ffffff';
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" role="img" aria-label="${escapeXml(
      branding.nomePlataforma || defaultBranding.nomePlataforma,
    )}">
      <defs>
        <linearGradient id="brand" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="${primary}" />
          <stop offset="100%" stop-color="${secondary}" />
        </linearGradient>
      </defs>
      <rect width="64" height="64" rx="16" fill="url(#brand)" />
      <circle cx="48" cy="16" r="18" fill="${withAlpha(secondary, branding.tema === 'dark' ? 0.2 : 0.14)}" />
      <text
        x="32"
        y="39"
        text-anchor="middle"
        font-family="Inter, Arial, sans-serif"
        font-size="24"
        font-weight="800"
        fill="${textColor}"
      >${escapeXml(initials)}</text>
    </svg>
  `.trim();

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
}

function ensureFaviconLink() {
  const head = document.head;
  const existing = head.querySelector('link[data-baseplus-favicon="true"]');
  if (existing) {
    return existing;
  }

  const iconLinks = Array.from(head.querySelectorAll('link[rel~="icon"]'));
  const link = iconLinks.find((item) => item.dataset.baseplusFavicon === 'true') ?? iconLinks[0] ?? document.createElement('link');

  if (!link.parentNode) {
    link.rel = 'icon';
    head.appendChild(link);
  }

  link.dataset.baseplusFavicon = 'true';

  iconLinks.forEach((item) => {
    if (item !== link && item.dataset.baseplusFavicon !== 'true') {
      item.remove();
    }
  });

  return link;
}

function setFaviconHref(link, href, type) {
  if (!link) {
    return;
  }

  link.rel = 'icon';
  link.href = href;
  if (type) {
    link.type = type;
  } else {
    link.removeAttribute('type');
  }
}

function validateImageSource(src) {
  return new Promise((resolve) => {
    if (!src) {
      resolve(false);
      return;
    }

    const image = new Image();
    let settled = false;

    const finish = (result) => {
      if (!settled) {
        settled = true;
        resolve(result);
      }
    };

    image.onload = () => finish(true);
    image.onerror = () => finish(false);
    image.referrerPolicy = 'no-referrer';
    image.src = src;

    if (image.complete) {
      finish(image.naturalWidth > 0);
    }
  });
}

function escapeXml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&apos;');
}

function buildBrandingVisualTokens(branding, assetVersion = 0) {
  const loginBackgroundTokens = buildLoginBackgroundTokens(branding, assetVersion);

  return {
    '--brand-platform-name': branding.nomePlataforma,
    '--brand-platform-subtitle': branding.subtituloInstitucional,
    '--color-primary': branding.corPrimaria,
    '--color-primary-hover': shadeColor(branding.corPrimaria, branding.tema === 'dark' ? 0.18 : -0.08),
    '--color-primary-muted': tintColor(branding.corPrimaria, branding.tema === 'dark' ? 0.18 : 0.88),
    '--color-secondary': branding.corSecundaria,
    '--color-secondary-hover': shadeColor(branding.corSecundaria, branding.tema === 'dark' ? 0.18 : -0.08),
    '--color-secondary-muted': tintColor(branding.corSecundaria, branding.tema === 'dark' ? 0.18 : 0.88),
    '--color-primary-text': branding.tema === 'dark' ? '#0b1220' : '#ffffff',
    '--color-focus-ring': withAlpha(branding.corPrimaria, branding.tema === 'dark' ? 0.28 : 0.24),
    ...loginBackgroundTokens,
  };
}

function buildLoginBackgroundTokens(branding, assetVersion = 0) {
  const loginBackgroundImage = buildLoginBackgroundImageToken(branding.loginBackgroundUrl, assetVersion);
  const hasBackgroundImage = loginBackgroundImage !== 'none';
  const primaryGlow = withAlpha(branding.corPrimaria, branding.tema === 'dark' ? 0.24 : 0.18);
  const secondaryGlow = withAlpha(branding.corSecundaria, branding.tema === 'dark' ? 0.18 : 0.12);
  const primaryHalo = withAlpha(branding.corPrimaria, branding.tema === 'dark' ? 0.16 : 0.1);
  const secondaryHalo = withAlpha(branding.corSecundaria, branding.tema === 'dark' ? 0.16 : 0.08);
  const baseSurface = branding.tema === 'dark'
    ? 'color-mix(in srgb, var(--color-surface) 92%, transparent)'
    : 'color-mix(in srgb, var(--color-surface) 97%, transparent)';
  const neutralSurface = branding.tema === 'dark'
    ? 'color-mix(in srgb, var(--color-surface) 96%, transparent)'
    : 'color-mix(in srgb, var(--color-background) 94%, transparent)';

  if (branding.loginBackgroundMode === 'INSTITUTIONAL_GRADIENT') {
    return {
      '--brand-login-background-mode': branding.loginBackgroundMode,
      '--brand-login-background-image': loginBackgroundImage,
      '--brand-login-background-image-has': hasBackgroundImage ? 'true' : 'false',
      '--brand-login-background-base': `radial-gradient(circle at 14% 14%, ${primaryGlow}, transparent 34%), radial-gradient(circle at 86% 82%, ${secondaryGlow}, transparent 30%), linear-gradient(145deg, color-mix(in srgb, var(--color-primary-muted) 34%, var(--color-surface) 66%), color-mix(in srgb, var(--color-background) 84%, transparent))`,
      '--brand-login-background-overlay': `linear-gradient(160deg, ${primaryHalo}, ${secondaryHalo})`,
      '--brand-login-background-grid-opacity': branding.tema === 'dark' ? '0.38' : '0.24',
      '--brand-login-background-panel': `color-mix(in srgb, var(--color-surface) 86%, transparent)`,
      '--brand-login-background-panel-border': 'color-mix(in srgb, var(--color-border) 78%, transparent)',
      '--brand-login-background-panel-shadow': branding.tema === 'dark' ? '0 22px 48px rgba(0, 0, 0, 0.28)' : '0 22px 42px rgba(16, 24, 40, 0.14)',
      '--brand-login-background-chip-shadow': `0 0 0 4px ${primaryHalo}`,
      '--brand-login-background-chrome-shadow': `0 1px 0 ${secondaryHalo}`,
    };
  }

  if (branding.loginBackgroundMode === 'NEUTRAL_SURFACE') {
    return {
      '--brand-login-background-mode': branding.loginBackgroundMode,
      '--brand-login-background-image': loginBackgroundImage,
      '--brand-login-background-image-has': hasBackgroundImage ? 'true' : 'false',
      '--brand-login-background-base': `linear-gradient(135deg, ${neutralSurface}, color-mix(in srgb, var(--color-background) 94%, transparent))`,
      '--brand-login-background-overlay': `radial-gradient(circle at 50% 0%, ${primaryHalo}, transparent 44%)`,
      '--brand-login-background-grid-opacity': branding.tema === 'dark' ? '0.16' : '0.1',
      '--brand-login-background-panel': `color-mix(in srgb, var(--color-surface) 95%, transparent)`,
      '--brand-login-background-panel-border': 'color-mix(in srgb, var(--color-border) 72%, transparent)',
      '--brand-login-background-panel-shadow': branding.tema === 'dark' ? '0 18px 40px rgba(0, 0, 0, 0.22)' : '0 18px 34px rgba(16, 24, 40, 0.1)',
      '--brand-login-background-chip-shadow': `0 0 0 4px ${secondaryHalo}`,
      '--brand-login-background-chrome-shadow': `0 1px 0 ${primaryHalo}`,
    };
  }

  return {
    '--brand-login-background-mode': branding.loginBackgroundMode,
    '--brand-login-background-image': loginBackgroundImage,
    '--brand-login-background-image-has': hasBackgroundImage ? 'true' : 'false',
    '--brand-login-background-base': `radial-gradient(circle at 12% 8%, ${primaryGlow}, transparent 36%), radial-gradient(circle at 88% 84%, ${secondaryGlow}, transparent 30%), linear-gradient(135deg, ${baseSurface}, color-mix(in srgb, var(--color-background) 88%, transparent))`,
    '--brand-login-background-overlay': `linear-gradient(155deg, ${primaryHalo}, ${secondaryHalo})`,
    '--brand-login-background-grid-opacity': branding.tema === 'dark' ? '0.28' : '0.18',
    '--brand-login-background-panel': `color-mix(in srgb, var(--color-surface) 90%, transparent)`,
    '--brand-login-background-panel-border': 'color-mix(in srgb, var(--color-border) 76%, transparent)',
    '--brand-login-background-panel-shadow': branding.tema === 'dark' ? '0 20px 44px rgba(0, 0, 0, 0.24)' : '0 20px 38px rgba(16, 24, 40, 0.12)',
    '--brand-login-background-chip-shadow': `0 0 0 4px ${primaryHalo}`,
    '--brand-login-background-chrome-shadow': `0 1px 0 ${secondaryHalo}`,
  };
}

function applyVisualTokens(style, tokens) {
  Object.entries(tokens).forEach(([key, value]) => {
    style.setProperty(key, value);
  });
}

function expandHexColor(value) {
  return normalizeHex(value) ?? value;
}

function shadeColor(hex, amount) {
  const rgb = parseHexToRgb(expandHexColor(hex));
  if (!rgb) {
    return hex;
  }

  const factor = 1 + amount;
  return rgbToHex(
    clamp(rgb.r * factor),
    clamp(rgb.g * factor),
    clamp(rgb.b * factor),
  );
}

function tintColor(hex, amount) {
  const rgb = parseHexToRgb(expandHexColor(hex));
  if (!rgb) {
    return hex;
  }

  return rgbToHex(
    clamp(rgb.r + (255 - rgb.r) * amount),
    clamp(rgb.g + (255 - rgb.g) * amount),
    clamp(rgb.b + (255 - rgb.b) * amount),
  );
}

function rgbToHex(r, g, b) {
  return formatRgbToHex({ r, g, b });
}

function withAlpha(hex, alpha) {
  const rgb = parseHexToRgb(hex);
  const safeAlpha = clampAlpha(alpha);

  if (!rgb) {
    return `rgba(37, 99, 235, ${safeAlpha})`;
  }

  return `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${safeAlpha})`;
}

function clampAlpha(value) {
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return 1;
  }

  return Math.min(1, Math.max(0, numeric));
}

function clamp(value) {
  return Math.max(0, Math.min(255, value));
}

function buildLoginBackgroundImageToken(value, assetVersion = 0) {
  const normalized = normalizeLoginBackgroundUrl(value);
  if (!normalized) {
    return 'none';
  }

  return `url("${escapeCssUrl(resolveBrandingAssetUrl(normalized, assetVersion))}")`;
}

function escapeCssUrl(value) {
  return String(value).replaceAll('\\', '\\\\').replaceAll('"', '\\"');
}
