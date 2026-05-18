import { Button } from './Button.jsx';

export function EmptyState({ actionLabel, className = '', description, onAction, title }) {
  const classes = ['bp-empty-state', className].filter(Boolean).join(' ');

  return (
    <div className={classes}>
      <strong className="bp-empty-state__title">{title}</strong>
      {description ? <p className="bp-empty-state__description">{description}</p> : null}
      {actionLabel && onAction ? (
        <Button variant="secondary" onClick={onAction}>
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
