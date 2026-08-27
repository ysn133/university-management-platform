import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { academicStructureKeys, getAcademicLevels, getAcademicYears, getProgramFiliere } from "@/features/academic-structure/api/academic-structure-api";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import {
  changeStudentStatus,
  getStudent,
  getStudentAcademicRegistrations,
  resetStudentPassword,
  studentRegistrationKeys,
  updateStudent,
  type StudentLifecycleAction,
} from "../api/student-registration-api";
import { StudentProfileForm, type StudentProfileFormValues } from "../components/StudentProfileForm";
import { useUrlSelection } from "@/shared/hooks/useUrlSelection";

const studentSections = ["overview", "academic-history"] as const;

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function actionLabel(action: StudentLifecycleAction): string {
  return action[0].toUpperCase() + action.slice(1);
}

export function StudentDetailsPage() {
  const { studentId } = useParams();
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [activeSection, setActiveSection] = useUrlSelection("tab", studentSections, "overview");
  const [isEditing, setEditing] = useState(false);
  const [isResettingPassword, setResettingPassword] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<StudentLifecycleAction | null>(null);

  const studentQuery = useQuery({ queryKey: studentRegistrationKeys.student(studentId ?? "missing"), queryFn: () => getStudent(studentId!), enabled: Boolean(studentId) });
  const historyQuery = useQuery({ queryKey: studentRegistrationKeys.studentRegistrations(studentId ?? "missing"), queryFn: () => getStudentAcademicRegistrations(studentId!), enabled: Boolean(studentId) });
  const yearsQuery = useQuery({ queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"), queryFn: () => getAcademicYears(establishmentId!), enabled: Boolean(establishmentId) });
  const programIds = [...new Set((historyQuery.data ?? []).map((registration) => registration.programFiliereId))];
  const programQueries = useQueries({ queries: programIds.map((programId) => ({ queryKey: academicStructureKeys.programFiliere(programId), queryFn: () => getProgramFiliere(programId) })) });
  const levelQueries = useQueries({ queries: programIds.map((programId) => ({ queryKey: academicStructureKeys.academicLevels(programId), queryFn: () => getAcademicLevels(programId) })) });
  const programsById = new Map(programQueries.flatMap((query) => query.data ? [[query.data.id, query.data] as const] : []));
  const levelsById = new Map(levelQueries.flatMap((query) => (query.data ?? []).map((level) => [level.id, level] as const)));
  const yearsById = new Map((yearsQuery.data ?? []).map((year) => [year.id, year]));

  async function refreshStudent() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: studentRegistrationKeys.student(studentId!) }),
      queryClient.invalidateQueries({ queryKey: ["student-registration", "students", establishmentId] }),
    ]);
  }

  const updateMutation = useMutation({
    mutationFn: (values: StudentProfileFormValues) => updateStudent(studentId!, {
      apogeeCode: values.apogeeCode,
      nationalStudentCode: values.nationalStudentCode || undefined,
      cin: values.cin || undefined,
      initialEnrollmentDate: values.initialEnrollmentDate,
      universityEmail: values.universityEmail,
      firstName: values.firstName,
      lastName: values.lastName,
      birth_date: values.birthDate,
      placeOfBirth: values.placeOfBirth,
      nationality: values.nationality,
      sex: values.sex,
      phone_number: values.phoneNumber || undefined,
    }),
    onSuccess: async () => { await refreshStudent(); setEditing(false); },
  });
  const resetMutation = useMutation({ mutationFn: (password: string) => resetStudentPassword(studentId!, password), onSuccess: () => { setResettingPassword(false); setNewPassword(""); setPasswordError(null); } });
  const lifecycleMutation = useMutation({ mutationFn: (action: StudentLifecycleAction) => changeStudentStatus(studentId!, action), onSuccess: async () => { await refreshStudent(); setConfirmation(null); } });

  if (!studentId || !establishmentId || !workspacePath) return <div className="management-state management-state--error"><h1>Student context unavailable</h1></div>;
  if (studentQuery.isPending) return <div className="management-state">Loading Student account...</div>;
  if (studentQuery.isError) return <div className="management-state management-state--error"><h1>Student unavailable</h1><p>{errorMessage(studentQuery.error)}</p></div>;

  const student = studentQuery.data;
  const history = [...(historyQuery.data ?? [])].sort((left, right) => (yearsById.get(right.academicYearId)?.startYear ?? 0) - (yearsById.get(left.academicYearId)?.startYear ?? 0));

  function submitPasswordReset() {
    if (newPassword.length < 8) { setPasswordError("The new password must contain at least 8 characters."); return; }
    resetMutation.mutate(newPassword);
  }

  return <div className="management-page admin-details-page student-details-page">
    <div className="admin-page-toolbar"><Link className="record-back-link" to={`${workspacePath}/students`}>Back to Students</Link><span>Apogee · {student.apogeeCode}</span></div>
    <section className="admin-profile-workspace">
      <header className="admin-profile-hero">
        <span className="admin-profile-avatar">{student.firstName[0]}{student.lastName[0]}</span>
        <div className="admin-profile-heading"><p className="management-kicker">Student record</p><h1>{student.firstName} {student.lastName}</h1><p>{student.universityEmail}</p></div>
        <div className="admin-profile-state"><StatusBadge status={student.accountStatus} /><button className="management-primary-button" disabled={student.accountStatus === "ARCHIVED"} onClick={() => { updateMutation.reset(); setEditing(true); }} type="button">Edit profile</button></div>
      </header>
      <nav aria-label="Student record sections" className="admin-profile-tabs">
        <button aria-current={activeSection === "overview" ? "page" : undefined} onClick={() => setActiveSection("overview")} type="button">Overview</button>
        <button aria-current={activeSection === "academic-history" ? "page" : undefined} onClick={() => setActiveSection("academic-history")} type="button">Academic history <span>{history.length}</span></button>
      </nav>
      <div className="admin-profile-content">
        {activeSection === "overview" && <section className="admin-section-panel"><header><p className="management-kicker">Identity</p><h2>Student information</h2><p>Personal, institutional, and enrollment information attached to this account.</p></header>
          <dl className="admin-info-grid">
            <div><dt>University email</dt><dd>{student.universityEmail}</dd></div><div><dt>Phone number</dt><dd>{student.phoneNumber || "Not provided"}</dd></div>
            <div><dt>Apogee code</dt><dd>{student.apogeeCode}</dd></div><div><dt>National student code</dt><dd>{student.nationalStudentCode || "Not provided"}</dd></div>
            <div><dt>CIN</dt><dd>{student.cin || "Not provided"}</dd></div><div><dt>Initial enrollment</dt><dd>{student.initialEnrollmentDate}</dd></div>
            <div><dt>Birth date</dt><dd>{student.birthDate}</dd></div><div><dt>Place of birth</dt><dd>{student.placeOfBirth}</dd></div>
            <div><dt>Nationality</dt><dd>{student.nationality}</dd></div><div><dt>Sex</dt><dd>{student.sex === "MALE" ? "Male" : "Female"}</dd></div>
          </dl>
          <footer className="admin-overview-actions">
            <button className="secondary-button" disabled={student.accountStatus === "ARCHIVED"} onClick={() => { resetMutation.reset(); setNewPassword(""); setPasswordError(null); setResettingPassword(true); }} type="button">Reset password</button>
            {student.accountStatus === "ACTIVE" && <button className="secondary-button" onClick={() => setConfirmation("lock")} type="button">Lock account</button>}
            {student.accountStatus === "LOCKED" && <button className="secondary-button" onClick={() => setConfirmation("unlock")} type="button">Unlock account</button>}
            {(student.accountStatus === "ACTIVE" || student.accountStatus === "LOCKED") && <button className="danger-button" onClick={() => setConfirmation("deactivate")} type="button">Deactivate</button>}
            {student.accountStatus !== "ARCHIVED" && <button className="danger-button" onClick={() => setConfirmation("archive")} type="button">Archive</button>}
          </footer>
        </section>}
        {activeSection === "academic-history" && <section className="admin-section-panel"><header><p className="management-kicker">Academic record</p><h2>Annual registrations</h2><p>Historical program and level placement remains visible across academic years.</p></header>
          {historyQuery.isPending ? <div className="panel-empty">Loading academic history...</div> : historyQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(historyQuery.error)}</div> : history.length === 0 ? <div className="panel-empty"><strong>No academic registration found.</strong></div> : <div className="resource-table-wrapper"><table className="resource-table"><thead><tr><th>Academic year</th><th>Program / Filière</th><th>Level</th><th>Status</th><th aria-label="Open record" /></tr></thead><tbody>{history.map((registration) => { const recordPath = `${workspacePath}/students/${studentId}/academic-record/${registration.id}`; return <tr className="resource-row--linked" key={registration.id}><td><Link className="student-history-cell-link student-history-year-link" to={recordPath}>{yearsById.get(registration.academicYearId)?.label ?? "Academic year"}</Link></td><td><Link className="student-history-cell-link table-contact" to={recordPath}><span>{programsById.get(registration.programFiliereId)?.name ?? "Program"}</span><small>{programsById.get(registration.programFiliereId)?.code}</small></Link></td><td><Link className="student-history-cell-link" to={recordPath}>{levelsById.get(registration.academicLevelId)?.name ?? "Level"}</Link></td><td><Link className="student-history-cell-link" to={recordPath}><StatusBadge status={registration.status} /></Link></td><td><Link className="record-open-link" to={recordPath}>View record</Link></td></tr>; })}</tbody></table></div>}
        </section>}
      </div>
    </section>

    {isEditing && <ManagementModal size="wide" title="Edit Student" description="Correct identity, institutional, and contact information." onClose={() => setEditing(false)}><StudentProfileForm student={student} isSubmitting={updateMutation.isPending} requestError={updateMutation.isError ? errorMessage(updateMutation.error) : null} onCancel={() => setEditing(false)} onSubmit={async (values) => { try { await updateMutation.mutateAsync(values); } catch { /* mutation state renders the error */ } }} /></ManagementModal>}
    {isResettingPassword && <ManagementModal title="Reset password" description={`Set a temporary password for ${student.firstName} ${student.lastName}.`} onClose={() => setResettingPassword(false)}><div className="management-form"><div className="form-field form-field--wide"><label htmlFor="reset-student-password">New temporary password</label><input id="reset-student-password" onChange={(event) => { setNewPassword(event.target.value); setPasswordError(null); }} type="password" value={newPassword} />{passwordError && <p className="field-error">{passwordError}</p>}</div>{resetMutation.isError && <div className="management-alert management-alert--error">{errorMessage(resetMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={() => setResettingPassword(false)} type="button">Cancel</button><button className="management-primary-button" disabled={resetMutation.isPending} onClick={submitPasswordReset} type="button">{resetMutation.isPending ? "Resetting..." : "Reset password"}</button></footer></div></ManagementModal>}
    {confirmation && <ConfirmActionModal actionLabel={actionLabel(confirmation)} destructive={confirmation === "deactivate" || confirmation === "archive"} description={`${actionLabel(confirmation)} the account for ${student.firstName} ${student.lastName}? Academic history will remain available.`} error={lifecycleMutation.isError ? errorMessage(lifecycleMutation.error) : null} isSubmitting={lifecycleMutation.isPending} onCancel={() => setConfirmation(null)} onConfirm={() => lifecycleMutation.mutate(confirmation)} title={`${actionLabel(confirmation)} Student`} />}
  </div>;
}
