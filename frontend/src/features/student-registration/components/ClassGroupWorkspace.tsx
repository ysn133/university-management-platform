import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useEffect, useState } from "react";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import {
  bulkAssignStudentClasses,
  classGroupKeys,
  createClassGroup,
  generateClassGroups,
  getClassGroupRoster,
  getClassGroups,
  rebalanceClassGroups,
} from "../api/class-group-api";
import type { AcademicRegistration, Student } from "../api/student-registration-api";

interface ClassGroupWorkspaceProps {
  academicLevelId: string;
  academicYearId: string;
  semesterId: string;
  registrations: AcademicRegistration[];
  students: Student[];
  onFilterChange: (registrationIds: Set<string> | null) => void;
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function balancedSizes(total: number, maximum: number): number[] {
  if (!total || maximum < 1) return [];
  const count = Math.ceil(total / maximum);
  const base = Math.floor(total / count);
  const remainder = total % count;
  return Array.from({ length: count }, (_, index) => base + (index < remainder ? 1 : 0));
}

function groupLabel(index: number): string {
  let value = index + 1;
  let label = "";
  while (value > 0) {
    value--;
    label = String.fromCharCode(65 + value % 26) + label;
    value = Math.floor(value / 26);
  }
  return label;
}

export function ClassGroupWorkspace({ academicLevelId, academicYearId, semesterId, registrations, students, onFilterChange }: ClassGroupWorkspaceProps) {
  const queryClient = useQueryClient();
  const [activeView, setActiveView] = useState("all");
  const [setupOpen, setSetupOpen] = useState(false);
  const [setupAction, setSetupAction] = useState<"create" | "rebalance">("create");
  const [assignmentOpen, setAssignmentOpen] = useState(false);
  const [setupMode, setSetupMode] = useState<"automatic" | "manual">("automatic");
  const [minimumSize, setMinimumSize] = useState("30");
  const [maximumSize, setMaximumSize] = useState("100");
  const [manualNames, setManualNames] = useState("Group A\nGroup B");
  const [assignmentSearch, setAssignmentSearch] = useState("");
  const [assignmentSelections, setAssignmentSelections] = useState<Record<string, string>>({});
  const deferredAssignmentSearch = useDeferredValue(assignmentSearch.trim().toLowerCase());

  const groupsQuery = useQuery({
    queryKey: classGroupKeys.groups(academicLevelId, academicYearId),
    queryFn: () => getClassGroups(academicLevelId, academicYearId),
  });
  const rosterQuery = useQuery({
    queryKey: classGroupKeys.roster(academicLevelId, academicYearId, semesterId),
    queryFn: () => getClassGroupRoster(academicLevelId, academicYearId, semesterId),
    enabled: Boolean(semesterId),
  });

  useEffect(() => {
    setActiveView("all");
    onFilterChange(null);
  }, [academicLevelId, academicYearId, semesterId, onFilterChange]);

  async function refreshGroups() {
    setActiveView("all");
    onFilterChange(null);
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: classGroupKeys.groups(academicLevelId, academicYearId) }),
      queryClient.invalidateQueries({ queryKey: ["class-group-roster", academicLevelId, academicYearId] }),
    ]);
  }

  const generationMutation = useMutation({
    mutationFn: () => generateClassGroups(academicLevelId, academicYearId, {
      minimumGroupSize: Number(minimumSize),
      maximumGroupSize: Number(maximumSize),
    }),
    onSuccess: async () => { await refreshGroups(); setSetupOpen(false); },
  });
  const rebalanceMutation = useMutation({
    mutationFn: () => rebalanceClassGroups(academicLevelId, academicYearId, {
      minimumGroupSize: Number(minimumSize),
      maximumGroupSize: Number(maximumSize),
    }),
    onSuccess: async () => { await refreshGroups(); setSetupOpen(false); },
  });
  const manualCreationMutation = useMutation({
    mutationFn: async () => {
      const names = manualNames.split(/\n|,/).map((name) => name.trim()).filter(Boolean);
      for (const name of names) await createClassGroup(academicLevelId, academicYearId, name);
    },
    onSuccess: async () => { await refreshGroups(); setSetupOpen(false); openAssignments(); },
  });
  const assignmentMutation = useMutation({
    mutationFn: ({ registrationId, classGroupId }: { registrationId: string; classGroupId: string }) => bulkAssignStudentClasses(academicLevelId, academicYearId, {
      assignments: [{ academicRegistrationId: registrationId, classGroupId }],
    }),
    onSuccess: async () => { await refreshGroups(); },
  });

  const groups = groupsQuery.data ?? [];
  const activeGroups = groups.filter((group) => group.status === "ACTIVE");
  const hasMultipleGroups = activeGroups.length > 1;
  const roster = rosterQuery.data;
  const hasUnassignedStudents = (roster?.unassignedAcademicRegistrationIds.length ?? 0) > 0;
  const studentById = new Map(students.map((student) => [student.studentId, student]));
  const currentGroupByRegistration = new Map<string, string>();
  roster?.groups.forEach((group) => group.academicRegistrationIds.forEach((registrationId) => currentGroupByRegistration.set(registrationId, group.classGroupId)));

  function selectView(view: string) {
    setActiveView(view);
    if (view === "all") onFilterChange(null);
    else if (view === "unassigned") onFilterChange(new Set(roster?.unassignedAcademicRegistrationIds ?? []));
    else onFilterChange(new Set(roster?.groups.find((group) => group.classGroupId === view)?.academicRegistrationIds ?? []));
  }

  function openAssignments() {
    setAssignmentSearch("");
    setAssignmentSelections({});
    assignmentMutation.reset();
    setAssignmentOpen(true);
  }

  const previewSizes = balancedSizes(registrations.length, Number(maximumSize));
  const setupError = generationMutation.error ?? rebalanceMutation.error ?? manualCreationMutation.error;
  const assignmentResults = deferredAssignmentSearch.length < 2 ? [] : registrations
    .map((registration) => ({ registration, student: studentById.get(registration.studentId) }))
    .filter(({ registration, student }) => [student?.firstName, student?.lastName, student?.apogeeCode, student?.nationalStudentCode, registration.id]
      .filter(Boolean)
      .join(" ")
      .toLowerCase()
      .includes(deferredAssignmentSearch))
    .slice(0, 20);

  return <>
    <div className="class-group-bar">
      <div className="class-group-bar__heading"><strong>Class groups</strong><span>{activeGroups.length === 1 ? "The cohort currently studies as one class" : activeGroups.length > 1 ? `${activeGroups.length} groups configured for this level` : groups.length ? "No active groups configured" : "Organize this cohort into teaching classes"}</span></div>
      <div className="class-group-bar__actions">
        {groups.length > 0 && <>{(hasMultipleGroups || hasUnassignedStudents) && <button className="secondary-button" disabled={!semesterId || rosterQuery.isPending} onClick={openAssignments} type="button">{hasMultipleGroups ? "Move or assign Student" : "Assign Student"}</button>}<button className="secondary-button" disabled={!registrations.length} onClick={() => { setSetupAction("rebalance"); setSetupMode("automatic"); setSetupOpen(true); }} type="button">Rebalance groups</button></>}
        {groups.length === 0 && <button className="secondary-button" disabled={!registrations.length} onClick={() => { setSetupAction("create"); setSetupOpen(true); setSetupMode("automatic"); }} type="button">Set up class groups</button>}
      </div>
    </div>
    {hasMultipleGroups && (!semesterId ? <div className="class-group-period-note">Select a semester to view its class-group roster.</div> : rosterQuery.isPending ? <div className="class-group-period-note">Loading class groups...</div> : rosterQuery.isError ? <div className="class-group-period-note class-group-period-note--error">{errorMessage(rosterQuery.error)}</div> : <div className="class-group-tabs" role="tablist" aria-label="Class group filter">
      <button aria-selected={activeView === "all"} className={activeView === "all" ? "is-active" : ""} onClick={() => selectView("all")} role="tab" type="button">All <span>{roster?.totalStudents ?? 0}</span></button>
      <button aria-selected={activeView === "unassigned"} className={activeView === "unassigned" ? "is-active" : ""} onClick={() => selectView("unassigned")} role="tab" type="button">Unassigned <span>{roster?.unassignedAcademicRegistrationIds.length ?? 0}</span></button>
      {roster?.groups.map((group) => <button aria-selected={activeView === group.classGroupId} className={activeView === group.classGroupId ? "is-active" : ""} key={group.classGroupId} onClick={() => selectView(group.classGroupId)} role="tab" type="button">{group.name} <span>{group.academicRegistrationIds.length}</span></button>)}
    </div>)}

    {setupOpen && <ManagementModal title={setupAction === "rebalance" ? "Rebalance class groups" : "Set up class groups"} description={`${registrations.length} active Students in this academic level.`} onClose={() => setSetupOpen(false)}><div className="management-form class-group-setup">
      {setupAction === "create" && <div className="class-group-mode-switch"><button className={setupMode === "automatic" ? "is-active" : ""} onClick={() => setSetupMode("automatic")} type="button"><strong>Automatic balanced</strong><span>Generate evenly sized groups and assign every Student.</span></button><button className={setupMode === "manual" ? "is-active" : ""} onClick={() => setSetupMode("manual")} type="button"><strong>Manual</strong><span>Create the groups first, then assign Students.</span></button></div>}
      {setupAction === "rebalance" || setupMode === "automatic" ? <>
        <div className="management-form management-form--two-columns class-group-size-fields"><div className="form-field"><label htmlFor="minimum-group-size">Minimum group size</label><input id="minimum-group-size" min="1" onChange={(event) => setMinimumSize(event.target.value)} type="number" value={minimumSize} /></div><div className="form-field"><label htmlFor="maximum-group-size">Maximum group size</label><input id="maximum-group-size" min="1" onChange={(event) => setMaximumSize(event.target.value)} type="number" value={maximumSize} /></div></div>
        <div className="class-group-preview"><span>Distribution preview</span><strong>{previewSizes.length === 1 ? `Whole class: ${previewSizes[0]}` : previewSizes.length > 1 ? previewSizes.map((size, index) => `Group ${groupLabel(index)}: ${size}`).join(" · ") : "No Students to assign"}</strong><small>{setupAction === "rebalance" ? "Existing assignments will be rearranged across both standard semesters." : "The same class assignment is applied to both standard semesters."}</small></div>
      </> : <div className="form-field form-field--wide"><label htmlFor="manual-group-names">Group names</label><textarea id="manual-group-names" onChange={(event) => setManualNames(event.target.value)} rows={5} value={manualNames} /><small>Enter one name per line. Student assignment opens after the groups are created.</small></div>}
      {setupError && <div className="management-alert management-alert--error">{errorMessage(setupError)}</div>}
      <footer className="form-actions"><button className="secondary-button" onClick={() => setSetupOpen(false)} type="button">Cancel</button><button className="management-primary-button" disabled={generationMutation.isPending || rebalanceMutation.isPending || manualCreationMutation.isPending || (setupAction === "create" && setupMode === "manual" && !manualNames.trim())} onClick={() => setupAction === "rebalance" ? rebalanceMutation.mutate() : setupMode === "automatic" ? generationMutation.mutate() : manualCreationMutation.mutate()} type="button">{generationMutation.isPending || rebalanceMutation.isPending || manualCreationMutation.isPending ? "Saving..." : setupAction === "rebalance" ? "Rebalance groups" : setupMode === "automatic" ? "Generate groups" : "Create groups"}</button></footer>
    </div></ManagementModal>}

    {assignmentOpen && <ManagementModal size="wide" title="Move or assign Student" description="Search the annual cohort and choose the Student's class group." onClose={() => setAssignmentOpen(false)}><div className="management-form class-group-assignment-form">
      <div className="form-field form-field--wide"><label htmlFor="class-group-student-search">Search Student</label><input autoFocus id="class-group-student-search" onChange={(event) => setAssignmentSearch(event.target.value)} placeholder="Name, Apogee code, or national code" value={assignmentSearch} /></div>
      {deferredAssignmentSearch.length < 2 ? <div className="class-group-search-empty">Enter at least two characters to search the cohort.</div> : assignmentResults.length === 0 ? <div className="class-group-search-empty">No Student matches this search.</div> : <div className="class-group-assignment-list">{assignmentResults.map(({ registration, student }) => { const currentGroupId = currentGroupByRegistration.get(registration.id) ?? ""; const targetGroupId = (assignmentSelections[registration.id] ?? currentGroupId) || (activeGroups.length === 1 ? activeGroups[0].id : ""); const isPending = assignmentMutation.isPending && assignmentMutation.variables?.registrationId === registration.id; return <div className="class-group-assignment-row" key={registration.id}><span><strong>{student ? `${student.firstName} ${student.lastName}` : "Student"}</strong><small>{student?.apogeeCode ?? registration.id}{currentGroupId ? hasMultipleGroups ? ` · ${groups.find((group) => group.id === currentGroupId)?.name ?? "Assigned"}` : " · Assigned" : " · Unassigned"}</small></span>{hasMultipleGroups ? <select onChange={(event) => setAssignmentSelections({ ...assignmentSelections, [registration.id]: event.target.value })} value={targetGroupId}><option value="">Select group</option>{activeGroups.map((group) => <option key={group.id} value={group.id}>{group.name}</option>)}</select> : <span className="class-group-assignment-target">Whole class</span>}<button className="secondary-button secondary-button--compact" disabled={!targetGroupId || targetGroupId === currentGroupId || isPending} onClick={() => assignmentMutation.mutate({ registrationId: registration.id, classGroupId: targetGroupId })} type="button">{isPending ? "Assigning..." : currentGroupId ? "Move" : "Assign"}</button></div>; })}</div>}
      {assignmentMutation.isError && <div className="management-alert management-alert--error">{errorMessage(assignmentMutation.error)}</div>}
      <footer className="form-actions"><button className="secondary-button" onClick={() => setAssignmentOpen(false)} type="button">Close</button></footer>
    </div></ManagementModal>}
  </>;
}
