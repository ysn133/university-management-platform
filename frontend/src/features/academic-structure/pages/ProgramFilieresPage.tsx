import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useState } from "react";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { academicStructureKeys, createProgramFiliere, deleteProgramFiliere, getDegreeCycles, getDepartments, getProgramFilieres, getProgramPaths, updateProgramFiliere, type ProgramFiliere } from "../api/academic-structure-api";

interface ProgramFormValues { departmentId: string; code: string; name: string; degreeCycleId: string; programPathId: string; }
const emptyForm: ProgramFormValues = { departmentId: "", code: "", name: "", degreeCycleId: "", programPathId: "" };
function errorMessage(error: unknown): string { return error instanceof ApiRequestError ? error.message : "The request could not be completed."; }

export function ProgramFilieresPage() {
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [departmentId, setDepartmentId] = useState("");
  const [programPathId, setProgramPathId] = useState("");
  const [degreeCycleId, setDegreeCycleId] = useState("");
  const [search, setSearch] = useState("");
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<ProgramFiliere | null>(null);
  const [deleting, setDeleting] = useState<ProgramFiliere | null>(null);
  const [form, setForm] = useState<ProgramFormValues>(emptyForm);
  const [validationError, setValidationError] = useState<string | null>(null);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const departmentsQuery = useQuery({ queryKey: academicStructureKeys.departments(establishmentId ?? "missing"), queryFn: () => getDepartments(establishmentId!), enabled: Boolean(establishmentId) });
  const pathsQuery = useQuery({ queryKey: academicStructureKeys.programPaths(establishmentId ?? "missing"), queryFn: () => getProgramPaths(establishmentId!), enabled: Boolean(establishmentId) });
  const cyclesQuery = useQuery({ queryKey: academicStructureKeys.degreeCycles(establishmentId ?? "missing"), queryFn: () => getDegreeCycles(establishmentId!), enabled: Boolean(establishmentId) });
  const programsQuery = useQuery({
    queryKey: ["academic-structure", "program-filieres", "directory", establishmentId, departmentId || "all"],
    queryFn: async () => {
      if (departmentId) return getProgramFilieres(departmentId);
      const programsByDepartment = await Promise.all(
        (departmentsQuery.data ?? []).map((department) => getProgramFilieres(department.id)),
      );
      return programsByDepartment.flat();
    },
    enabled: Boolean(departmentId || departmentsQuery.data?.length),
  });

  const programs = (programsQuery.data ?? []).filter((program) => {
    const matchesSearch = `${program.code} ${program.name}`.toLowerCase().includes(deferredSearch);
    const matchesPath = !programPathId || program.programPathId === programPathId;
    const matchesCycle = !degreeCycleId || program.degreeCycleId === degreeCycleId;
    return matchesSearch && matchesPath && matchesCycle;
  });
  const selectedDepartment = departmentsQuery.data?.find((item) => item.id === departmentId);
  const departmentNames = new Map(departmentsQuery.data?.map((item) => [item.id, item.name]));
  const pathNames = new Map(pathsQuery.data?.map((item) => [item.id, item.name]));
  const cycleNames = new Map(cyclesQuery.data?.map((item) => [item.id, item.name]));

  async function refresh() { await queryClient.invalidateQueries({ queryKey: ["academic-structure", "program-filieres"] }); }
  function closeForm() { setCreating(false); setEditing(null); setForm(emptyForm); setValidationError(null); createMutation.reset(); updateMutation.reset(); }
  const createMutation = useMutation({
    mutationFn: () => createProgramFiliere(form.departmentId, { code: form.code, name: form.name, degreeCycleId: form.degreeCycleId, programPathId: form.programPathId }),
    onSuccess: async () => {
      await refresh();
      closeForm();
    },
  });
  const updateMutation = useMutation({ mutationFn: () => updateProgramFiliere(editing!.id, { code: form.code, name: form.name, degreeCycleId: form.degreeCycleId, programPathId: form.programPathId }), onSuccess: async () => { await refresh(); closeForm(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => deleteProgramFiliere(id), onSuccess: async () => { await refresh(); setDeleting(null); } });

  function openCreate() {
    setForm({ departmentId: departmentId || departmentsQuery.data?.[0]?.id || "", code: "", name: "", degreeCycleId: degreeCycleId || cyclesQuery.data?.[0]?.id || "", programPathId: programPathId || pathsQuery.data?.[0]?.id || "" });
    setCreating(true);
  }
  function openEdit(program: ProgramFiliere) {
    setEditing(program);
    setForm({ departmentId: program.departmentId, code: program.code, name: program.name, degreeCycleId: program.degreeCycleId, programPathId: program.programPathId });
  }
  function updateField(field: keyof ProgramFormValues, value: string) { setForm((current) => ({ ...current, [field]: value })); setValidationError(null); }
  function submit() {
    if (!form.departmentId || !form.code.trim() || !form.name.trim() || !form.degreeCycleId || !form.programPathId) { setValidationError("Complete all program fields before saving."); return; }
    if (editing) updateMutation.mutate(); else createMutation.mutate();
  }

  if (!establishmentId || !workspacePath) return <div className="management-state management-state--error"><h1>No establishment assigned</h1></div>;
  const catalogError = departmentsQuery.error ?? pathsQuery.error ?? cyclesQuery.error;
  const catalogsPending = departmentsQuery.isPending || pathsQuery.isPending || cyclesQuery.isPending;
  const canCreate = Boolean(departmentsQuery.data?.length && pathsQuery.data?.length && cyclesQuery.data?.length);
  const formMutation = editing ? updateMutation : createMutation;

  return <div className="management-page academic-directory-page program-directory-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Academic structure</p><h1>Programs / Filières</h1><p>Define each department's programs and classify them by degree cycle and study path.</p></div><button className="management-primary-button" disabled={!canCreate} onClick={openCreate} type="button">New Program</button></header>
    {catalogError && <div className="management-alert management-alert--error">{errorMessage(catalogError)}</div>}
    <section className="directory-toolbar program-directory-toolbar"><label><span>Department</span><select disabled={catalogsPending || !departmentsQuery.data?.length} onChange={(event) => setDepartmentId(event.target.value)} value={departmentId}><option value="">All departments</option>{departmentsQuery.data?.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}</select></label><label><span>Program path</span><select disabled={pathsQuery.isPending || !pathsQuery.data?.length} onChange={(event) => setProgramPathId(event.target.value)} value={programPathId}><option value="">All paths</option>{pathsQuery.data?.map((path) => <option key={path.id} value={path.id}>{path.name}</option>)}</select></label><label><span>Degree cycle</span><select disabled={cyclesQuery.isPending || !cyclesQuery.data?.length} onChange={(event) => setDegreeCycleId(event.target.value)} value={degreeCycleId}><option value="">All cycles</option>{cyclesQuery.data?.map((cycle) => <option key={cycle.id} value={cycle.id}>{cycle.name}</option>)}</select></label><label className="search-field"><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Program name or code" value={search} /></label><span className="directory-result-count">{programs.length} {programs.length === 1 ? "program" : "programs"}</span></section>
    <section className="management-panel directory-panel">{!departmentsQuery.isPending && !departmentsQuery.data?.length ? <div className="panel-empty"><strong>A department is required first.</strong><p>Create a department before adding programs.</p></div> : programsQuery.isPending ? <div className="panel-empty">Loading programs...</div> : programsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(programsQuery.error)}</div> : programs.length === 0 ? <div className="panel-empty"><strong>No program found {selectedDepartment ? `in ${selectedDepartment.name}` : "in this establishment"}.</strong><p>{programPathId || degreeCycleId || search ? "Adjust the current filters or create a matching program." : "Create the first program to continue the academic structure."}</p></div> : <div className="resource-table-wrapper"><table className="resource-table academic-resource-table program-resource-table"><thead><tr><th>Program</th><th>Department</th><th>Degree cycle</th><th>Program path</th><th>Actions</th></tr></thead><tbody>{programs.map((program) => <tr key={program.id}><td><div className="resource-name"><span className="resource-monogram">{program.code.slice(0, 2).toUpperCase()}</span><div><strong>{program.name}</strong><small>{program.code}</small></div></div></td><td>{departmentNames.get(program.departmentId) ?? "Unknown department"}</td><td>{cycleNames.get(program.degreeCycleId) ?? "Unknown cycle"}</td><td>{pathNames.get(program.programPathId) ?? "Unknown path"}</td><td><div className="row-actions"><button onClick={() => openEdit(program)} type="button">Edit</button><button className="danger-text" onClick={() => setDeleting(program)} type="button">Delete</button></div></td></tr>)}</tbody></table></div>}</section>
    {!canCreate && !catalogsPending && departmentsQuery.data?.length ? <div className="academic-prerequisite-note"><strong>Program setup is incomplete.</strong><span>Add at least one degree cycle and one program path before creating a program.</span></div> : null}
    {(creating || editing) && <ManagementModal title={`${editing ? "Edit" : "Create"} Program`} description="Connect the program to its academic structure." onClose={closeForm}><div className="management-form management-form--two-columns"><div className="form-field form-field--wide"><label htmlFor="program-department">Department</label><select disabled={Boolean(editing)} id="program-department" onChange={(event) => updateField("departmentId", event.target.value)} value={form.departmentId}>{departmentsQuery.data?.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}</select></div><div className="form-field"><label htmlFor="program-code">Code</label><input autoFocus id="program-code" maxLength={100} onChange={(event) => updateField("code", event.target.value)} placeholder="IL" value={form.code} /></div><div className="form-field"><label htmlFor="program-name">Name</label><input id="program-name" maxLength={255} onChange={(event) => updateField("name", event.target.value)} placeholder="Software Engineering" value={form.name} /></div><div className="form-field"><label htmlFor="program-cycle">Degree cycle</label><select id="program-cycle" onChange={(event) => updateField("degreeCycleId", event.target.value)} value={form.degreeCycleId}>{cyclesQuery.data?.map((cycle) => <option key={cycle.id} value={cycle.id}>{cycle.name}</option>)}</select></div><div className="form-field"><label htmlFor="program-path">Program path</label><select id="program-path" onChange={(event) => updateField("programPathId", event.target.value)} value={form.programPathId}>{pathsQuery.data?.map((path) => <option key={path.id} value={path.id}>{path.name}</option>)}</select></div>{validationError && <div className="management-alert management-alert--error">{validationError}</div>}{formMutation.isError && <div className="management-alert management-alert--error">{errorMessage(formMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={closeForm} type="button">Cancel</button><button className="management-primary-button" disabled={formMutation.isPending} onClick={submit} type="button">{formMutation.isPending ? "Saving..." : "Save"}</button></footer></div></ManagementModal>}
    {deleting && <ConfirmActionModal actionLabel="Delete" destructive description={`Delete ${deleting.name}? This is only possible when no academic records depend on it.`} error={deleteMutation.isError ? errorMessage(deleteMutation.error) : null} isSubmitting={deleteMutation.isPending} onCancel={() => setDeleting(null)} onConfirm={() => deleteMutation.mutate(deleting.id)} title="Delete Program" />}
  </div>;
}
