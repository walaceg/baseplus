export function Button({ children, className = '', size = 'md', type = 'button', variant = 'primary', ...props }) {
  const classes = ['bp-button', `bp-button--${variant}`, size !== 'md' ? `bp-button--${size}` : '', className]
    .filter(Boolean)
    .join(' ');

  return (
    <button className={classes} type={type} {...props}>
      {children}
    </button>
  );
}
