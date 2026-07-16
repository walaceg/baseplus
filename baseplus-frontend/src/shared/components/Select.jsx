import { useId } from 'react';

export function Select({
  error,
  errorMessage,
  helperText,
  hint,
  id,
  label,
  loading = false,
  options = [],
  placeholder,
  ...props
}) {
  const generatedId = useId();
  const selectId = id || `bp-select-${generatedId}`;
  const resolvedHelperText = helperText ?? hint;
  const resolvedError = errorMessage ?? error;
  const describedBy = [
    resolvedHelperText ? `${selectId}-helper` : '',
    resolvedError ? `${selectId}-error` : '',
  ].filter(Boolean).join(' ') || undefined;
  const isMultiple = Boolean(props.multiple);
  const value = isMultiple && !Array.isArray(props.value) ? [] : props.value;
  const disabled = props.disabled || loading;

  return (
    <div className={['bp-field', disabled ? 'bp-field--disabled' : ''].filter(Boolean).join(' ')}>
      {label ? (
        <label className="bp-field__label" htmlFor={selectId}>
          {label}
          {props.required ? <span className="bp-field__required" aria-hidden="true">*</span> : null}
        </label>
      ) : null}
      <div className={['bp-select', loading ? 'bp-select--loading' : ''].filter(Boolean).join(' ')}>
        <select
          {...props}
          aria-busy={loading || undefined}
          aria-describedby={describedBy}
          aria-invalid={Boolean(resolvedError)}
          className="bp-select__control"
          disabled={disabled}
          id={selectId}
          value={value}
        >
          {!isMultiple && placeholder ? <option value="">{placeholder}</option> : null}
          {options.map((option) => (
            <option disabled={option.disabled} key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        {loading ? (
          <span className="bp-select__status" aria-hidden="true">
            <span className="bp-input__spinner" />
          </span>
        ) : null}
      </div>
      {resolvedHelperText ? (
        <p className="bp-field__hint" id={`${selectId}-helper`}>
          {resolvedHelperText}
        </p>
      ) : null}
      {resolvedError ? (
        <p className="bp-field__error" id={`${selectId}-error`}>
          {resolvedError}
        </p>
      ) : null}
    </div>
  );
}
