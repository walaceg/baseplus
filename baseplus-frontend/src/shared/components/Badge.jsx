export function Badge({ children, className = '', variant = 'neutral' }) {
  const classes = ['bp-badge', variant !== 'neutral' ? `bp-badge--${variant}` : '', className].filter(Boolean).join(' ');

  return <span className={classes}>{children}</span>;
}
