import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useAuth } from "@/features/auth/hooks/useAuth";
import {
  activateEstablishment,
  deactivateEstablishment,
  getEstablishment,
  getSuperAdmins,
  rootGovernanceKeys,
  updateEstablishment,
} from "@/features/root-governance/api/root-governance-api";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { EstablishmentForm, type EstablishmentFormValues } from "@/features/root-governance/components/EstablishmentForm";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import { establishmentAdminKeys, getAdmins } from "../api/establishment-admin-api";
import { useEstablishmentScope } from "../context/useEstablishmentScope";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The establishment workspace is unavailable.";
}

export function EstablishmentWorkspacePage() {
  const { establishmentId, isRootContext, workspacePath } = useEstablishmentScope();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [isEditOpen, setEditOpen] = useState(false);
  const [statusAction, setStatusAction] = useState<"activate" | "deactivate" | null>(null);
  const establishmentQuery = useQuery({
    queryKey: rootGovernanceKeys.establishment(establishmentId ?? "missing"),
    queryFn: () => getEstablishment(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const adminsQuery = useQuery({
    queryKey: establishmentAdminKeys.admins(establishmentId ?? "missing"),
    queryFn: () => getAdmins(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const superAdminsQuery = useQuery({
    queryKey: rootGovernanceKeys.superAdmins(establishmentId ?? "missing"),
    queryFn: () => getSuperAdmins(establishmentId!),
    enabled: Boolean(establishmentId) && isRootContext,
  });
  const updateMutation = useMutation({
    mutationFn: (values: EstablishmentFormValues) => updateEstablishment(establishmentId!, values),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: rootGovernanceKeys.establishment(establishmentId!) }),
        queryClient.invalidateQueries({ queryKey: ["root-governance", "establishments"] }),
      ]);
      setEditOpen(false);
    },
  });
  const statusMutation = useMutation({
    mutationFn: (action: "activate" | "deactivate") => action === "activate"
      ? activateEstablishment(establishmentId!)
      : deactivateEstablishment(establishmentId!),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: rootGovernanceKeys.establishment(establishmentId!) }),
        queryClient.invalidateQueries({ queryKey: ["root-governance", "establishments"] }),
      ]);
      setStatusAction(null);
    },
  });

  if (!establishmentId || !workspacePath) {
    return <div className="management-state management-state--error"><h1>No establishment assigned</h1><p>This account has no establishment context.</p></div>;
  }

  if (establishmentQuery.isPending) return <div className="management-state">Loading establishment workspace...</div>;
  if (establishmentQuery.isError) {
    return <div className="management-state management-state--error"><h1>Workspace unavailable</h1><p>{errorMessage(establishmentQuery.error)}</p></div>;
  }

  const establishment = establishmentQuery.data;
  const admins = adminsQuery.data ?? [];
  const activeAdmins = admins.filter((admin) => admin.status === "ACTIVE").length;
  const superAdmins = superAdminsQuery.data ?? [];

  return (
    <div className="management-page establishment-workspace-page">
      <header className="context-overview-header">
        <span className="establishment-emblem">{establishment.name.slice(0, 2).toUpperCase()}</span>
        <div>
          <div className="identity-meta"><span>{establishment.type[0] + establishment.type.slice(1).toLowerCase()}</span><StatusBadge status={establishment.status} /></div>
          <h1>{establishment.name}</h1>
          <p>Establishment overview and operational access.</p>
        </div>
        <span className="root-context-label">{user?.role.replaceAll("_", " ")} context</span>
      </header>

      <section className="context-stat-strip" aria-label="Establishment summary">
        {isRootContext && <article><span>Super Admins</span><strong>{superAdmins.length}</strong><small>Establishment leadership</small></article>}
        <article><span>Admin accounts</span><strong>{admins.length}</strong><small>Delegated operations</small></article>
        <article><span>Active Admins</span><strong>{activeAdmins}</strong><small>Current access</small></article>
        <article><span>Require attention</span><strong>{admins.length - activeAdmins}</strong><small>Non-active accounts</small></article>
      </section>

      <section className="context-overview-grid">
        <article className="management-panel context-summary-panel">
          <header className="context-panel-heading">
            <div><p className="management-kicker">Current context</p><h2>Establishment record</h2></div>
            {isRootContext && <button className="secondary-button secondary-button--compact" onClick={() => setEditOpen(true)} type="button">Edit establishment</button>}
          </header>
          <dl>
            <div><dt>Official name</dt><dd>{establishment.name}</dd></div>
            <div><dt>Type</dt><dd>{establishment.type[0] + establishment.type.slice(1).toLowerCase()}</dd></div>
            <div><dt>Operational status</dt><dd><StatusBadge status={establishment.status} /></dd></div>
            <div><dt>Identifier</dt><dd>{establishment.id}</dd></div>
          </dl>
          {isRootContext && <div className="context-status-control">
            <span>Operational control</span>
            {establishment.status === "ACTIVE" && <button className="danger-ghost-button" onClick={() => setStatusAction("deactivate")} type="button">Deactivate</button>}
            {establishment.status === "INACTIVE" && <button className="management-primary-button" onClick={() => setStatusAction("activate")} type="button">Activate</button>}
          </div>}
        </article>

      
      </section>

      {isEditOpen && <ManagementModal title="Edit establishment" description="Update the official establishment identity." onClose={() => setEditOpen(false)}>
        <EstablishmentForm establishment={establishment} isSubmitting={updateMutation.isPending} requestError={updateMutation.isError ? errorMessage(updateMutation.error) : null} onCancel={() => setEditOpen(false)} onSubmit={async (values) => { try { await updateMutation.mutateAsync(values); } catch { /* mutation state renders the error */ } }} />
      </ManagementModal>}

      {statusAction && <ConfirmActionModal actionLabel={statusAction === "activate" ? "Activate" : "Deactivate"} destructive={statusAction === "deactivate"} description={`${statusAction === "activate" ? "Activate" : "Deactivate"} ${establishment.name}? Existing records remain preserved.`} error={statusMutation.isError ? errorMessage(statusMutation.error) : null} isSubmitting={statusMutation.isPending} title={`${statusAction === "activate" ? "Activate" : "Deactivate"} establishment`} onCancel={() => setStatusAction(null)} onConfirm={() => statusMutation.mutate(statusAction)} />}
    </div>
  );
}
