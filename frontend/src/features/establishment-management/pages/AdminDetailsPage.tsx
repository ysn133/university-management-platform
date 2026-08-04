import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import {
  changeAdminStatus,
  establishmentAdminKeys,
  getAdmin,
  getAdminGrants,
  getPermissionCatalog,
  replaceAdminGrants,
  resetAdminPassword,
  updateAdmin,
  type AdminLifecycleAction,
  type PermissionCode,
} from "../api/establishment-admin-api";
import { AdminForm, type AdminFormValues } from "../components/AdminForm";
import { PermissionGrantForm } from "../components/PermissionGrantForm";
import { useEstablishmentScope } from "../context/useEstablishmentScope";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function actionLabel(action: AdminLifecycleAction): string {
  return action[0].toUpperCase() + action.slice(1);
}

export function AdminDetailsPage() {
  const { adminId } = useParams();
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [isEditing, setEditing] = useState(false);
  const [isEditingPermissions, setEditingPermissions] = useState(false);
  const [isResettingPassword, setResettingPassword] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<AdminLifecycleAction | null>(null);
  const [activeSection, setActiveSection] = useState<"overview" | "permissions">("overview");

  const adminQuery = useQuery({
    queryKey: establishmentAdminKeys.admin(adminId ?? "missing"),
    queryFn: () => getAdmin(adminId!),
    enabled: Boolean(adminId),
  });
  const permissionCatalogQuery = useQuery({
    queryKey: establishmentAdminKeys.permissions,
    queryFn: getPermissionCatalog,
    enabled: Boolean(adminId),
  });
  const grantsQuery = useQuery({
    queryKey: establishmentAdminKeys.grants(adminId ?? "missing"),
    queryFn: () => getAdminGrants(adminId!),
    enabled: Boolean(adminId),
  });

  async function refreshAdmin() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: establishmentAdminKeys.admin(adminId!) }),
      queryClient.invalidateQueries({ queryKey: ["establishment-management", "admins", establishmentId] }),
    ]);
  }

  const updateMutation = useMutation({
    mutationFn: (values: AdminFormValues) => updateAdmin(adminId!, {
      universityEmail: values.universityEmail,
      firstName: values.firstName,
      lastName: values.lastName,
      birth_date: values.birthDate,
      cin: values.cin || undefined,
      sex: values.sex,
      phone_number: values.phoneNumber || undefined,
    }),
    onSuccess: async () => {
      await refreshAdmin();
      setEditing(false);
    },
  });
  const grantsMutation = useMutation({
    mutationFn: (permissions: PermissionCode[]) => replaceAdminGrants(adminId!, permissions),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: establishmentAdminKeys.grants(adminId!) });
      setEditingPermissions(false);
    },
  });
  const resetMutation = useMutation({
    mutationFn: (password: string) => resetAdminPassword(adminId!, password),
    onSuccess: () => {
      setResettingPassword(false);
      setNewPassword("");
      setPasswordError(null);
    },
  });
  const lifecycleMutation = useMutation({
    mutationFn: (action: AdminLifecycleAction) => changeAdminStatus(adminId!, action),
    onSuccess: async () => {
      await refreshAdmin();
      setConfirmation(null);
    },
  });

  if (!adminId || !establishmentId || !workspacePath) {
    return <div className="management-state management-state--error"><h1>Admin context unavailable</h1></div>;
  }
  if (adminQuery.isPending) return <div className="management-state">Loading Admin account...</div>;
  if (adminQuery.isError) return <div className="management-state management-state--error"><h1>Admin unavailable</h1><p>{errorMessage(adminQuery.error)}</p></div>;

  const admin = adminQuery.data;
  const adminsPath = `${workspacePath}/admins`;
  const grantedCodes = grantsQuery.data ?? [];

  function submitPasswordReset() {
    if (newPassword.length < 8) {
      setPasswordError("The new password must contain at least 8 characters.");
      return;
    }
    resetMutation.mutate(newPassword);
  }

  return (
    <div className="management-page admin-details-page">
      <div className="admin-page-toolbar"><Link className="record-back-link" to={adminsPath}>Back to Admins</Link><span>Account ID · {admin.id.slice(0, 8)}</span></div>

      <section className="admin-profile-workspace">
        <header className="admin-profile-hero">
          <span className="admin-profile-avatar">{admin.firstName[0]}{admin.lastName[0]}</span>
          <div className="admin-profile-heading">
            <p className="management-kicker">Establishment Admin</p>
            <h1>{admin.firstName} {admin.lastName}</h1>
            <p>{admin.email}</p>
          </div>
          <div className="admin-profile-state"><StatusBadge status={admin.status} /><button className="management-primary-button" onClick={() => { updateMutation.reset(); setEditing(true); }} type="button">Edit profile</button></div>
        </header>

        <nav className="admin-profile-tabs" aria-label="Admin account sections">
          <button aria-current={activeSection === "overview" ? "page" : undefined} onClick={() => setActiveSection("overview")} type="button">Overview</button>
          <button aria-current={activeSection === "permissions" ? "page" : undefined} onClick={() => setActiveSection("permissions")} type="button">Permissions <span>{grantedCodes.length}</span></button>
        </nav>

        <div className="admin-profile-content">
          {activeSection === "overview" && (
            <section className="admin-section-panel">
              <header><p className="management-kicker">Profile</p><h2>Personal information</h2><p>Identity and contact information attached to this account.</p></header>
              <dl className="admin-info-grid">
                <div><dt>University email</dt><dd>{admin.email}</dd></div>
                <div><dt>Phone number</dt><dd>{admin.phoneNumber || "Not provided"}</dd></div>
                <div><dt>CIN</dt><dd>{admin.cin || "Not provided"}</dd></div>
                <div><dt>Birth date</dt><dd>{admin.birthDate}</dd></div>
                <div><dt>Sex</dt><dd>{admin.sex === "MALE" ? "Male" : "Female"}</dd></div>
                <div><dt>Account role</dt><dd>Establishment Admin</dd></div>
              </dl>
              <footer className="admin-overview-actions">
                <button className="secondary-button" disabled={admin.status === "ARCHIVED"} onClick={() => { resetMutation.reset(); setNewPassword(""); setResettingPassword(true); }} type="button">Reset password</button>
                {admin.status === "ACTIVE" && <button className="secondary-button" onClick={() => setConfirmation("lock")} type="button">Lock account</button>}
                {admin.status === "LOCKED" && <button className="secondary-button" onClick={() => setConfirmation("unlock")} type="button">Unlock account</button>}
                {(admin.status === "ACTIVE" || admin.status === "LOCKED") && <button className="danger-button" onClick={() => setConfirmation("deactivate")} type="button">Deactivate</button>}
                {admin.status === "DEACTIVATED" && <button className="management-primary-button" onClick={() => setConfirmation("activate")} type="button">Activate</button>}
                {admin.status !== "ARCHIVED" && <button className="danger-button" onClick={() => setConfirmation("archive")} type="button">Archive</button>}
                {admin.status === "ARCHIVED" && <button className="management-primary-button" onClick={() => setConfirmation("restore")} type="button">Restore</button>}
              </footer>
            </section>
          )}

          {activeSection === "permissions" && (
            <section className="admin-section-panel admin-permission-overview">
              <div className="admin-permission-copy">
                <p className="management-kicker">Delegated access</p>
                <h2>Permission profile</h2>
                <p>This account uses an explicit permission set for establishment operations. Changes apply the next time an authorized action is requested.</p>
                <div className="admin-access-note"><span aria-hidden="true">i</span><p>Super Admins retain full establishment authority. These grants apply only to this Admin account.</p></div>
              </div>
              <aside className="admin-permission-summary">
                <span className="admin-permission-state">{grantedCodes.length === 0 ? "No delegated access" : "Custom access"}</span>
                <strong>{permissionCatalogQuery.isPending || grantsQuery.isPending ? "—" : grantedCodes.length}</strong>
                <p>permissions assigned</p>
                {permissionCatalogQuery.isError || grantsQuery.isError
                  ? <p className="control-error">Permission grants are currently unavailable.</p>
                  : <button className="management-primary-button" disabled={permissionCatalogQuery.isPending || grantsQuery.isPending} onClick={() => { grantsMutation.reset(); setEditingPermissions(true); }} type="button">Edit permissions</button>}
              </aside>
            </section>
          )}

        </div>
      </section>

      {isEditing && <ManagementModal title="Edit Admin" description="Correct account identity and contact information." onClose={() => setEditing(false)}><AdminForm admin={admin} isSubmitting={updateMutation.isPending} requestError={updateMutation.isError ? errorMessage(updateMutation.error) : null} onCancel={() => setEditing(false)} onSubmit={async (values) => { try { await updateMutation.mutateAsync(values); } catch { /* mutation state renders the error */ } }} /></ManagementModal>}
      {isEditingPermissions && <ManagementModal size="wide" title="Admin permissions" description={`Control what ${admin.firstName} ${admin.lastName} can manage.`} onClose={() => setEditingPermissions(false)}><PermissionGrantForm catalog={permissionCatalogQuery.data ?? []} grantedPermissions={grantedCodes} isSubmitting={grantsMutation.isPending} requestError={grantsMutation.isError ? errorMessage(grantsMutation.error) : null} onCancel={() => setEditingPermissions(false)} onSubmit={async (permissions) => { try { await grantsMutation.mutateAsync(permissions); } catch { /* mutation state renders the error */ } }} /></ManagementModal>}
      {isResettingPassword && <ManagementModal title="Reset password" description={`Set a temporary password for ${admin.firstName} ${admin.lastName}.`} onClose={() => setResettingPassword(false)}><div className="management-form"><div className="form-field form-field--wide"><label htmlFor="reset-admin-password">New temporary password</label><input id="reset-admin-password" onChange={(event) => { setNewPassword(event.target.value); setPasswordError(null); }} type="password" value={newPassword} />{passwordError && <p className="field-error">{passwordError}</p>}</div>{resetMutation.isError && <div className="management-alert management-alert--error">{errorMessage(resetMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={() => setResettingPassword(false)} type="button">Cancel</button><button className="management-primary-button" disabled={resetMutation.isPending} onClick={submitPasswordReset} type="button">{resetMutation.isPending ? "Resetting..." : "Reset password"}</button></footer></div></ManagementModal>}
      {confirmation && <ConfirmActionModal actionLabel={actionLabel(confirmation)} destructive={confirmation === "deactivate" || confirmation === "archive"} description={`${actionLabel(confirmation)} the account for ${admin.firstName} ${admin.lastName}?`} error={lifecycleMutation.isError ? errorMessage(lifecycleMutation.error) : null} isSubmitting={lifecycleMutation.isPending} title={`${actionLabel(confirmation)} Admin`} onCancel={() => setConfirmation(null)} onConfirm={() => lifecycleMutation.mutate(confirmation)} />}
    </div>
  );
}
