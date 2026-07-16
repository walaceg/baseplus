import { AlertCircle, AlertTriangle, CheckCircle2, Info, X } from 'lucide-react';

const ICONS = {
  error: AlertCircle,
  info: Info,
  success: CheckCircle2,
  warning: AlertTriangle,
};

const DEFAULT_TITLES = {
  error: 'Não foi possível concluir',
  info: 'Informação',
  success: 'Tudo certo',
  warning: 'Atenção',
};

function getRole(variant) {
  return variant === 'error' || variant === 'warning' ? 'alert' : 'status';
}

export function Alert({
  children,
  className = '',
  dismissLabel = 'Fechar mensagem',
  icon,
  onDismiss,
  title,
  variant = 'info',
  ...props
}) {
  const classes = ['bp-alert', `bp-alert--${variant}`, className].filter(Boolean).join(' ');
  const role = props.role ?? getRole(variant);
  const Icon = icon === false ? null : icon ?? ICONS[variant] ?? ICONS.info;
  const displayTitle = title ?? DEFAULT_TITLES[variant];

  return (
    <div aria-live={role === 'alert' ? 'assertive' : 'polite'} className={classes} role={role} {...props}>
      {Icon ? (
        <span className="bp-alert__icon" aria-hidden="true">
          <Icon size={18} />
        </span>
      ) : null}
      <div className="bp-alert__content">
        {displayTitle ? <strong className="bp-alert__title">{displayTitle}</strong> : null}
        <div className="bp-alert__body">{children}</div>
      </div>
      {onDismiss ? (
        <button className="bp-alert__dismiss" type="button" aria-label={dismissLabel} onClick={onDismiss}>
          <X size={16} aria-hidden="true" />
        </button>
      ) : null}
    </div>
  );
}
