import { useEffect, useMemo, useRef, useState } from 'react';
import { Badge, Button, Input } from '../../index.js';
import { calculateContrast, hexToRgb, normalizeHex, rgbToHex } from '../../../utils/color/index.js';
import './BrandingColorPicker.css';

const DEFAULT_PRESETS = [
  { label: 'Base+', value: '#2563EB' },
  { label: 'Azul oceano', value: '#0EA5E9' },
  { label: 'Índigo', value: '#4F46E5' },
  { label: 'Verde', value: '#0F766E' },
  { label: 'Grafite', value: '#334155' },
  { label: 'Vermelho', value: '#B42318' },
];

const DEFAULT_HEX = '#2563eb';
const HEX_ERROR_MESSAGE = 'Use um hexadecimal válido em #RGB ou #RRGGBB.';

export function BrandingColorPicker({
  id,
  label,
  presets = DEFAULT_PRESETS,
  value,
  onChange,
  onValidityChange,
  hint = 'Seleção visual, HEX e RGB sincronizados.',
}) {
  const rootRef = useRef(null);
  const squareRef = useRef(null);
  const draggingRef = useRef(false);
  const lastValidHexRef = useRef(normalizeHex(value) ?? DEFAULT_HEX);
  const externalHex = useMemo(() => normalizeHex(value) ?? DEFAULT_HEX, [value]);

  const [isOpen, setIsOpen] = useState(false);
  const [isHexFocused, setIsHexFocused] = useState(false);
  const [hexDraft, setHexDraft] = useState(lastValidHexRef.current.toUpperCase());
  const [localError, setLocalError] = useState('');
  const [color, setColor] = useState(() => createColorState(lastValidHexRef.current));

  useEffect(() => {
    if (isHexFocused || localError || externalHex === lastValidHexRef.current) {
      return;
    }

    lastValidHexRef.current = externalHex;
    setColor(createColorState(externalHex));
    setHexDraft(externalHex.toUpperCase());
    setLocalError('');
  }, [externalHex, isHexFocused, localError]);

  useEffect(() => {
    function handleOutsideClick(event) {
      if (rootRef.current && !rootRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }

    function handleEscape(event) {
      if (event.key === 'Escape') {
        setIsOpen(false);
      }
    }

    document.addEventListener('mousedown', handleOutsideClick);
    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  useEffect(() => {
    function handleMove(event) {
      if (!draggingRef.current) {
        return;
      }

      updateFromPointer(event.clientX, event.clientY);
    }

    function handleUp() {
      draggingRef.current = false;
    }

    window.addEventListener('pointermove', handleMove);
    window.addEventListener('pointerup', handleUp);

    return () => {
      window.removeEventListener('pointermove', handleMove);
      window.removeEventListener('pointerup', handleUp);
    };
  }, []);

  const contrast = useMemo(() => {
    const surface = readCssVariable('--color-surface', '#ffffff');
    const ratio = calculateContrast(color.hex, surface);
    return {
      ratio,
      meetsAA: ratio >= 4.5,
    };
  }, [color.hex]);

  const accessibleTextColor = useMemo(() => {
    const primaryText = readCssVariable('--color-primary-text', '#ffffff');
    const text = readCssVariable('--color-text', '#1f2933');
    return calculateContrast(color.hex, primaryText) >= calculateContrast(color.hex, text) ? primaryText : text;
  }, [color.hex]);

  function commitHex(nextHex) {
    const normalized = normalizeHex(nextHex);
    if (!normalized) {
      return;
    }

    const nextColor = createColorState(normalized);
    lastValidHexRef.current = normalized;
    setColor(nextColor);
    setHexDraft(nextColor.hexDisplay);
    setLocalError('');
    setIsHexFocused(false);
    onValidityChange?.(true);
    onChange?.(normalized);
  }

  function revertToLastValid(message) {
    setLocalError(message);
    onValidityChange?.(false);
  }

  function applyPreset(preset) {
    if (isHexFocused) {
      return;
    }

    const normalized = normalizeHex(preset.value) ?? DEFAULT_HEX;
    const nextColor = createColorState(normalized);
    lastValidHexRef.current = normalized;
    setColor(nextColor);
    setHexDraft(nextColor.hexDisplay);
    setLocalError('');
    setIsHexFocused(false);
    onValidityChange?.(true);
    onChange?.(normalized);
  }

  function updateFromPointer(clientX, clientY) {
    if (isHexFocused) {
      return;
    }

    const square = squareRef.current;
    if (!square) {
      return;
    }

    const rect = square.getBoundingClientRect();
    const saturation = clamp(((clientX - rect.left) / rect.width) * 100, 0, 100);
    const lightness = clamp(100 - ((clientY - rect.top) / rect.height) * 100, 0, 100);
    const nextHex = hslToHex({
      h: color.hsl.h,
      s: saturation,
      l: lightness,
    });

    commitHex(nextHex);
  }

  function handlePickerPointerDown(event) {
    draggingRef.current = true;
    updateFromPointer(event.clientX, event.clientY);
    event.preventDefault();
  }

  function handleHueChange(event) {
    if (isHexFocused) {
      return;
    }

    const hue = Number(event.target.value);
    const nextHex = hslToHex({ ...color.hsl, h: hue });
    commitHex(nextHex);
  }

  function handleRgbChange(channel, rawValue) {
    if (isHexFocused) {
      return;
    }

    const nextRgb = {
      ...color.rgb,
      [channel]: clamp(Number(rawValue), 0, 255),
    };
    commitHex(rgbToHex(nextRgb));
  }

  function handleHexChange(event) {
    const raw = event.target.value;
    setHexDraft(raw);
    setIsHexFocused(true);

    onValidityChange?.(false);

    const trimmed = raw.trim();
    if (trimmed === '') {
      setLocalError('');
      return;
    }

    const normalized = normalizeHex(trimmed);
    if (normalized) {
      setLocalError('');
      return;
    }

    if (isPotentialHexDraft(trimmed)) {
      setLocalError('');
      return;
    }

    setLocalError(HEX_ERROR_MESSAGE);
    onValidityChange?.(false);
  }

  function handleHexBlur() {
    const normalized = normalizeHex(hexDraft);
    if (normalized) {
      commitHex(normalized);
      return;
    }

    setIsHexFocused(false);
    revertToLastValid(HEX_ERROR_MESSAGE);
  }

  function handleHexKeyDown(event) {
    if (event.key !== 'Enter') {
      return;
    }

    event.preventDefault();
    handleHexBlur();
  }

  function handleApplyHex() {
    const normalized = normalizeHex(hexDraft);
    if (normalized) {
      commitHex(normalized);
      return;
    }

    setIsHexFocused(false);
    revertToLastValid(HEX_ERROR_MESSAGE);
  }

  return (
    <div className="bp-branding-color-picker" ref={rootRef}>
      <div className="bp-branding-color-picker__header">
        <div>
          <strong className="bp-branding-color-picker__label">{label}</strong>
          <p className="bp-branding-color-picker__hint">{hint}</p>
        </div>
        <div className="bp-branding-color-picker__meta">
          <Badge variant={contrast.meetsAA ? 'success' : 'warning'}>
            {contrast.meetsAA ? 'AA OK' : 'Contraste baixo'}
          </Badge>
          <Button
            className="bp-branding-color-picker__trigger"
            size="sm"
            variant="secondary"
            type="button"
            onClick={() => setIsOpen((current) => !current)}
          >
            <span className="bp-branding-color-picker__trigger-swatch" style={{ background: color.hex }} />
            <span className="bp-branding-color-picker__trigger-value">{color.hexDisplay}</span>
          </Button>
        </div>
      </div>

      {localError ? <p className="bp-branding-color-picker__error">{localError}</p> : null}

      {isOpen ? (
        <div className="bp-branding-color-picker__popover" role="dialog" aria-label={label}>
          <div className="bp-branding-color-picker__layout">
            <div className="bp-branding-color-picker__visual">
              <div
                ref={squareRef}
                className="bp-branding-color-picker__square"
                onPointerDown={handlePickerPointerDown}
                role="application"
                style={{ '--picker-hue': color.hsl.h }}
              >
                <span
                  aria-hidden="true"
                  className="bp-branding-color-picker__handle"
                  style={{ left: `${color.hsl.s}%`, top: `${100 - color.hsl.l}%` }}
                />
              </div>

              <label className="bp-branding-color-picker__slider">
                <span>Matiz</span>
                <input
                  aria-label="Matiz"
                  max="360"
                  min="0"
                  type="range"
                  value={color.hsl.h}
                  onChange={handleHueChange}
                />
              </label>
            </div>

            <div className="bp-branding-color-picker__content">
              <div className="bp-branding-color-picker__presets" aria-label="Presets rápidos">
                {presets.map((preset) => {
                  const presetHex = normalizeHex(preset.value) ?? DEFAULT_HEX;
                  const isActive = presetHex === color.hex;

                  return (
                    <button
                      key={`${preset.label}-${presetHex}`}
                      aria-pressed={isActive}
                      className={['bp-branding-color-picker__preset', isActive ? 'bp-branding-color-picker__preset--active' : '']
                        .filter(Boolean)
                        .join(' ')}
                      type="button"
                      onClick={() => applyPreset(preset)}
                    >
                      <span className="bp-branding-color-picker__preset-swatch" style={{ background: presetHex }} />
                      <span className="bp-branding-color-picker__preset-label">{preset.label}</span>
                    </button>
                  );
                })}
              </div>

              <div className="bp-branding-color-picker__fields">
                <div className="bp-branding-color-picker__hex-row">
                  <Input
                    error={localError}
                    id={`${id}-hex`}
                    label="Hexadecimal"
                    placeholder="#FFFFFF"
                    spellCheck={false}
                    type="text"
                    value={hexDraft}
                    onBlur={handleHexBlur}
                    onChange={handleHexChange}
                    onFocus={() => {
                      setIsHexFocused(true);
                    }}
                    onKeyDown={handleHexKeyDown}
                  />
                  <Button
                    className="bp-branding-color-picker__apply"
                    type="button"
                    variant="secondary"
                    onClick={handleApplyHex}
                  >
                    Aplicar
                  </Button>
                </div>

                <div className="bp-branding-color-picker__rgb" aria-label="RGB">
                  {(['r', 'g', 'b']).map((channel) => (
                    <Input
                      key={channel}
                      id={`${id}-${channel}`}
                      label={channel.toUpperCase()}
                      min="0"
                      max="255"
                      type="number"
                      value={color.rgb[channel]}
                      onChange={(event) => handleRgbChange(channel, event.target.value)}
                      disabled={isHexFocused}
                    />
                  ))}
                </div>
              </div>

              <div className="bp-branding-color-picker__preview">
                <div
                  className="bp-branding-color-picker__swatch"
                  style={{ background: color.hex, color: accessibleTextColor }}
                >
                  <strong>{color.hexDisplay}</strong>
                  <span>
                    {color.rgb.r}, {color.rgb.g}, {color.rgb.b}
                  </span>
                </div>
                <div className="bp-branding-color-picker__contrast">
                  <span>Contraste com superfície</span>
                  <strong>{contrast.ratio.toFixed(2)}:1</strong>
                </div>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}

function createColorState(hex) {
  const normalized = normalizeHex(hex) ?? DEFAULT_HEX;
  const rgb = hexToRgb(normalized);
  const hsl = rgbToHsl(rgb);

  return {
    hex: normalized,
    hexDisplay: normalized.toUpperCase(),
    rgb,
    hsl,
  };
}

function isPotentialHexDraft(value) {
  return /^#?[0-9a-fA-F]{0,6}$/.test(value);
}

function readCssVariable(name, fallback) {
  if (typeof document === 'undefined') {
    return fallback;
  }

  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  return value || fallback;
}

function rgbToHsl(rgb) {
  const r = rgb.r / 255;
  const g = rgb.g / 255;
  const b = rgb.b / 255;
  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  const delta = max - min;
  let h = 0;
  const l = (max + min) / 2;
  const s = delta === 0 ? 0 : delta / (1 - Math.abs(2 * l - 1));

  if (delta !== 0) {
    switch (max) {
      case r:
        h = ((g - b) / delta) % 6;
        break;
      case g:
        h = (b - r) / delta + 2;
        break;
      default:
        h = (r - g) / delta + 4;
        break;
    }

    h = Math.round(h * 60);
    if (h < 0) {
      h += 360;
    }
  }

  return {
    h,
    s: Math.round(s * 100),
    l: Math.round(l * 100),
  };
}

function hslToHex(hsl) {
  const rgb = hslToRgb(hsl);
  return rgbToHex(rgb);
}

function hslToRgb(hsl) {
  const h = ((Number(hsl.h) % 360) + 360) % 360;
  const s = clamp(Number(hsl.s) || 0, 0, 100) / 100;
  const l = clamp(Number(hsl.l) || 0, 0, 100) / 100;
  const c = (1 - Math.abs(2 * l - 1)) * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = l - c / 2;
  let r = 0;
  let g = 0;
  let b = 0;

  if (h < 60) {
    r = c;
    g = x;
  } else if (h < 120) {
    r = x;
    g = c;
  } else if (h < 180) {
    g = c;
    b = x;
  } else if (h < 240) {
    g = x;
    b = c;
  } else if (h < 300) {
    r = x;
    b = c;
  } else {
    r = c;
    b = x;
  }

  return {
    r: Math.round((r + m) * 255),
    g: Math.round((g + m) * 255),
    b: Math.round((b + m) * 255),
  };
}

function clamp(value, min, max) {
  if (Number.isNaN(value)) {
    return min;
  }

  return Math.min(max, Math.max(min, value));
}
