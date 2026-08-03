import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import type { Establishment, EstablishmentType } from "../api/root-governance-api";

const establishmentFormSchema = z.object({
  name: z.string().trim().min(2, "Enter an establishment name.").max(255),
  type: z.enum(["FACULTY", "SCHOOL", "INSTITUTE"]),
});

export type EstablishmentFormValues = z.infer<typeof establishmentFormSchema>;

interface EstablishmentFormProps {
  establishment?: Establishment;
  isSubmitting: boolean;
  requestError?: string | null;
  onCancel: () => void;
  onSubmit: (values: EstablishmentFormValues) => Promise<void>;
}

export function EstablishmentForm({
  establishment,
  isSubmitting,
  requestError,
  onCancel,
  onSubmit,
}: EstablishmentFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<EstablishmentFormValues>({
    resolver: zodResolver(establishmentFormSchema),
    defaultValues: {
      name: establishment?.name ?? "",
      type: establishment?.type ?? "FACULTY",
    },
  });

  useEffect(() => {
    reset({ name: establishment?.name ?? "", type: establishment?.type ?? "FACULTY" });
  }, [establishment, reset]);

  return (
    <form className="management-form" noValidate onSubmit={handleSubmit(onSubmit)}>
      <div className="form-field form-field--wide">
        <label htmlFor="establishment-name">Official name</label>
        <input id="establishment-name" {...register("name")} />
        {errors.name && <p className="field-error">{errors.name.message}</p>}
      </div>

      <div className="form-field form-field--wide">
        <label htmlFor="establishment-type">Establishment type</label>
        <select id="establishment-type" {...register("type")}>
          {(["FACULTY", "SCHOOL", "INSTITUTE"] satisfies EstablishmentType[]).map((type) => (
            <option key={type} value={type}>{type[0] + type.slice(1).toLowerCase()}</option>
          ))}
        </select>
      </div>

      {requestError && <div className="management-alert management-alert--error">{requestError}</div>}

      <footer className="form-actions">
        <button className="secondary-button" onClick={onCancel} type="button">Cancel</button>
        <button className="management-primary-button" disabled={isSubmitting} type="submit">
          {isSubmitting ? "Saving..." : establishment ? "Save changes" : "Create establishment"}
        </button>
      </footer>
    </form>
  );
}
