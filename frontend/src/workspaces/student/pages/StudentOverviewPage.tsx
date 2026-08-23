import { useQuery } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getMyAbsences, getMyAcademicContexts, getMyExamInvitations, getMyStudentGrades, studentOverviewKeys, type StudentAcademicContext } from "../api/student-overview-api";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "Your academic overview could not be loaded.";
}

function displayDate(value: string): string {
  return new Intl.DateTimeFormat("en-GB", { weekday: "short", day: "numeric", month: "short" }).format(new Date(`${value}T00:00:00`));
}

const resultLabels = { V: "Validated", AV: "Compensated", NV: "Not validated" } as const;

function GroupValue({ groups }: { groups: string[] }) {
  return groups.length ? <div className="student-context-groups">{groups.map((group) => <span key={group}>{group}</span>)}</div> : <strong>Not assigned</strong>;
}

function ContextDetails({ context }: { context: StudentAcademicContext }) {
  return <div className="student-context-details">
    <div className="student-context-program"><span>Programme</span><strong>{context.programFiliereName}</strong><small>{context.programFiliereCode} · {context.programPathName}</small></div>
    <div><span>Level</span><strong>{context.academicLevelName}</strong></div>
    <div><span>Semester</span><strong>{context.semesterName}</strong></div>
    <div><span>Class</span><strong>{context.classGroupName ?? "Not assigned"}</strong></div>
    <div><span>TD</span><GroupValue groups={context.tdGroups} /></div>
    <div><span>TP</span><GroupValue groups={context.tpGroups} /></div>
  </div>;
}

