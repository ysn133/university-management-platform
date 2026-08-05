import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useState } from "react";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { academicStructureKeys, createAcademicYear, deleteAcademicYear, getAcademicYears, updateAcademicYear, type AcademicYear, type AcademicYearStatus } from "../api/academic-structure-api";

function errorMessage(error: unknown): string { return error instanceof ApiRequestError ? error.message : "The request could not be completed."; }

export function AcademicYearsPage() {
  const { establishmentId } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [editing, setEditing] = useState<AcademicYear | null>(null);
  const [creating, setCreating] = useState(false);
  const [deleting, setDeleting] = useState<AcademicYear | null>(null);
  const [label, setLabel] = useState("");
  const [status, setStatus] = useState<AcademicYearStatus>("PLANNED");
  const [validationError, setValidationError] = useState<string | null>(null);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const yearsQuery = useQuery({ queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"), queryFn: () => getAcademicYears(establishmentId!), enabled: Boolean(establishmentId) });
  const years = (yearsQuery.data ?? []).filter((year) => year.label.toLowerCase().includes(deferredSearch));

  async function refresh() { await queryClient.invalidateQueries({ queryKey: academicStructureKeys.academicYears(establishmentId!) }); }
  function closeForm() { setCreating(false); setEditing(null); setLabel(""); setStatus("PLANNED"); setValidationError(null); createMutation.reset(); updateMutation.reset(); }
  const createMutation = useMutation({ mutationFn: () => createAcademicYear(establishmentId!, { label: label.trim(), status }), onSuccess: async () => { await refresh(); closeForm(); } });
  const updateMutation = useMutation({ mutationFn: () => updateAcademicYear(editing!.id, { label: label.trim(), status }), onSuccess: async () => { await refresh(); closeForm(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => deleteAcademicYear(id), onSuccess: async () => { await refresh(); setDeleting(null); } });

  function openCreate() { setLabel(""); setStatus("PLANNED"); setCreating(true); }
  function openEdit(year: AcademicYear) { setEditing(year); setLabel(year.label); setStatus(year.status); }
  function submit() {
    const match = /^(\d{4})-(\d{4})$/.exec(label.trim());
    if (!match || Number(match[2]) !== Number(match[1]) + 1) { setValidationError("Use two consecutive years in YYYY-YYYY format."); return; }
    if (editing) updateMutation.mutate(); else createMutation.mutate();
  }

  if (!establishmentId) return <div className="management-state management-state--error"><h1>No establishment assigned</h1></div>;
  const formMutation = editing ? updateMutation : createMutation;

  return <div className="management-page academic-directory-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Academic structure</p><h1>Academic Years</h1><p>Control the annual academic periods used by registrations, semesters, schedules, and results.</p></div><button className="management-primary-button" onClick={openCreate} type="button">New Academic Year</button></header>
    <section className="directory-toolbar academic-directory-toolbar"><label className="search-field"><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Search by year" value={search} /></label><span className="directory-result-count">{years.length} academic {years.length === 1 ? "year" : "years"}</span></section>
    <section className="management-panel directory-panel">{yearsQuery.isPending ? <div className="panel-empty">Loading academic years...</div> : yearsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(yearsQuery.error)}</div> : years.length === 0 ? <div className="panel-empty"><strong>No academic year found.</strong><p>Create the first annual academic period.</p></div> : <div className="resource-table-wrapper"><table className="resource-table academic-resource-table"><thead><tr><th>Academic year</th><th>Period</th><th>Status</th><th>Actions</th></tr></thead><tbody>{years.map((year) => <tr key={year.id}><td><div className="resource-name"><span className="resource-monogram">{String(year.startYear).slice(2)}</span><strong>{year.label}</strong></div></td><td>{year.startYear} to {year.endYear}</td><td><StatusBadge status={year.status} /></td><td><div className="row-actions"><button onClick={() => openEdit(year)} type="button">Edit</button><button className="danger-text" onClick={() => setDeleting(year)} type="button">Delete</button></div></td></tr>)}</tbody></table></div>}</section>
    {(creating || editing) && <ManagementModal title={`${editing ? "Edit" : "Create"} Academic Year`} description="Academic years must contain two consecutive calendar years." onClose={closeForm}><div className="management-form management-form--two-columns"><div className="form-field"><label htmlFor="academic-year-label">Label</label><input autoFocus id="academic-year-label" onChange={(event) => { setLabel(event.target.value); setValidationError(null); }} placeholder="2026-2027" value={label} />{validationError && <p className="field-error">{validationError}</p>}</div><div className="form-field"><label htmlFor="academic-year-status">Status</label><select id="academic-year-status" onChange={(event) => setStatus(event.target.value as AcademicYearStatus)} value={status}><option value="PLANNED">Planned</option><option value="ACTIVE">Active</option><option value="CLOSED">Closed</option></select></div>{formMutation.isError && <div className="management-alert management-alert--error">{errorMessage(formMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={closeForm} type="button">Cancel</button><button className="management-primary-button" disabled={formMutation.isPending} onClick={submit} type="button">{formMutation.isPending ? "Saving..." : "Save"}</button></footer></div></ManagementModal>}
    {deleting && <ConfirmActionModal actionLabel="Delete" destructive description={`Delete ${deleting.label}? This is only possible when no academic records depend on it.`} error={deleteMutation.isError ? errorMessage(deleteMutation.error) : null} isSubmitting={deleteMutation.isPending} onCancel={() => setDeleting(null)} onConfirm={() => deleteMutation.mutate(deleting.id)} title="Delete Academic Year" />}
  </div>;
}
