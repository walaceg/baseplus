import { Inbox } from 'lucide-react';
import { Button } from './Button.jsx';

function renderAction(action, fallbackVariant = 'primary') {
  if (!action) {
    return null;
  }

  if (action.node) {
    return action.node;
  }

  return (
    <Button
      disabled={action.disabled}
      type="button"
      variant={action.variant ?? fallbackVariant}
      onClick={action.onClick}
    >
      {action.label}
    </Button>
  );
}

export function EmptyState({
  actionLabel,
  ariaLabel,
  className = '',
  description,
  icon,
  imageAlt = '',
  imageSrc,
  message,
  onAction,
  primaryAction,
  secondaryAction,
  title,
}) {
  const classes = ['bp-empty-state', className].filter(Boolean).join(' ');
  const Icon = icon === false ? null : icon ?? Inbox;
  const legacyAction = actionLabel && onAction ? { label: actionLabel, onClick: onAction, variant: 'secondary' } : null;
  const resolvedPrimaryAction = primaryAction ?? legacyAction;

  return (
    <div aria-label={ariaLabel} className={classes} role="status">
      {imageSrc ? (
        <img alt={imageAlt} className="bp-empty-state__image" src={imageSrc} />
      ) : Icon ? (
        <span className="bp-empty-state__icon" aria-hidden="true">
          <Icon size={22} />
        </span>
      ) : null}

      <div className="bp-empty-state__content">
        {title ? <strong className="bp-empty-state__title">{title}</strong> : null}
        {message ? <p className="bp-empty-state__message">{message}</p> : null}
        {description ? <p className="bp-empty-state__description">{description}</p> : null}
      </div>

      {resolvedPrimaryAction || secondaryAction ? (
        <div className="bp-empty-state__actions">
          {renderAction(resolvedPrimaryAction, 'primary')}
          {renderAction(secondaryAction, 'secondary')}
        </div>
      ) : null}
    </div>
  );
}