export function StudentOverviewPage() {
  const { user } = useAuth();
  const gradesQuery = useQuery({ queryKey: studentOverviewKeys.grades(), queryFn: () => getMyStudentGrades() });
  const examsQuery = useQuery({ queryKey: studentOverviewKeys.exams(), queryFn: getMyExamInvitations });
  const absencesQuery = useQuery({ queryKey: studentOverviewKeys.absences(), queryFn: getMyAbsences });
  const contextsQuery = useQuery({ queryKey: studentOverviewKeys.academicContexts(), queryFn: getMyAcademicContexts });
  const today = new Date().toISOString().slice(0, 10);
  const contexts = contextsQuery.data ?? [];
  const activeYearContexts = contexts.filter((context) => context.academicYearStatus === "ACTIVE");
  const currentContext = activeYearContexts.find((context) => context.semesterStartDate <= today && context.semesterEndDate >= today)
    ?? [...activeYearContexts].filter((context) => context.semesterStartDate > today).sort((left, right) => left.semesterStartDate.localeCompare(right.semesterStartDate))[0]
    ?? [...activeYearContexts].filter((context) => context.semesterEndDate < today).sort((left, right) => right.semesterEndDate.localeCompare(left.semesterEndDate))[0]
    ?? contexts[0];
  const historicalContexts = contexts.filter((context) => context.semesterRegistrationId !== currentContext?.semesterRegistrationId);
  const isCurrentContext = (academicYearId: string, semesterId: string) => Boolean(currentContext)
    && academicYearId === currentContext.academicYearId
    && semesterId === currentContext.semesterId;
  const grades = (gradesQuery.data ?? []).filter((grade) => isCurrentContext(grade.academicYearId, grade.semesterId));
  const absences = (absencesQuery.data ?? []).filter((absence) => isCurrentContext(absence.academicYearId, absence.semesterId));
  const upcomingExams = (examsQuery.data ?? [])
    .filter((exam) => isCurrentContext(exam.academicYearId, exam.semesterId) && exam.examDate >= today)
    .sort((left, right) => left.examDate.localeCompare(right.examDate) || left.startTime.localeCompare(right.startTime));
  const latestGrades = [...grades].sort((left, right) => right.publishedAt.localeCompare(left.publishedAt)).slice(0, 5);
  const moduleResults = new Map(grades.filter((grade) => grade.moduleResultStatus).map((grade) => [grade.subjectModuleId, grade.moduleResultStatus]));
  const loading = contextsQuery.isPending || gradesQuery.isPending || examsQuery.isPending || absencesQuery.isPending;
  const loadError = gradesQuery.error ?? examsQuery.error ?? absencesQuery.error ?? contextsQuery.error;

  return <div className="management-page student-dashboard-page">
    <header className="student-dashboard-hero"><div><p className="management-kicker">Student portal</p><h1>Academic overview</h1><p>{[user?.firstName, user?.lastName].filter(Boolean).join(" ")} · Published results, examinations, and attendance.</p></div><div className="student-dashboard-period"><span>Current period</span><strong>{currentContext?.academicYearLabel ?? "Not configured"}</strong><div>{currentContext ? <><b>{currentContext.academicLevelName}</b><i>{currentContext.semesterName}</i></> : <small>No active registration</small>}</div></div></header>

    {loadError && <div className="management-alert management-alert--error">{errorMessage(loadError)}</div>}

    <section className="management-panel student-context-card">
      <header><div><p className="management-kicker">Registration</p><h2>{currentContext ? currentContext.programFiliereName : "Academic context"}</h2>{currentContext && <p>{currentContext.programPathName} · {currentContext.programFiliereCode}</p>}</div>{currentContext && <span className={`status-badge status-badge--${currentContext.registrationStatus === "ACTIVE" ? "active" : "inactive"}`}>{currentContext.registrationStatus}</span>}</header>
      {contextsQuery.isPending ? <div className="panel-empty">Loading registration...</div> : !currentContext ? <div className="panel-empty"><strong>No academic registration is available.</strong></div> : <>
        <ContextDetails context={currentContext} />
        {historicalContexts.length > 0 && <details className="student-context-history"><summary>Previous semesters <span>{historicalContexts.length}</span></summary><div>{historicalContexts.map((context) => <article key={context.semesterRegistrationId}><header><strong>{context.academicYearLabel} · {context.semesterName}</strong><span>{context.registrationStatus}</span></header><ContextDetails context={context} /></article>)}</div></details>}
      </>}
    </section>

    <section aria-label="Academic summary" className="student-dashboard-stats">
      <article data-tone="blue"><div><span>Published results</span><small>Normal and rattrapage</small></div><strong>{loading ? "—" : grades.length}</strong></article>
      <article data-tone="green"><div><span>Validated modules</span><small>Including compensation</small></div><strong>{loading ? "—" : Array.from(moduleResults.values()).filter((status) => status === "V" || status === "AV").length}</strong></article>
      <article data-tone="amber"><div><span>Upcoming exams</span><small>Confirmed invitations</small></div><strong>{loading ? "—" : upcomingExams.length}</strong></article>
      <article data-tone="red"><div><span>Absences</span><small>{absences.filter((absence) => absence.justified).length} justified</small></div><strong>{loading ? "—" : absences.length}</strong></article>
    </section>

    <section className="student-dashboard-grid">
      <article className="management-panel student-dashboard-card student-dashboard-card--exams"><header><div><p className="management-kicker">Examinations</p><h2>Upcoming exams</h2></div><span>{upcomingExams.length} scheduled</span></header>{examsQuery.isPending ? <div className="panel-empty">Loading examinations...</div> : upcomingExams.length === 0 ? <div className="panel-empty"><strong>No upcoming examination invitation.</strong><p>Published invitations will appear here.</p></div> : <div className="student-upcoming-exams">{upcomingExams.slice(0, 4).map((exam) => <article key={exam.id}><time dateTime={exam.examDate}><strong>{displayDate(exam.examDate)}</strong><span>{exam.startTime.slice(0, 5)}</span></time><div><strong>{exam.subjectModuleTitle}</strong><span>{exam.sessionType === "NORMAL" ? "Normal session" : "Rattrapage"} · {exam.examGroupLabel ?? "Individual invitation"}</span></div><div><strong>{exam.roomCode ?? "Room pending"}</strong><span>Room</span></div></article>)}</div>}</article>

      <article className="management-panel student-dashboard-card student-dashboard-card--results"><header><div><p className="management-kicker">Published record</p><h2>Latest results</h2></div><span>{latestGrades.length} recent</span></header>{gradesQuery.isPending ? <div className="panel-empty">Loading results...</div> : latestGrades.length === 0 ? <div className="panel-empty"><strong>No published result yet.</strong></div> : <div className="student-latest-results">{latestGrades.map((grade) => <article key={grade.gradeRecordId}><span>{grade.subjectModuleCode}</span><div><strong>{grade.subjectModuleTitle}</strong><small>{grade.sessionType === "NORMAL" ? "Normal session" : "Rattrapage"}</small></div><strong>{grade.gradeValue?.toFixed(2) ?? "—"}</strong><i className={`student-result-status student-result-status--${grade.moduleResultStatus?.toLowerCase() ?? "pending"}`}>{grade.moduleResultStatus ? resultLabels[grade.moduleResultStatus] : "Published"}</i></article>)}</div>}</article>

      <article className="management-panel student-dashboard-card student-dashboard-card--attendance"><header><div><p className="management-kicker">Attendance</p><h2>Absence record</h2></div><span>{absences.length} total</span></header><div className="student-attendance-summary"><div><span>Unjustified</span><strong>{absences.filter((absence) => !absence.justified).length}</strong></div><div><span>Justified</span><strong>{absences.filter((absence) => absence.justified).length}</strong></div><p>Records appear after the professor confirms attendance.</p></div></article>
    </section>
  </div>;
}
