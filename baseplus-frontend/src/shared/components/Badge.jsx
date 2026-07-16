import { AlertTriangle, CheckCircle2, Circle, Info, ShieldAlert } from 'lucide-react';

const ICONS = {
  danger: ShieldAlert,
  error: ShieldAlert,
  neutral: Circle,
  primary: Info,
  secondary: Circle,
  success: CheckCircle2,
  warning: AlertTriangle,
};

export function Badge({
  as: Component = 'span',
  children,
  className = '',
  icon,
  title,
  variant = 'neutral',
  ...props
}) {
  const normalizedVariant = variant === 'error' ? 'danger' : variant;
  const classes = ['bp-badge', normalizedVariant !== 'neutral' ? `bp-badge--${normalizedVariant}` : '', className].filter(Boolean).join(' ');
  const Icon = icon === false ? null : icon ?? ICONS[variant];

  return (
    <Component className={classes} title={title} {...props}>
      {Icon ? (
        <span className="bp-badge__icon" aria-hidden="true">
          <Icon size={12} />
        </span>
      ) : null}
      <span className="bp-badge__label">{children}</span>
    </Component>
  );
}
