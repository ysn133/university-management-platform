import { useDeferredValue, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { getMyTeachingAssignments, teachingPlanKeys, type TeachingAssignment } from "@/features/teaching-planning/api/teaching-plan-api";

export function ProfessorTeachingPage() {
  const [requestedYearId, setRequestedYearId] = useState("");
  const [requestedTerm, setRequestedTerm] = useState<"AUTUMN" | "SPRING" | "">("");
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const assignments = (assignmentsQuery.data ?? []).filter((assignment) => assignment.status === "ACTIVE");
  const years = Array.from(new Map(assignments.map((assignment) => [assignment.academicYearId, assignment.academicYearLabel])).entries());
  const yearId = years.some(([id]) => id === requestedYearId) ? requestedYearId : assignments.find((assignment) => assignment.academicYearStatus === "ACTIVE")?.academicYearId ?? years.at(-1)?.[0] ?? "";
  const yearAssignments = assignments.filter((assignment) => assignment.academicYearId === yearId);
  const terms = Array.from(new Set(yearAssignments.map((assignment) => assignment.semesterTermType)));
  const activeTerm = yearAssignments.find((assignment) => assignment.semesterLifecycleStatus === "ACTIVE")?.semesterTermType;
  const term = requestedTerm && terms.includes(requestedTerm) ? requestedTerm : activeTerm && terms.includes(activeTerm) ? activeTerm : terms[0] ?? "";
  const periodAssignments = yearAssignments.filter((assignment) => assignment.semesterTermType === term && (!deferredSearch || `${assignment.subjectModuleCode} ${assignment.subjectModuleTitle} ${assignment.programFiliereName} ${assignment.academicLevelName} ${assignment.teachingGroupName} ${assignment.componentType}`.toLowerCase().includes(deferredSearch)));
  const modules = new Map<string, { assignment: TeachingAssignment; deliveries: TeachingAssignment[] }>();
  periodAssignments.forEach((assignment) => {
    const key = `${assignment.subjectModuleId}:${assignment.semesterId}`;
    const module = modules.get(key) ?? { assignment, deliveries: [] };
    module.deliveries.push(assignment);
    modules.set(key, module);
  });

  return <div className="management-page professor-teaching-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Teaching workspace</p><h1>My Teaching</h1><p>Your Course, TD, and TP delivery assignments and their student audiences.</p></div></header>
    <section className="management-panel professor-teaching-panel">
      <header><div><p className="management-kicker">Current academic context</p><h2>Teaching Assignments</h2><p>{years.find(([id]) => id === yearId)?.[1]} · {term === "AUTUMN" ? "Autumn" : "Spring"}</p></div><strong>{periodAssignments.length}<span>deliveries</span></strong></header>
      <div className="professor-attendance-directory-filters"><label><span>Academic year</span><select onChange={(event) => { setRequestedYearId(event.target.value); setRequestedTerm(""); }} value={yearId}>{years.map(([id, label]) => <option key={id} value={id}>{label}</option>)}</select></label><label><span>Academic term</span><select onChange={(event) => setRequestedTerm(event.target.value as "AUTUMN" | "SPRING")} value={term}>{terms.map((item) => <option key={item} value={item}>{item === "AUTUMN" ? "Autumn" : "Spring"}</option>)}</select></label><label><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Module, program, component, or group" value={search} /></label></div>
      {assignmentsQuery.isPending ? <div className="panel-empty">Loading your teaching assignments...</div> : modules.size === 0 ? <div className="panel-empty"><strong>No teaching assignment is available for this period.</strong></div> : <div className="professor-teaching-module-list">{Array.from(modules.values()).sort((a, b) => a.assignment.subjectModuleTitle.localeCompare(b.assignment.subjectModuleTitle)).map(({ assignment, deliveries }) => <article key={`${assignment.subjectModuleId}:${assignment.semesterId}`}><header><span>{assignment.subjectModuleCode}</span><div><h2>{assignment.subjectModuleTitle}</h2><p>{assignment.programFiliereName} · {assignment.academicLevelName} · {assignment.semesterName}</p></div></header><div>{deliveries.sort((a, b) => a.componentType.localeCompare(b.componentType)).map((delivery) => <Link key={delivery.id} to={`/professor/teaching/${delivery.id}`}><span className={`professor-teaching-component professor-teaching-component--${delivery.componentType.toLowerCase()}`}>{delivery.componentType === "COURSE" ? "Course" : delivery.componentType}</span><div><strong>{delivery.teachingGroupName}</strong><small>{delivery.sessionsPerWeek} session{delivery.sessionsPerWeek === 1 ? "" : "s"} per week · {delivery.sessionDurationMinutes} min</small></div><b>Open →</b></Link>)}</div></article>)}</div>}
    </section>
  </div>;
}
