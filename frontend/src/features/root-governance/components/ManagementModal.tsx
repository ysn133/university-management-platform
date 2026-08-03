import { type PropsWithChildren, useEffect } from "react";

interface ManagementModalProps extends PropsWithChildren {
  title: string;
  description?: string;
  onClose: () => void;
}

export function ManagementModal({ title, description, onClose, children }: ManagementModalProps) {
  useEffect(() => {
    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onClose();
      }
    }

    document.addEventListener("keydown", closeOnEscape);
    return () => document.removeEventListener("keydown", closeOnEscape);
  }, [onClose]);

  return (
    <div className="management-modal-backdrop" onMouseDown={onClose}>
      <section
        aria-describedby={description ? "management-modal-description" : undefined}
        aria-labelledby="management-modal-title"
        aria-modal="true"
        className="management-modal"
        onMouseDown={(event) => event.stopPropagation()}
        role="dialog"
      >
        <header>
          <div>
            <p className="management-kicker">University governance</p>
            <h2 id="management-modal-title">{title}</h2>
            {description && <p id="management-modal-description">{description}</p>}
          </div>
          <button aria-label="Close dialog" className="icon-button" onClick={onClose} type="button">
            ×
          </button>
        </header>
        {children}
      </section>
    </div>
  );
}
