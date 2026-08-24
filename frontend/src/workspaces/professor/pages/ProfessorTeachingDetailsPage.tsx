import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { getMyTeachingAssignments, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { getMyScheduleEntries, scheduleKeys } from "@/features/scheduling/api/schedule-api";
import { getAttendanceRoster, professorAttendanceKeys } from "../api/professor-attendance-api";
import { ProfessorAttendanceRegister } from "../components/ProfessorAttendanceRegister";
import { ProfessorAbsenceJustificationsPanel } from "../components/ProfessorAbsenceJustificationsPanel";

const dayLabels = { MONDAY: "Monday", TUESDAY: "Tuesday", WEDNESDAY: "Wednesday", THURSDAY: "Thursday", FRIDAY: "Friday", SATURDAY: "Saturday", SUNDAY: "Sunday" } as const;
type TeachingWorkspaceTab = "schedule" | "students" | "attendance";
type AttendanceWorkspaceView = "register" | "justifications";

export function ProfessorTeachingDetailsPage() {
  const { teachingAssignmentId = "" } = useParams();
  const [searchParams] = useSearchParams();
  const requestedTab = searchParams.get("tab");
  const [activeTab, setActiveTab] = useState<TeachingWorkspaceTab>(requestedTab === "attendance" || requestedTab === "justifications" ? "attendance" : "schedule");
  const [attendanceView, setAttendanceView] = useState<AttendanceWorkspaceView>(requestedTab === "justifications" ? "justifications" : "register");
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const scheduleQuery = useQuery({ queryKey: scheduleKeys.myEntries(), queryFn: getMyScheduleEntries });
  const rosterQuery = useQuery({ queryKey: professorAttendanceKeys.roster(teachingAssignmentId), queryFn: () => getAttendanceRoster(teachingAssignmentId), enabled: Boolean(teachingAssignmentId) });
  const assignment = (assignmentsQuery.data ?? []).find((item) => item.status === "ACTIVE" && item.id === teachingAssignmentId);
  const entries = (scheduleQuery.data ?? []).filter((entry) => entry.teachingAssignmentId === teachingAssignmentId);

  return <div className="management-page professor-teaching-details-page">
    <Link className="management-back-link" to={searchParams.get("from") === "attendance" ? "/professor/attendance" : "/professor/teaching"}>← Back to {searchParams.get("from") === "attendance" ? "Attendance" : "My Teaching"}</Link>
    {assignmentsQuery.isPending ? <div className="management-panel panel-empty">Loading teaching assignment...</div> : !assignment ? <div className="management-panel panel-empty"><strong>Teaching assignment not found.</strong></div> : <>
      <header className="curriculum-header professor-class-header"><span className="curriculum-program-code">{assignment.subjectModuleCode}</span><div><p className="management-kicker">My Teaching · {assignment.componentType === "COURSE" ? "Course" : assignment.componentType}</p><h1>{assignment.subjectModuleTitle}</h1><p>{assignment.teachingGroupName}</p></div><div className="professor-class-context"><strong>{assignment.programFiliereName}</strong><span>{assignment.academicLevelName} · {assignment.semesterName} · {assignment.academicYearLabel}</span></div></header>
      <nav aria-label="Teaching assignment workspace" className="curriculum-section-tabs" role="tablist"><button aria-selected={activeTab === "schedule"} onClick={() => setActiveTab("schedule")} role="tab" type="button">Schedule</button><button aria-selected={activeTab === "students"} onClick={() => setActiveTab("students")} role="tab" type="button">Students</button><button aria-selected={activeTab === "attendance"} onClick={() => setActiveTab("attendance")} role="tab" type="button">Attendance</button></nav>
      {activeTab === "attendance" ? <div className="professor-teaching-attendance-workspace"><nav aria-label="Attendance views" className="professor-teaching-attendance-tabs" role="tablist"><button aria-selected={attendanceView === "register"} onClick={() => setAttendanceView("register")} role="tab" type="button">Attendance register</button><button aria-selected={attendanceView === "justifications"} onClick={() => setAttendanceView("justifications")} role="tab" type="button">Justifications</button></nav>{attendanceView === "register" ? <ProfessorAttendanceRegister teachingAssignmentId={teachingAssignmentId} /> : <section className="management-panel professor-class-workspace professor-teaching-justifications"><header className="panel-header panel-header--bordered"><div><p className="management-kicker">Attendance review</p><h2>Absence Justifications</h2><p>Submissions for {assignment.teachingGroupName} in this teaching assignment.</p></div></header><ProfessorAbsenceJustificationsPanel assignments={[assignment]} /></section>}</div> : activeTab === "students" ? <section className="management-panel professor-class-workspace"><header className="panel-header panel-header--bordered"><div><p className="management-kicker">Teaching audience</p><h2>Students</h2><p>{rosterQuery.data?.length ?? 0} students in {assignment.teachingGroupName}.</p></div></header>{rosterQuery.isPending ? <div className="panel-empty">Loading students...</div> : <div className="resource-table-wrapper"><table className="resource-table"><thead><tr><th>Student</th><th>Apogee</th><th>University email</th></tr></thead><tbody>{(rosterQuery.data ?? []).map((student) => <tr key={student.studentId}><td><div className="resource-name"><span className="person-monogram">{student.firstName[0]}{student.lastName[0]}</span><strong>{student.firstName} {student.lastName}</strong></div></td><td>{student.apogeeCode}</td><td>{student.universityEmail}</td></tr>)}</tbody></table></div>}</section> : <section className="management-panel professor-class-workspace"><header className="panel-header panel-header--bordered"><div><p className="management-kicker">Published timetable</p><h2>Teaching Schedule</h2><p>Your sessions for this exact teaching assignment.</p></div></header>{entries.length === 0 ? <div className="panel-empty"><strong>No published session is available.</strong></div> : <div className="professor-class-schedule-list">{entries.map((entry) => <article key={entry.id}><div><strong>{dayLabels[entry.dayOfWeek]}</strong><span>{entry.startTime.slice(0, 5)} – {entry.endTime.slice(0, 5)}</span></div><div><strong>{assignment.componentType === "COURSE" ? "Course" : assignment.componentType}</strong><span>{assignment.teachingGroupName}</span></div><div><strong>{entry.roomCode}</strong><span>{entry.blockName ?? "Standalone room"}</span></div></article>)}</div>}</section>}
    </>}
  </div>;
}
