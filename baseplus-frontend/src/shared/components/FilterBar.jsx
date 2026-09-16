export function FilterBar({ children }) {
  return <div className="bp-list-page__toolbar">{children}</div>;
}

FilterBar.Row = function FilterBarRow({ children }) {
  return <div className="bp-list-page__toolbar-row">{children}</div>;
};

FilterBar.Search = function FilterBarSearch({ children }) {
  return <div className="bp-list-page__search">{children}</div>;
};

FilterBar.Actions = function FilterBarActions({ children }) {
  return <div className="bp-list-page__actions">{children}</div>;
};

FilterBar.Filters = function FilterBarFilters({ children, className = '' }) {
  return <div className={['bp-list-page__filters', className].filter(Boolean).join(' ')}>{children}</div>;
};
