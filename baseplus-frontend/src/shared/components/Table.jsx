import { useMemo, useState } from 'react';
import { ArrowDown, ArrowUp, ArrowUpDown } from 'lucide-react';
import { Skeleton } from './Loading.jsx';

function getCellValue(row, column) {
  if (column.sortValue) {
    return column.sortValue(row);
  }

  return row?.[column.key];
}

function compareValues(a, b) {
  if (a == null && b == null) {
    return 0;
  }

  if (a == null) {
    return 1;
  }

  if (b == null) {
    return -1;
  }

  if (typeof a === 'number' && typeof b === 'number') {
    return a - b;
  }

  return String(a).localeCompare(String(b), 'pt-BR', { numeric: true, sensitivity: 'base' });
}

function getNextSortDirection(currentKey, currentDirection, columnKey) {
  if (currentKey !== columnKey) {
    return 'ascending';
  }

  if (currentDirection === 'ascending') {
    return 'descending';
  }

  if (currentDirection === 'descending') {
    return null;
  }

  return 'ascending';
}

function SortIcon({ direction }) {
  if (direction === 'ascending') {
    return <ArrowUp aria-hidden="true" size={14} />;
  }

  if (direction === 'descending') {
    return <ArrowDown aria-hidden="true" size={14} />;
  }

  return <ArrowUpDown aria-hidden="true" size={14} />;
}

