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
    <section className="security-page">
      <header className="security-page-header">
        <div>
          <p className="management-kicker">Account control</p>
          <h1>Security &amp; password</h1>
          <p>Manage the password used to access your university account.</p>
        </div>
        <span className="security-state"><span /> Protected account</span>
      </header>

      <div className="security-layout">
        <article className="security-card security-card--form">
          <header className="security-card-header">
            <span className="security-icon" aria-hidden="true">
              <svg fill="none" viewBox="0 0 24 24">
                <rect height="10" rx="2" stroke="currentColor" strokeWidth="1.8" width="14" x="5" y="10" />
                <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" strokeLinecap="round" strokeWidth="1.8" />
                <path d="M12 14v2" stroke="currentColor" strokeLinecap="round" strokeWidth="1.8" />
              </svg>
            </span>
            <div>
              <h2>Change your password</h2>
              <p>Confirm your current password before choosing a new one.</p>
            </div>
          </header>

          <form className="security-form" noValidate onSubmit={handleSubmit(submit)}>
            <PasswordField
              autoComplete="current-password"
              error={errors.currentPassword?.message}
              id="current-password"
              label="Current password"
              {...register("currentPassword")}
            />

            <div className="security-form-divider"><span>New credentials</span></div>

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

            {message && (
              <div className={`security-alert security-alert--${message.type}`} role="status">
                <strong>{message.type === "success" ? "Password updated" : "Update failed"}</strong>
                <span>{message.text}</span>
              </div>
            )}

            <footer className="security-form-actions">
              <p>You will use the new password the next time you sign in.</p>
              <button className="management-primary-button" disabled={isSubmitting} type="submit">
                {isSubmitting ? "Updating..." : "Update password"}
              </button>
            </footer>
          </form>
        </article>

        <aside className="security-aside">
          <article className="security-card security-account-card">
            <p className="management-kicker">Signed-in account</p>
            <div className="security-account-identity">
              <span>{user?.firstName.slice(0, 1)}{user?.lastName.slice(0, 1)}</span>
              <div>
                <strong>{user?.firstName} {user?.lastName}</strong>
                <small>{user?.role.replaceAll("_", " ")}</small>
              </div>
            </div>
            <dl>
              <div><dt>University email</dt><dd>{user?.universityEmail}</dd></div>
              <div><dt>Account status</dt><dd className="account-active"><span /> {user?.accountStatus}</dd></div>
            </dl>
            <p className="security-account-note">Your university email is managed by authorized administration and cannot be changed here.</p>
          </article>

          <article className="security-card security-guidance-card">
            <h2>Password guidance</h2>
            <ul>
              <li><span>01</span> Use at least 8 characters.</li>
              <li><span>02</span> Do not reuse a password from another service.</li>
              <li><span>03</span> Keep your password private.</li>
            </ul>
          </article>
        </aside>
      </div>
    </section>
  );
}
