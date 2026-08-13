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

export function ProfessorOverviewPage() {
  const { user } = useAuth();
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const responsibilitiesQuery = useQuery({ queryKey: professorOverviewKeys.responsibilities(), queryFn: getMyModuleResponsibilities });
  const scheduleQuery = useQuery({ queryKey: scheduleKeys.myEntries(), queryFn: getMyScheduleEntries });
  const assignments = (assignmentsQuery.data ?? []).filter((assignment) => assignment.status === "ACTIVE");
  const assignmentById = new Map(assignments.map((assignment) => [assignment.id, assignment]));
  const scheduledEntries = scheduleQuery.data ?? [];
  const activeResponsibilities = (responsibilitiesQuery.data ?? []).filter((responsibility) => responsibility.status === "ACTIVE");
  const responsibilityModules = Array.from(
    activeResponsibilities.reduce((modules, responsibility) => {
      const existing = modules.get(responsibility.subjectModuleId);
      if (existing) {
        existing.classGroups.add(responsibility.classGroupName);
      } else {
        modules.set(responsibility.subjectModuleId, {
          ...responsibility,
          classGroups: new Set([responsibility.classGroupName]),
        });
      }
      return modules;
    }, new Map<string, (typeof activeResponsibilities)[number] & { classGroups: Set<string> }>()).values(),
  );
  const assignmentModuleIds = Array.from(new Set(assignments.map((assignment) => assignment.subjectModuleId)));
  const weeklySessions = assignments.reduce((total, assignment) => total + assignment.sessionsPerWeek, 0);
  const loading = assignmentsQuery.isPending || responsibilitiesQuery.isPending;
  const loadError = assignmentsQuery.error ?? responsibilitiesQuery.error ?? scheduleQuery.error;
  const displayedAssignments = assignments.slice(0, 5);

  return (
    <div className="management-page professor-dashboard-page">
      <header className="management-page-header professor-dashboard-header">
        <div><p className="management-kicker">Professor workspace</p><h1>Welcome, {user?.firstName}</h1><p>Review your teaching activity, weekly planning, attendance, and assessment work from one place.</p></div>
        <div className="professor-dashboard-identity"><span className="person-monogram">{user?.firstName[0]}{user?.lastName[0]}</span><div><strong>{user?.firstName} {user?.lastName}</strong><small>{user?.universityEmail}</small></div></div>
      </header>

      <section className="context-stat-strip professor-dashboard-stats" aria-label="Teaching summary">
        <article><span>Assigned modules</span><strong>{loading ? "—" : assignmentModuleIds.length}</strong><small>Active teaching assignments</small></article>
        <article><span>Weekly sessions</span><strong>{loading ? "—" : weeklySessions}</strong><small>Configured teaching delivery</small></article>
        <article><span>Student groups</span><strong>{loading ? "—" : new Set(assignments.map((assignment) => assignment.teachingGroupId)).size}</strong><small>Course, TD, and TP audiences</small></article>
        <article><span>Module responsibility</span><strong>{loading ? "—" : responsibilityModules.length}</strong><small>Modules under your responsibility</small></article>
      </section>

      {loadError && <div className="management-alert management-alert--error">{errorMessage(loadError)}</div>}

      <section className="professor-dashboard-grid">
        <article className="management-panel professor-dashboard-card professor-dashboard-card--assignments"><header><div><p className="management-kicker">Teaching</p><h2>My assignments</h2></div><span>{assignments.length} active</span></header>{loading ? <div className="panel-empty">Loading teaching assignments...</div> : assignments.length === 0 ? <div className="panel-empty"><strong>No active teaching assignment.</strong><p>Your Course, TD, and TP assignments will appear here once assigned.</p></div> : <div className="professor-overview-assignment-list">{displayedAssignments.map((assignment) => <article key={assignment.id}><span className={`teaching-component-badge teaching-component-badge--${assignment.componentType.toLowerCase()}`}>{assignment.componentType === "COURSE" ? "Course" : assignment.componentType}</span><div><strong>{assignment.subjectModuleTitle}</strong><small>{assignment.subjectModuleCode} · {assignment.programFiliereCode} · {assignment.academicLevelName} · {assignment.semesterName} · {assignment.teachingGroupName}</small></div></article>)}{assignments.length > displayedAssignments.length && <small className="professor-overview-more">+{assignments.length - displayedAssignments.length} more assignments</small>}</div>}</article>
        <article className="management-panel professor-dashboard-card professor-dashboard-card--delivery"><header><div><p className="management-kicker">Published timetable</p><h2>Weekly delivery</h2></div><span>{scheduledEntries.length} scheduled</span></header>{scheduleQuery.isPending || assignmentsQuery.isPending ? <div className="panel-empty">Loading weekly schedule...</div> : scheduledEntries.length === 0 ? <div className="panel-empty"><strong>No published sessions yet.</strong><p>Assigned teaching will appear here when its timetable is published.</p></div> : <div className="professor-weekly-schedule">{scheduledEntries.map((entry) => { const assignment = assignmentById.get(entry.teachingAssignmentId); return <article key={entry.id}><div className="professor-weekly-schedule-time"><strong>{dayLabels[entry.dayOfWeek]}</strong><span>{entry.startTime.slice(0, 5)} – {entry.endTime.slice(0, 5)}</span></div><div className="professor-weekly-schedule-session"><div><span className={`teaching-component-badge teaching-component-badge--${assignment?.componentType.toLowerCase() ?? "course"}`}>{assignment?.componentType === "COURSE" ? "Course" : assignment?.componentType ?? "Session"}</span><strong>{assignment?.subjectModuleTitle ?? "Scheduled session"}</strong></div><span>{assignment?.programFiliereCode} · {assignment?.academicLevelName} · {assignment?.semesterName} · {entry.teachingGroupName}</span></div><div className="professor-weekly-schedule-location"><strong>{entry.roomCode}</strong><span>{entry.blockCode ? `${entry.blockCode} · ${entry.blockName}` : "Standalone room"}</span></div></article>; })}</div>}</article>
        <article className="management-panel professor-dashboard-card professor-dashboard-card--wide"><header><div><p className="management-kicker">Academic work</p><h2>My Modules</h2></div><Link className="professor-card-link" to="/professor/modules">View all</Link></header>{responsibilitiesQuery.isPending ? <div className="panel-empty">Loading your modules...</div> : responsibilityModules.length === 0 ? <div className="panel-empty"><strong>No active modules assigned.</strong></div> : <div className="professor-responsibility-list">{responsibilityModules.map((responsibility) => <Link key={responsibility.subjectModuleId} to={`/professor/modules/${responsibility.subjectModuleId}`}><span className="professor-responsibility-code">{responsibility.subjectModuleCode}</span><div><strong>{responsibility.subjectModuleTitle}</strong><span>{responsibility.academicYearLabel} · {responsibility.semesterName}</span></div><div className="professor-responsibility-groups"><small>Classes</small><strong>{Array.from(responsibility.classGroups).join(", ")}</strong></div></Link>)}</div>}</article>
      </section>

      <footer className="management-panel professor-dashboard-security"><div><p className="management-kicker">Account</p><strong>Security settings</strong><span>Manage the password used to access your Professor workspace.</span></div><Link className="secondary-button" to="/professor/account/password">Change password</Link></footer>
    </div>
  );
}
