import { Button } from './Button.jsx';

export function Pagination({
  className = '',
  onChangePage,
  page = 0,
  size = 0,
  totalElements = 0,
  totalPages = 0,
}) {
  const classes = ['bp-pagination', className].filter(Boolean).join(' ');
  const hasPrevious = page > 0;
  const hasNext = page + 1 < totalPages;
  const start = totalElements > 0 ? page * size + 1 : 0;
  const end = totalElements > 0 ? Math.min((page + 1) * size, totalElements) : 0;

  if (totalElements === 0) {
    return null;
  }

  return (
    <div className={classes}>
      <p className="bp-pagination__summary">
        Mostrando {start}-{end} de {totalElements}
      </p>
      <div className="bp-pagination__actions">
        <Button disabled={!hasPrevious} size="sm" variant="secondary" onClick={() => onChangePage(page - 1)}>
          Anterior
        </Button>
        <span className="bp-pagination__page">
          Pagina {page + 1} de {Math.max(totalPages, 1)}
        </span>
        <Button disabled={!hasNext} size="sm" variant="secondary" onClick={() => onChangePage(page + 1)}>
          Proxima
        </Button>
      </div>
    </div>
  );
}
