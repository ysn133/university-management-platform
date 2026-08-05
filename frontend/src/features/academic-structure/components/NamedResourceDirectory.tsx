import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useState } from "react";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import type { NamedResource } from "../api/academic-structure-api";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";

interface NamedResourceDirectoryProps {
  title: string;
  singular: string;
  description: string;
  emptyDescription: string;
  queryKey: (establishmentId: string) => readonly unknown[];
  load: (establishmentId: string) => Promise<NamedResource[]>;
  create: (establishmentId: string, name: string) => Promise<NamedResource>;
  update: (id: string, name: string) => Promise<NamedResource>;
  remove: (id: string) => Promise<void>;
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function NamedResourceDirectory({ title, singular, description, emptyDescription, queryKey, load, create, update, remove }: NamedResourceDirectoryProps) {
  const { establishmentId } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<NamedResource | null>(null);
  const [deleting, setDeleting] = useState<NamedResource | null>(null);
  const [name, setName] = useState("");
  const [validationError, setValidationError] = useState<string | null>(null);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const resourcesQuery = useQuery({ queryKey: queryKey(establishmentId ?? "missing"), queryFn: () => load(establishmentId!), enabled: Boolean(establishmentId) });
  const resources = (resourcesQuery.data ?? []).filter((resource) => resource.name.toLowerCase().includes(deferredSearch));

  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: queryKey(establishmentId!) });
  }

  const createMutation = useMutation({ mutationFn: (value: string) => create(establishmentId!, value), onSuccess: async () => { await refresh(); closeForm(); } });
  const updateMutation = useMutation({ mutationFn: ({ id, value }: { id: string; value: string }) => update(id, value), onSuccess: async () => { await refresh(); closeForm(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => remove(id), onSuccess: async () => { await refresh(); setDeleting(null); } });

  function closeForm() {
    setCreating(false);
    setEditing(null);
    setName("");
    setValidationError(null);
    createMutation.reset();
    updateMutation.reset();
  }

  function submit() {
    const value = name.trim();
    if (!value) { setValidationError(`${singular} name is required.`); return; }
    if (editing) updateMutation.mutate({ id: editing.id, value });
    else createMutation.mutate(value);
  }

  if (!establishmentId) return <div className="management-state management-state--error"><h1>No establishment assigned</h1></div>;
  const formMutation = editing ? updateMutation : createMutation;

  return <div className="management-page academic-directory-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Academic structure</p><h1>{title}</h1><p>{description}</p></div><button className="management-primary-button" onClick={() => { setName(""); setCreating(true); }} type="button">New {singular}</button></header>
    <section className="directory-toolbar academic-directory-toolbar"><label className="search-field"><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder={`Search ${title.toLowerCase()}`} value={search} /></label><span className="directory-result-count">{resources.length} {resources.length === 1 ? singular.toLowerCase() : title.toLowerCase()}</span></section>
    <section className="management-panel directory-panel">
      {resourcesQuery.isPending ? <div className="panel-empty">Loading {title.toLowerCase()}...</div> : resourcesQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(resourcesQuery.error)}</div> : resources.length === 0 ? <div className="panel-empty"><strong>No {singular.toLowerCase()} found.</strong><p>{search ? "Try a different search term." : emptyDescription}</p></div> : <div className="resource-table-wrapper"><table className="resource-table academic-resource-table"><thead><tr><th>{singular}</th><th>Last updated</th><th>Actions</th></tr></thead><tbody>{resources.map((resource) => <tr key={resource.id}><td><div className="resource-name"><span className="resource-monogram">{resource.name.slice(0, 2).toUpperCase()}</span><strong>{resource.name}</strong></div></td><td>{resource.updatedAt ? new Date(resource.updatedAt).toLocaleDateString() : "Not available"}</td><td><div className="row-actions"><button onClick={() => { setEditing(resource); setName(resource.name); }} type="button">Edit</button><button className="danger-text" onClick={() => setDeleting(resource)} type="button">Delete</button></div></td></tr>)}</tbody></table></div>}
    </section>
    {(creating || editing) && <ManagementModal title={`${editing ? "Edit" : "Create"} ${singular}`} description={`${editing ? "Update" : "Add"} this ${singular.toLowerCase()} in the current establishment.`} onClose={closeForm}><div className="management-form"><div className="form-field form-field--wide"><label htmlFor="named-resource-name">Name</label><input autoFocus id="named-resource-name" maxLength={255} onChange={(event) => { setName(event.target.value); setValidationError(null); }} onKeyDown={(event) => { if (event.key === "Enter") submit(); }} value={name} />{validationError && <p className="field-error">{validationError}</p>}</div>{formMutation.isError && <div className="management-alert management-alert--error">{errorMessage(formMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={closeForm} type="button">Cancel</button><button className="management-primary-button" disabled={formMutation.isPending} onClick={submit} type="button">{formMutation.isPending ? "Saving..." : "Save"}</button></footer></div></ManagementModal>}
    {deleting && <ConfirmActionModal actionLabel="Delete" destructive description={`Delete ${deleting.name}? This is only possible when no dependent academic records use it.`} error={deleteMutation.isError ? errorMessage(deleteMutation.error) : null} isSubmitting={deleteMutation.isPending} onCancel={() => setDeleting(null)} onConfirm={() => deleteMutation.mutate(deleting.id)} title={`Delete ${singular}`} />}
  </div>;
}
