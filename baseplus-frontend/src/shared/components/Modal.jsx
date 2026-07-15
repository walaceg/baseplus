import { useEffect, useId, useRef } from 'react';
import { X } from 'lucide-react';
import { Button } from './Button.jsx';

const FOCUSABLE_SELECTOR = [
  'a[href]',
  'button:not([disabled])',
  'textarea:not([disabled])',
  'input:not([disabled])',
  'select:not([disabled])',
  '[tabindex]:not([tabindex="-1"])',
].join(',');

export function Modal({ children, closeOnOverlayClick = true, description, footer, isOpen, onClose, title }) {
  const modalRef = useRef(null);
  const onCloseRef = useRef(onClose);
  const previouslyFocusedElementRef = useRef(null);
  const titleId = useId();
  const descriptionId = useId();

  useEffect(() => {
    onCloseRef.current = onClose;
  }, [onClose]);

  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    previouslyFocusedElementRef.current = document.activeElement instanceof HTMLElement
      ? document.activeElement
      : null;

    const modalElement = modalRef.current;
    const focusableElements = getFocusableElements(modalElement);
    const firstFocusableElement = focusableElements[0];

    window.setTimeout(() => {
      if (!modalRef.current) {
        return;
      }

      (firstFocusableElement ?? modalRef.current).focus();
    }, 0);

    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        event.preventDefault();
        onCloseRef.current?.();
        return;
      }

      if (event.key !== 'Tab') {
        return;
      }

      trapFocus(event, modalRef.current);
    }

    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('keydown', handleKeyDown);

      const previouslyFocusedElement = previouslyFocusedElementRef.current;
      if (previouslyFocusedElement && document.contains(previouslyFocusedElement)) {
        previouslyFocusedElement.focus();
      }
    };
  }, [isOpen]);

  if (!isOpen) {
    return null;
  }

  function handleOverlayMouseDown(event) {
    if (!closeOnOverlayClick || event.target !== event.currentTarget) {
      return;
    }

    onClose?.();
  }

  return (
    <div className="bp-modal__overlay" role="presentation" onMouseDown={handleOverlayMouseDown}>
      <section
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
        aria-describedby={description ? descriptionId : undefined}
        className="bp-modal"
        role="dialog"
        ref={modalRef}
        tabIndex={-1}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className="bp-modal__header">
          <h2 className="bp-modal__title" id={titleId}>
            {title}
          </h2>
          <Button aria-label="Fechar modal" className="bp-modal__close" size="sm" variant="ghost" onClick={onClose}>
            <X aria-hidden="true" size={18} strokeWidth={2.2} />
          </Button>
        </header>
        {description ? (
          <p className="bp-modal__description" id={descriptionId}>
            {description}
          </p>
        ) : null}
        {children ? <div className="bp-modal__body">{children}</div> : null}
        {footer ? <footer className="bp-modal__footer">{footer}</footer> : null}
      </section>
    </div>
  );
}

function getFocusableElements(container) {
  if (!container) {
    return [];
  }

  return Array.from(container.querySelectorAll(FOCUSABLE_SELECTOR)).filter((element) => {
    return element instanceof HTMLElement && !element.hasAttribute('disabled') && element.getAttribute('aria-hidden') !== 'true';
  });
}

function trapFocus(event, container) {
  const focusableElements = getFocusableElements(container);

  if (!focusableElements.length) {
    event.preventDefault();
    container?.focus();
    return;
  }

  const firstElement = focusableElements[0];
  const lastElement = focusableElements[focusableElements.length - 1];
  const activeElement = document.activeElement;

  if (event.shiftKey && activeElement === firstElement) {
    event.preventDefault();
    lastElement.focus();
    return;
  }

  if (!event.shiftKey && activeElement === lastElement) {
    event.preventDefault();
    firstElement.focus();
  }
}
