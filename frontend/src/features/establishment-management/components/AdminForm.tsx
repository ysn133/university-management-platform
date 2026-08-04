import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import type { AdminAccount } from "../api/establishment-admin-api";

const adminFormSchema = z.object({
  universityEmail: z.string().trim().email("Enter a valid university email."),
  password: z.string().optional(),
  firstName: z.string().trim().min(1, "Enter the first name.").max(255),
  lastName: z.string().trim().min(1, "Enter the last name.").max(255),
  birthDate: z.string().min(1, "Select the birth date."),
  cin: z.string().trim().max(50).optional(),
  sex: z.enum(["MALE", "FEMALE"]),
  phoneNumber: z.string().trim().max(50).optional(),
});

export type AdminFormValues = z.infer<typeof adminFormSchema>;

interface AdminFormProps {
  admin?: AdminAccount;
  isSubmitting: boolean;
  requestError?: string | null;
  onCancel: () => void;
  onSubmit: (values: AdminFormValues) => Promise<void>;
  submitLabel?: string;
}

export function AdminForm({ admin, isSubmitting, requestError, onCancel, onSubmit, submitLabel }: AdminFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<AdminFormValues>({ resolver: zodResolver(adminFormSchema) });

  useEffect(() => {
    reset({
      universityEmail: admin?.email ?? "",
      password: "",
      firstName: admin?.firstName ?? "",
      lastName: admin?.lastName ?? "",
      birthDate: admin?.birthDate ?? "",
      cin: admin?.cin ?? "",
      sex: admin?.sex ?? "MALE",
      phoneNumber: admin?.phoneNumber ?? "",
    });
  }, [admin, reset]);

  async function submit(values: AdminFormValues) {
    if (!admin && (!values.password || values.password.length < 8)) {
      setError("password", { message: "The initial password must contain at least 8 characters." });
      return;
    }
    await onSubmit(values);
  }

  return (
    <form className="management-form management-form--two-columns" noValidate onSubmit={handleSubmit(submit)}>
      <div className="form-field">
        <label htmlFor="admin-first-name">First name</label>
        <input id="admin-first-name" {...register("firstName")} />
        {errors.firstName && <p className="field-error">{errors.firstName.message}</p>}
      </div>
      <div className="form-field">
        <label htmlFor="admin-last-name">Last name</label>
        <input id="admin-last-name" {...register("lastName")} />
        {errors.lastName && <p className="field-error">{errors.lastName.message}</p>}
      </div>
      <div className="form-field form-field--wide">
        <label htmlFor="admin-email">University email</label>
        <input autoComplete="off" id="admin-email" type="email" {...register("universityEmail")} />
        {errors.universityEmail && <p className="field-error">{errors.universityEmail.message}</p>}
      </div>
      {!admin && (
        <div className="form-field form-field--wide">
          <label htmlFor="admin-password">Initial password</label>
          <input autoComplete="new-password" id="admin-password" type="password" {...register("password")} />
          {errors.password && <p className="field-error">{errors.password.message}</p>}
        </div>
      )}
      <div className="form-field">
        <label htmlFor="admin-birth-date">Birth date</label>
        <input id="admin-birth-date" max={new Date().toISOString().slice(0, 10)} type="date" {...register("birthDate")} />
        {errors.birthDate && <p className="field-error">{errors.birthDate.message}</p>}
      </div>
      <div className="form-field">
        <label htmlFor="admin-sex">Sex</label>
        <select id="admin-sex" {...register("sex")}>
          <option value="MALE">Male</option>
          <option value="FEMALE">Female</option>
        </select>
      </div>
      {admin && (
        <div className="form-field">
          <label htmlFor="admin-cin">CIN</label>
          <input id="admin-cin" {...register("cin")} />
        </div>
      )}
      <div className={`form-field ${admin ? "" : "form-field--wide"}`}>
        <label htmlFor="admin-phone">Phone number</label>
        <input id="admin-phone" type="tel" {...register("phoneNumber")} />
      </div>

      {requestError && <div className="management-alert management-alert--error form-field--wide">{requestError}</div>}

      <footer className="form-actions form-field--wide">
        <button className="secondary-button" onClick={onCancel} type="button">Cancel</button>
        <button className="management-primary-button" disabled={isSubmitting} type="submit">
          {isSubmitting ? "Saving..." : submitLabel ?? (admin ? "Save changes" : "Create Admin")}
        </button>
      </footer>
    </form>
  );
}
