import { useId } from 'react';

export function Switch({
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
  const switchId = id || `bp-switch-${generatedId}`;
  const resolvedHelperText = helperText ?? hint;
  const resolvedError = errorMessage ?? error;
  const describedBy = [
    description ? `${switchId}-description` : '',
    resolvedHelperText ? `${switchId}-helper` : '',
    resolvedError ? `${switchId}-error` : '',
  ].filter(Boolean).join(' ') || undefined;

  return (
    <div className={['bp-field', props.disabled ? 'bp-field--disabled' : '', className].filter(Boolean).join(' ')}>
      <label className="bp-switch" htmlFor={switchId}>
        <input
          {...props}
          aria-describedby={describedBy}
          aria-invalid={Boolean(resolvedError)}
          checked={checked}
          className="bp-switch__input"
          id={switchId}
          role="switch"
          type="checkbox"
        />
        <span className="bp-switch__control" aria-hidden="true" />
        <span className="bp-switch__content">
          <span className="bp-switch__label">
            {label}
            {props.required ? <span className="bp-field__required" aria-hidden="true">*</span> : null}
          </span>
          {description ? <span className="bp-switch__description" id={`${switchId}-description`}>{description}</span> : null}
        </span>
      </label>
      {resolvedHelperText ? <p className="bp-field__hint" id={`${switchId}-helper`}>{resolvedHelperText}</p> : null}
      {resolvedError ? <p className="bp-field__error" id={`${switchId}-error`}>{resolvedError}</p> : null}
    </div>
  );
}
