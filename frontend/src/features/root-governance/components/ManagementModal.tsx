import { type PropsWithChildren, useEffect, useEffectEvent, useId, useRef } from "react";
import { createPortal } from "react-dom";

interface ManagementModalProps extends PropsWithChildren {
  title: string;
  description?: string;
  onClose: () => void;
  size?: "default" | "wide";
}

let openModalCount = 0;
let bodyOverflowBeforeModals = "";

export function ManagementModal({ title, description, onClose, children, size = "default" }: ManagementModalProps) {
  const titleId = useId();
  const descriptionId = useId();
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const closeModal = useEffectEvent(onClose);

  useEffect(() => {
    const previouslyFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null;

    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        closeModal();
      }
    }

    if (openModalCount === 0) {
      bodyOverflowBeforeModals = document.body.style.overflow;
      document.body.style.overflow = "hidden";
    }
    openModalCount += 1;
    document.addEventListener("keydown", closeOnEscape);
    closeButtonRef.current?.focus();

    return () => {
      openModalCount = Math.max(0, openModalCount - 1);
      if (openModalCount === 0) {
        document.body.style.overflow = bodyOverflowBeforeModals;
      }
      document.removeEventListener("keydown", closeOnEscape);
      previouslyFocused?.focus();
    };
  }, []);

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
