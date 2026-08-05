import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";

const studentFormSchema = z.object({
  apogeeCode: z.string().trim().min(1, "Enter the Apogee code.").max(50),
  nationalStudentCode: z.string().trim().max(50).optional(),
  cin: z.string().trim().max(50).optional(),
  initialEnrollmentDate: z.string().min(1, "Select the initial enrollment date."),
  universityEmail: z.string().trim().email("Enter a valid university email."),
  password: z.string().min(8, "The initial password must contain at least 8 characters."),
  firstName: z.string().trim().min(1, "Enter the first name.").max(255),
  lastName: z.string().trim().min(1, "Enter the last name.").max(255),
  birthDate: z.string().min(1, "Select the birth date."),
  placeOfBirth: z.string().trim().min(1, "Enter the place of birth.").max(255),
  nationality: z.string().trim().min(1, "Enter the nationality.").max(100),
  sex: z.enum(["MALE", "FEMALE"]),
  phoneNumber: z.string().trim().max(50).optional(),
});

export type StudentFormValues = z.infer<typeof studentFormSchema>;

interface StudentFormProps {
  isSubmitting: boolean;
  requestError?: string | null;
  onUseExistingStudent: () => void;
  onSubmit: (values: StudentFormValues) => Promise<void>;
}

export function StudentForm({ isSubmitting, requestError, onUseExistingStudent, onSubmit }: StudentFormProps) {
  const { register, handleSubmit, formState: { errors } } = useForm<StudentFormValues>({
    resolver: zodResolver(studentFormSchema),
    defaultValues: { initialEnrollmentDate: new Date().toISOString().slice(0, 10), nationality: "Moroccan", sex: "MALE" },
  });

  return <form className="management-form management-form--two-columns" noValidate onSubmit={handleSubmit(onSubmit)}>
    <div className="form-field"><label htmlFor="student-first-name">First name</label><input autoFocus id="student-first-name" {...register("firstName")} />{errors.firstName && <p className="field-error">{errors.firstName.message}</p>}</div>
    <div className="form-field"><label htmlFor="student-last-name">Last name</label><input id="student-last-name" {...register("lastName")} />{errors.lastName && <p className="field-error">{errors.lastName.message}</p>}</div>
    <div className="form-field"><label htmlFor="student-apogee">Apogee code</label><input id="student-apogee" {...register("apogeeCode")} />{errors.apogeeCode && <p className="field-error">{errors.apogeeCode.message}</p>}</div>
    <div className="form-field"><label htmlFor="student-national-code">National student code</label><input id="student-national-code" {...register("nationalStudentCode")} /></div>
    <div className="form-field form-field--wide"><label htmlFor="student-email">University email</label><input autoComplete="off" id="student-email" type="email" {...register("universityEmail")} />{errors.universityEmail && <p className="field-error">{errors.universityEmail.message}</p>}</div>
    <div className="form-field form-field--wide"><label htmlFor="student-password">Initial password</label><input autoComplete="new-password" id="student-password" type="password" {...register("password")} />{errors.password && <p className="field-error">{errors.password.message}</p>}</div>
    <div className="form-field"><label htmlFor="student-birth-date">Birth date</label><input id="student-birth-date" max={new Date().toISOString().slice(0, 10)} type="date" {...register("birthDate")} />{errors.birthDate && <p className="field-error">{errors.birthDate.message}</p>}</div>
    <div className="form-field"><label htmlFor="student-birth-place">Place of birth</label><input id="student-birth-place" {...register("placeOfBirth")} />{errors.placeOfBirth && <p className="field-error">{errors.placeOfBirth.message}</p>}</div>
    <div className="form-field"><label htmlFor="student-nationality">Nationality</label><input id="student-nationality" {...register("nationality")} />{errors.nationality && <p className="field-error">{errors.nationality.message}</p>}</div>
    <div className="form-field"><label htmlFor="student-sex">Sex</label><select id="student-sex" {...register("sex")}><option value="MALE">Male</option><option value="FEMALE">Female</option></select></div>
    <div className="form-field"><label htmlFor="student-cin">CIN</label><input id="student-cin" {...register("cin")} /></div>
    <div className="form-field"><label htmlFor="student-phone">Phone number</label><input id="student-phone" type="tel" {...register("phoneNumber")} /></div>
    <div className="form-field form-field--wide"><label htmlFor="student-enrollment-date">Initial enrollment date</label><input id="student-enrollment-date" max={new Date().toISOString().slice(0, 10)} type="date" {...register("initialEnrollmentDate")} />{errors.initialEnrollmentDate && <p className="field-error">{errors.initialEnrollmentDate.message}</p>}</div>
    {requestError && <div className="management-alert management-alert--error">{requestError}</div>}
    <footer className="form-actions"><button className="secondary-button" onClick={onUseExistingStudent} type="button">Register existing Student</button><button className="management-primary-button" disabled={isSubmitting} type="submit">{isSubmitting ? "Creating and registering..." : "Create and register Student"}</button></footer>
  </form>;
}
