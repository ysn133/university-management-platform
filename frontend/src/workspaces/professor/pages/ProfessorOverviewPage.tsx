import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getMyTeachingAssignments, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { getMyScheduleEntries, scheduleKeys } from "@/features/scheduling/api/schedule-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getMyModuleResponsibilities, professorOverviewKeys } from "../api/professor-overview-api";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The Professor overview could not be loaded.";
}

const dayLabels = {
  MONDAY: "Monday",
  TUESDAY: "Tuesday",
  WEDNESDAY: "Wednesday",
  THURSDAY: "Thursday",
  FRIDAY: "Friday",
  SATURDAY: "Saturday",
  SUNDAY: "Sunday",
} as const;

const dayOrder = Object.keys(dayLabels) as Array<keyof typeof dayLabels>;

export function ProfessorOverviewPage() {
  const { user } = useAuth();
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const responsibilitiesQuery = useQuery({ queryKey: professorOverviewKeys.responsibilities(), queryFn: getMyModuleResponsibilities });
  const scheduleQuery = useQuery({ queryKey: scheduleKeys.myEntries(), queryFn: getMyScheduleEntries });
  const allAssignments = (assignmentsQuery.data ?? []).filter((assignment) => assignment.status === "ACTIVE");
  const activeYearId = allAssignments.find((assignment) => assignment.academicYearStatus === "ACTIVE")?.academicYearId;
  const yearAssignments = activeYearId ? allAssignments.filter((assignment) => assignment.academicYearId === activeYearId) : [];
  const availableTerms = Array.from(new Set(yearAssignments.map((assignment) => assignment.semesterTermType)));
  const currentTerm = yearAssignments.find((assignment) => assignment.semesterLifecycleStatus === "ACTIVE")?.semesterTermType ?? availableTerms[0];
  const currentAssignments = yearAssignments.filter((assignment) => assignment.semesterTermType === currentTerm);
  const currentYear = yearAssignments[0]?.academicYearLabel;
  const currentYearIds = new Set(currentAssignments.map((assignment) => assignment.academicYearId));
  const currentSemesterIds = new Set(currentAssignments.map((assignment) => assignment.semesterId));
  const assignmentById = new Map(currentAssignments.map((assignment) => [assignment.id, assignment]));
  const scheduledEntries = (scheduleQuery.data ?? [])
    .filter((entry) => assignmentById.has(entry.teachingAssignmentId))
    .sort((left, right) => dayOrder.indexOf(left.dayOfWeek) - dayOrder.indexOf(right.dayOfWeek) || left.startTime.localeCompare(right.startTime));
  const activeResponsibilities = (responsibilitiesQuery.data ?? []).filter((responsibility) => responsibility.status === "ACTIVE"
    && currentYearIds.has(responsibility.academicYearId)
    && currentSemesterIds.has(responsibility.semesterId));
  const responsibilityModules = Array.from(
    activeResponsibilities.reduce((modules, responsibility) => {
      const key = `${responsibility.subjectModuleId}:${responsibility.semesterId}`;
      const existing = modules.get(key);
      if (existing) existing.classGroups.add(responsibility.classGroupName);
      else modules.set(key, { ...responsibility, classGroups: new Set([responsibility.classGroupName]) });
      return modules;
    }, new Map<string, (typeof activeResponsibilities)[number] & { classGroups: Set<string> }>()).values(),
  );
  const assignmentModuleIds = new Set(currentAssignments.map((assignment) => assignment.subjectModuleId));
  const weeklySessions = currentAssignments.reduce((total, assignment) => total + assignment.sessionsPerWeek, 0);
  const loading = assignmentsQuery.isPending || responsibilitiesQuery.isPending || scheduleQuery.isPending;
  const loadError = assignmentsQuery.error ?? responsibilitiesQuery.error ?? scheduleQuery.error;
  const displayedAssignments = currentAssignments.slice(0, 5);
  const displayedSessions = scheduledEntries.slice(0, 6);
  const periodLabel = currentTerm === "AUTUMN" ? "Autumn period" : currentTerm === "SPRING" ? "Spring period" : "No active period";

  return (
    <div className="management-page professor-dashboard-page">
      <header className="professor-dashboard-hero">
        <div><p className="management-kicker">Professor workspace</p><h1>Good to see you, {user?.firstName}</h1><p>Your current teaching, timetable, and academic responsibilities in one place.</p></div>
        <div className="professor-dashboard-period"><span>Current academic context</span><strong>{currentYear ?? "Not configured"}</strong><small>{periodLabel}</small></div>
      </header>

      <section className="professor-dashboard-stats" aria-label="Current teaching summary">
        <Link to="/professor/teaching"><span>Teaching modules</span><strong>{loading ? "—" : assignmentModuleIds.size}</strong><small>Current period</small><i>View teaching →</i></Link>
        <Link to="/professor/schedule"><span>Weekly sessions</span><strong>{loading ? "—" : weeklySessions}</strong><small>Published delivery</small><i>Open schedule →</i></Link>
        <Link to="/professor/teaching"><span>Teaching groups</span><strong>{loading ? "—" : new Set(currentAssignments.map((assignment) => assignment.teachingGroupId)).size}</strong><small>Course, TD and TP</small><i>View groups →</i></Link>
        <Link to="/professor/modules"><span>Responsibilities</span><strong>{loading ? "—" : responsibilityModules.length}</strong><small>Assessment ownership</small><i>Open modules →</i></Link>
      </section>

      {loadError && <div className="management-alert management-alert--error">{errorMessage(loadError)}</div>}
      {!loading && currentAssignments.length === 0 && <div className="management-panel professor-dashboard-no-period"><span>Current period</span><strong>No active teaching context is available.</strong><p>The dashboard will populate when an active academic year and semester contain teaching assignments.</p></div>}

      <section className="professor-dashboard-grid">
        <article className="management-panel professor-dashboard-card professor-dashboard-card--assignments">
          <header><div><p className="management-kicker">Current delivery</p><h2>Teaching assignments</h2></div><Link className="professor-card-link" to="/professor/teaching">View all</Link></header>
          {loading ? <div className="panel-empty">Loading teaching assignments...</div> : displayedAssignments.length === 0 ? <div className="panel-empty"><strong>No assignment in the active period.</strong></div> : <div className="professor-overview-assignment-list">{displayedAssignments.map((assignment) => <Link key={assignment.id} to={`/professor/teaching/${assignment.id}`}><span className={`teaching-component-badge teaching-component-badge--${assignment.componentType.toLowerCase()}`}>{assignment.componentType === "COURSE" ? "Course" : assignment.componentType}</span><div><strong>{assignment.subjectModuleTitle}</strong><small>{assignment.programFiliereCode} · {assignment.academicLevelName} · {assignment.semesterName} · {assignment.teachingGroupName}</small></div><i>→</i></Link>)}{currentAssignments.length > displayedAssignments.length && <Link className="professor-overview-more" to="/professor/teaching">View {currentAssignments.length - displayedAssignments.length} more assignments</Link>}</div>}
        </article>

        <article className="management-panel professor-dashboard-card professor-dashboard-card--delivery">
          <header><div><p className="management-kicker">This week</p><h2>Teaching agenda</h2></div><Link className="professor-card-link" to="/professor/schedule">Full schedule</Link></header>
          {loading ? <div className="panel-empty">Loading weekly schedule...</div> : displayedSessions.length === 0 ? <div className="panel-empty"><strong>No published sessions in the active period.</strong></div> : <div className="professor-weekly-schedule">{displayedSessions.map((entry) => { const assignment = assignmentById.get(entry.teachingAssignmentId); return <Link key={entry.id} to={`/professor/teaching/${entry.teachingAssignmentId}`}><div className="professor-weekly-schedule-time"><strong>{dayLabels[entry.dayOfWeek]}</strong><span>{entry.startTime.slice(0, 5)} – {entry.endTime.slice(0, 5)}</span></div><div className="professor-weekly-schedule-session"><div><span className={`teaching-component-badge teaching-component-badge--${assignment?.componentType.toLowerCase() ?? "course"}`}>{assignment?.componentType === "COURSE" ? "Course" : assignment?.componentType ?? "Session"}</span><strong>{assignment?.subjectModuleTitle ?? "Scheduled session"}</strong></div><span>{assignment?.programFiliereCode} · {assignment?.academicLevelName} · {entry.teachingGroupName}</span></div><div className="professor-weekly-schedule-location"><strong>{entry.roomCode}</strong><span>{entry.blockName ?? "Standalone room"}</span></div></Link>; })}</div>}
        </article>

        {responsibilityModules.length > 0 && <article className="management-panel professor-dashboard-card professor-dashboard-card--wide"><header><div><p className="management-kicker">Assessment ownership</p><h2>Module responsibilities</h2></div><Link className="professor-card-link" to="/professor/modules">View all</Link></header><div className="professor-responsibility-list">{responsibilityModules.map((responsibility) => <Link key={`${responsibility.subjectModuleId}:${responsibility.semesterId}`} to={`/professor/modules/${responsibility.subjectModuleId}`}><span className="professor-responsibility-code">{responsibility.subjectModuleCode}</span><div><strong>{responsibility.subjectModuleTitle}</strong><span>{responsibility.semesterName} · {responsibility.academicYearLabel}</span></div><div className="professor-responsibility-groups"><small>Classes</small><strong>{Array.from(responsibility.classGroups).join(", ")}</strong></div><i>→</i></Link>)}</div></article>}
      </section>
    </div>
  );
}
