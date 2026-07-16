import { useId } from 'react';

export function Checkbox({
  checked,
  className = '',
  description,
  error,
  errorMessage,
  helperText,
  hint,
  id,
  label,
  ...props
}) {
  const generatedId = useId();
  const checkboxId = id || `bp-checkbox-${generatedId}`;
  const resolvedHelperText = helperText ?? hint;
  const resolvedError = errorMessage ?? error;
  const describedBy = [
    description ? `${checkboxId}-description` : '',
    resolvedHelperText ? `${checkboxId}-helper` : '',
    resolvedError ? `${checkboxId}-error` : '',
  ].filter(Boolean).join(' ') || undefined;

  return (
    <div className={['bp-field', props.disabled ? 'bp-field--disabled' : '', className].filter(Boolean).join(' ')}>
      <label className="bp-choice" htmlFor={checkboxId}>
        <input
          {...props}
          aria-describedby={describedBy}
          aria-invalid={Boolean(resolvedError)}
          checked={checked}
          className="bp-choice__input"
          id={checkboxId}
          type="checkbox"
        />
        <span className="bp-choice__box" aria-hidden="true" />
        <span className="bp-choice__content">
          <span className="bp-choice__label">
            {label}
            {props.required ? <span className="bp-field__required" aria-hidden="true">*</span> : null}
          </span>
          {description ? <span className="bp-choice__description" id={`${checkboxId}-description`}>{description}</span> : null}
        </span>
      </label>
      {resolvedHelperText ? <p className="bp-field__hint" id={`${checkboxId}-helper`}>{resolvedHelperText}</p> : null}
      {resolvedError ? <p className="bp-field__error" id={`${checkboxId}-error`}>{resolvedError}</p> : null}
    </div>
  );
}
