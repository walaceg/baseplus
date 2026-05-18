export function normalizeHex(value) {
  if (typeof value !== 'string') {
    return null;
  }

  const candidate = value.trim().replace(/^#/, '');
  if (!candidate || !/^[0-9a-fA-F]{3}$|^[0-9a-fA-F]{6}$/.test(candidate)) {
    return null;
  }

  const expanded = candidate.length === 3 ? candidate.split('').map((part) => part + part).join('') : candidate;
  return `#${expanded.toLowerCase()}`;
}

export function isValidHex(value) {
  return normalizeHex(value) !== null;
}

export function hexToRgb(value) {
  const normalized = normalizeHex(value);
  if (!normalized) {
    return null;
  }

  const int = Number.parseInt(normalized.slice(1), 16);
  return {
    r: (int >> 16) & 255,
    g: (int >> 8) & 255,
    b: int & 255,
  };
}

export function rgbToHex(rgb = {}) {
  const red = clampChannel(rgb.r);
  const green = clampChannel(rgb.g);
  const blue = clampChannel(rgb.b);

  return `#${[red, green, blue]
    .map((value) => value.toString(16).padStart(2, '0'))
    .join('')}`;
}

export function calculateContrast(firstColor, secondColor) {
  const firstRgb = typeof firstColor === 'string' ? hexToRgb(firstColor) : firstColor;
  const secondRgb = typeof secondColor === 'string' ? hexToRgb(secondColor) : secondColor;

  if (!firstRgb || !secondRgb) {
    return 1;
  }

  const firstLuminance = relativeLuminance(firstRgb);
  const secondLuminance = relativeLuminance(secondRgb);
  const brightest = Math.max(firstLuminance, secondLuminance);
  const darkest = Math.min(firstLuminance, secondLuminance);
  return (brightest + 0.05) / (darkest + 0.05);
}

function relativeLuminance(rgb) {
  const values = [rgb.r, rgb.g, rgb.b].map((value) => {
    const normalized = clampChannel(value) / 255;
    return normalized <= 0.03928 ? normalized / 12.92 : ((normalized + 0.055) / 1.055) ** 2.4;
  });

  return values[0] * 0.2126 + values[1] * 0.7152 + values[2] * 0.0722;
}

function clampChannel(value) {
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return 0;
  }

  return Math.min(255, Math.max(0, Math.round(numeric)));
}
