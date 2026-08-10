import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import {
  academicStructureKeys,
  createAcademicRank,
  deleteAcademicRank,
  getAcademicRanks,
  updateAcademicRank,
  type AcademicRank,
  type AcademicRankRequest,
} from "../api/academic-structure-api";

interface AcademicRanksSettingsProps {
  establishmentId: string;
}

const emptyRank: AcademicRankRequest = {
  code: "",
  name: "",
  seniorityOrder: 1,
  canHoldModuleResponsibility: false,
  status: "ACTIVE",
};

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function AcademicRanksSettings({ establishmentId }: AcademicRanksSettingsProps) {
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState<AcademicRank | "new" | null>(null);
  const [form, setForm] = useState<AcademicRankRequest>(emptyRank);
  const ranksQuery = useQuery({
    queryKey: academicStructureKeys.academicRanks(establishmentId),
    queryFn: () => getAcademicRanks(establishmentId),
  });

  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: academicStructureKeys.academicRanks(establishmentId) });
  }

  function close() {
    setEditing(null);
    setForm(emptyRank);
    createMutation.reset();
    updateMutation.reset();
  }

  const createMutation = useMutation({
    mutationFn: () => createAcademicRank(establishmentId, form),
    onSuccess: async () => { await refresh(); close(); },
  });
  const updateMutation = useMutation({
    mutationFn: () => updateAcademicRank((editing as AcademicRank).id, form),
    onSuccess: async () => { await refresh(); close(); },
  });
  const deleteMutation = useMutation({
    mutationFn: deleteAcademicRank,
    onSuccess: refresh,
  });

  function openCreate() {
    setForm(emptyRank);
    setEditing("new");
  }

  function openEdit(rank: AcademicRank) {
    setForm({
      code: rank.code,
      name: rank.name,
      seniorityOrder: rank.seniorityOrder,
      canHoldModuleResponsibility: rank.canHoldModuleResponsibility,
      status: rank.status,
    });
    setEditing(rank);
  }

  function submit() {
    if (!form.code.trim() || !form.name.trim() || form.seniorityOrder < 1) return;
    if (editing === "new") createMutation.mutate(); else updateMutation.mutate();
  }

  const mutation = editing === "new" ? createMutation : updateMutation;
  const ranks = ranksQuery.data ?? [];

  return <>
    <section className="settings-section-heading">
      <div><h2>Academic ranks</h2><p>Define the ranks used for professor classification and teaching responsibility.</p></div>
      <button className="management-primary-button" onClick={openCreate} type="button">New Rank</button>
    </section>
    <section className="management-panel directory-panel">
      {ranksQuery.isPending ? <div className="panel-empty">Loading academic ranks...</div> : ranksQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(ranksQuery.error)}</div> : ranks.length === 0 ? <div className="panel-empty"><strong>No academic rank configured.</strong><p>Create ranks before registering professors or setting assignment preferences.</p></div> : <div className="resource-table-wrapper"><table className="resource-table academic-rank-table"><thead><tr><th>Rank</th><th>Seniority</th><th>Module responsibility</th><th>Status</th><th>Actions</th></tr></thead><tbody>{ranks.map((rank) => <tr key={rank.id}><td><div className="table-contact"><strong>{rank.name}</strong><small>{rank.code}</small></div></td><td>{rank.seniorityOrder}</td><td>{rank.canHoldModuleResponsibility ? "Eligible" : "Not eligible"}</td><td><StatusBadge status={rank.status} /></td><td><div className="row-actions"><button onClick={() => openEdit(rank)} type="button">Edit</button><button className="danger-text" disabled={deleteMutation.isPending} onClick={() => { if (window.confirm(`Delete ${rank.name}?`)) deleteMutation.mutate(rank.id); }} type="button">Delete</button></div></td></tr>)}</tbody></table></div>}
      {deleteMutation.isError && <div className="management-alert management-alert--error settings-inline-alert">{errorMessage(deleteMutation.error)}</div>}
    </section>

    {editing && <ManagementModal title={editing === "new" ? "Create Academic Rank" : `Edit ${editing.name}`} description="Ranks are managed inside this establishment and used during professor assignment." onClose={close}><div className="management-form management-form--two-columns"><div className="form-field"><label htmlFor="rank-code">Code</label><input autoFocus id="rank-code" onChange={(event) => setForm((current) => ({ ...current, code: event.target.value }))} placeholder="ASSISTANT_PROFESSOR" value={form.code} /></div><div className="form-field"><label htmlFor="rank-name">Name</label><input id="rank-name" onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} placeholder="Assistant Professor" value={form.name} /></div><div className="form-field"><label htmlFor="rank-order">Seniority order</label><input id="rank-order" min="1" onChange={(event) => setForm((current) => ({ ...current, seniorityOrder: Number(event.target.value) }))} type="number" value={form.seniorityOrder} /></div><div className="form-field"><label htmlFor="rank-status">Status</label><select id="rank-status" onChange={(event) => setForm((current) => ({ ...current, status: event.target.value as AcademicRankRequest["status"] }))} value={form.status}><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></div><label className="form-field form-field--wide curriculum-policy-check"><input checked={form.canHoldModuleResponsibility} onChange={(event) => setForm((current) => ({ ...current, canHoldModuleResponsibility: event.target.checked }))} type="checkbox" /><span><strong>Eligible for module responsibility</strong><small>Professors with this rank may be responsible for a module and class.</small></span></label>{mutation.isError && <div className="management-alert management-alert--error">{errorMessage(mutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={close} type="button">Cancel</button><button className="management-primary-button" disabled={mutation.isPending || !form.code.trim() || !form.name.trim() || form.seniorityOrder < 1} onClick={submit} type="button">{mutation.isPending ? "Saving..." : "Save rank"}</button></footer></div></ManagementModal>}
  </>;
}
