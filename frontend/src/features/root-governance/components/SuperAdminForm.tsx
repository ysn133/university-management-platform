import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import type { SuperAdmin } from "../api/root-governance-api";

const superAdminFormSchema = z.object({
  universityEmail: z.string().trim().email("Enter a valid university email."),
  password: z.string().optional(),
  firstName: z.string().trim().min(1, "Enter the first name.").max(255),
  lastName: z.string().trim().min(1, "Enter the last name.").max(255),
  birthDate: z.string().min(1, "Select the birth date."),
  cin: z.string().trim().max(50).optional(),
  sex: z.enum(["MALE", "FEMALE"]),
  phoneNumber: z.string().trim().max(50).optional(),
});

export type SuperAdminFormValues = z.infer<typeof superAdminFormSchema>;

interface SuperAdminFormProps {
  superAdmin?: SuperAdmin;
  isSubmitting: boolean;
  requestError?: string | null;
  onCancel: () => void;
  onSubmit: (values: SuperAdminFormValues) => Promise<void>;
}

export function SuperAdminForm({
  superAdmin,
  isSubmitting,
  requestError,
  onCancel,
  onSubmit,
}: SuperAdminFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<SuperAdminFormValues>({
    resolver: zodResolver(superAdminFormSchema),
  });

  useEffect(() => {
    reset({
      universityEmail: superAdmin?.email ?? "",
      password: "",
      firstName: superAdmin?.firstName ?? "",
      lastName: superAdmin?.lastName ?? "",
      birthDate: superAdmin?.birthDate ?? "",
      cin: superAdmin?.cin ?? "",
      sex: superAdmin?.sex ?? "MALE",
      phoneNumber: superAdmin?.phoneNumber ?? "",
    });
  }, [reset, superAdmin]);

  async function submit(values: SuperAdminFormValues) {
    if (!superAdmin && (!values.password || values.password.length < 8)) {
      setError("password", { message: "The initial password must contain at least 8 characters." });
      return;
    }
    await onSubmit(values);
  }

  return (
    <form className="management-form management-form--two-columns" noValidate onSubmit={handleSubmit(submit)}>
      <div className="form-field">
        <label htmlFor="super-admin-first-name">First name</label>
        <input id="super-admin-first-name" {...register("firstName")} />
        {errors.firstName && <p className="field-error">{errors.firstName.message}</p>}
      </div>
      <div className="form-field">
        <label htmlFor="super-admin-last-name">Last name</label>
        <input id="super-admin-last-name" {...register("lastName")} />
        {errors.lastName && <p className="field-error">{errors.lastName.message}</p>}
      </div>
      <div className="form-field form-field--wide">
        <label htmlFor="super-admin-email">University email</label>
        <input autoComplete="off" id="super-admin-email" type="email" {...register("universityEmail")} />
        {errors.universityEmail && <p className="field-error">{errors.universityEmail.message}</p>}
      </div>
      {!superAdmin && (
        <div className="form-field form-field--wide">
          <label htmlFor="super-admin-password">Initial password</label>
          <input autoComplete="new-password" id="super-admin-password" type="password" {...register("password")} />
          {errors.password && <p className="field-error">{errors.password.message}</p>}
        </div>
      )}
      <div className="form-field">
        <label htmlFor="super-admin-birth-date">Birth date</label>
        <input id="super-admin-birth-date" max={new Date().toISOString().slice(0, 10)} type="date" {...register("birthDate")} />
        {errors.birthDate && <p className="field-error">{errors.birthDate.message}</p>}
      </div>
      <div className="form-field">
        <label htmlFor="super-admin-sex">Sex</label>
        <select id="super-admin-sex" {...register("sex")}>
          <option value="MALE">Male</option>
          <option value="FEMALE">Female</option>
        </select>
      </div>
      <div className="form-field">
        <label htmlFor="super-admin-cin">CIN</label>
        <input id="super-admin-cin" {...register("cin")} />
      </div>
      <div className="form-field">
        <label htmlFor="super-admin-phone">Phone number</label>
        <input id="super-admin-phone" type="tel" {...register("phoneNumber")} />
      </div>

      {requestError && <div className="management-alert management-alert--error form-field--wide">{requestError}</div>}

      <footer className="form-actions form-field--wide">
        <button className="secondary-button" onClick={onCancel} type="button">Cancel</button>
        <button className="management-primary-button" disabled={isSubmitting} type="submit">
          {isSubmitting ? "Saving..." : superAdmin ? "Save changes" : "Create Super Admin"}
        </button>
      </footer>
    </form>
  );
}
