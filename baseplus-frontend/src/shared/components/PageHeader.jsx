export function PageHeader({ title, description, action }) {
  return (
    <section className="bp-list-page__header">
      <div>
        <h1 className="bp-list-page__title">{title}</h1>
        {description ? <p>{description}</p> : null}
      </div>
      {action}
    </section>
  );
}
