import { useId } from 'react';

export function TextArea({
  className = '',
  counter = false,
  error,
  errorMessage,
  helperText,
  hint,
  id,
  label,
  maxLength,
  resize = 'vertical',
  value,
  ...props
}) {
  const generatedId = useId();
  const textAreaId = id || `bp-textarea-${generatedId}`;
  const resolvedHelperText = helperText ?? hint;
  const resolvedError = errorMessage ?? error;
  const describedBy = [
    resolvedHelperText ? `${textAreaId}-helper` : '',
    resolvedError ? `${textAreaId}-error` : '',
    counter && maxLength ? `${textAreaId}-counter` : '',
  ].filter(Boolean).join(' ') || undefined;
  const currentLength = String(value ?? props.defaultValue ?? '').length;
  const resizeClassName = ['none', 'both', 'horizontal'].includes(resize) ? `bp-textarea__control--resize-${resize}` : '';
  const controlClassName = ['bp-textarea__control', resizeClassName, className].filter(Boolean).join(' ');

  return (
    <div className={['bp-field', props.disabled ? 'bp-field--disabled' : '', props.readOnly ? 'bp-field--readonly' : ''].filter(Boolean).join(' ')}>
      {label ? (
        <label className="bp-field__label" htmlFor={textAreaId}>
          {label}
          {props.required ? <span className="bp-field__required" aria-hidden="true">*</span> : null}
        </label>
      ) : null}
      <textarea
        {...props}
        aria-describedby={describedBy}
        aria-invalid={Boolean(resolvedError)}
        className={controlClassName}
        id={textAreaId}
        maxLength={maxLength}
        value={value}
      />
      <div className={['bp-field__meta', counter && maxLength ? 'bp-field__meta--split' : ''].filter(Boolean).join(' ')}>
        <div>
          {resolvedHelperText ? (
            <p className="bp-field__hint" id={`${textAreaId}-helper`}>
              {resolvedHelperText}
            </p>
          ) : null}
          {resolvedError ? (
            <p className="bp-field__error" id={`${textAreaId}-error`}>
              {resolvedError}
            </p>
          ) : null}
        </div>
        {counter && maxLength ? (
          <p className="bp-field__counter" id={`${textAreaId}-counter`}>
            {currentLength}/{maxLength}
          </p>
        ) : null}
      </div>
    </div>
  );
}
