import { useDeferredValue, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getMyTeachingAssignments, teachingPlanKeys, type TeachingAssignment } from "@/features/teaching-planning/api/teaching-plan-api";

type AttendanceModule = {
  subjectModuleId: string;
  code: string;
  title: string;
  program: string;
  level: string;
  semester: string;
  academicYear: string;
  deliveries: TeachingAssignment[];
};

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "Your attendance classes could not be loaded.";
}

export function ProfessorAttendancePage() {
  const [requestedYearId, setRequestedYearId] = useState("");
  const [requestedTerm, setRequestedTerm] = useState<"AUTUMN" | "SPRING" | "">("");
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const assignments = (assignmentsQuery.data ?? []).filter((assignment) => assignment.status === "ACTIVE");
  const academicYears = Array.from(new Map(assignments.map((assignment) => [assignment.academicYearId, assignment.academicYearLabel])).entries());
  const academicYearId = academicYears.some(([id]) => id === requestedYearId)
    ? requestedYearId
    : assignments.find((assignment) => assignment.academicYearStatus === "ACTIVE")?.academicYearId ?? academicYears.at(-1)?.[0] ?? "";
  const yearAssignments = assignments.filter((assignment) => assignment.academicYearId === academicYearId);
  const terms = Array.from(new Set(yearAssignments.map((assignment) => assignment.semesterTermType)));
  const activeTerm = yearAssignments.find((assignment) => assignment.semesterLifecycleStatus === "ACTIVE")?.semesterTermType;
  const term = requestedTerm && terms.includes(requestedTerm)
    ? requestedTerm
    : activeTerm && terms.includes(activeTerm) ? activeTerm : terms[0] ?? "";
  const contextAssignments = yearAssignments.filter((assignment) => assignment.semesterTermType === term);
  const modules = new Map<string, AttendanceModule>();
  contextAssignments.forEach((assignment) => {
    const key = `${assignment.subjectModuleId}:${assignment.semesterId}:${assignment.academicYearId}`;
    const context: AttendanceModule = modules.get(key) ?? {
      subjectModuleId: assignment.subjectModuleId,
      code: assignment.subjectModuleCode,
      title: assignment.subjectModuleTitle,
      program: assignment.programFiliereName,
      level: assignment.academicLevelName,
      semester: assignment.semesterName,
      academicYear: assignment.academicYearLabel,
      deliveries: [],
    };
    context.deliveries.push(assignment);
    modules.set(key, context);
  });
  const visibleModules = Array.from(modules.values()).filter((module) => !deferredSearch || `${module.code} ${module.title} ${module.program} ${module.level} ${module.semester} ${module.deliveries.map((item) => `${item.teachingGroupName} ${item.componentType}`).join(" ")}`.toLowerCase().includes(deferredSearch));
  const selectedYearLabel = academicYears.find(([id]) => id === academicYearId)?.[1] ?? "Academic year";
  const selectedTermLabel = term === "AUTUMN" ? "Autumn" : term === "SPRING" ? "Spring" : "Academic term";
  const deliveryCount = visibleModules.reduce((total, module) => total + module.deliveries.length, 0);

  return <div className="management-page professor-attendance-directory">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Teaching operations</p><h1>Attendance</h1><p>Select a module, then open the Class Group whose attendance you want to record.</p></div></header>
    {assignmentsQuery.error && <div className="management-alert management-alert--error">{errorMessage(assignmentsQuery.error)}</div>}
    <section className="management-panel professor-attendance-directory-panel">
      <header><div><p className="management-kicker">My teaching context</p><h2>Module Attendance</h2><p>{selectedYearLabel} · {selectedTermLabel}</p></div><div className="professor-attendance-directory-summary"><span><strong>{visibleModules.length}</strong> modules</span><span><strong>{deliveryCount}</strong> teaching groups</span></div></header>
      <div className="professor-attendance-directory-filters"><label><span>Academic year</span><select onChange={(event) => { setRequestedYearId(event.target.value); setRequestedTerm(""); }} value={academicYearId}>{academicYears.map(([id, label]) => <option key={id} value={id}>{label}</option>)}</select></label><label><span>Academic term</span><select onChange={(event) => setRequestedTerm(event.target.value as "AUTUMN" | "SPRING")} value={term}>{terms.map((item) => <option key={item} value={item}>{item === "AUTUMN" ? "Autumn" : "Spring"}</option>)}</select></label><label className="professor-attendance-directory-search"><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Module, program, level, or class" value={search} /></label></div>
      {assignmentsQuery.isPending ? <div className="panel-empty">Loading your modules...</div> : contextAssignments.length === 0 ? <div className="panel-empty"><strong>No teaching assignment is available for this academic period.</strong></div> : visibleModules.length === 0 ? <div className="panel-empty"><strong>{search ? "No module matches your search." : "No attendance register is available in this period."}</strong></div> : <div className="professor-attendance-module-grid">{visibleModules.sort((a, b) => a.title.localeCompare(b.title)).map((module) => <article key={`${module.subjectModuleId}:${module.semester}:${module.academicYear}`}>
        <header><span>{module.code}</span><div><h2>{module.title}</h2><p>{module.program} · {module.level}</p></div><strong>{module.deliveries.length}<small>{module.deliveries.length === 1 ? "group" : "groups"}</small></strong></header>
        <div className="professor-attendance-class-links">{module.deliveries.sort((a, b) => a.teachingGroupName.localeCompare(b.teachingGroupName)).map((delivery) => <Link key={delivery.id} to={`/professor/teaching/${delivery.id}?tab=attendance&from=attendance`}><div><span>{delivery.teachingGroupName}</span><small>{delivery.componentType === "COURSE" ? "Course" : delivery.componentType} · {module.semester}</small></div><strong>Open →</strong></Link>)}</div>
      </article>)}</div>}
    </section>
  </div>;
}
