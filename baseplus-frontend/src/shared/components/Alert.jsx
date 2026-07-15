export function Alert({ children, className = '', title, variant = 'info', ...props }) {
  const classes = ['bp-alert', `bp-alert--${variant}`, className].filter(Boolean).join(' ');
  const role = variant === 'error' || variant === 'warning' ? 'alert' : 'status';

  return (
    <div className={classes} role={role} {...props}>
      {title ? <strong className="bp-alert__title">{title}</strong> : null}
      <div className="bp-alert__body">{children}</div>
    </div>
  );
}
