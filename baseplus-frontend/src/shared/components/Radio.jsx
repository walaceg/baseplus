import { useId } from 'react';

export function Radio({
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
  const radioId = id || `bp-radio-${generatedId}`;
  const resolvedHelperText = helperText ?? hint;
  const resolvedError = errorMessage ?? error;
  const describedBy = [
    description ? `${radioId}-description` : '',
    resolvedHelperText ? `${radioId}-helper` : '',
    resolvedError ? `${radioId}-error` : '',
  ].filter(Boolean).join(' ') || undefined;

  return (
    <div className={['bp-field', props.disabled ? 'bp-field--disabled' : '', className].filter(Boolean).join(' ')}>
      <label className="bp-choice" htmlFor={radioId}>
        <input
          {...props}
          aria-describedby={describedBy}
          aria-invalid={Boolean(resolvedError)}
          checked={checked}
          className="bp-choice__input"
          id={radioId}
          type="radio"
        />
        <span className="bp-choice__box bp-choice__box--radio" aria-hidden="true" />
        <span className="bp-choice__content">
          <span className="bp-choice__label">
            {label}
            {props.required ? <span className="bp-field__required" aria-hidden="true">*</span> : null}
          </span>
          {description ? <span className="bp-choice__description" id={`${radioId}-description`}>{description}</span> : null}
        </span>
      </label>
      {resolvedHelperText ? <p className="bp-field__hint" id={`${radioId}-helper`}>{resolvedHelperText}</p> : null}
      {resolvedError ? <p className="bp-field__error" id={`${radioId}-error`}>{resolvedError}</p> : null}
    </div>
  );
}
