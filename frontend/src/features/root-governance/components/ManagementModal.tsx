import { type PropsWithChildren, useEffect, useId, useRef } from "react";
import { createPortal } from "react-dom";

interface ManagementModalProps extends PropsWithChildren {
  title: string;
  description?: string;
  onClose: () => void;
  size?: "default" | "wide";
}

export function ManagementModal({ title, description, onClose, children, size = "default" }: ManagementModalProps) {
  const titleId = useId();
  const descriptionId = useId();
  const closeButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    const previouslyFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null;
    const previousOverflow = document.body.style.overflow;

    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onClose();
      }
    }

    document.body.style.overflow = "hidden";
    document.addEventListener("keydown", closeOnEscape);
    closeButtonRef.current?.focus();

    return () => {
      document.body.style.overflow = previousOverflow;
      document.removeEventListener("keydown", closeOnEscape);
      previouslyFocused?.focus();
    };
  }, [onClose]);

  return createPortal(
    <div className="management-modal-backdrop" onMouseDown={onClose}>
      <section
        aria-describedby={description ? descriptionId : undefined}
        aria-labelledby={titleId}
        aria-modal="true"
        className={`management-modal management-modal--${size}`}
        onMouseDown={(event) => event.stopPropagation()}
        role="dialog"
      >
        <header>
          <div>
            <p className="management-kicker">Management action</p>
            <h2 id={titleId}>{title}</h2>
            {description && <p id={descriptionId}>{description}</p>}
          </div>
          <button aria-label="Close dialog" className="icon-button" onClick={onClose} ref={closeButtonRef} type="button">
            ×
          </button>
        </header>
        <div className="management-modal-body">{children}</div>
      </section>
    </div>,
    document.body,
  );
}
