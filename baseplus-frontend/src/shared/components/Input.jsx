import { useId } from 'react';

export function Input({
  error,
  errorMessage,
  helperText,
  hint,
  id,
  label,
  loading = false,
  prefix,
  prefixIcon: PrefixIcon,
  suffix,
  suffixIcon: SuffixIcon,
  ...props
}) {
  const generatedId = useId();
  const inputId = id || `bp-input-${generatedId}`;
  const resolvedHelperText = helperText ?? hint;
  const resolvedError = errorMessage ?? error;
  const describedBy = [
    resolvedHelperText ? `${inputId}-helper` : '',
    resolvedError ? `${inputId}-error` : '',
  ].filter(Boolean).join(' ') || undefined;
  const hasPrefix = Boolean(prefix || PrefixIcon);
  const hasSuffix = Boolean(suffix || SuffixIcon || loading);
  const className = [
    'bp-input__control',
    hasPrefix ? 'bp-input__control--with-prefix' : '',
    hasSuffix ? 'bp-input__control--with-suffix' : '',
    props.className,
  ].filter(Boolean).join(' ');
  const inputProps = { ...props };
  delete inputProps.className;

  return (
    <div className={['bp-field', props.disabled ? 'bp-field--disabled' : '', props.readOnly ? 'bp-field--readonly' : ''].filter(Boolean).join(' ')}>
      {label ? (
        <label className="bp-field__label" htmlFor={inputId}>
          {label}
          {props.required ? <span className="bp-field__required" aria-hidden="true">*</span> : null}
        </label>
      ) : null}
      <div className={['bp-input', hasPrefix ? 'bp-input--with-prefix' : '', hasSuffix ? 'bp-input--with-suffix' : ''].filter(Boolean).join(' ')}>
        {hasPrefix ? (
          <span className="bp-input__affix bp-input__affix--prefix">
            {PrefixIcon ? <PrefixIcon aria-hidden="true" size={16} strokeWidth={2} /> : prefix}
          </span>
        ) : null}
        <input
          aria-busy={loading || undefined}
          aria-describedby={describedBy}
          aria-invalid={Boolean(resolvedError)}
          className={className}
          id={inputId}
          {...inputProps}
        />
        {hasSuffix ? (
          <span className="bp-input__affix bp-input__affix--suffix">
            {loading ? <span className="bp-input__spinner" aria-hidden="true" /> : null}
            {!loading && SuffixIcon ? <SuffixIcon aria-hidden="true" size={16} strokeWidth={2} /> : null}
            {!loading && !SuffixIcon ? suffix : null}
          </span>
        ) : null}
      </div>
      {resolvedHelperText ? (
        <p className="bp-field__hint" id={`${inputId}-helper`}>
          {resolvedHelperText}
        </p>
      ) : null}
      {resolvedError ? (
        <p className="bp-field__error" id={`${inputId}-error`}>
          {resolvedError}
        </p>
      ) : null}
    </div>
  );
}
