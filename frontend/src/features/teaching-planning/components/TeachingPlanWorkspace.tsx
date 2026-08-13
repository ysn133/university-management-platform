import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { academicStructureKeys, getModuleTeachingComponents, type Semester, type SubjectModule } from "@/features/academic-structure/api/academic-structure-api";
import { getProfessors, professorManagementKeys } from "@/features/professor-management/api/professor-management-api";
import { getTeachingGroups, teachingGroupKeys } from "@/features/student-registration/api/teaching-group-api";
import { assignProfessor, clearTeachingAssignments, generateTeachingAssignments, generateTeachingPlan, getTeachingAssignments, getTeachingPlan, teachingPlanKeys, type TeachingAssignment, type TeachingAssignmentGeneration, type TeachingPlanItem, unassignProfessor } from "../api/teaching-plan-api";

interface TeachingPlanWorkspaceProps {
  academicLevelName?: string;
  academicYearLabel?: string;
  establishmentId: string;
  semesterId: string;
  semesterName?: string;
  semesters: Semester[];
  modules: SubjectModule[];
  onSelectSemester: (semesterId: string) => void;
}

interface TeachingPlanChildSection {
  id: string;
  name: string;
  type: string;
  items: TeachingPlanItem[];
}

interface TeachingPlanClassSection {
  id: string;
  name: string;
  classItems: TeachingPlanItem[];
  subgroups: Map<string, TeachingPlanChildSection>;
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function TeachingPlanWorkspace({ academicLevelName, academicYearLabel, establishmentId, semesterId, semesterName, semesters, modules, onSelectSemester }: TeachingPlanWorkspaceProps) {
  const location = useLocation();
  const { workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [selectedClassId, setSelectedClassId] = useState("all");
  const [selectedAudienceId, setSelectedAudienceId] = useState("all");
  const [selectedComponentType, setSelectedComponentType] = useState<"ALL" | "COURSE" | "TD" | "TP">("ALL");
  const [generationResult, setGenerationResult] = useState<TeachingAssignmentGeneration | null>(null);
  const [confirmingClear, setConfirmingClear] = useState(false);
  const [assignmentTarget, setAssignmentTarget] = useState<TeachingPlanItem | null>(null);
  const [unassignmentTarget, setUnassignmentTarget] = useState<TeachingAssignment | null>(null);
  const [professorSearch, setProfessorSearch] = useState("");
  const returnSearchParams = new URLSearchParams(location.search);
  returnSearchParams.set("section", "teaching-plan");
  const teachingPlanReturnPath = `${location.pathname}?${returnSearchParams.toString()}`;
  const planQuery = useQuery({ queryKey: teachingPlanKeys.semester(semesterId || "missing"), queryFn: () => getTeachingPlan(semesterId), enabled: Boolean(semesterId) });
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.assignments(establishmentId), queryFn: () => getTeachingAssignments(establishmentId), enabled: Boolean(establishmentId) });
  const professorsQuery = useQuery({ queryKey: professorManagementKeys.professors(establishmentId), queryFn: () => getProfessors(establishmentId), enabled: Boolean(establishmentId) });
  const teachingGroupsQuery = useQuery({ queryKey: teachingGroupKeys.roster(semesterId || "missing"), queryFn: () => getTeachingGroups(semesterId), enabled: Boolean(semesterId) });
  const componentQueries = useQueries({ queries: modules.map((module) => ({ queryKey: academicStructureKeys.moduleTeachingComponents(module.id), queryFn: () => getModuleTeachingComponents(module.id), enabled: Boolean(semesterId) })) });
  const moduleById = new Map(modules.map((module) => [module.id, module]));
  const componentById = new Map(componentQueries.flatMap((query) => query.data ?? []).map((component) => [component.id, component]));
  const plan = planQuery.data ?? [];
  const activeAssignmentByRequirement = new Map((assignmentsQuery.data ?? []).filter((assignment) => assignment.status === "ACTIVE").map((assignment) => [assignment.teachingRequirementId, assignment]));
  const professorById = new Map((professorsQuery.data ?? []).map((professor) => [professor.professorId, professor]));
  const selectableProfessors = (professorsQuery.data ?? []).filter((professor) => professor.accountStatus === "ACTIVE" && `${professor.firstName} ${professor.lastName} ${professor.employeeNumber} ${professor.universityEmail}`.toLowerCase().includes(professorSearch.trim().toLowerCase()));
  const assignedCount = plan.filter((item) => activeAssignmentByRequirement.has(item.id)).length;
  const activeCount = plan.filter((item) => item.status === "ACTIVE").length;
  const subgroupById = new Map((teachingGroupsQuery.data?.groups ?? []).map((group) => [group.id, group]));
  const sourceClassByName = new Map((teachingGroupsQuery.data?.groups ?? []).map((group) => [group.sourceClassGroupName, { sourceClassGroupId: group.sourceClassGroupId, sourceClassGroupName: group.sourceClassGroupName }]));
  const wholeCohortItems = plan.filter((item) => item.audienceType === "WHOLE_COHORT");
  const classSections = new Map<string, TeachingPlanClassSection>();
  for (const item of plan.filter((entry) => entry.audienceType !== "WHOLE_COHORT")) {
    const subgroup = subgroupById.get(item.teachingGroupId);
    const sourceClass = subgroup ?? sourceClassByName.get(item.teachingGroupName);
    const classId = sourceClass?.sourceClassGroupId ?? item.teachingGroupId;
    const className = sourceClass?.sourceClassGroupName ?? item.teachingGroupName;
    const section: TeachingPlanClassSection = classSections.get(classId) ?? { id: classId, name: className, classItems: [], subgroups: new Map() };
    if (item.audienceType === "CLASS_GROUP") {
      section.classItems.push(item);
    } else {
      const child: TeachingPlanChildSection = section.subgroups.get(item.teachingGroupId) ?? { id: item.teachingGroupId, name: item.teachingGroupName, type: item.componentType, items: [] };
      child.items.push(item);
      section.subgroups.set(item.teachingGroupId, child);
    }
    classSections.set(classId, section);
  }
  const sortedClassSections = [...classSections.values()].sort((left, right) => left.name.localeCompare(right.name));
  const onlyClassSection = sortedClassSections[0];
  const subgroupCountsByType = new Map<string, number>();
  for (const subgroup of onlyClassSection?.subgroups.values() ?? []) {
    subgroupCountsByType.set(subgroup.type, (subgroupCountsByType.get(subgroup.type) ?? 0) + 1);
  }
  const compactSingleClass = sortedClassSections.length === 1
    && [...subgroupCountsByType.values()].every((count) => count <= 1);
  const selectedClass = classSections.get(selectedClassId);
  const selectedSubgroup = selectedClass?.subgroups.get(selectedAudienceId);
  const selectedClassTotal = (selectedClass?.classItems.length ?? 0) + [...(selectedClass?.subgroups.values() ?? [])].reduce((total, child) => total + child.items.length, 0);
  const scopedItems = selectedClassId === "all"
    ? selectedAudienceId === "cohort" ? wholeCohortItems : plan
    : selectedAudienceId === "class"
    ? selectedClass?.classItems ?? []
    : selectedAudienceId === "all"
    ? [...(selectedClass?.classItems ?? []), ...[...(selectedClass?.subgroups.values() ?? [])].flatMap((child) => child.items)]
    : selectedSubgroup?.items ?? [];
  const componentOrder = ["COURSE", "TD", "TP"] as const;
  const availableComponentTypes = componentOrder.filter((type) => scopedItems.some((item) => item.componentType === type));
  const effectiveComponentType = selectedComponentType === "ALL" || availableComponentTypes.includes(selectedComponentType)
    ? selectedComponentType
    : "ALL";
  const visibleItems = effectiveComponentType === "ALL"
    ? scopedItems
    : scopedItems.filter((item) => item.componentType === effectiveComponentType);
  const visibleLabel = selectedClassId === "all"
    ? selectedAudienceId === "cohort" ? "Whole Cohort" : "All Delivery"
    : selectedAudienceId === "class" ? `${selectedClass?.name} · Whole Class`
    : selectedAudienceId === "all" ? `All in ${selectedClass?.name}`
    : selectedSubgroup?.name ?? selectedClass?.name ?? "Teaching Plan";

