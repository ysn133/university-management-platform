import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  changeSuperAdminStatus,
  createSuperAdmin,
  getEstablishment,
  getSuperAdmins,
  resetSuperAdminPassword,
  rootGovernanceKeys,
  updateSuperAdmin,
  type AccountStatus,
  type SuperAdmin,
  type SuperAdminLifecycleAction,
} from "../api/root-governance-api";
import { ConfirmActionModal } from "../components/ConfirmActionModal";
import { ManagementModal } from "../components/ManagementModal";
import { StatusBadge } from "../components/StatusBadge";
import { SuperAdminForm, type SuperAdminFormValues } from "../components/SuperAdminForm";

type Confirmation = { action: SuperAdminLifecycleAction; superAdmin: SuperAdmin };

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function lifecycleLabel(action: SuperAdminLifecycleAction | "activate"): string {
  return action[0].toUpperCase() + action.slice(1);
}

export function EstablishmentDetailsPage() {
  const { establishmentId = "" } = useParams();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<AccountStatus | "">("");
  const [isCreateAdminOpen, setCreateAdminOpen] = useState(false);
  const [editingSuperAdmin, setEditingSuperAdmin] = useState<SuperAdmin | null>(null);
  const [resettingSuperAdmin, setResettingSuperAdmin] = useState<SuperAdmin | null>(null);
  const [newPassword, setNewPassword] = useState("");
  const [passwordValidationError, setPasswordValidationError] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<Confirmation | null>(null);
  const deferredQuery = useDeferredValue(query.trim());
  const superAdminFilters = {
    ...(deferredQuery ? { query: deferredQuery } : {}),
    ...(status ? { status } : {}),
  };

  const establishmentQuery = useQuery({
    queryKey: rootGovernanceKeys.establishment(establishmentId),
    queryFn: () => getEstablishment(establishmentId),
    enabled: Boolean(establishmentId),
  });
  const superAdminsQuery = useQuery({
    queryKey: rootGovernanceKeys.superAdmins(establishmentId, superAdminFilters),
    queryFn: () => getSuperAdmins(establishmentId, superAdminFilters),
    enabled: Boolean(establishmentId),
  });

  async function refreshSuperAdmins() {
    await queryClient.invalidateQueries({
      queryKey: ["root-governance", "super-admins", establishmentId],
    });
  }

  const createSuperAdminMutation = useMutation({
    mutationFn: (values: SuperAdminFormValues) =>
      createSuperAdmin(establishmentId, {
        universityEmail: values.universityEmail,
        password: values.password!,
        firstName: values.firstName,
        lastName: values.lastName,
        birth_date: values.birthDate,
        cin: values.cin || undefined,
        sex: values.sex,
        phone_number: values.phoneNumber || undefined,
      }),
    onSuccess: async () => {
      await refreshSuperAdmins();
      setCreateAdminOpen(false);
    },
  });
  const updateSuperAdminMutation = useMutation({
    mutationFn: ({ superAdminId, values }: { superAdminId: string; values: SuperAdminFormValues }) =>
      updateSuperAdmin(superAdminId, {
        universityEmail: values.universityEmail,
        firstName: values.firstName,
        lastName: values.lastName,
        birth_date: values.birthDate,
        cin: values.cin || undefined,
        sex: values.sex,
        phone_number: values.phoneNumber || undefined,
      }),
    onSuccess: async () => {
      await refreshSuperAdmins();
      setEditingSuperAdmin(null);
    },
  });
  const resetPasswordMutation = useMutation({
    mutationFn: ({ superAdminId, password }: { superAdminId: string; password: string }) =>
      resetSuperAdminPassword(superAdminId, password),
    onSuccess: () => {
      setResettingSuperAdmin(null);
      setNewPassword("");
      setPasswordValidationError(null);
    },
  });
  const superAdminStatusMutation = useMutation({
    mutationFn: ({ superAdminId, action }: { superAdminId: string; action: SuperAdminLifecycleAction }) =>
      changeSuperAdminStatus(superAdminId, action),
    onSuccess: async () => {
      await refreshSuperAdmins();
      setConfirmation(null);
    },
  });

  if (establishmentQuery.isPending) {
    return <div className="management-state">Loading establishment...</div>;
  }

  if (establishmentQuery.isError) {
    return (
      <div className="management-state management-state--error">
        <h1>Establishment unavailable</h1>
        <p>{errorMessage(establishmentQuery.error)}</p>
        <Link className="secondary-button" to="/management/establishments">Back to directory</Link>
      </div>
    );
  }

  const establishment = establishmentQuery.data;
  const superAdmins = superAdminsQuery.data ?? [];

  function submitResetPassword() {
    if (newPassword.length < 8) {
      setPasswordValidationError("The new password must contain at least 8 characters.");
      return;
    }
    if (resettingSuperAdmin) {
      resetPasswordMutation.mutate({ superAdminId: resettingSuperAdmin.id, password: newPassword });
    }
  }

  function confirmAction() {
    if (!confirmation) return;
    superAdminStatusMutation.mutate({
      superAdminId: confirmation.superAdmin.id,
      action: confirmation.action,
    });
  }

  return (
    <div className="management-page establishment-details-page">
      <header className="management-page-header management-page-header--compact">
          <div><p className="management-kicker">Establishment leadership</p><h1>Super Admins</h1><p>Root-managed accounts with complete authority inside {establishment.name}.</p></div>
          <button className="management-primary-button" disabled={establishment.status !== "ACTIVE"} onClick={() => setCreateAdminOpen(true)} type="button">Add Super Admin</button>
        </header>

        <section className="management-panel directory-panel">
        <header className="panel-header panel-header--bordered">
          <div>
            <h2>Leadership directory</h2>
            <p>{superAdmins.length} accounts found</p>
          </div>
        </header>

        <div className="directory-toolbar directory-toolbar--inside" aria-label="Super Admin filters">
          <label className="search-field">
            <span>Search</span>
            <input onChange={(event) => setQuery(event.target.value)} placeholder="Name, email, or CIN" value={query} />
          </label>
          <label>
            <span>Status</span>
            <select onChange={(event) => setStatus(event.target.value as AccountStatus | "")} value={status}>
              <option value="">All statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="LOCKED">Locked</option>
              <option value="DEACTIVATED">Deactivated</option>
              <option value="ARCHIVED">Archived</option>
            </select>
          </label>
        </div>

        {superAdminsQuery.isPending ? (
          <div className="panel-empty">Loading Super Admins...</div>
        ) : superAdminsQuery.isError ? (
          <div className="panel-empty panel-empty--error">{errorMessage(superAdminsQuery.error)}</div>
        ) : superAdmins.length === 0 ? (
          <div className="panel-empty">
            <strong>No Super Admin matches this view.</strong>
            <p>{query || status ? "Adjust the filters to view other accounts." : "Create the first Super Admin for this establishment."}</p>
          </div>
        ) : (
          <div className="resource-table-wrapper">
            <table className="resource-table resource-table--accounts">
              <thead><tr><th>Super Admin</th><th>Contact</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {superAdmins.map((superAdmin) => (
                  <tr key={superAdmin.id}>
                    <td>
                      <div className="resource-name">
                        <span className="person-monogram">{superAdmin.firstName.slice(0, 1)}{superAdmin.lastName.slice(0, 1)}</span>
                        <div><strong>{superAdmin.firstName} {superAdmin.lastName}</strong><small>{superAdmin.cin || "No CIN"}</small></div>
                      </div>
                    </td>
                    <td><div className="table-contact"><span>{superAdmin.email}</span><small>{superAdmin.phoneNumber || "No phone number"}</small></div></td>
                    <td><StatusBadge status={superAdmin.status} /></td>
                    <td>
                      <div className="row-actions">
                        <button onClick={() => setEditingSuperAdmin(superAdmin)} type="button">Edit</button>
                        {superAdmin.status !== "ARCHIVED" && <button onClick={() => { setResettingSuperAdmin(superAdmin); setNewPassword(""); }} type="button">Reset password</button>}
                        {superAdmin.status === "ACTIVE" && <button onClick={() => setConfirmation({ action: "lock", superAdmin })} type="button">Lock</button>}
                        {superAdmin.status === "LOCKED" && <button onClick={() => setConfirmation({ action: "unlock", superAdmin })} type="button">Unlock</button>}
                        {(superAdmin.status === "ACTIVE" || superAdmin.status === "LOCKED") && <button className="danger-text" onClick={() => setConfirmation({ action: "deactivate", superAdmin })} type="button">Deactivate</button>}
                        {superAdmin.status === "DEACTIVATED" && <button onClick={() => setConfirmation({ action: "activate", superAdmin })} type="button">Activate</button>}
                        {superAdmin.status !== "ARCHIVED" && <button className="danger-text" onClick={() => setConfirmation({ action: "archive", superAdmin })} type="button">Archive</button>}
                        {superAdmin.status === "ARCHIVED" && <button onClick={() => setConfirmation({ action: "restore", superAdmin })} type="button">Restore</button>}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        </section>

      {isCreateAdminOpen && (
        <ManagementModal title="Create Super Admin" description={`Create an establishment leader for ${establishment.name}.`} onClose={() => setCreateAdminOpen(false)}>
          <SuperAdminForm
            isSubmitting={createSuperAdminMutation.isPending}
            requestError={createSuperAdminMutation.isError ? errorMessage(createSuperAdminMutation.error) : null}
            onCancel={() => setCreateAdminOpen(false)}
            onSubmit={async (values) => { try { await createSuperAdminMutation.mutateAsync(values); } catch { /* shown by mutation state */ } }}
          />
        </ManagementModal>
      )}

      {editingSuperAdmin && (
        <ManagementModal title="Edit Super Admin" description="Correct account identity and contact information." onClose={() => setEditingSuperAdmin(null)}>
          <SuperAdminForm
            superAdmin={editingSuperAdmin}
            isSubmitting={updateSuperAdminMutation.isPending}
            requestError={updateSuperAdminMutation.isError ? errorMessage(updateSuperAdminMutation.error) : null}
            onCancel={() => setEditingSuperAdmin(null)}
            onSubmit={async (values) => { try { await updateSuperAdminMutation.mutateAsync({ superAdminId: editingSuperAdmin.id, values }); } catch { /* shown by mutation state */ } }}
          />
        </ManagementModal>
      )}

      {resettingSuperAdmin && (
        <ManagementModal title="Reset password" description={`Set a temporary password for ${resettingSuperAdmin.firstName} ${resettingSuperAdmin.lastName}.`} onClose={() => setResettingSuperAdmin(null)}>
          <div className="management-form">
            <div className="form-field form-field--wide">
              <label htmlFor="reset-super-admin-password">New temporary password</label>
              <input id="reset-super-admin-password" onChange={(event) => { setNewPassword(event.target.value); setPasswordValidationError(null); }} type="password" value={newPassword} />
              {passwordValidationError && <p className="field-error">{passwordValidationError}</p>}
            </div>
            {resetPasswordMutation.isError && <div className="management-alert management-alert--error">{errorMessage(resetPasswordMutation.error)}</div>}
            <footer className="form-actions">
              <button className="secondary-button" onClick={() => setResettingSuperAdmin(null)} type="button">Cancel</button>
              <button className="management-primary-button" disabled={resetPasswordMutation.isPending} onClick={submitResetPassword} type="button">
                {resetPasswordMutation.isPending ? "Resetting..." : "Reset password"}
              </button>
            </footer>
          </div>
        </ManagementModal>
      )}

      {confirmation && (
        <ConfirmActionModal
          actionLabel={lifecycleLabel(confirmation.action)}
          destructive={confirmation.action === "deactivate" || confirmation.action === "archive"}
          description={`${lifecycleLabel(confirmation.action)} the account for ${confirmation.superAdmin.firstName} ${confirmation.superAdmin.lastName}?`}
          error={superAdminStatusMutation.isError ? errorMessage(superAdminStatusMutation.error) : null}
          isSubmitting={superAdminStatusMutation.isPending}
          title={`${lifecycleLabel(confirmation.action)} Super Admin`}
          onCancel={() => setConfirmation(null)}
          onConfirm={confirmAction}
        />
      )}
    </div>
  );
}
