export function Input({ error, hint, id, label, ...props }) {
  const describedBy = [hint ? `${id}-hint` : '', error ? `${id}-error` : ''].filter(Boolean).join(' ') || undefined;
  const className = ['bp-input__control', props.className].filter(Boolean).join(' ');
  const inputProps = { ...props };
  delete inputProps.className;

  return (
    <div className="bp-field">
      {label ? (
        <label className="bp-field__label" htmlFor={id}>
          {label}
        </label>
      ) : null}
      <input
        aria-describedby={describedBy}
        aria-invalid={Boolean(error)}
        className={className}
        id={id}
        {...inputProps}
      />
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