  function planTable(items: TeachingPlanItem[], showGrouping = true) {
    return <div className="resource-table-wrapper"><table className="resource-table teaching-plan-table"><thead><tr><th>Module</th><th>Component</th>{showGrouping && <><th>Audience</th><th>Group</th></>}<th>Weekly delivery</th><th>Professor</th><th>Status</th><th>Action</th></tr></thead><tbody>{items.map((item) => {
      const module = moduleById.get(item.subjectModuleId);
      const component = componentById.get(item.moduleTeachingComponentId);
      const assignment = activeAssignmentByRequirement.get(item.id);
      const professor = assignment ? professorById.get(assignment.professorId) : undefined;
      return <tr className={assignment ? undefined : "teaching-plan-row--unassigned"} key={item.id}><td><div className="table-contact"><span>{module?.title ?? "Subject Module"}</span><small>{module?.code ?? "Module"}</small></div></td><td><span className={`teaching-component-badge teaching-component-badge--${item.componentType.toLowerCase()}`}>{item.componentType === "COURSE" ? "Course" : item.componentType}</span></td>{showGrouping && <><td>{item.audienceType === "WHOLE_COHORT" ? "Whole cohort" : item.audienceType === "CLASS_GROUP" ? "Whole class" : "Subgroup"}</td><td>{item.teachingGroupName}</td></>}<td>{component ? `${component.sessionsPerWeek} × ${component.sessionDurationMinutes} min` : "Loading configuration..."}</td><td>{assignment ? <div className="teaching-plan-professor">{professor && workspacePath ? <Link className="record-name-link" state={{ returnTo: teachingPlanReturnPath, returnLabel: "Back to Teaching Plan" }} to={`${workspacePath}/professors/${professor.professorId}`}>{professor.firstName} {professor.lastName}</Link> : <strong>Assigned professor</strong>}<small>{item.componentType === "COURSE" ? "Module responsible" : assignment.assignmentSource === "AUTOMATIC" ? "Automatically assigned" : "Manually assigned"}</small></div> : <span className="teaching-plan-unassigned">Not assigned</span>}</td><td><span className={`status-badge status-badge--${assignment ? "active" : "inactive"}`}>{assignment ? "Assigned" : "Unassigned"}</span></td><td><div className="row-actions">{assignment ? <button className="danger-text" onClick={() => setUnassignmentTarget(assignment)} type="button">Unassign</button> : <button onClick={() => { setProfessorSearch(""); setAssignmentTarget(item); }} type="button">Assign</button>}</div></td></tr>;
    })}</tbody></table></div>;
  }

