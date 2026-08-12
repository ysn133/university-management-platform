import { useDeferredValue, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { getMyScheduleEntries, scheduleKeys } from "@/features/scheduling/api/schedule-api";
import { getMyTeachingAssignments, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getMyClassStudents, getMyExams, getMyModuleResponsibilities, professorOverviewKeys } from "../api/professor-overview-api";
import { ProfessorExamCalendar } from "./ProfessorExamSchedulePage";

const dayLabels = { MONDAY: "Monday", TUESDAY: "Tuesday", WEDNESDAY: "Wednesday", THURSDAY: "Thursday", FRIDAY: "Friday", SATURDAY: "Saturday", SUNDAY: "Sunday" } as const;

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The class workspace could not be loaded.";
}

export function ProfessorClassPage() {
  const { subjectModuleId = "", classGroupId = "" } = useParams();
  const [activeTab, setActiveTab] = useState<"schedule" | "exams" | "students">("schedule");
  const [requestedExamSession, setRequestedExamSession] = useState<"NORMAL" | "RATTRAPAGE" | "">("");
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const responsibilitiesQuery = useQuery({ queryKey: professorOverviewKeys.responsibilities(), queryFn: getMyModuleResponsibilities });
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const scheduleQuery = useQuery({ queryKey: scheduleKeys.myEntries(), queryFn: getMyScheduleEntries });
  const studentsQuery = useQuery({ queryKey: professorOverviewKeys.classStudents(subjectModuleId, classGroupId), queryFn: () => getMyClassStudents(subjectModuleId, classGroupId) });
  const examsQuery = useQuery({ queryKey: professorOverviewKeys.exams(), queryFn: getMyExams });
  const responsibility = (responsibilitiesQuery.data ?? []).find((item) => item.status === "ACTIVE" && item.subjectModuleId === subjectModuleId && item.classGroupId === classGroupId);
  const assignment = (assignmentsQuery.data ?? []).find((item) => item.status === "ACTIVE" && item.subjectModuleId === subjectModuleId && item.semesterId === responsibility?.semesterId);
  const assignmentById = new Map((assignmentsQuery.data ?? []).map((item) => [item.id, item]));
  const entries = (scheduleQuery.data ?? []).filter((entry) => entry.subjectModuleId === subjectModuleId && (entry.audienceType === "WHOLE_COHORT" || entry.sourceClassGroupId === classGroupId));
  const classExams = (examsQuery.data ?? []).filter((exam) => exam.subjectModuleId === subjectModuleId && exam.classGroupId === classGroupId && exam.academicYearId === responsibility?.academicYearId && exam.semesterId === responsibility?.semesterId);
  const examSessions = Array.from(new Set(classExams.map((exam) => exam.sessionType)));
  const examSession = examSessions.includes(requestedExamSession as "NORMAL" | "RATTRAPAGE") ? requestedExamSession : examSessions.includes("NORMAL") ? "NORMAL" : examSessions[0];
  const exams = classExams.filter((exam) => exam.sessionType === examSession);
  const students = (studentsQuery.data ?? []).filter((student) => !deferredSearch || `${student.firstName} ${student.lastName} ${student.apogeeCode} ${student.universityEmail}`.toLowerCase().includes(deferredSearch));
  const loading = responsibilitiesQuery.isPending || assignmentsQuery.isPending;
  const loadError = responsibilitiesQuery.error ?? assignmentsQuery.error ?? scheduleQuery.error ?? studentsQuery.error ?? examsQuery.error;

  return <div className="management-page professor-class-page">
    <Link className="management-back-link" to={`/professor/modules/${subjectModuleId}`}>← Back to module classes</Link>
    {loadError && <div className="management-alert management-alert--error">{errorMessage(loadError)}</div>}
    {loading ? <div className="management-panel panel-empty">Loading class...</div> : !responsibility ? <div className="management-panel panel-empty"><strong>Class not found.</strong><p>This class is not assigned to your account for this module.</p></div> : <>
      <header className="curriculum-header professor-class-header"><span className="curriculum-program-code">{responsibility.subjectModuleCode}</span><div><p className="management-kicker">My Modules · Class workspace</p><h1>{responsibility.classGroupName}</h1><p>{responsibility.subjectModuleTitle}</p></div><div className="professor-class-context"><strong>{assignment?.programFiliereName}</strong><span>{assignment?.academicLevelName} · {responsibility.semesterName} · {responsibility.academicYearLabel}</span></div></header>
      <nav aria-label="Class workspace" className="curriculum-section-tabs" role="tablist"><button aria-selected={activeTab === "schedule"} onClick={() => setActiveTab("schedule")} role="tab" type="button">Schedule</button><button aria-selected={activeTab === "students"} onClick={() => setActiveTab("students")} role="tab" type="button">Students</button><button aria-selected={activeTab === "exams"} onClick={() => setActiveTab("exams")} role="tab" type="button">Exam Schedule</button></nav>
      {activeTab === "schedule" ? <section className="management-panel professor-class-workspace"><header className="panel-header panel-header--bordered"><div><p className="management-kicker">Published timetable</p><h2>Class Schedule</h2><p>Only your sessions for {responsibility.classGroupName} in {responsibility.subjectModuleTitle}.</p></div><span>{entries.length} scheduled</span></header>{scheduleQuery.isPending ? <div className="panel-empty">Loading class schedule...</div> : entries.length === 0 ? <div className="panel-empty"><strong>No published sessions for this class.</strong></div> : <div className="professor-class-schedule-list">{entries.map((entry) => { const entryAssignment = assignmentById.get(entry.teachingAssignmentId); return <article key={entry.id}><div><strong>{dayLabels[entry.dayOfWeek]}</strong><span>{entry.startTime.slice(0, 5)} – {entry.endTime.slice(0, 5)}</span></div><div><strong>{entryAssignment?.subjectModuleTitle}</strong><span>{entryAssignment?.componentType === "COURSE" ? "Course" : entryAssignment?.componentType} · {entry.teachingGroupName}</span></div><div><strong>{entry.roomCode}</strong><span>{entry.blockCode ? `${entry.blockCode} · ${entry.blockName}` : "Standalone room"}</span></div></article>; })}</div>}</section>
      : activeTab === "exams" ? <section className="management-panel professor-class-workspace professor-class-exams"><header className="panel-header panel-header--bordered"><div><p className="management-kicker">Published planning</p><h2>{examSession === "RATTRAPAGE" ? "Rattrapage Session" : "Normal Session"}</h2><p>{responsibility.subjectModuleTitle} · {responsibility.classGroupName}</p></div>{examSessions.length > 1 && <div className="professor-exam-session-switch">{examSessions.map((session) => <button className={session === examSession ? "is-active" : ""} key={session} onClick={() => setRequestedExamSession(session)} type="button">{session === "NORMAL" ? "Normal" : "Rattrapage"}</button>)}</div>}</header>{examsQuery.isPending ? <div className="panel-empty">Loading class exams...</div> : classExams.length === 0 ? <div className="panel-empty"><strong>No published exams for this class.</strong></div> : <ProfessorExamCalendar exams={exams} />}</section>
      : <section className="management-panel professor-class-workspace"><header className="panel-header panel-header--bordered professor-class-students-header"><div><p className="management-kicker">Class roster</p><h2>Students</h2><p>{studentsQuery.data?.length ?? 0} students assigned to this class.</p></div><label><span>Search students</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Name, Apogee, or email" value={search} /></label></header>{studentsQuery.isPending ? <div className="panel-empty">Loading students...</div> : students.length === 0 ? <div className="panel-empty"><strong>{search ? "No matching students." : "No students assigned."}</strong></div> : <div className="resource-table-wrapper"><table className="resource-table"><thead><tr><th>Student</th><th>Apogee code</th><th>University email</th></tr></thead><tbody>{students.map((student) => <tr key={student.studentId}><td><div className="resource-name"><span className="person-monogram">{student.firstName[0]}{student.lastName[0]}</span><div><strong>{student.firstName} {student.lastName}</strong><small>{student.nationalStudentCode ?? "No national code"}</small></div></div></td><td>{student.apogeeCode}</td><td>{student.universityEmail}</td></tr>)}</tbody></table></div>}</section>}
    </>}
  </div>;
}
