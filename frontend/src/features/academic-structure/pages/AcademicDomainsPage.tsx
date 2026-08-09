import { useDeferredValue, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import {
  academicStructureKeys,
  createAcademicDomain,
  deleteAcademicDomain,
  getAcademicDomains,
  updateAcademicDomain,
  type AcademicDomain,
} from "../api/academic-structure-api";

interface DomainFormState {
  code: string;
  name: string;
}

const emptyForm: DomainFormState = { code: "", name: "" };

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function AcademicDomainsPage() {
  const { establishmentId } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<AcademicDomain | null>(null);
  const [deleting, setDeleting] = useState<AcademicDomain | null>(null);
  const [form, setForm] = useState<DomainFormState>(emptyForm);
  const [validationError, setValidationError] = useState<string | null>(null);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());

  const domainsQuery = useQuery({
    queryKey: academicStructureKeys.academicDomains(establishmentId ?? "missing"),
    queryFn: () => getAcademicDomains(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const domains = (domainsQuery.data ?? []).filter((domain) =>
    domain.name.toLowerCase().includes(deferredSearch) || domain.code.toLowerCase().includes(deferredSearch));

  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: academicStructureKeys.academicDomains(establishmentId!) });
  }

  const createMutation = useMutation({
    mutationFn: (value: DomainFormState) => createAcademicDomain(establishmentId!, value),
    onSuccess: async () => { await refresh(); closeForm(); },
  });
  const updateMutation = useMutation({
    mutationFn: ({ id, value }: { id: string; value: DomainFormState }) => updateAcademicDomain(id, value),
    onSuccess: async () => { await refresh(); closeForm(); },
  });
  const deleteMutation = useMutation({
    mutationFn: deleteAcademicDomain,
    onSuccess: async () => { await refresh(); setDeleting(null); },
  });

  function closeForm() {
    setCreating(false);
    setEditing(null);
    setForm(emptyForm);
    setValidationError(null);
    createMutation.reset();
    updateMutation.reset();
  }

  function submit() {
    const value = { code: form.code.trim().toUpperCase(), name: form.name.trim() };
    if (!value.code || !value.name) {
      setValidationError("Code and name are required.");
      return;
    }
    if (editing) updateMutation.mutate({ id: editing.id, value });
    else createMutation.mutate(value);
  }

  if (!establishmentId) return <div className="management-state management-state--error"><h1>No establishment assigned</h1></div>;
  const formMutation = editing ? updateMutation : createMutation;

  return <div className="management-page academic-directory-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Academic structure</p><h1>Academic Domains</h1><p>Maintain the knowledge areas used to classify modules and Professor expertise.</p></div><button className="management-primary-button" onClick={() => { setForm(emptyForm); setCreating(true); }} type="button">New Academic Domain</button></header>
    <section className="directory-toolbar academic-directory-toolbar"><label className="search-field"><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Search by code or name" value={search} /></label><span className="directory-result-count">{domains.length} {domains.length === 1 ? "domain" : "domains"}</span></section>
    <section className="management-panel directory-panel">
      {domainsQuery.isPending ? <div className="panel-empty">Loading academic domains...</div> : domainsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(domainsQuery.error)}</div> : domains.length === 0 ? <div className="panel-empty"><strong>No academic domain found.</strong><p>{search ? "Try a different search term." : "Create the first domain before assigning modules or Professor expertise."}</p></div> : <div className="resource-table-wrapper"><table className="resource-table academic-resource-table"><thead><tr><th>Academic Domain</th><th>Code</th><th>Last updated</th><th>Actions</th></tr></thead><tbody>{domains.map((domain) => <tr key={domain.id}><td><div className="resource-name"><span className="resource-monogram">{domain.code.slice(0, 2)}</span><strong>{domain.name}</strong></div></td><td><span className="academic-domain-code">{domain.code}</span></td><td>{domain.updatedAt ? new Date(domain.updatedAt).toLocaleDateString() : "Not available"}</td><td><div className="row-actions"><button onClick={() => { setEditing(domain); setForm({ code: domain.code, name: domain.name }); }} type="button">Edit</button><button className="danger-text" onClick={() => setDeleting(domain)} type="button">Delete</button></div></td></tr>)}</tbody></table></div>}
    </section>
    {(creating || editing) && <ManagementModal title={`${editing ? "Edit" : "Create"} Academic Domain`} description="Define a reusable knowledge area for modules and Professor qualifications." onClose={closeForm}><div className="management-form"><div className="form-grid"><div className="form-field"><label htmlFor="academic-domain-code">Code</label><input autoFocus id="academic-domain-code" maxLength={50} onChange={(event) => { setForm((current) => ({ ...current, code: event.target.value })); setValidationError(null); }} placeholder="e.g. AI" value={form.code} /></div><div className="form-field"><label htmlFor="academic-domain-name">Name</label><input id="academic-domain-name" maxLength={255} onChange={(event) => { setForm((current) => ({ ...current, name: event.target.value })); setValidationError(null); }} onKeyDown={(event) => { if (event.key === "Enter") submit(); }} placeholder="Artificial Intelligence" value={form.name} /></div></div>{validationError && <p className="field-error">{validationError}</p>}{formMutation.isError && <div className="management-alert management-alert--error">{errorMessage(formMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={closeForm} type="button">Cancel</button><button className="management-primary-button" disabled={formMutation.isPending} onClick={submit} type="button">{formMutation.isPending ? "Saving..." : "Save Domain"}</button></footer></div></ManagementModal>}
    {deleting && <ConfirmActionModal actionLabel="Delete" destructive description={`Delete ${deleting.name}? Domains assigned to modules or Professors must remain available.`} error={deleteMutation.isError ? errorMessage(deleteMutation.error) : null} isSubmitting={deleteMutation.isPending} onCancel={() => setDeleting(null)} onConfirm={() => deleteMutation.mutate(deleting.id)} title="Delete Academic Domain" />}
  </div>;
}
