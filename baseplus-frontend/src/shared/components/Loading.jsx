export function Loading({ className = '', label = 'Carregando...', size = 'md' }) {
  const classes = ['bp-loading', `bp-loading--${size}`, className].filter(Boolean).join(' ');

  return (
    <div className={classes} role="status" aria-live="polite">
      <span className="bp-loading__spinner" aria-hidden="true" />
      <span className="bp-loading__label">{label}</span>
    </div>
  );
}

export function Skeleton({ className = '', lines = 3 }) {
  const classes = ['bp-skeleton', className].filter(Boolean).join(' ');

  return (
    <div aria-hidden="true" className={classes}>
      {Array.from({ length: lines }).map((_, index) => (
        <span className="bp-skeleton__line" key={index} />
      ))}
    </div>
  );
}