  const generateMutation = useMutation({
    mutationFn: () => generateTeachingPlan(semesterId),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: teachingPlanKeys.semester(semesterId) }); },
  });

  const assignmentMutation = useMutation({
    mutationFn: () => generateTeachingAssignments(semesterId),
    onSuccess: async (result) => {
      setGenerationResult(result);
      await queryClient.invalidateQueries({ queryKey: teachingPlanKeys.assignments(establishmentId) });
    },
  });

  const clearMutation = useMutation({
    mutationFn: () => clearTeachingAssignments(semesterId),
    onSuccess: async () => {
      setConfirmingClear(false);
      setGenerationResult(null);
      await queryClient.invalidateQueries({ queryKey: teachingPlanKeys.assignments(establishmentId) });
    },
  });

  const manualAssignmentMutation = useMutation({
    mutationFn: (professorId: string) => assignProfessor(establishmentId, professorId, assignmentTarget!.id),
    onSuccess: async () => {
      setAssignmentTarget(null);
      setProfessorSearch("");
      await queryClient.invalidateQueries({ queryKey: teachingPlanKeys.assignments(establishmentId) });
    },
  });

  const unassignMutation = useMutation({
    mutationFn: () => unassignProfessor(unassignmentTarget!.id),
    onSuccess: async () => {
      setUnassignmentTarget(null);
      await queryClient.invalidateQueries({ queryKey: teachingPlanKeys.assignments(establishmentId) });
    },
  });

  if (!semesterId) return <section className="management-panel teaching-plan-panel"><div className="panel-empty"><strong>Select a semester.</strong><p>The Teaching Plan is generated for one semester at a time.</p></div></section>;

  return <section className="management-panel teaching-plan-panel">
    <div className="teaching-plan-context"><label><span>Semester</span><select onChange={(event) => { setSelectedClassId("all"); setSelectedAudienceId("all"); setSelectedComponentType("ALL"); onSelectSemester(event.target.value); }} value={semesterId}>{semesters.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label><p>{academicLevelName} · {academicYearLabel}</p></div>
    <header className="panel-header panel-header--bordered"><div><p className="management-kicker">{academicYearLabel ?? "Academic year"} · {academicLevelName ?? "Academic level"}</p><h2>{semesterName ? `${semesterName} Teaching Plan` : "Teaching Plan"}</h2><p>Review the Course, TD, and TP sessions that must be assigned and scheduled.</p></div><div className="teaching-plan-header-actions"><button className="management-secondary-button" disabled={generateMutation.isPending || modules.length === 0} onClick={() => generateMutation.mutate()} type="button">{generateMutation.isPending ? "Generating plan..." : plan.length ? "Regenerate plan" : "Generate plan"}</button>{assignedCount > 0 && <button className="danger-ghost-button" onClick={() => setConfirmingClear(true)} type="button">Clear assignments</button>}<button className="management-primary-button" disabled={!plan.length || assignmentMutation.isPending || assignedCount === plan.length} onClick={() => assignmentMutation.mutate()} type="button">{assignmentMutation.isPending ? "Assigning professors..." : "Generate assignments"}</button></div></header>
    {generateMutation.isError && <div className="management-alert management-alert--error teaching-plan-alert">{errorMessage(generateMutation.error)}</div>}
    {assignmentMutation.isError && <div className="management-alert management-alert--error teaching-plan-alert">{errorMessage(assignmentMutation.error)}</div>}
    {generationResult && <div className={`teaching-assignment-result ${generationResult.unresolvedRequirements.length ? "has-unresolved" : "is-complete"}`}><strong>{generationResult.createdAssignments.length} assigned</strong><span>{generationResult.preservedAssignmentCount} existing preserved</span><span>{generationResult.unresolvedRequirements.length} unresolved</span>{generationResult.unresolvedRequirements.length > 0 && <small>Review the highlighted unassigned requirements and complete them manually.</small>}</div>}
    {planQuery.isPending || teachingGroupsQuery.isPending || assignmentsQuery.isPending || professorsQuery.isPending ? <div className="panel-empty">Loading Teaching Plan...</div> : planQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(planQuery.error)}</div> : teachingGroupsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(teachingGroupsQuery.error)}</div> : assignmentsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(assignmentsQuery.error)}</div> : professorsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(professorsQuery.error)}</div> : plan.length === 0 ? <div className="panel-empty"><strong>No Teaching Plan generated.</strong><p>Configure module teaching components and Teaching Groups, then generate the plan.</p></div> : <><div className="teaching-plan-summary"><span><strong>{activeCount}</strong> active teaching sessions</span><span><strong>{assignedCount}</strong> assigned</span><span><strong>{plan.length - assignedCount}</strong> unassigned</span>{!compactSingleClass && <span><strong>{classSections.size}</strong> Class Groups</span>}<span><strong>{modules.length}</strong> curriculum modules</span></div>
      {!compactSingleClass && <><nav aria-label="Teaching Plan Class Groups" className="teaching-plan-parent-tabs"><button aria-selected={selectedClassId === "all"} onClick={() => { setSelectedClassId("all"); setSelectedAudienceId("all"); }} type="button">All Classes <span>{plan.length}</span></button>{sortedClassSections.map((section) => <button aria-selected={selectedClassId === section.id} key={section.id} onClick={() => { setSelectedClassId(section.id); setSelectedAudienceId("all"); }} type="button">{section.name} <span>{section.classItems.length + [...section.subgroups.values()].reduce((total, child) => total + child.items.length, 0)}</span></button>)}</nav>
      <nav aria-label="Teaching Plan audiences" className="teaching-plan-child-tabs"><button aria-selected={selectedAudienceId === "all"} onClick={() => setSelectedAudienceId("all")} type="button">{selectedClassId === "all" ? "All Delivery" : `All in ${selectedClass?.name}`} <span>{selectedClassId === "all" ? plan.length : selectedClassTotal}</span></button>{selectedClassId === "all" ? <button aria-selected={selectedAudienceId === "cohort"} onClick={() => setSelectedAudienceId("cohort")} type="button">Whole Cohort <span>{wholeCohortItems.length}</span></button> : <><button aria-selected={selectedAudienceId === "class"} onClick={() => setSelectedAudienceId("class")} type="button">Whole Class <span>{selectedClass?.classItems.length ?? 0}</span></button>{[...(selectedClass?.subgroups.values() ?? [])].sort((left, right) => left.name.localeCompare(right.name)).map((child) => <button aria-selected={selectedAudienceId === child.id} key={child.id} onClick={() => setSelectedAudienceId(child.id)} type="button"><span className={`teaching-plan-tab-type teaching-plan-tab-type--${child.type.toLowerCase()}`}>{child.type}</span>{child.name} <span>{child.items.length}</span></button>)}</>}</nav></>}
      <div className="teaching-plan-selection"><header><div><span>{compactSingleClass ? "Class delivery" : selectedClassId === "all" ? "Program delivery" : `Class Group · ${selectedClass?.name}`}</span><h3>{compactSingleClass ? `${academicLevelName ?? "Academic"} Class` : visibleLabel}</h3></div><small>{visibleItems.length} {visibleItems.length === 1 ? "planned session" : "planned sessions"}</small></header><nav aria-label="Teaching component filter" className="teaching-plan-component-filter"><span>Component</span><button aria-pressed={effectiveComponentType === "ALL"} onClick={() => setSelectedComponentType("ALL")} type="button">All Components <small>{scopedItems.length}</small></button>{availableComponentTypes.map((type) => <button aria-pressed={effectiveComponentType === type} key={type} onClick={() => setSelectedComponentType(type)} type="button">{type === "COURSE" ? "Course" : type}<small>{scopedItems.filter((item) => item.componentType === type).length}</small></button>)}</nav>{visibleItems.length ? planTable(visibleItems, !compactSingleClass) : <div className="panel-empty"><strong>No delivery configured for this selection.</strong></div>}</div>
    </>}
    {confirmingClear && <ConfirmActionModal actionLabel="Clear assignments" destructive description={`Clear all ${assignedCount} active Professor assignments from ${semesterName ?? "this semester"}? Assignment history will be retained.`} error={clearMutation.isError ? errorMessage(clearMutation.error) : null} isSubmitting={clearMutation.isPending} onCancel={() => setConfirmingClear(false)} onConfirm={() => clearMutation.mutate()} title="Clear Teaching Assignments" />}
    {assignmentTarget && <ManagementModal description="Select an active Professor for this teaching requirement." onClose={() => { setAssignmentTarget(null); setProfessorSearch(""); manualAssignmentMutation.reset(); }} title="Assign Professor"><div className="teaching-professor-picker"><div className="form-field"><label htmlFor="teaching-professor-search">Search Professors</label><input autoFocus id="teaching-professor-search" onChange={(event) => setProfessorSearch(event.target.value)} placeholder="Name, employee number, or email" value={professorSearch} /></div>{manualAssignmentMutation.isError && <div className="management-alert management-alert--error">{errorMessage(manualAssignmentMutation.error)}</div>}<div className="teaching-professor-picker-list">{selectableProfessors.length ? selectableProfessors.map((professor) => <button disabled={manualAssignmentMutation.isPending} key={professor.professorId} onClick={() => manualAssignmentMutation.mutate(professor.professorId)} type="button"><span className="person-monogram">{professor.firstName[0]}{professor.lastName[0]}</span><span><strong>{professor.firstName} {professor.lastName}</strong><small>{professor.academicRank || "Rank not specified"} · {professor.employeeNumber}</small></span><span>{Math.round(professor.maximumWeeklyTeachingMinutes / 60 * 10) / 10}h max</span></button>) : <div className="panel-empty"><strong>No active Professor matches this search.</strong></div>}</div></div></ManagementModal>}
    {unassignmentTarget && <ConfirmActionModal actionLabel="Unassign Professor" destructive description={`Remove ${professorById.get(unassignmentTarget.professorId)?.firstName ?? "this Professor"} from this teaching requirement? The assignment record will remain in history.`} error={unassignMutation.isError ? errorMessage(unassignMutation.error) : null} isSubmitting={unassignMutation.isPending} onCancel={() => { setUnassignmentTarget(null); unassignMutation.reset(); }} onConfirm={() => unassignMutation.mutate()} title="Unassign Professor" />}
  </section>;
}
