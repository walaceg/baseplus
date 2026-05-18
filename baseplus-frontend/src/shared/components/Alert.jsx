export function Alert({ children, className = '', title, variant = 'info', ...props }) {
  const classes = ['bp-alert', `bp-alert--${variant}`, className].filter(Boolean).join(' ');

  return (
    <div className={classes} role="alert" {...props}>
      {title ? <strong className="bp-alert__title">{title}</strong> : null}
      <div className="bp-alert__body">{children}</div>
    </div>
  );
}
