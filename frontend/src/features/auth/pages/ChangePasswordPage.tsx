import { zodResolver } from "@hookform/resolvers/zod";
import { type ComponentProps, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useAuth } from "../hooks/useAuth";

const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(8, "Enter your current password."),
    newPassword: z.string().min(8, "The new password must contain at least 8 characters."),
    confirmPassword: z.string(),
  })
  .refine((value) => value.newPassword === value.confirmPassword, {
    message: "The new passwords do not match.",
    path: ["confirmPassword"],
  });

type ChangePasswordForm = z.infer<typeof changePasswordSchema>;

interface PasswordFieldProps extends ComponentProps<"input"> {
  label: string;
  error?: string;
}

function PasswordField({ label, error, id, ...inputProps }: PasswordFieldProps) {
  const [isVisible, setVisible] = useState(false);

  return (
    <div className="security-field">
      <label htmlFor={id}>{label}</label>
      <div className="password-input">
        <input id={id} type={isVisible ? "text" : "password"} {...inputProps} />
        <button
          aria-label={`${isVisible ? "Hide" : "Show"} ${label.toLowerCase()}`}
          onClick={() => setVisible((current) => !current)}
          type="button"
        >
          {isVisible ? "Hide" : "Show"}
        </button>
      </div>
      {error && <p className="field-error">{error}</p>}
    </div>
  );
}

export function ChangePasswordPage() {
  const { changePassword, user } = useAuth();
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ChangePasswordForm>({ resolver: zodResolver(changePasswordSchema) });

  async function submit(values: ChangePasswordForm) {
    setMessage(null);
    try {
      await changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      reset();
      setMessage({ type: "success", text: "Your password has been changed." });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof ApiRequestError ? error.message : "The password could not be changed.",
      });
    }
  }

  return (
    <section className="account-page">
      <header className="account-page__title">
        <p className="management-kicker">Account</p>
        <h1>Profile and security</h1>
      </header>

      <article className="account-profile-card">
        <div className="account-profile-card__identity">
          <span className="account-profile-card__avatar">{user?.firstName.slice(0, 1)}{user?.lastName.slice(0, 1)}</span>
          <div>
            <h2>{user?.firstName} {user?.lastName}</h2>
            <p>{user?.universityEmail}</p>
          </div>
        </div>
        <dl className="account-profile-card__facts">
          <div><dt>Role</dt><dd>{user?.role.replaceAll("_", " ")}</dd></div>
          <div><dt>Status</dt><dd className="account-profile-status"><span /> {user?.accountStatus}</dd></div>
        </dl>
      </article>

      <article className="account-security-card">
        <header>
          <div>
            <p className="management-kicker">Security</p>
            <h2>Change password</h2>
          </div>
          <p>Use at least 8 characters. Your university email can only be changed by authorized administration.</p>
        </header>

        <form className="account-password-form" noValidate onSubmit={handleSubmit(submit)}>
          <PasswordField
            autoComplete="current-password"
            error={errors.currentPassword?.message}
            id="current-password"
            label="Current password"
            {...register("currentPassword")}
          />

          <div className="account-password-form__new">
            <PasswordField
              autoComplete="new-password"
              error={errors.newPassword?.message}
              id="new-password"
              label="New password"
              {...register("newPassword")}
            />
            <PasswordField
              autoComplete="new-password"
              error={errors.confirmPassword?.message}
              id="confirm-password"
              label="Confirm new password"
              {...register("confirmPassword")}
            />
          </div>

          {message && (
            <div className={`security-alert security-alert--${message.type}`} role="status">
              <strong>{message.type === "success" ? "Password updated" : "Update failed"}</strong>
              <span>{message.text}</span>
            </div>
          )}

          <footer className="account-password-form__actions">
            <button className="management-primary-button" disabled={isSubmitting} type="submit">
              {isSubmitting ? "Updating..." : "Update password"}
            </button>
          </footer>
        </form>
      </article>
    </section>
  );
}
