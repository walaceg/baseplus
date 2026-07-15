export function Button({ children, className = '', loading = false, size = 'md', type = 'button', variant = 'primary', ...props }) {
  const classes = ['bp-button', `bp-button--${variant}`, loading ? 'bp-button--loading' : '', size !== 'md' ? `bp-button--${size}` : '', className]
    .filter(Boolean)
    .join(' ');
  const disabled = loading || props.disabled;

  return (
    <button {...props} aria-busy={loading || undefined} className={classes} disabled={disabled} type={type}>
      {children}
    </button>
  );
}
