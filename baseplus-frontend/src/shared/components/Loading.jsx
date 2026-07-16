export function Loading({ className = '', label = 'Carregando...', size = 'md' }) {
  const classes = ['bp-loading', `bp-loading--${size}`, className].filter(Boolean).join(' ');

  return (
    <div className={classes} role="status" aria-live="polite">
      <span className="bp-loading__spinner" aria-hidden="true" />
      <span className="bp-loading__label">{label}</span>
    </div>
  );
}

function SkeletonLine({ className = '', width }) {
  return <span className={['bp-skeleton__line', className].filter(Boolean).join(' ')} style={width ? { width } : undefined} />;
}

function SkeletonAvatar() {
  return <span className="bp-skeleton__avatar" />;
}

function SkeletonText({ lines = 3 }) {
  return (
    <div className="bp-skeleton__text">
      {Array.from({ length: lines }).map((_, index) => (
        <SkeletonLine key={index} />
      ))}
    </div>
  );
}

function SkeletonCard() {
  return (
    <div className="bp-skeleton__card">
      <div className="bp-skeleton__row">
        <SkeletonAvatar />
        <div className="bp-skeleton__stack">
          <SkeletonLine width="52%" />
          <SkeletonLine width="34%" />
        </div>
      </div>
      <SkeletonText lines={3} />
    </div>
  );
}

function SkeletonTable({ columns = 4, rows = 5 }) {
  return (
    <div className="bp-skeleton__table" style={{ '--skeleton-columns': columns }}>
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <div className="bp-skeleton__table-row" key={rowIndex}>
          {Array.from({ length: columns }).map((_, columnIndex) => (
            <SkeletonLine key={columnIndex} />
          ))}
        </div>
      ))}
    </div>
  );
}

function SkeletonForm({ fields = 4 }) {
  return (
    <div className="bp-skeleton__form">
      {Array.from({ length: fields }).map((_, index) => (
        <div className="bp-skeleton__field" key={index}>
          <SkeletonLine width="28%" />
          <SkeletonLine className="bp-skeleton__control" />
        </div>
      ))}
    </div>
  );
}

function SkeletonDashboard({ items = 4 }) {
  return (
    <div className="bp-skeleton__dashboard">
      {Array.from({ length: items }).map((_, index) => (
        <div className="bp-skeleton__metric" key={index}>
          <SkeletonLine width="44%" />
          <SkeletonLine className="bp-skeleton__metric-value" width="32%" />
          <SkeletonLine width="68%" />
        </div>
      ))}
    </div>
  );
}

function SkeletonList({ items = 4 }) {
  return (
    <div className="bp-skeleton__list">
      {Array.from({ length: items }).map((_, index) => (
        <div className="bp-skeleton__row" key={index}>
          <SkeletonAvatar />
          <div className="bp-skeleton__stack">
            <SkeletonLine width="54%" />
            <SkeletonLine width="78%" />
          </div>
        </div>
      ))}
    </div>
  );
}

function renderSkeletonVariant({ columns, fields, items, lines, rows, variant }) {
  switch (variant) {
    case 'avatar':
      return <SkeletonAvatar />;
    case 'card':
      return <SkeletonCard />;
    case 'dashboard':
      return <SkeletonDashboard items={items} />;
    case 'form':
      return <SkeletonForm fields={fields} />;
    case 'list':
      return <SkeletonList items={items} />;
    case 'table':
      return <SkeletonTable columns={columns} rows={rows} />;
    default:
      return <SkeletonText lines={lines} />;
  }
}

export function Skeleton({
  ariaLabel,
  className = '',
  columns = 4,
  fields = 4,
  items = 4,
  lines = 3,
  rows = 5,
  variant = 'text',
}) {
  const classes = ['bp-skeleton', `bp-skeleton--${variant}`, className].filter(Boolean).join(' ');
  const accessibilityProps = ariaLabel
    ? { 'aria-live': 'polite', role: 'status' }
    : { 'aria-hidden': 'true' };

  return (
    <div className={classes} {...accessibilityProps}>
      {ariaLabel ? <span className="bp-sr-only">{ariaLabel}</span> : null}
      {renderSkeletonVariant({ columns, fields, items, lines, rows, variant })}
    </div>
  );
}

export function SkeletonAvatarBlock(props) {
  return <Skeleton variant="avatar" {...props} />;
}

export function SkeletonCardBlock(props) {
  return <Skeleton variant="card" {...props} />;
}

export function SkeletonDashboardBlock(props) {
  return <Skeleton variant="dashboard" {...props} />;
}

export function SkeletonFormBlock(props) {
  return <Skeleton variant="form" {...props} />;
}

export function SkeletonListBlock(props) {
  return <Skeleton variant="list" {...props} />;
}

export function SkeletonTableBlock(props) {
  return <Skeleton variant="table" {...props} />;
}
