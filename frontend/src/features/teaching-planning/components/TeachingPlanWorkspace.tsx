import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { academicStructureKeys, getModuleTeachingComponents, type Semester, type SubjectModule } from "@/features/academic-structure/api/academic-structure-api";
import { getTeachingGroups, teachingGroupKeys } from "@/features/student-registration/api/teaching-group-api";
import { generateTeachingPlan, getTeachingPlan, teachingPlanKeys, type TeachingPlanItem } from "../api/teaching-plan-api";

interface TeachingPlanWorkspaceProps {
  academicLevelName?: string;
  academicYearLabel?: string;
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

function roomLabel(value: "LECTURE_HALL" | "CLASSROOM" | "COMPUTER_LAB"): string {
  return value.split("_").map((word) => word[0] + word.slice(1).toLowerCase()).join(" ");
}

export function TeachingPlanWorkspace({ academicLevelName, academicYearLabel, semesterId, semesterName, semesters, modules, onSelectSemester }: TeachingPlanWorkspaceProps) {
  const queryClient = useQueryClient();
  const [selectedClassId, setSelectedClassId] = useState("all");
  const [selectedAudienceId, setSelectedAudienceId] = useState("all");
  const [selectedComponentType, setSelectedComponentType] = useState<"ALL" | "COURSE" | "TD" | "TP">("ALL");
  const planQuery = useQuery({ queryKey: teachingPlanKeys.semester(semesterId || "missing"), queryFn: () => getTeachingPlan(semesterId), enabled: Boolean(semesterId) });
  const teachingGroupsQuery = useQuery({ queryKey: teachingGroupKeys.roster(semesterId || "missing"), queryFn: () => getTeachingGroups(semesterId), enabled: Boolean(semesterId) });
  const componentQueries = useQueries({ queries: modules.map((module) => ({ queryKey: academicStructureKeys.moduleTeachingComponents(module.id), queryFn: () => getModuleTeachingComponents(module.id), enabled: Boolean(semesterId) })) });
  const moduleById = new Map(modules.map((module) => [module.id, module]));
  const componentById = new Map(componentQueries.flatMap((query) => query.data ?? []).map((component) => [component.id, component]));
  const plan = planQuery.data ?? [];
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
    return <div className="resource-table-wrapper"><table className="resource-table teaching-plan-table"><thead><tr><th>Module</th><th>Component</th>{showGrouping && <><th>Audience</th><th>Group</th></>}<th>Weekly delivery</th><th>Room</th><th>Status</th></tr></thead><tbody>{items.map((item) => {
      const module = moduleById.get(item.subjectModuleId);
      const component = componentById.get(item.moduleTeachingComponentId);
      return <tr key={item.id}><td><div className="table-contact"><span>{module?.title ?? "Subject Module"}</span><small>{module?.code ?? "Module"}</small></div></td><td><span className={`teaching-component-badge teaching-component-badge--${item.componentType.toLowerCase()}`}>{item.componentType === "COURSE" ? "Course" : item.componentType}</span></td>{showGrouping && <><td>{item.audienceType === "WHOLE_COHORT" ? "Whole cohort" : item.audienceType === "CLASS_GROUP" ? "Whole class" : "Subgroup"}</td><td>{item.teachingGroupName}</td></>}<td>{component ? `${component.sessionsPerWeek} × ${component.sessionDurationMinutes} min` : "Loading configuration..."}</td><td>{component ? roomLabel(component.requiredRoomType) : "Loading..."}</td><td><span className={`status-badge status-badge--${item.status.toLowerCase()}`}>{item.status === "ACTIVE" ? "Active" : "Inactive"}</span></td></tr>;
    })}</tbody></table></div>;
  }

