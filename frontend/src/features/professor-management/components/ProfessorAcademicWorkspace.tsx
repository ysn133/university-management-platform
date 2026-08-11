import { useEffect, useState } from "react";
import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { academicStructureKeys, getAcademicDomains, getAcademicLevel, getAcademicYears, getModuleTeachingComponents, getProgramFiliere, getProgramPaths, getSemester, getSubjectModule } from "@/features/academic-structure/api/academic-structure-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { getScheduleEntries, getSemesterSchedules, scheduleKeys } from "@/features/scheduling/api/schedule-api";
import { getTeachingAssignments, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { getProfessorExpertise, professorManagementKeys, replaceProfessorExpertise } from "../api/professor-management-api";

interface ProfessorAcademicWorkspaceProps {
  establishmentId: string;
  professorId: string;
  section: "teaching" | "schedule" | "expertise";
}

const days = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SUNDAY"] as const;
const dayLabels = { MONDAY: "Monday", TUESDAY: "Tuesday", WEDNESDAY: "Wednesday", THURSDAY: "Thursday", FRIDAY: "Friday", SATURDAY: "Saturday", SUNDAY: "Sunday" } as const;
const gridStart = 8 * 60;
const gridEnd = 18 * 60 + 30;
const hourLabels = Array.from({ length: 11 }, (_, index) => 8 + index);
function timeToMinutes(value: string): number { const [hours, minutes] = value.split(":").map(Number); return hours * 60 + minutes; }
function assignLanes<T extends { startTime: string; endTime: string }>(entries: T[]) { const laneEnds: number[] = []; return [...entries].sort((left, right) => left.startTime.localeCompare(right.startTime)).map((entry) => { const start = timeToMinutes(entry.startTime); let lane = laneEnds.findIndex((end) => end <= start); if (lane < 0) { lane = laneEnds.length; laneEnds.push(0); } laneEnds[lane] = timeToMinutes(entry.endTime); return { entry, lane }; }); }
function errorMessage(error: unknown): string { return error instanceof ApiRequestError ? error.message : "The request could not be completed."; }

export function ProfessorAcademicWorkspace({ establishmentId, professorId, section }: ProfessorAcademicWorkspaceProps) {
  const queryClient = useQueryClient();
  const [academicYearId, setAcademicYearId] = useState("");
  const [termType, setTermType] = useState<"AUTUMN" | "SPRING">("AUTUMN");
  const [editingExpertise, setEditingExpertise] = useState(false);
  const [selectedDomainIds, setSelectedDomainIds] = useState<string[]>([]);
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.assignments(establishmentId), queryFn: () => getTeachingAssignments(establishmentId) });
  const schedulesQuery = useQuery({ queryKey: scheduleKeys.schedules(establishmentId), queryFn: () => getSemesterSchedules(establishmentId) });
  const yearsQuery = useQuery({ queryKey: academicStructureKeys.academicYears(establishmentId), queryFn: () => getAcademicYears(establishmentId) });
  const expertiseQuery = useQuery({ queryKey: professorManagementKeys.expertise(professorId), queryFn: () => getProfessorExpertise(professorId) });
  const domainsQuery = useQuery({ queryKey: academicStructureKeys.academicDomains(establishmentId), queryFn: () => getAcademicDomains(establishmentId), enabled: section === "expertise" });
  const programPathsQuery = useQuery({ queryKey: academicStructureKeys.programPaths(establishmentId), queryFn: () => getProgramPaths(establishmentId) });
  const professorAssignments = (assignmentsQuery.data ?? []).filter((assignment) => assignment.professorId === professorId && assignment.status === "ACTIVE");
  const assignmentModuleIds = Array.from(new Set(professorAssignments.map((assignment) => assignment.subjectModuleId)));
  const moduleQueries = useQueries({ queries: assignmentModuleIds.map((id) => ({ queryKey: academicStructureKeys.subjectModule(id), queryFn: () => getSubjectModule(id) })) });
  const componentQueries = useQueries({ queries: assignmentModuleIds.map((id) => ({ queryKey: academicStructureKeys.moduleTeachingComponents(id), queryFn: () => getModuleTeachingComponents(id) })) });
  const moduleById = new Map(moduleQueries.flatMap((query) => query.data ? [query.data] : []).map((module) => [module.id, module]));
  const components = componentQueries.flatMap((query) => query.data ?? []);
  const scheduleSemesterIds = Array.from(new Set([...assignmentModuleIds.map((id) => moduleById.get(id)?.semesterId).filter((id): id is string => Boolean(id)), ...(schedulesQuery.data ?? []).map((schedule) => schedule.semesterId)]));
  const semesterQueries = useQueries({ queries: scheduleSemesterIds.map((id) => ({ queryKey: academicStructureKeys.semester(id), queryFn: () => getSemester(id) })) });
  const semesterById = new Map(semesterQueries.flatMap((query) => query.data ? [query.data] : []).map((semester) => [semester.id, semester]));
  const academicLevelIds = Array.from(new Set(Array.from(semesterById.values()).map((semester) => semester.academicLevelId)));
  const academicLevelQueries = useQueries({ queries: academicLevelIds.map((id) => ({ queryKey: academicStructureKeys.academicLevel(id), queryFn: () => getAcademicLevel(id) })) });
  const academicLevelById = new Map(academicLevelQueries.flatMap((query) => query.data ? [query.data] : []).map((level) => [level.id, level]));
  const programFiliereIds = Array.from(new Set(Array.from(academicLevelById.values()).map((level) => level.programFiliereId)));
  const programFiliereQueries = useQueries({ queries: programFiliereIds.map((id) => ({ queryKey: academicStructureKeys.programFiliere(id), queryFn: () => getProgramFiliere(id) })) });
  const programFiliereById = new Map(programFiliereQueries.flatMap((query) => query.data ? [query.data] : []).map((program) => [program.id, program]));
  const programPathById = new Map((programPathsQuery.data ?? []).map((path) => [path.id, path]));
  const relevantSchedules = (schedulesQuery.data ?? []).filter((schedule) => schedule.academicYearId === academicYearId && semesterById.get(schedule.semesterId)?.termType === termType);
  const entryQueries = useQueries({ queries: relevantSchedules.map((schedule) => ({ queryKey: scheduleKeys.entries(schedule.id), queryFn: () => getScheduleEntries(schedule.id) })) });
  const professorEntries = entryQueries.flatMap((query) => query.data ?? []).filter((entry) => entry.professorId === professorId).sort((left, right) => left.startTime.localeCompare(right.startTime));
  const entryModuleIds = Array.from(new Set(professorEntries.map((entry) => entry.subjectModuleId).filter((id) => !moduleById.has(id))));
  const entryModuleQueries = useQueries({ queries: entryModuleIds.map((id) => ({ queryKey: academicStructureKeys.subjectModule(id), queryFn: () => getSubjectModule(id) })) });
  entryModuleQueries.forEach((query) => { if (query.data) moduleById.set(query.data.id, query.data); });

  useEffect(() => {
    if (!academicYearId && yearsQuery.data?.length) {
      setAcademicYearId(yearsQuery.data.find((year) => year.status === "ACTIVE")?.id ?? yearsQuery.data[0].id);
    }
  }, [academicYearId, yearsQuery.data]);

  const expertiseMutation = useMutation({
    mutationFn: () => replaceProfessorExpertise(professorId, selectedDomainIds),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: professorManagementKeys.expertise(professorId) }),
        queryClient.invalidateQueries({ queryKey: ["professor-management", "professors", establishmentId] }),
      ]);
      setEditingExpertise(false);
    },
  });

  function openExpertiseEditor() {
    expertiseMutation.reset();
    setSelectedDomainIds(expertiseQuery.data?.academicDomains.map((domain) => domain.academicDomainId) ?? []);
    setEditingExpertise(true);
  }

  function toggleDomain(domainId: string) {
    setSelectedDomainIds((current) => current.includes(domainId) ? current.filter((id) => id !== domainId) : [...current, domainId]);
  }

  const teachingMinutes = professorAssignments.reduce((total, assignment) => {
    const component = components.find((item) => item.subjectModuleId === assignment.subjectModuleId && item.componentType === assignment.componentType);
    return total + (component ? component.sessionsPerWeek * component.sessionDurationMinutes : 0);
  }, 0);
  const loadingContext = semesterQueries.some((query) => query.isPending) || academicLevelQueries.some((query) => query.isPending) || programFiliereQueries.some((query) => query.isPending) || programPathsQuery.isPending;
  const loadingTeaching = assignmentsQuery.isPending || moduleQueries.some((query) => query.isPending) || componentQueries.some((query) => query.isPending) || loadingContext;
  const loadingSchedule = schedulesQuery.isPending || yearsQuery.isPending || entryQueries.some((query) => query.isPending) || entryModuleQueries.some((query) => query.isPending) || loadingContext;

  function academicContext(subjectModuleId: string): string {
    const semester = semesterById.get(moduleById.get(subjectModuleId)?.semesterId ?? "");
    const level = academicLevelById.get(semester?.academicLevelId ?? "");
    const program = programFiliereById.get(level?.programFiliereId ?? "");
    const path = programPathById.get(program?.programPathId ?? "");
    return [path?.name, program?.name, level?.name].filter(Boolean).join(" · ") || "Academic context unavailable";
  }

  return <section className="admin-section-panel professor-academic-workspace">
    {section === "teaching" ? <><header className="professor-tab-heading"><p className="management-kicker">Academic activity</p><h2>Teaching responsibilities</h2><p>Active module and audience assignments for this Professor.</p></header><div className="professor-teaching-view"><div className="professor-teaching-summary"><span><strong>{professorAssignments.length}</strong> active assignments</span><span><strong>{Math.round(teachingMinutes / 6) / 10}h</strong> scheduled weekly delivery</span><span><strong>{new Set(professorAssignments.map((assignment) => assignment.subjectModuleId)).size}</strong> modules</span></div>{loadingTeaching ? <div className="panel-empty">Loading teaching responsibilities...</div> : professorAssignments.length === 0 ? <div className="panel-empty"><strong>No active teaching assignment.</strong></div> : <div className="resource-table-wrapper"><table className="resource-table"><thead><tr><th>Module</th><th>Academic context</th><th>Component</th><th>Audience</th><th>Weekly delivery</th></tr></thead><tbody>{professorAssignments.map((assignment) => { const module = moduleById.get(assignment.subjectModuleId); const component = components.find((item) => item.subjectModuleId === assignment.subjectModuleId && item.componentType === assignment.componentType); return <tr key={assignment.id}><td><div className="table-contact"><strong>{module?.title ?? "Subject Module"}</strong><small>{module?.code}</small></div></td><td><span className="professor-academic-context">{academicContext(assignment.subjectModuleId)}</span></td><td>{assignment.componentType === "COURSE" ? "Course" : assignment.componentType}</td><td>{assignment.teachingGroupName}</td><td>{component ? `${component.sessionsPerWeek} × ${component.sessionDurationMinutes} min` : "Not configured"}</td></tr>; })}</tbody></table></div>}</div></> : section === "expertise" ? <><header className="professor-tab-heading professor-tab-heading--action"><div><p className="management-kicker">Qualifications</p><h2>Academic expertise</h2><p>Approved domains used for teaching assignment eligibility.</p></div><button className="management-primary-button" disabled={expertiseQuery.isPending} onClick={openExpertiseEditor} type="button">Edit Expertise</button></header><div className="professor-expertise-view">{expertiseQuery.isPending ? <div className="panel-empty">Loading academic expertise...</div> : expertiseQuery.isError ? <div className="panel-empty panel-empty--error">Academic expertise could not be loaded.</div> : !expertiseQuery.data?.academicDomains.length ? <div className="panel-empty"><strong>No academic expertise assigned.</strong></div> : <div className="professor-domain-card-grid">{expertiseQuery.data.academicDomains.map((domain) => <article key={domain.academicDomainId}><span>{domain.code}</span><strong>{domain.name}</strong></article>)}</div>}</div></> : <><header className="professor-tab-heading professor-tab-heading--schedule"><div><p className="management-kicker">Academic activity</p><h2>Weekly schedule</h2><p>All scheduled teaching across programs in the selected period.</p></div><div className="professor-schedule-toolbar"><label><span>Academic year</span><select onChange={(event) => setAcademicYearId(event.target.value)} value={academicYearId}>{(yearsQuery.data ?? []).map((year) => <option key={year.id} value={year.id}>{year.label}</option>)}</select></label><div role="group" aria-label="Academic term"><button aria-pressed={termType === "AUTUMN"} onClick={() => setTermType("AUTUMN")} type="button">Autumn</button><button aria-pressed={termType === "SPRING"} onClick={() => setTermType("SPRING")} type="button">Spring</button></div></div></header>{loadingSchedule ? <div className="panel-empty">Loading Professor schedule...</div> : professorEntries.length === 0 ? <div className="panel-empty"><strong>No scheduled sessions for this period.</strong></div> : <div className="timetable-scroll professor-timetable-scroll"><div className="weekly-timetable professor-weekly-timetable"><div className="timetable-time-header"><span>Days</span><div>{hourLabels.map((hour) => <span key={hour} style={{ left: `${((hour * 60 - gridStart) / (gridEnd - gridStart)) * 100}%` }}>{hour}h</span>)}</div></div>{days.map((day) => { const laidOut = assignLanes(professorEntries.filter((entry) => entry.dayOfWeek === day)); const laneCount = Math.max(1, ...laidOut.map((item) => item.lane + 1)); return <div className="timetable-day-row" key={day}><strong>{dayLabels[day]}</strong><div className="timetable-day-track professor-day-track" style={{ minHeight: `${Math.max(112, laneCount * 112)}px` }}>{laidOut.map(({ entry, lane }) => { const assignment = professorAssignments.find((item) => item.id === entry.teachingAssignmentId); const componentType = assignment?.componentType ?? "COURSE"; const start = timeToMinutes(entry.startTime); const end = timeToMinutes(entry.endTime); const context = academicContext(entry.subjectModuleId); return <article className={`timetable-session timetable-session--${componentType.toLowerCase()}`} key={entry.id} style={{ left: `${((start - gridStart) / (gridEnd - gridStart)) * 100}%`, top: `${lane * 108 + 5}px`, width: `${((end - start) / (gridEnd - gridStart)) * 100}%` }} title={context}><strong>{moduleById.get(entry.subjectModuleId)?.title ?? "Subject Module"}</strong><span>{context}</span><span>{componentType === "COURSE" ? "Course" : componentType} · {entry.audienceType === "WHOLE_COHORT" ? "Whole Cohort" : entry.teachingGroupName}</span><small>{entry.roomCode} · {entry.startTime.slice(0, 5)}–{entry.endTime.slice(0, 5)}</small></article>; })}</div></div>; })}</div></div>}</>}
    {editingExpertise && <ManagementModal title="Edit Academic Expertise" description="Select the academic domains this Professor is qualified to teach." onClose={() => setEditingExpertise(false)}><div className="professor-expertise-form">{domainsQuery.isPending ? <div className="panel-empty">Loading academic domains...</div> : domainsQuery.isError ? <div className="management-alert management-alert--error">{errorMessage(domainsQuery.error)}</div> : domainsQuery.data?.length ? <div className="professor-domain-options">{domainsQuery.data.map((domain) => <label key={domain.id}><input checked={selectedDomainIds.includes(domain.id)} onChange={() => toggleDomain(domain.id)} type="checkbox" /><span><strong>{domain.name}</strong><small>{domain.code}</small></span></label>)}</div> : <div className="panel-empty"><strong>No academic domains are available.</strong><p>Create an academic domain before assigning Professor expertise.</p></div>}{expertiseMutation.isError && <div className="management-alert management-alert--error">{errorMessage(expertiseMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={() => setEditingExpertise(false)} type="button">Cancel</button><button className="management-primary-button" disabled={domainsQuery.isPending || expertiseMutation.isPending} onClick={() => expertiseMutation.mutate()} type="button">{expertiseMutation.isPending ? "Saving..." : "Save Expertise"}</button></footer></div></ManagementModal>}
  </section>;
}
