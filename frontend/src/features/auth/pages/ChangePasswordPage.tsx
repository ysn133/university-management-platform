import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
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

export function ChangePasswordPage() {
  const { changePassword } = useAuth();
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
      <p className="eyebrow">Account security</p>
      <h1>Change password</h1>
      <p>Choose a password that is not used for another service.</p>

      <form className="account-form" noValidate onSubmit={handleSubmit(submit)}>
        <label htmlFor="current-password">Current password</label>
        <input id="current-password" type="password" {...register("currentPassword")} />
        {errors.currentPassword && <p className="field-error">{errors.currentPassword.message}</p>}

        <label htmlFor="new-password">New password</label>
        <input id="new-password" type="password" {...register("newPassword")} />
        {errors.newPassword && <p className="field-error">{errors.newPassword.message}</p>}

        <label htmlFor="confirm-password">Confirm new password</label>
        <input id="confirm-password" type="password" {...register("confirmPassword")} />
        {errors.confirmPassword && <p className="field-error">{errors.confirmPassword.message}</p>}

        {message && (
          <div className={`form-alert form-alert--${message.type}`} role="status">
            {message.text}
          </div>
        )}

        <button className="primary-button" disabled={isSubmitting} type="submit">
          {isSubmitting ? "Updating…" : "Update password"}
        </button>
      </form>
    </section>
  );
}
