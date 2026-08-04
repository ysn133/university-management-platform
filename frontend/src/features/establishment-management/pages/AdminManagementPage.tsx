import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useState } from "react";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getEstablishment, rootGovernanceKeys } from "@/features/root-governance/api/root-governance-api";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import {
  changeAdminStatus,
  createAdmin,
  establishmentAdminKeys,
  getAdminGrants,
  getAdmins,
  getPermissionCatalog,
  replaceAdminGrants,
  resetAdminPassword,
  updateAdmin,
  type AccountStatus,
  type AdminAccount,
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

export function AdminManagementPage() {
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<AccountStatus | "">("");
  const [createdFrom, setCreatedFrom] = useState("");
  const [createdTo, setCreatedTo] = useState("");
  const [isCreateOpen, setCreateOpen] = useState(false);
  const [createdAdminId, setCreatedAdminId] = useState<string | null>(null);
  const [editingAdmin, setEditingAdmin] = useState<AdminAccount | null>(null);
  const [permissionAdmin, setPermissionAdmin] = useState<AdminAccount | null>(null);
  const [resettingAdmin, setResettingAdmin] = useState<AdminAccount | null>(null);
  const [newPassword, setNewPassword] = useState("");
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<{ admin: AdminAccount; action: AdminLifecycleAction } | null>(null);
  const deferredQuery = useDeferredValue(query.trim());
  const filters = {
    ...(deferredQuery ? { query: deferredQuery } : {}),
    ...(status ? { status } : {}),
    ...(createdFrom ? { createdFrom } : {}),
    ...(createdTo ? { createdTo } : {}),
  };

  const establishmentQuery = useQuery({
    queryKey: rootGovernanceKeys.establishment(establishmentId ?? "missing"),
    queryFn: () => getEstablishment(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const adminsQuery = useQuery({
    queryKey: establishmentAdminKeys.admins(establishmentId ?? "missing", filters),
    queryFn: () => getAdmins(establishmentId!, filters),
    enabled: Boolean(establishmentId),
  });
  const permissionCatalogQuery = useQuery({
    queryKey: establishmentAdminKeys.permissions,
    queryFn: getPermissionCatalog,
    enabled: Boolean(permissionAdmin) || isCreateOpen,
  });
  const grantsQuery = useQuery({
    queryKey: establishmentAdminKeys.grants(permissionAdmin?.id ?? "missing"),
    queryFn: () => getAdminGrants(permissionAdmin!.id),
    enabled: Boolean(permissionAdmin),
  });

  async function refreshAdmins() {
    await queryClient.invalidateQueries({ queryKey: ["establishment-management", "admins", establishmentId] });
  }

  const createMutation = useMutation({
    mutationFn: (values: AdminFormValues) => createAdmin(establishmentId!, {
      universityEmail: values.universityEmail,
      password: values.password!,
      firstName: values.firstName,
      lastName: values.lastName,
      birth_date: values.birthDate,
      sex: values.sex,
      phone_number: values.phoneNumber || undefined,
    }),
    onSuccess: async (response) => {
      await refreshAdmins();
      setCreatedAdminId(response.adminId);
    },
  });
  const updateMutation = useMutation({
    mutationFn: ({ adminId, values }: { adminId: string; values: AdminFormValues }) => updateAdmin(adminId, {
      universityEmail: values.universityEmail,
      firstName: values.firstName,
      lastName: values.lastName,
      birth_date: values.birthDate,
      cin: values.cin || undefined,
      sex: values.sex,
      phone_number: values.phoneNumber || undefined,
    }),
    onSuccess: async () => { await refreshAdmins(); setEditingAdmin(null); },
  });
  const lifecycleMutation = useMutation({
    mutationFn: ({ adminId, action }: { adminId: string; action: AdminLifecycleAction }) => changeAdminStatus(adminId, action),
    onSuccess: async () => { await refreshAdmins(); setConfirmation(null); },
  });
  const resetMutation = useMutation({
    mutationFn: ({ adminId, password }: { adminId: string; password: string }) => resetAdminPassword(adminId, password),
    onSuccess: () => { setResettingAdmin(null); setNewPassword(""); setPasswordError(null); },
  });
  const grantsMutation = useMutation({
    mutationFn: ({ adminId, permissions }: { adminId: string; permissions: PermissionCode[] }) => replaceAdminGrants(adminId, permissions),
    onSuccess: async (_, variables) => {
      await queryClient.invalidateQueries({ queryKey: establishmentAdminKeys.grants(variables.adminId) });
      if (variables.adminId === createdAdminId) {
        setCreateOpen(false);
        setCreatedAdminId(null);
      } else {
        setPermissionAdmin(null);
      }
    },
  });

  function openCreateFlow() {
    createMutation.reset();
    grantsMutation.reset();
    setCreatedAdminId(null);
    setCreateOpen(true);
  }

  function closeCreateFlow() {
    setCreateOpen(false);
    setCreatedAdminId(null);
    createMutation.reset();
    grantsMutation.reset();
  }

  function submitPasswordReset() {
    if (newPassword.length < 8) {
      setPasswordError("The new password must contain at least 8 characters.");
      return;
    }
    if (resettingAdmin) resetMutation.mutate({ adminId: resettingAdmin.id, password: newPassword });
  }

  if (!establishmentId || !workspacePath) return <div className="management-state management-state--error"><h1>No establishment assigned</h1></div>;
  if (establishmentQuery.isPending) return <div className="management-state">Loading Admin management...</div>;
  if (establishmentQuery.isError) return <div className="management-state management-state--error"><h1>Establishment unavailable</h1><p>{errorMessage(establishmentQuery.error)}</p></div>;

  const establishment = establishmentQuery.data;
  const admins = adminsQuery.data ?? [];

  return (
    <div className="management-page admin-management-page">
      <header className="management-page-header management-page-header--compact">
        <div><p className="management-kicker">People and access</p><h1>Admins</h1><p>Delegate daily operations in {establishment.name} through controlled accounts and explicit permission grants.</p></div>
        <button className="management-primary-button" onClick={openCreateFlow} type="button">New Admin</button>
      </header>

      <section className="directory-toolbar admin-directory-toolbar" aria-label="Admin filters">
        <label className="search-field"><span>Search</span><input onChange={(event) => setQuery(event.target.value)} placeholder="Name, email, or CIN" value={query} /></label>
        <label><span>Status</span><select onChange={(event) => setStatus(event.target.value as AccountStatus | "")} value={status}><option value="">All statuses</option><option value="ACTIVE">Active</option><option value="LOCKED">Locked</option><option value="DEACTIVATED">Deactivated</option><option value="ARCHIVED">Archived</option></select></label>
        <label><span>Created from</span><input onChange={(event) => setCreatedFrom(event.target.value)} type="date" value={createdFrom} /></label>
        <label><span>Created to</span><input min={createdFrom || undefined} onChange={(event) => setCreatedTo(event.target.value)} type="date" value={createdTo} /></label>
      </section>

      <section className="management-panel directory-panel">
        <header className="panel-header panel-header--bordered"><div><h2>Operational Admins</h2><p>{admins.length} accounts found</p></div></header>
        {adminsQuery.isPending ? <div className="panel-empty">Loading Admins...</div>
          : adminsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(adminsQuery.error)}</div>
          : admins.length === 0 ? <div className="panel-empty"><strong>No Admin matches this view.</strong><p>Create an Admin or adjust the filters.</p></div>
          : (
            <div className="resource-table-wrapper"><table className="resource-table resource-table--accounts"><thead><tr><th>Admin</th><th>Contact</th><th>Status</th><th>Actions</th></tr></thead><tbody>
              {admins.map((admin) => (
                <tr className="resource-row--linked" key={admin.id}>
                  <td><Link className="resource-name resource-name--link" to={`${workspacePath}/admins/${admin.id}`}><span className="person-monogram">{admin.firstName[0]}{admin.lastName[0]}</span><div><strong>{admin.firstName} {admin.lastName}</strong><small>{admin.cin || "No CIN"}</small></div></Link></td>
                  <td><div className="table-contact"><span>{admin.email}</span><small>{admin.phoneNumber || "No phone number"}</small></div></td>
                  <td><StatusBadge status={admin.status} /></td>
                  <td><div className="row-actions">
                    <Link aria-label={`Open ${admin.firstName} ${admin.lastName}`} className="record-open-link" to={`${workspacePath}/admins/${admin.id}`}>View</Link>
                    <button onClick={() => setEditingAdmin(admin)} type="button">Edit</button>
                    <button onClick={() => { grantsMutation.reset(); setPermissionAdmin(admin); }} type="button">Permissions</button>
                    {admin.status !== "ARCHIVED" && <button onClick={() => { setResettingAdmin(admin); setNewPassword(""); setPasswordError(null); }} type="button">Reset password</button>}
                    {admin.status === "ACTIVE" && <button onClick={() => setConfirmation({ admin, action: "lock" })} type="button">Lock</button>}
                    {admin.status === "LOCKED" && <button onClick={() => setConfirmation({ admin, action: "unlock" })} type="button">Unlock</button>}
                    {(admin.status === "ACTIVE" || admin.status === "LOCKED") && <button className="danger-text" onClick={() => setConfirmation({ admin, action: "deactivate" })} type="button">Deactivate</button>}
                    {admin.status === "DEACTIVATED" && <button onClick={() => setConfirmation({ admin, action: "activate" })} type="button">Activate</button>}
                    {admin.status !== "ARCHIVED" && <button className="danger-text" onClick={() => setConfirmation({ admin, action: "archive" })} type="button">Archive</button>}
                    {admin.status === "ARCHIVED" && <button onClick={() => setConfirmation({ admin, action: "restore" })} type="button">Restore</button>}
                  </div></td>
                </tr>
              ))}
            </tbody></table></div>
          )}
      </section>

      {isCreateOpen && <ManagementModal size={createdAdminId ? "wide" : "default"} title="Create Admin" description={`Create an operational account for ${establishment.name}.`} onClose={closeCreateFlow}>
        <div className="admin-create-progress" aria-label="Admin creation progress">
          <div className={createdAdminId ? "is-complete" : "is-active"}><span>1</span><p><strong>Account information</strong><small>Identity and sign-in details</small></p></div>
          <i />
          <div className={createdAdminId ? "is-active" : ""}><span>2</span><p><strong>Permissions</strong><small>Operational access</small></p></div>
        </div>
        {!createdAdminId
          ? <AdminForm isSubmitting={createMutation.isPending} requestError={createMutation.isError ? errorMessage(createMutation.error) : null} submitLabel="Continue to permissions" onCancel={closeCreateFlow} onSubmit={async (values) => { try { await createMutation.mutateAsync(values); } catch { /* mutation state renders the error */ } }} />
          : permissionCatalogQuery.isPending
            ? <div className="panel-empty">Loading permission catalog...</div>
            : permissionCatalogQuery.isError
              ? <div className="permission-step-error"><div className="management-alert management-alert--error">{errorMessage(permissionCatalogQuery.error)}</div><button className="secondary-button" onClick={closeCreateFlow} type="button">Finish without permissions</button></div>
              : <PermissionGrantForm catalog={permissionCatalogQuery.data} grantedPermissions={[]} isSubmitting={grantsMutation.isPending} requestError={grantsMutation.isError ? errorMessage(grantsMutation.error) : null} cancelLabel="Finish without permissions" submitLabel="Create with permissions" onCancel={closeCreateFlow} onSubmit={async (permissions) => { try { await grantsMutation.mutateAsync({ adminId: createdAdminId, permissions }); } catch { /* mutation state renders the error */ } }} />}
      </ManagementModal>}
      {editingAdmin && <ManagementModal title="Edit Admin" description="Correct account identity and contact information." onClose={() => setEditingAdmin(null)}><AdminForm admin={editingAdmin} isSubmitting={updateMutation.isPending} requestError={updateMutation.isError ? errorMessage(updateMutation.error) : null} onCancel={() => setEditingAdmin(null)} onSubmit={async (values) => { try { await updateMutation.mutateAsync({ adminId: editingAdmin.id, values }); } catch { /* mutation state renders the error */ } }} /></ManagementModal>}
      {permissionAdmin && <ManagementModal size="wide" title="Admin permissions" description={`Control what ${permissionAdmin.firstName} ${permissionAdmin.lastName} can manage.`} onClose={() => setPermissionAdmin(null)}>{permissionCatalogQuery.isPending || grantsQuery.isPending ? <div className="panel-empty">Loading permission grants...</div> : permissionCatalogQuery.isError || grantsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(permissionCatalogQuery.error ?? grantsQuery.error)}</div> : <PermissionGrantForm catalog={permissionCatalogQuery.data} grantedPermissions={grantsQuery.data} isSubmitting={grantsMutation.isPending} requestError={grantsMutation.isError ? errorMessage(grantsMutation.error) : null} onCancel={() => setPermissionAdmin(null)} onSubmit={async (permissions) => { try { await grantsMutation.mutateAsync({ adminId: permissionAdmin.id, permissions }); } catch { /* mutation state renders the error */ } }} />}</ManagementModal>}
      {resettingAdmin && <ManagementModal title="Reset password" description={`Set a temporary password for ${resettingAdmin.firstName} ${resettingAdmin.lastName}.`} onClose={() => setResettingAdmin(null)}><div className="management-form"><div className="form-field form-field--wide"><label htmlFor="reset-admin-password">New temporary password</label><input id="reset-admin-password" onChange={(event) => { setNewPassword(event.target.value); setPasswordError(null); }} type="password" value={newPassword} />{passwordError && <p className="field-error">{passwordError}</p>}</div>{resetMutation.isError && <div className="management-alert management-alert--error">{errorMessage(resetMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={() => setResettingAdmin(null)} type="button">Cancel</button><button className="management-primary-button" disabled={resetMutation.isPending} onClick={submitPasswordReset} type="button">{resetMutation.isPending ? "Resetting..." : "Reset password"}</button></footer></div></ManagementModal>}
      {confirmation && <ConfirmActionModal actionLabel={actionLabel(confirmation.action)} destructive={confirmation.action === "deactivate" || confirmation.action === "archive"} description={`${actionLabel(confirmation.action)} the account for ${confirmation.admin.firstName} ${confirmation.admin.lastName}?`} error={lifecycleMutation.isError ? errorMessage(lifecycleMutation.error) : null} isSubmitting={lifecycleMutation.isPending} title={`${actionLabel(confirmation.action)} Admin`} onCancel={() => setConfirmation(null)} onConfirm={() => lifecycleMutation.mutate({ adminId: confirmation.admin.id, action: confirmation.action })} />}
    </div>
  );
}
