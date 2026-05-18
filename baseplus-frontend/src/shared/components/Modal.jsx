import { Button } from './Button.jsx';

export function Modal({ children, footer, isOpen, onClose, title }) {
  if (!isOpen) {
    return null;
  }

  return (
    <div className="bp-modal__overlay" role="presentation" onMouseDown={onClose}>
      <section
        aria-modal="true"
        aria-labelledby="bp-modal-title"
        className="bp-modal"
        role="dialog"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className="bp-modal__header">
          <h2 className="bp-modal__title" id="bp-modal-title">
            {title}
          </h2>
          <Button aria-label="Fechar modal" size="sm" variant="ghost" onClick={onClose}>
            X
          </Button>
        </header>
        <div className="bp-modal__body">{children}</div>
        {footer ? <footer className="bp-modal__footer">{footer}</footer> : null}
      </section>
    </div>
  );
}
