export function Card({ children, className = '', ...props }) {
  return (
    <section className={['bp-card', className].filter(Boolean).join(' ')} {...props}>
      {children}
    </section>
  );
}

Card.Header = function CardHeader({ children }) {
  return <div className="bp-card__header">{children}</div>;
};

Card.Body = function CardBody({ children }) {
  return <div className="bp-card__body">{children}</div>;
};

Card.Footer = function CardFooter({ children }) {
  return <div className="bp-card__footer">{children}</div>;
};