  const generateMutation = useMutation({
    mutationFn: () => generateTeachingPlan(semesterId),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: teachingPlanKeys.semester(semesterId) }); },
  });

  if (!semesterId) return <section className="management-panel teaching-plan-panel"><div className="panel-empty"><strong>Select a semester.</strong><p>The Teaching Plan is generated for one semester at a time.</p></div></section>;

  return <section className="management-panel teaching-plan-panel">
    <div className="teaching-plan-context"><label><span>Semester</span><select onChange={(event) => { setSelectedClassId("all"); setSelectedAudienceId("all"); setSelectedComponentType("ALL"); onSelectSemester(event.target.value); }} value={semesterId}>{semesters.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label><p>{academicLevelName} · {academicYearLabel}</p></div>
    <header className="panel-header panel-header--bordered"><div><p className="management-kicker">{academicYearLabel ?? "Academic year"} · {academicLevelName ?? "Academic level"}</p><h2>{semesterName ? `${semesterName} Teaching Plan` : "Teaching Plan"}</h2><p>Review the Course, TD, and TP sessions that must be assigned and scheduled.</p></div><button className="management-primary-button" disabled={generateMutation.isPending || modules.length === 0} onClick={() => generateMutation.mutate()} type="button">{generateMutation.isPending ? "Generating..." : plan.length ? "Regenerate Teaching Plan" : "Generate Teaching Plan"}</button></header>
    {generateMutation.isError && <div className="management-alert management-alert--error teaching-plan-alert">{errorMessage(generateMutation.error)}</div>}
    {planQuery.isPending || teachingGroupsQuery.isPending ? <div className="panel-empty">Loading Teaching Plan...</div> : planQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(planQuery.error)}</div> : teachingGroupsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(teachingGroupsQuery.error)}</div> : plan.length === 0 ? <div className="panel-empty"><strong>No Teaching Plan generated.</strong><p>Configure module teaching components and Teaching Groups, then generate the plan.</p></div> : <><div className="teaching-plan-summary"><span><strong>{activeCount}</strong> active teaching sessions</span>{!compactSingleClass && <span><strong>{classSections.size}</strong> Class Groups</span>}<span><strong>{modules.length}</strong> curriculum modules</span></div>
      {!compactSingleClass && <><nav aria-label="Teaching Plan Class Groups" className="teaching-plan-parent-tabs"><button aria-selected={selectedClassId === "all"} onClick={() => { setSelectedClassId("all"); setSelectedAudienceId("all"); }} type="button">All Classes <span>{plan.length}</span></button>{sortedClassSections.map((section) => <button aria-selected={selectedClassId === section.id} key={section.id} onClick={() => { setSelectedClassId(section.id); setSelectedAudienceId("all"); }} type="button">{section.name} <span>{section.classItems.length + [...section.subgroups.values()].reduce((total, child) => total + child.items.length, 0)}</span></button>)}</nav>
      <nav aria-label="Teaching Plan audiences" className="teaching-plan-child-tabs"><button aria-selected={selectedAudienceId === "all"} onClick={() => setSelectedAudienceId("all")} type="button">{selectedClassId === "all" ? "All Delivery" : `All in ${selectedClass?.name}`} <span>{selectedClassId === "all" ? plan.length : selectedClassTotal}</span></button>{selectedClassId === "all" ? <button aria-selected={selectedAudienceId === "cohort"} onClick={() => setSelectedAudienceId("cohort")} type="button">Whole Cohort <span>{wholeCohortItems.length}</span></button> : <><button aria-selected={selectedAudienceId === "class"} onClick={() => setSelectedAudienceId("class")} type="button">Whole Class <span>{selectedClass?.classItems.length ?? 0}</span></button>{[...(selectedClass?.subgroups.values() ?? [])].sort((left, right) => left.name.localeCompare(right.name)).map((child) => <button aria-selected={selectedAudienceId === child.id} key={child.id} onClick={() => setSelectedAudienceId(child.id)} type="button"><span className={`teaching-plan-tab-type teaching-plan-tab-type--${child.type.toLowerCase()}`}>{child.type}</span>{child.name} <span>{child.items.length}</span></button>)}</>}</nav></>}
      <div className="teaching-plan-selection"><header><div><span>{compactSingleClass ? "Class delivery" : selectedClassId === "all" ? "Program delivery" : `Class Group · ${selectedClass?.name}`}</span><h3>{compactSingleClass ? `${academicLevelName ?? "Academic"} Class` : visibleLabel}</h3></div><small>{visibleItems.length} {visibleItems.length === 1 ? "planned session" : "planned sessions"}</small></header><nav aria-label="Teaching component filter" className="teaching-plan-component-filter"><span>Component</span><button aria-pressed={effectiveComponentType === "ALL"} onClick={() => setSelectedComponentType("ALL")} type="button">All Components <small>{scopedItems.length}</small></button>{availableComponentTypes.map((type) => <button aria-pressed={effectiveComponentType === type} key={type} onClick={() => setSelectedComponentType(type)} type="button">{type === "COURSE" ? "Course" : type}<small>{scopedItems.filter((item) => item.componentType === type).length}</small></button>)}</nav>{visibleItems.length ? planTable(visibleItems, !compactSingleClass) : <div className="panel-empty"><strong>No delivery configured for this selection.</strong></div>}</div>
    </>}
  </section>;
}
