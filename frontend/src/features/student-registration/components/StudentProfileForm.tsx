import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import type { Student } from "../api/student-registration-api";

const studentProfileSchema = z.object({
  apogeeCode: z.string().trim().min(1, "Enter the Apogee code.").max(50),
  nationalStudentCode: z.string().trim().max(50).optional(),
  cin: z.string().trim().max(50).optional(),
  initialEnrollmentDate: z.string().min(1, "Select the initial enrollment date."),
  universityEmail: z.string().trim().email("Enter a valid university email."),
  firstName: z.string().trim().min(1, "Enter the first name.").max(255),
  lastName: z.string().trim().min(1, "Enter the last name.").max(255),
  birthDate: z.string().min(1, "Select the birth date."),
  placeOfBirth: z.string().trim().min(1, "Enter the place of birth.").max(255),
  nationality: z.string().trim().min(1, "Enter the nationality.").max(100),
  sex: z.enum(["MALE", "FEMALE"]),
  phoneNumber: z.string().trim().max(50).optional(),
});

export type StudentProfileFormValues = z.infer<typeof studentProfileSchema>;

interface StudentProfileFormProps {
  student: Student;
  isSubmitting: boolean;
  requestError?: string | null;
  onCancel: () => void;
  onSubmit: (values: StudentProfileFormValues) => Promise<void>;
}

export function StudentProfileForm({ student, isSubmitting, requestError, onCancel, onSubmit }: StudentProfileFormProps) {
  const { register, handleSubmit, formState: { errors } } = useForm<StudentProfileFormValues>({
    resolver: zodResolver(studentProfileSchema),
    defaultValues: {
      apogeeCode: student.apogeeCode,
      nationalStudentCode: student.nationalStudentCode ?? "",
      cin: student.cin ?? "",
      initialEnrollmentDate: student.initialEnrollmentDate,
      universityEmail: student.universityEmail,
      firstName: student.firstName,
      lastName: student.lastName,
      birthDate: student.birthDate,
      placeOfBirth: student.placeOfBirth,
      nationality: student.nationality,
      sex: student.sex,
      phoneNumber: student.phoneNumber ?? "",
    },
  });

  return <form className="management-form management-form--two-columns" noValidate onSubmit={handleSubmit(onSubmit)}>
    <div className="form-field"><label htmlFor="edit-student-first-name">First name</label><input autoFocus id="edit-student-first-name" {...register("firstName")} />{errors.firstName && <p className="field-error">{errors.firstName.message}</p>}</div>
    <div className="form-field"><label htmlFor="edit-student-last-name">Last name</label><input id="edit-student-last-name" {...register("lastName")} />{errors.lastName && <p className="field-error">{errors.lastName.message}</p>}</div>
    <div className="form-field"><label htmlFor="edit-student-apogee">Apogee code</label><input id="edit-student-apogee" {...register("apogeeCode")} />{errors.apogeeCode && <p className="field-error">{errors.apogeeCode.message}</p>}</div>
    <div className="form-field"><label htmlFor="edit-student-national-code">National student code</label><input id="edit-student-national-code" {...register("nationalStudentCode")} /></div>
    <div className="form-field form-field--wide"><label htmlFor="edit-student-email">University email</label><input id="edit-student-email" type="email" {...register("universityEmail")} />{errors.universityEmail && <p className="field-error">{errors.universityEmail.message}</p>}</div>
    <div className="form-field"><label htmlFor="edit-student-birth-date">Birth date</label><input id="edit-student-birth-date" max={new Date().toISOString().slice(0, 10)} type="date" {...register("birthDate")} />{errors.birthDate && <p className="field-error">{errors.birthDate.message}</p>}</div>
    <div className="form-field"><label htmlFor="edit-student-birth-place">Place of birth</label><input id="edit-student-birth-place" {...register("placeOfBirth")} />{errors.placeOfBirth && <p className="field-error">{errors.placeOfBirth.message}</p>}</div>
    <div className="form-field"><label htmlFor="edit-student-nationality">Nationality</label><input id="edit-student-nationality" {...register("nationality")} />{errors.nationality && <p className="field-error">{errors.nationality.message}</p>}</div>
    <div className="form-field"><label htmlFor="edit-student-sex">Sex</label><select id="edit-student-sex" {...register("sex")}><option value="MALE">Male</option><option value="FEMALE">Female</option></select></div>
    <div className="form-field"><label htmlFor="edit-student-cin">CIN</label><input id="edit-student-cin" {...register("cin")} /></div>
    <div className="form-field"><label htmlFor="edit-student-phone">Phone number</label><input id="edit-student-phone" type="tel" {...register("phoneNumber")} /></div>
    <div className="form-field form-field--wide"><label htmlFor="edit-student-enrollment-date">Initial enrollment date</label><input id="edit-student-enrollment-date" max={new Date().toISOString().slice(0, 10)} type="date" {...register("initialEnrollmentDate")} />{errors.initialEnrollmentDate && <p className="field-error">{errors.initialEnrollmentDate.message}</p>}</div>
    {requestError && <div className="management-alert management-alert--error">{requestError}</div>}
    <footer className="form-actions"><button className="secondary-button" onClick={onCancel} type="button">Cancel</button><button className="management-primary-button" disabled={isSubmitting} type="submit">{isSubmitting ? "Saving..." : "Save changes"}</button></footer>
  </form>;
}
