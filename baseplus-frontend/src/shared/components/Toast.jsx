import { AlertCircle, AlertTriangle, CheckCircle2, Info, X } from 'lucide-react';

const ICONS = {
  error: AlertCircle,
  info: Info,
  success: CheckCircle2,
  warning: AlertTriangle,
};

const DEFAULT_TITLES = {
  error: 'Ação não concluída',
  info: 'Informação',
  success: 'Ação concluída',
  warning: 'Atenção',
};

function getRole(variant) {
  return variant === 'error' || variant === 'warning' ? 'alert' : 'status';
}

export function Toast({
  action,
  children,
  className = '',
  dismissLabel = 'Fechar notificação',
  icon,
  onDismiss,
  title,
  variant = 'info',
  ...props
}) {
  const classes = ['bp-toast', `bp-toast--${variant}`, className].filter(Boolean).join(' ');
  const role = props.role ?? getRole(variant);
  const Icon = icon === false ? null : icon ?? ICONS[variant] ?? ICONS.info;
  const displayTitle = title ?? DEFAULT_TITLES[variant];

  return (
    <div aria-live={role === 'alert' ? 'assertive' : 'polite'} className={classes} role={role} {...props}>
      {Icon ? (
        <span className="bp-toast__icon" aria-hidden="true">
          <Icon size={18} />
        </span>
      ) : null}
      <div className="bp-toast__content">
        {displayTitle ? <strong className="bp-toast__title">{displayTitle}</strong> : null}
        {children ? <div className="bp-toast__body">{children}</div> : null}
        {action ? <div className="bp-toast__action">{action}</div> : null}
      </div>
      {onDismiss ? (
        <button className="bp-toast__dismiss" type="button" aria-label={dismissLabel} onClick={onDismiss}>
          <X size={16} aria-hidden="true" />
        </button>
      ) : null}
    </div>
  );
}

export function ToastRegion({ children, className = '', label = 'Notificações' }) {
  return (
    <div aria-label={label} className={['bp-toast-region', className].filter(Boolean).join(' ')} role="region">
      {children}
    </div>
  );
}