export function Table({
  ariaLabel = 'Tabela de dados',
  caption,
  columns = [],
  emptyDescription = 'Ajuste os filtros ou cadastre um novo item quando disponível.',
  emptyMessage = 'Nenhum registro encontrado.',
  getRowId = (row, rowIndex) => row?.id ?? rowIndex,
  loading = false,
  loadingLabel = 'Carregando dados...',
  onRowClick,
  onSelectionChange,
  onSortChange,
  rowClassName,
  rows = [],
  selectable = false,
  selectedRowIds,
  selectionLabel = 'Selecionar linha',
  skeletonRows = 5,
  sortDirection,
  sortKey,
}) {
  const [internalSort, setInternalSort] = useState({ key: null, direction: null });
  const [internalSelectedRowIds, setInternalSelectedRowIds] = useState([]);
  const activeSortKey = sortKey ?? internalSort.key;
  const activeSortDirection = sortDirection ?? internalSort.direction;
  const activeSelectedRowIds = selectedRowIds ?? internalSelectedRowIds;
  const selectedSet = useMemo(() => new Set(activeSelectedRowIds.map(String)), [activeSelectedRowIds]);

  const sortedRows = useMemo(() => {
    if (!activeSortKey || !activeSortDirection) {
      return rows;
    }

    const column = columns.find((item) => item.key === activeSortKey);
    if (!column?.sortable) {
      return rows;
    }

    const sorted = [...rows].sort((left, right) => compareValues(getCellValue(left, column), getCellValue(right, column)));
    return activeSortDirection === 'descending' ? sorted.reverse() : sorted;
  }, [activeSortDirection, activeSortKey, columns, rows]);

  function handleSort(column) {
    if (!column.sortable) {
      return;
    }

    const nextDirection = getNextSortDirection(activeSortKey, activeSortDirection, column.key);
    const nextSort = { key: nextDirection ? column.key : null, direction: nextDirection };

    setInternalSort(nextSort);
    onSortChange?.(nextSort);
  }

  function isSelected(row, rowIndex) {
    return selectedSet.has(String(getRowId(row, rowIndex)));
  }

  function toggleRowSelection(row, rowIndex) {
    const rowId = getRowId(row, rowIndex);
    const rowKey = String(rowId);
    const nextSelected = new Set(selectedSet);

    if (nextSelected.has(rowKey)) {
      nextSelected.delete(rowKey);
    } else {
      nextSelected.add(rowKey);
    }

    const nextSelectedRowIds = Array.from(nextSelected);
    if (!selectedRowIds) {
      setInternalSelectedRowIds(nextSelectedRowIds);
    }
    onSelectionChange?.(nextSelectedRowIds);
  }

  function handleRowClick(event, row, rowIndex) {
    if (selectable) {
      toggleRowSelection(row, rowIndex);
    }

    onRowClick?.(row, event);
  }

  function handleRowKeyDown(event, row, rowIndex) {
    if (event.defaultPrevented) {
      return;
    }

    if (event.target !== event.currentTarget) {
      return;
    }

    if (event.key !== 'Enter' && event.key !== ' ') {
      return;
    }

    event.preventDefault();

    if (selectable) {
      toggleRowSelection(row, rowIndex);
      return;
    }

    onRowClick?.(row, event);
  }

  const hasInteractiveRows = Boolean(onRowClick || selectable);
  const columnCount = columns.length + (selectable ? 1 : 0);

  return (
    <div className="bp-table-wrap">
      <table aria-busy={loading ? 'true' : undefined} aria-label={caption ? undefined : ariaLabel} className="bp-table">
        {caption ? <caption className="bp-table__caption">{caption}</caption> : null}
        <thead>
          <tr>
            {selectable ? (
              <th className="bp-table__selection-cell" scope="col">
                <span className="bp-sr-only">Seleção</span>
              </th>
            ) : null}
            {columns.map((column) => {
              const isActiveSort = activeSortKey === column.key && activeSortDirection;
              const ariaSort = column.sortable ? (isActiveSort ? activeSortDirection : 'none') : undefined;
              const headerClasses = ['bp-table__header', column.headerClassName, column.align ? `bp-table__cell--${column.align}` : ''].filter(Boolean).join(' ');

              return (
                <th
                  aria-sort={ariaSort}
                  className={headerClasses}
                  key={column.key}
                  scope="col"
                  style={column.width ? { width: column.width } : undefined}
                >
                  {column.sortable ? (
                    <button
                      className="bp-table__sort-button"
                      type="button"
                      onClick={() => handleSort(column)}
                    >
                      <span>{column.header}</span>
                      <SortIcon direction={isActiveSort ? activeSortDirection : null} />
                    </button>
                  ) : (
                    column.header
                  )}
                </th>
              );
            })}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr className="bp-table__row bp-table__row--loading">
              <td colSpan={Math.max(columnCount, 1)}>
                <Skeleton columns={Math.max(columnCount, 1)} rows={skeletonRows} variant="table" />
              </td>
            </tr>
          ) : sortedRows.length ? (
            sortedRows.map((row, rowIndex) => {
              const rowId = getRowId(row, rowIndex);
              const selected = isSelected(row, rowIndex);
              const classes = [
                'bp-table__row',
                selected ? 'bp-table__row--selected' : '',
                hasInteractiveRows ? 'bp-table__row--interactive' : '',
                typeof rowClassName === 'function' ? rowClassName(row, rowIndex) : rowClassName,
              ].filter(Boolean).join(' ');

              return (
                <tr
                  aria-selected={selectable ? selected : undefined}
                  className={classes}
                  key={rowId}
                  tabIndex={hasInteractiveRows ? 0 : undefined}
                  onClick={hasInteractiveRows ? (event) => handleRowClick(event, row, rowIndex) : undefined}
                  onKeyDown={(event) => handleRowKeyDown(event, row, rowIndex)}
                >
                  {selectable ? (
                    <td className="bp-table__selection-cell">
                      <input
                        aria-label={`${selectionLabel}: ${rowIndex + 1}`}
                        checked={selected}
                        className="bp-table__selection-input"
                        type="checkbox"
                        onChange={() => toggleRowSelection(row, rowIndex)}
                        onClick={(event) => event.stopPropagation()}
                      />
                    </td>
                  ) : null}
                  {columns.map((column) => {
                    const cellClasses = [
                      column.className,
                      column.align ? `bp-table__cell--${column.align}` : '',
                    ].filter(Boolean).join(' ');

                    return (
                      <td className={cellClasses || undefined} key={column.key}>
                        {column.render ? column.render(row) : row[column.key]}
                      </td>
                    );
                  })}
                </tr>
              );
            })
          ) : (
            <tr>
              <td className="bp-table__empty" colSpan={Math.max(columnCount, 1)}>
                <strong>{emptyMessage}</strong>
                {emptyDescription ? <span>{emptyDescription}</span> : null}
              </td>
            </tr>
          )}
        </tbody>
      </table>
      {loading ? <span className="bp-sr-only" role="status">{loadingLabel}</span> : null}
    </div>
  );
}
