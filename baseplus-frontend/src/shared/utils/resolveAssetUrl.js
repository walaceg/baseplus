function isAbsoluteHttpUrl(value) {
  return /^https?:\/\//i.test(value);
}

function getWindowOrigin() {
  if (typeof window === 'undefined' || !window.location?.origin) {
    return null;
  }

  return window.location.origin;
}

function resolveBaseOrigin(apiBaseURL) {
  const normalizedBase = String(apiBaseURL ?? '').trim();

  if (!normalizedBase) {
    return getWindowOrigin();
  }

  try {
    if (isAbsoluteHttpUrl(normalizedBase)) {
      return new URL(normalizedBase).origin;
    }

    const windowOrigin = getWindowOrigin();
    return windowOrigin ? new URL(normalizedBase, windowOrigin).origin : null;
  } catch {
    return getWindowOrigin();
  }
}

export function resolveAssetUrl(value, apiBaseURL, version) {
  if (value === undefined || value === null) {
    return null;
  }

  const rawValue = String(value).trim();

  if (!rawValue) {
    return null;
  }

  if (rawValue.startsWith('data:') || rawValue.startsWith('blob:')) {
    return rawValue;
  }

  if (isAbsoluteHttpUrl(rawValue)) {
    return appendVersion(rawValue, version);
  }

  if (rawValue.startsWith('/uploads/')) {
    const baseOrigin = resolveBaseOrigin(apiBaseURL);

    if (!baseOrigin) {
      return appendVersion(rawValue, version);
    }

    return appendVersion(new URL(rawValue, baseOrigin).toString(), version);
  }

  return appendVersion(rawValue, version);
}

function appendVersion(value, version) {
  if (version === undefined || version === null || version === '') {
    return value;
  }

  try {
    const resolvedUrl = new URL(value, getWindowOrigin() ?? undefined);
    resolvedUrl.searchParams.set('v', String(version));
    return resolvedUrl.toString();
  } catch {
    return value;
  }
}
