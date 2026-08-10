import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { academicStructureKeys, getAcademicRanks } from "@/features/academic-structure/api/academic-structure-api";
import type { Professor } from "../api/professor-management-api";

export interface ProfessorFormValues {
  employeeNumber: string;
  academicRankId: string;
  hireDate: string;
  maximumWeeklyTeachingMinutes: string;
  cin: string;
  universityEmail: string;
  password: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  placeOfBirth: string;
  nationality: string;
  sex: "MALE" | "FEMALE";
  phoneNumber: string;
}

const initialValues: ProfessorFormValues = {
  employeeNumber: "",
  academicRankId: "",
  hireDate: "",
  maximumWeeklyTeachingMinutes: "720",
  cin: "",
  universityEmail: "",
  password: "",
  firstName: "",
  lastName: "",
  birthDate: "",
  placeOfBirth: "",
  nationality: "Moroccan",
  sex: "MALE",
  phoneNumber: "",
};

interface ProfessorFormProps {
  establishmentId: string;
  professor?: Professor;
  error?: string | null;
  isSubmitting: boolean;
  onCancel: () => void;
  onSubmit: (values: ProfessorFormValues) => void;
}

export function ProfessorForm({ establishmentId, professor, error, isSubmitting, onCancel, onSubmit }: ProfessorFormProps) {
  const ranksQuery = useQuery({
    queryKey: academicStructureKeys.academicRanks(establishmentId),
    queryFn: () => getAcademicRanks(establishmentId),
  });
  const [values, setValues] = useState<ProfessorFormValues>(() => professor ? {
    employeeNumber: professor.employeeNumber,
    academicRankId: professor.academicRankId ?? "",
    hireDate: professor.hireDate ?? "",
    maximumWeeklyTeachingMinutes: String(professor.maximumWeeklyTeachingMinutes),
    cin: professor.cin ?? "",
    universityEmail: professor.universityEmail,
    password: "",
    firstName: professor.firstName,
    lastName: professor.lastName,
    birthDate: professor.birthDate,
    placeOfBirth: professor.placeOfBirth,
    nationality: professor.nationality,
    sex: professor.sex,
    phoneNumber: professor.phoneNumber ?? "",
  } : initialValues);
  const [validationError, setValidationError] = useState<string | null>(null);
  const today = new Date().toISOString().slice(0, 10);

  function update<K extends keyof ProfessorFormValues>(field: K, value: ProfessorFormValues[K]) {
    setValues((current) => ({ ...current, [field]: value }));
    setValidationError(null);
  }

  function submit() {
    const weeklyMinutes = Number(values.maximumWeeklyTeachingMinutes);
    if (!values.employeeNumber.trim() || !values.academicRankId || !values.universityEmail.trim() || (!professor && values.password.length < 8) || !values.firstName.trim() || !values.lastName.trim() || !values.birthDate || !values.placeOfBirth.trim() || !values.nationality.trim()) {
      setValidationError(professor ? "Complete all required identity and employment fields." : "Complete all required fields and use a password of at least 8 characters.");
      return;
    }
    if (!Number.isInteger(weeklyMinutes) || weeklyMinutes < 1) {
      setValidationError("Maximum weekly teaching time must be a positive number of minutes.");
      return;
    }
    if (values.hireDate && values.hireDate < values.birthDate) {
      setValidationError("Hire date cannot be before the Professor's birth date.");
      return;
    }
    onSubmit(values);
  }

  return <div className="management-form management-form--two-columns professor-form">
    <div className="form-field"><label htmlFor="professor-first-name">First name</label><input autoFocus id="professor-first-name" onChange={(event) => update("firstName", event.target.value)} value={values.firstName} /></div>
    <div className="form-field"><label htmlFor="professor-last-name">Last name</label><input id="professor-last-name" onChange={(event) => update("lastName", event.target.value)} value={values.lastName} /></div>
    <div className="form-field"><label htmlFor="professor-email">University email</label><input id="professor-email" onChange={(event) => update("universityEmail", event.target.value)} type="email" value={values.universityEmail} /></div>
    {!professor && <div className="form-field"><label htmlFor="professor-password">Temporary password</label><input id="professor-password" minLength={8} onChange={(event) => update("password", event.target.value)} type="password" value={values.password} /></div>}
    <div className="form-field"><label htmlFor="professor-employee-number">Employee number</label><input id="professor-employee-number" onChange={(event) => update("employeeNumber", event.target.value)} value={values.employeeNumber} /></div>
    <div className="form-field"><label htmlFor="professor-rank">Academic rank</label><select disabled={ranksQuery.isPending || ranksQuery.isError} id="professor-rank" onChange={(event) => update("academicRankId", event.target.value)} value={values.academicRankId}><option value="">{ranksQuery.isPending ? "Loading academic ranks..." : ranksQuery.isError ? "Academic ranks unavailable" : "Select an academic rank"}</option>{(ranksQuery.data ?? []).filter((rank) => rank.status === "ACTIVE" || rank.id === values.academicRankId).map((rank) => <option key={rank.id} value={rank.id}>{rank.name}</option>)}</select>{!ranksQuery.isPending && !ranksQuery.isError && ranksQuery.data?.length === 0 && <small>Create an academic rank in Academic Settings first.</small>}</div>
    <div className="form-field"><label htmlFor="professor-hire-date">Hire date</label><input id="professor-hire-date" max={today} min={values.birthDate || undefined} onChange={(event) => update("hireDate", event.target.value)} type="date" value={values.hireDate} /></div>
    <div className="form-field"><label htmlFor="professor-weekly-minutes">Maximum weekly teaching minutes</label><input id="professor-weekly-minutes" min="1" onChange={(event) => update("maximumWeeklyTeachingMinutes", event.target.value)} type="number" value={values.maximumWeeklyTeachingMinutes} /></div>
    <div className="form-field"><label htmlFor="professor-birth-date">Birth date</label><input id="professor-birth-date" max={today} onChange={(event) => update("birthDate", event.target.value)} type="date" value={values.birthDate} /></div>
    <div className="form-field"><label htmlFor="professor-birth-place">Place of birth</label><input id="professor-birth-place" onChange={(event) => update("placeOfBirth", event.target.value)} value={values.placeOfBirth} /></div>
    <div className="form-field"><label htmlFor="professor-nationality">Nationality</label><input id="professor-nationality" onChange={(event) => update("nationality", event.target.value)} value={values.nationality} /></div>
    <div className="form-field"><label htmlFor="professor-sex">Sex</label><select id="professor-sex" onChange={(event) => update("sex", event.target.value as ProfessorFormValues["sex"])} value={values.sex}><option value="MALE">Male</option><option value="FEMALE">Female</option></select></div>
    <div className="form-field"><label htmlFor="professor-cin">CIN</label><input id="professor-cin" onChange={(event) => update("cin", event.target.value)} value={values.cin} /></div>
    <div className="form-field"><label htmlFor="professor-phone">Phone number</label><input id="professor-phone" onChange={(event) => update("phoneNumber", event.target.value)} type="tel" value={values.phoneNumber} /></div>
    {(validationError || error || ranksQuery.isError) && <div className="management-alert management-alert--error">{validationError ?? error ?? "Academic ranks could not be loaded."}</div>}
    <footer className="form-actions"><button className="secondary-button" onClick={onCancel} type="button">Cancel</button><button className="management-primary-button" disabled={isSubmitting || ranksQuery.isPending || ranksQuery.isError || !ranksQuery.data?.length} onClick={submit} type="button">{isSubmitting ? "Saving..." : professor ? "Save changes" : "Create Professor"}</button></footer>
  </div>;
}
