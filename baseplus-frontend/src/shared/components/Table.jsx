export function Table({
  ariaLabel = 'Tabela de dados',
  caption,
  columns = [],
  emptyDescription = 'Ajuste os filtros ou cadastre um novo item quando disponivel.',
  emptyMessage = 'Nenhum registro encontrado.',
  rows = [],
}) {
  return (
    <div className="bp-table-wrap">
      <table aria-label={caption ? undefined : ariaLabel} className="bp-table">
        {caption ? <caption className="bp-table__caption">{caption}</caption> : null}
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column.key} scope="col">{column.header}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length ? (
            rows.map((row, rowIndex) => (
              <tr key={row.id ?? rowIndex}>
                {columns.map((column) => (
                  <td key={column.key}>{column.render ? column.render(row) : row[column.key]}</td>
                ))}
              </tr>
            ))
          ) : (
            <tr>
              <td className="bp-table__empty" colSpan={Math.max(columns.length, 1)}>
                <strong>{emptyMessage}</strong>
                {emptyDescription ? <span>{emptyDescription}</span> : null}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
