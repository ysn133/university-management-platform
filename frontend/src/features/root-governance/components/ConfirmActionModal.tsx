import { ManagementModal } from "./ManagementModal";

interface ConfirmActionModalProps {
  title: string;
  description: string;
  actionLabel: string;
  isSubmitting: boolean;
  destructive?: boolean;
  error?: string | null;
  onCancel: () => void;
  onConfirm: () => void;
}

export function ConfirmActionModal({
  title,
  description,
  actionLabel,
  isSubmitting,
  destructive = false,
  error,
  onCancel,
  onConfirm,
}: ConfirmActionModalProps) {
  return (
    <ManagementModal title={title} description={description} onClose={onCancel}>
      {error && <div className="management-alert management-alert--error">{error}</div>}
      <footer className="form-actions">
        <button className="secondary-button" onClick={onCancel} type="button">Cancel</button>
        <button
          className={destructive ? "danger-button" : "management-primary-button"}
          disabled={isSubmitting}
          onClick={onConfirm}
          type="button"
        >
          {isSubmitting ? "Working..." : actionLabel}
        </button>
      </footer>
    </ManagementModal>
  );
}
