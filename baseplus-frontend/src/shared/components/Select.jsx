export function Select({ error, hint, id, label, options = [], placeholder, ...props }) {
  const describedBy = [hint ? `${id}-hint` : '', error ? `${id}-error` : ''].filter(Boolean).join(' ') || undefined;
  const isMultiple = Boolean(props.multiple);
  const value = isMultiple && !Array.isArray(props.value) ? [] : props.value;

  return (
    <div className="bp-field">
      {label ? (
        <label className="bp-field__label" htmlFor={id}>
          {label}
        </label>
      ) : null}
      <select
        aria-describedby={describedBy}
        aria-invalid={Boolean(error)}
        className="bp-select__control"
        id={id}
        {...props}
        value={value}
      >
        {!isMultiple && placeholder ? <option value="">{placeholder}</option> : null}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {hint ? (
        <p className="bp-field__hint" id={`${id}-hint`}>
          {hint}
        </p>
      ) : null}
      {error ? (
        <p className="bp-field__error" id={`${id}-error`}>
          {error}
        </p>
      ) : null}
    </div>
  );
}
