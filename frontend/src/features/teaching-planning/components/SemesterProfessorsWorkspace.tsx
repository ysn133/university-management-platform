import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { academicStructureKeys, getModuleTeachingComponents, type Semester, type SubjectModule } from "@/features/academic-structure/api/academic-structure-api";
import { getProfessors, professorManagementKeys } from "@/features/professor-management/api/professor-management-api";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { getTeachingAssignments, getTeachingPlan, teachingPlanKeys, type TeachingAssignment, unassignProfessor } from "../api/teaching-plan-api";

interface SemesterProfessorsWorkspaceProps {
  academicLevelName?: string;
  academicYearLabel?: string;
  establishmentId: string;
  modules: SubjectModule[];
  professorDetailsPath: (professorId: string) => string;
  semesterId: string;
  semesterName?: string;
  semesters: Semester[];
  onSelectSemester: (semesterId: string) => void;
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function SemesterProfessorsWorkspace({ academicLevelName, academicYearLabel, establishmentId, modules, professorDetailsPath, semesterId, semesterName, semesters, onSelectSemester }: SemesterProfessorsWorkspaceProps) {
  const location = useLocation();
  const queryClient = useQueryClient();
  const [expandedProfessorId, setExpandedProfessorId] = useState<string | null>(null);
  const [unassignmentTarget, setUnassignmentTarget] = useState<TeachingAssignment | null>(null);
  const planQuery = useQuery({ queryKey: teachingPlanKeys.semester(semesterId || "missing"), queryFn: () => getTeachingPlan(semesterId), enabled: Boolean(semesterId) });
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.assignments(establishmentId), queryFn: () => getTeachingAssignments(establishmentId), enabled: Boolean(establishmentId && semesterId) });
  const professorsQuery = useQuery({ queryKey: professorManagementKeys.professors(establishmentId), queryFn: () => getProfessors(establishmentId), enabled: Boolean(establishmentId && semesterId) });
  const componentQueries = useQueries({ queries: modules.map((module) => ({ queryKey: academicStructureKeys.moduleTeachingComponents(module.id), queryFn: () => getModuleTeachingComponents(module.id), enabled: Boolean(semesterId) })) });

  const plan = planQuery.data ?? [];
  const requirementIds = new Set(plan.map((requirement) => requirement.id));
  const assignments = (assignmentsQuery.data ?? []).filter((assignment) => assignment.status === "ACTIVE" && requirementIds.has(assignment.teachingRequirementId));
  const planById = new Map(plan.map((requirement) => [requirement.id, requirement]));
  const moduleById = new Map(modules.map((module) => [module.id, module]));
  const componentById = new Map(componentQueries.flatMap((query) => query.data ?? []).map((component) => [component.id, component]));
  const assignmentsByProfessor = new Map<string, TeachingAssignment[]>();
  for (const assignment of assignments) {
    assignmentsByProfessor.set(assignment.professorId, [...(assignmentsByProfessor.get(assignment.professorId) ?? []), assignment]);
  }
  const assignedProfessors = (professorsQuery.data ?? []).filter((professor) => assignmentsByProfessor.has(professor.professorId)).sort((left, right) => `${left.lastName} ${left.firstName}`.localeCompare(`${right.lastName} ${right.firstName}`));
  const returnSearchParams = new URLSearchParams(location.search);
  returnSearchParams.set("section", "professors");
  const returnTo = `${location.pathname}?${returnSearchParams.toString()}`;

  const unassignMutation = useMutation({
    mutationFn: () => unassignProfessor(unassignmentTarget!.id),
    onSuccess: async () => {
      setUnassignmentTarget(null);
      await queryClient.invalidateQueries({ queryKey: teachingPlanKeys.assignments(establishmentId) });
    },
  });

  if (!semesterId) return <section className="management-panel semester-professors-panel"><div className="panel-empty"><strong>Select a semester.</strong><p>Assigned Professors are displayed for one semester at a time.</p></div></section>;

  const isLoading = planQuery.isPending || assignmentsQuery.isPending || professorsQuery.isPending || componentQueries.some((query) => query.isPending);
  const loadError = planQuery.error ?? assignmentsQuery.error ?? professorsQuery.error ?? componentQueries.find((query) => query.error)?.error;

  return <section className="management-panel semester-professors-panel">
    <div className="teaching-plan-context"><label><span>Semester</span><select onChange={(event) => { setExpandedProfessorId(null); onSelectSemester(event.target.value); }} value={semesterId}>{semesters.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label><p>{academicLevelName} · {academicYearLabel}</p></div>
    <header className="panel-header panel-header--bordered"><div><p className="management-kicker">{semesterName} · Teaching staff</p><h2>Assigned Professors</h2><p>Review each Professor's teaching delivery and weekly workload for this semester.</p></div><span className="semester-professor-total">{assignedProfessors.length} assigned</span></header>
    {isLoading ? <div className="panel-empty">Loading assigned Professors...</div> : loadError ? <div className="panel-empty panel-empty--error">{errorMessage(loadError)}</div> : assignedProfessors.length === 0 ? <div className="panel-empty"><strong>No Professors assigned.</strong><p>Generate or manually create assignments from the Teaching Plan tab.</p></div> : <div className="semester-professor-list">{assignedProfessors.map((professor) => {
      const professorAssignments = assignmentsByProfessor.get(professor.professorId) ?? [];
      const weeklyMinutes = professorAssignments.reduce((total, assignment) => {
        const requirement = planById.get(assignment.teachingRequirementId);
        const component = requirement ? componentById.get(requirement.moduleTeachingComponentId) : undefined;
        return total + (component ? component.sessionsPerWeek * component.sessionDurationMinutes : 0);
      }, 0);
      const counts = { COURSE: 0, TD: 0, TP: 0 };
      professorAssignments.forEach((assignment) => { counts[assignment.componentType] += 1; });
      const isExpanded = expandedProfessorId === professor.professorId;
      return <article className={isExpanded ? "is-expanded" : ""} key={professor.professorId}><header><button aria-expanded={isExpanded} className="semester-professor-expand" onClick={() => setExpandedProfessorId(isExpanded ? null : professor.professorId)} type="button"><span className="person-monogram">{professor.firstName[0]}{professor.lastName[0]}</span><span><strong>{professor.firstName} {professor.lastName}</strong><small>{professor.academicRank || "Rank not specified"} · {professor.employeeNumber}</small></span><span className="semester-professor-workload"><strong>{Math.round(weeklyMinutes / 6) / 10}h</strong><small>weekly</small></span><span className="semester-professor-chevron">⌄</span></button><Link className="semester-professor-profile-link" state={{ returnLabel: "Back to Semester Professors", returnTo }} to={professorDetailsPath(professor.professorId)}>View profile</Link></header><div className="semester-professor-counts"><span>{professorAssignments.length} requirements</span>{counts.COURSE > 0 && <span>{counts.COURSE} Course</span>}{counts.TD > 0 && <span>{counts.TD} TD</span>}{counts.TP > 0 && <span>{counts.TP} TP</span>}</div>{isExpanded && <div className="resource-table-wrapper"><table className="resource-table semester-professor-delivery"><thead><tr><th>Module</th><th>Component</th><th>Audience</th><th>Weekly delivery</th><th>Source</th><th>Action</th></tr></thead><tbody>{professorAssignments.map((assignment) => {
        const requirement = planById.get(assignment.teachingRequirementId);
        const module = moduleById.get(assignment.subjectModuleId);
        const component = requirement ? componentById.get(requirement.moduleTeachingComponentId) : undefined;
        return <tr key={assignment.id}><td><div className="table-contact"><span>{module?.title ?? "Subject Module"}</span><small>{module?.code ?? "Module"}</small></div></td><td><span className={`teaching-component-badge teaching-component-badge--${assignment.componentType.toLowerCase()}`}>{assignment.componentType === "COURSE" ? "Course" : assignment.componentType}</span></td><td>{assignment.teachingGroupName}</td><td>{component ? `${component.sessionsPerWeek} × ${component.sessionDurationMinutes} min` : "Not configured"}</td><td>{assignment.assignmentSource === "AUTOMATIC" ? "Automatic" : "Manual"}</td><td><div className="row-actions"><button className="danger-text" onClick={() => setUnassignmentTarget(assignment)} type="button">Unassign</button></div></td></tr>;
      })}</tbody></table></div>}</article>;
    })}</div>}
    {unassignmentTarget && <ConfirmActionModal actionLabel="Unassign Professor" destructive description={`Remove this teaching requirement from ${professorsQuery.data?.find((professor) => professor.professorId === unassignmentTarget.professorId)?.firstName ?? "the Professor"}? Assignment history will be retained.`} error={unassignMutation.isError ? errorMessage(unassignMutation.error) : null} isSubmitting={unassignMutation.isPending} onCancel={() => { setUnassignmentTarget(null); unassignMutation.reset(); }} onConfirm={() => unassignMutation.mutate()} title="Unassign Professor" />}
  </section>;
}
