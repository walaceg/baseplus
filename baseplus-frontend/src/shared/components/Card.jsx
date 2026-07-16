function joinClasses(...classes) {
  return classes.filter(Boolean).join(' ');
}

export function Card({
  as: Component = 'section',
  children,
  className = '',
  elevated = false,
  interactive = false,
  muted = false,
  ...props
}) {
  const classes = joinClasses(
    'bp-card',
    elevated ? 'bp-card--elevated' : '',
    interactive ? 'bp-card--interactive' : '',
    muted ? 'bp-card--muted' : '',
    className,
  );

  return (
    <Component className={classes} {...props}>
      {children}
    </Component>
  );
}

Card.Header = function CardHeader({ actions, children, className = '', ...props }) {
  return (
    <div className={joinClasses('bp-card__header', className)} {...props}>
      <div className="bp-card__header-content">{children}</div>
      {actions ? <div className="bp-card__header-actions">{actions}</div> : null}
    </div>
  );
};

Card.Body = function CardBody({ children, className = '', ...props }) {
  return <div className={joinClasses('bp-card__body', className)} {...props}>{children}</div>;
};

Card.Footer = function CardFooter({ actions, children, className = '', ...props }) {
  return (
    <div className={joinClasses('bp-card__footer', className)} {...props}>
      <div className="bp-card__footer-content">{children}</div>
      {actions ? <div className="bp-card__footer-actions">{actions}</div> : null}
    </div>
  );
};
