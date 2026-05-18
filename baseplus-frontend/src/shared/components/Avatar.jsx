import { useEffect, useMemo, useState } from 'react';
import { apiBaseURL } from '../api/apiClient.js';

function getInitials(name = '') {
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
}

function resolveAvatarSrc(src, version) {
  if (!src) {
    return '';
  }

  const rawSrc = String(src).trim();

  if (!rawSrc) {
    return '';
  }

  if (rawSrc.startsWith('data:') || rawSrc.startsWith('blob:')) {
    return rawSrc;
  }

  let resolvedSrc;

  try {
    resolvedSrc = new URL(rawSrc, apiBaseURL).toString();
  } catch {
    return rawSrc;
  }

  if (version === undefined || version === null || version === '') {
    return resolvedSrc;
  }

  try {
    const resolvedUrl = new URL(resolvedSrc);
    resolvedUrl.searchParams.set('v', String(version));
    return resolvedUrl.toString();
  } catch {
    return resolvedSrc;
  }
}

export function Avatar({
  alt = '',
  className = '',
  name = '',
  showStatus = false,
  size = 'md',
  src,
  statusLabel = 'Online',
  version,
  variant = 'default',
}) {
  const classes = [
    'bp-avatar',
    variant !== 'default' ? `bp-avatar--${variant}` : '',
    size !== 'md' ? `bp-avatar--${size}` : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');
  const initials = getInitials(name || alt) || '?';
  const resolvedSrc = useMemo(() => resolveAvatarSrc(src, version), [src, version]);
  const [status, setStatus] = useState(resolvedSrc ? 'loading' : 'fallback');

  useEffect(() => {
    setStatus(resolvedSrc ? 'loading' : 'fallback');
  }, [resolvedSrc]);

  return (
    <span aria-label={alt || name} className={classes} data-avatar-status={status} role="img">
      {resolvedSrc && status !== 'error' ? (
        <img
          alt={alt || name}
          className={['bp-avatar__image', status === 'loaded' ? 'bp-avatar__image--loaded' : 'bp-avatar__image--loading']
            .filter(Boolean)
            .join(' ')}
          src={resolvedSrc}
          onError={() => setStatus('error')}
          onLoad={() => setStatus('loaded')}
        />
      ) : null}
      {!resolvedSrc || status === 'error' ? <span className="bp-avatar__fallback">{initials}</span> : null}
      {showStatus ? <span aria-label={statusLabel} className="bp-avatar__status" role="img" /> : null}
    </span>
  );
}
