export function ActionIconButton({
  className = '',
  icon: Icon,
  label,
  title,
  type = 'button',
  variant = 'subtle',
  ...props
}) {
  const classes = ['bp-action-icon-button', `bp-action-icon-button--${variant}`, className].filter(Boolean).join(' ');
  const accessibleLabel = label ?? title;

  return (
    <button aria-label={accessibleLabel} className={classes} title={title ?? label} type={type} {...props}>
      {Icon ? <Icon aria-hidden="true" size={18} strokeWidth={2} /> : null}
    </button>
  );
}
