import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useLocation, useParams } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import {
  changeProfessorStatus,
  getProfessor,
  professorManagementKeys,
  resetProfessorPassword,
  updateProfessor,
  type ProfessorLifecycleAction,
} from "../api/professor-management-api";
import { ProfessorForm, type ProfessorFormValues } from "../components/ProfessorForm";
import { ProfessorAcademicWorkspace } from "../components/ProfessorAcademicWorkspace";
import { useUrlSelection } from "@/shared/hooks/useUrlSelection";

const professorSections = ["profile", "expertise", "teaching", "schedule"] as const;

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function displayDate(value?: string | null): string {
  if (!value) return "Not provided";
  return new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "long", year: "numeric" }).format(new Date(`${value}T00:00:00`));
}

function actionLabel(action: ProfessorLifecycleAction): string {
  return action[0].toUpperCase() + action.slice(1);
}

export function ProfessorDetailsPage() {
  const { professorId } = useParams();
  const location = useLocation();
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [isEditing, setEditing] = useState(false);
  const [isResettingPassword, setResettingPassword] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<ProfessorLifecycleAction | null>(null);
  const [activeTab, setActiveTab] = useUrlSelection("tab", professorSections, "profile");

  const professorQuery = useQuery({
    queryKey: professorManagementKeys.professor(professorId ?? "missing"),
    queryFn: () => getProfessor(professorId!),
    enabled: Boolean(professorId),
  });

  async function refreshProfessor() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: professorManagementKeys.professor(professorId!) }),
      queryClient.invalidateQueries({ queryKey: ["professor-management", "professors", establishmentId] }),
    ]);
  }

  const updateMutation = useMutation({
    mutationFn: (values: ProfessorFormValues) => updateProfessor(professorId!, {
      employeeNumber: values.employeeNumber.trim(),
      academicRankId: values.academicRankId,
      hireDate: values.hireDate || undefined,
      maximumWeeklyTeachingMinutes: Number(values.maximumWeeklyTeachingMinutes),
      cin: values.cin.trim() || undefined,
      universityEmail: values.universityEmail.trim(),
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      birth_date: values.birthDate,
      placeOfBirth: values.placeOfBirth.trim(),
      nationality: values.nationality.trim(),
      sex: values.sex,
      phone_number: values.phoneNumber.trim() || undefined,
    }),
    onSuccess: async () => { await refreshProfessor(); setEditing(false); },
  });
  const resetMutation = useMutation({
    mutationFn: (password: string) => resetProfessorPassword(professorId!, password),
    onSuccess: () => { setResettingPassword(false); setNewPassword(""); setPasswordError(null); },
  });
  const lifecycleMutation = useMutation({
    mutationFn: (action: ProfessorLifecycleAction) => changeProfessorStatus(professorId!, action),
    onSuccess: async () => { await refreshProfessor(); setConfirmation(null); },
  });

  if (!professorId || !establishmentId || !workspacePath) return <div className="management-state management-state--error"><h1>Professor context unavailable</h1></div>;
  if (professorQuery.isPending) return <div className="management-state">Loading Professor account...</div>;
  if (professorQuery.isError) return <div className="management-state management-state--error"><h1>Professor unavailable</h1><p>{errorMessage(professorQuery.error)}</p></div>;

  const professor = professorQuery.data;
  const navigationState = location.state as { returnLabel?: string; returnTo?: string } | null;
  const returnTo = navigationState?.returnTo ?? `${workspacePath}/professors`;
  const returnLabel = navigationState?.returnLabel ?? "Back to Professors";

  function submitPasswordReset() {
    if (newPassword.length < 8) {
      setPasswordError("The new password must contain at least 8 characters.");
      return;
    }
    resetMutation.mutate(newPassword);
  }

  return <div className="management-page admin-details-page professor-details-page">
    <div className="admin-page-toolbar"><Link className="record-back-link" to={returnTo}>{returnLabel}</Link><span>Employee · {professor.employeeNumber}</span></div>
    <section className="admin-profile-workspace">
      <header className="admin-profile-hero">
        <span className="admin-profile-avatar">{professor.firstName[0]}{professor.lastName[0]}</span>
        <div className="admin-profile-heading"><p className="management-kicker">Professor record</p><h1>{professor.firstName} {professor.lastName}</h1><p>{professor.universityEmail}</p></div>
        <div className="admin-profile-state"><StatusBadge status={professor.accountStatus} /><button className="management-primary-button" disabled={professor.accountStatus === "ARCHIVED"} onClick={() => { updateMutation.reset(); setEditing(true); }} type="button">Edit profile</button></div>
      </header>
      <nav aria-label="Professor record sections" className="professor-record-tabs"><button aria-selected={activeTab === "profile"} onClick={() => setActiveTab("profile")} type="button">General Information</button><button aria-selected={activeTab === "expertise"} onClick={() => setActiveTab("expertise")} type="button">Academic Expertise</button><button aria-selected={activeTab === "teaching"} onClick={() => setActiveTab("teaching")} type="button">Teaching</button><button aria-selected={activeTab === "schedule"} onClick={() => setActiveTab("schedule")} type="button">Schedule</button></nav>
      {activeTab === "profile" ? <div className="admin-profile-content professor-profile-content">
        <section className="admin-section-panel"><header><p className="management-kicker">Profile</p><h2>Professional and personal information</h2><p>Employment, identity, and contact information attached to this account.</p></header>
          <dl className="admin-info-grid">
            <div><dt>Employee number</dt><dd>{professor.employeeNumber}</dd></div><div><dt>Academic rank</dt><dd>{professor.academicRank || "Not provided"}</dd></div>
            <div><dt>Hire date</dt><dd>{displayDate(professor.hireDate)}</dd></div><div><dt>Teaching capacity</dt><dd>{professor.maximumWeeklyTeachingMinutes} minutes / week</dd></div>
            <div><dt>University email</dt><dd>{professor.universityEmail}</dd></div><div><dt>Phone number</dt><dd>{professor.phoneNumber || "Not provided"}</dd></div>
            <div><dt>CIN</dt><dd>{professor.cin || "Not provided"}</dd></div><div><dt>Birth date</dt><dd>{displayDate(professor.birthDate)}</dd></div>
            <div><dt>Place of birth</dt><dd>{professor.placeOfBirth}</dd></div><div><dt>Nationality</dt><dd>{professor.nationality}</dd></div>
            <div><dt>Sex</dt><dd>{professor.sex === "MALE" ? "Male" : "Female"}</dd></div><div><dt>Account role</dt><dd>Professor</dd></div>
          </dl>
          <footer className="admin-overview-actions">
            <button className="secondary-button" disabled={professor.accountStatus === "ARCHIVED"} onClick={() => { resetMutation.reset(); setNewPassword(""); setPasswordError(null); setResettingPassword(true); }} type="button">Reset password</button>
            {professor.accountStatus === "ACTIVE" && <button className="secondary-button" onClick={() => setConfirmation("lock")} type="button">Lock account</button>}
            {professor.accountStatus === "LOCKED" && <button className="secondary-button" onClick={() => setConfirmation("unlock")} type="button">Unlock account</button>}
            {(professor.accountStatus === "ACTIVE" || professor.accountStatus === "LOCKED") && <button className="danger-button" onClick={() => setConfirmation("deactivate")} type="button">Deactivate</button>}
            {professor.accountStatus !== "ARCHIVED" && <button className="danger-button" onClick={() => setConfirmation("archive")} type="button">Archive</button>}
          </footer>
        </section>
      </div> : <ProfessorAcademicWorkspace establishmentId={establishmentId} professorId={professorId} section={activeTab} />}
    </section>
    {isEditing && <ManagementModal size="wide" title="Edit Professor" description="Update professional, identity, and contact information." onClose={() => setEditing(false)}><ProfessorForm establishmentId={establishmentId} professor={professor} error={updateMutation.isError ? errorMessage(updateMutation.error) : null} isSubmitting={updateMutation.isPending} onCancel={() => setEditing(false)} onSubmit={(values) => updateMutation.mutate(values)} /></ManagementModal>}
    {isResettingPassword && <ManagementModal title="Reset password" description={`Set a temporary password for ${professor.firstName} ${professor.lastName}.`} onClose={() => setResettingPassword(false)}><div className="management-form"><div className="form-field form-field--wide"><label htmlFor="reset-professor-password">New temporary password</label><input id="reset-professor-password" onChange={(event) => { setNewPassword(event.target.value); setPasswordError(null); }} type="password" value={newPassword} />{passwordError && <p className="field-error">{passwordError}</p>}</div>{resetMutation.isError && <div className="management-alert management-alert--error">{errorMessage(resetMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={() => setResettingPassword(false)} type="button">Cancel</button><button className="management-primary-button" disabled={resetMutation.isPending} onClick={submitPasswordReset} type="button">{resetMutation.isPending ? "Resetting..." : "Reset password"}</button></footer></div></ManagementModal>}
    {confirmation && <ConfirmActionModal actionLabel={actionLabel(confirmation)} destructive={confirmation === "deactivate" || confirmation === "archive"} description={`${actionLabel(confirmation)} the account for ${professor.firstName} ${professor.lastName}?`} error={lifecycleMutation.isError ? errorMessage(lifecycleMutation.error) : null} isSubmitting={lifecycleMutation.isPending} onCancel={() => setConfirmation(null)} onConfirm={() => lifecycleMutation.mutate(confirmation)} title={`${actionLabel(confirmation)} Professor`} />}
  </div>;
}
