import { useEffect, useMemo, useState } from 'react';
import { getBrandingInitials, normalizeLogoUrl, resolveBrandingAssetUrl } from '../../../branding/branding.js';
import './BrandingLogo.css';

export function BrandingLogo({
  className = '',
  logoUrl,
  assetVersion = 0,
  name = 'Base+',
  subtitle = '',
  showText = true,
  showSubtitle = true,
  size = 'md',
  variant = 'inline',
}) {
  const normalizedLogoUrl = useMemo(
    () => resolveBrandingAssetUrl(normalizeLogoUrl(logoUrl), assetVersion),
    [assetVersion, logoUrl],
  );
  const initials = useMemo(() => getBrandingInitials(name), [name]);
  const [status, setStatus] = useState(normalizedLogoUrl ? 'loading' : 'fallback');

  useEffect(() => {
    setStatus(normalizedLogoUrl ? 'loading' : 'fallback');
  }, [normalizedLogoUrl]);

  const classes = ['bp-branding-logo', `bp-branding-logo--${variant}`, `bp-branding-logo--${size}`, className]
    .filter(Boolean)
    .join(' ');

  return (
    <div
      className={classes}
      aria-busy={status === 'loading'}
      aria-label={!showText ? (name ? `Logo institucional de ${name}` : 'Logo institucional') : undefined}
      role={!showText ? 'img' : undefined}
    >
      <span className="bp-branding-logo__media" aria-hidden="true">
        {normalizedLogoUrl && status === 'loading' ? <span className="bp-branding-logo__placeholder" /> : null}
        {normalizedLogoUrl && status !== 'error' ? (
          <img
            key={normalizedLogoUrl}
            alt=""
            className="bp-branding-logo__image"
            decoding="async"
            loading="eager"
            src={normalizedLogoUrl}
            onError={() => setStatus('error')}
            onLoad={() => setStatus('loaded')}
          />
        ) : null}
        {(!normalizedLogoUrl || status === 'error') ? <span className="bp-branding-logo__fallback">{initials}</span> : null}
      </span>

      {showText ? (
        <span className="bp-branding-logo__content">
          <strong className="bp-branding-logo__name">{name}</strong>
          {showSubtitle && subtitle ? <span className="bp-branding-logo__subtitle">{subtitle}</span> : null}
        </span>
      ) : null}
    </div>
  );
}
