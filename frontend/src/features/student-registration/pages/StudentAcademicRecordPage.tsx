import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { WeeklyTimetable, type WeeklyTimetableEntry } from "@/features/scheduling/components/WeeklyTimetable";
import { academicStructureKeys, getAcademicLevels, getAcademicYears, getProgramFiliere } from "@/features/academic-structure/api/academic-structure-api";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getRegistrationStudyContext, getStudent, getStudentAcademicRegistrations, studentRegistrationKeys } from "../api/student-registration-api";
import {
  getManagedStudentAbsences,
  getManagedStudentGrades,
  getManagedStudentProgression,
  getManagedStudentSchedule,
  studentAcademicRecordKeys,
} from "../api/student-academic-record-api";

type RecordSection = "overview" | "grades" | "attendance" | "schedule" | "decision";
const decisionLabels = { PROMOTED: "Promoted", PROMOTED_BY_COMPENSATION: "Promoted by compensation", PROMOTED_WITH_DEBT: "Promoted with module debt", LEVEL_VALIDATED: "Academic level validated", REPEAT: "Repeat academic level", FAILED: "Failed" } as const;
const resultLabels = { V: "Validated", AV: "Compensated", NV: "Not validated" } as const;

function message(error: unknown) {
  return error instanceof ApiRequestError ? error.message : "The academic record could not be loaded.";
}

export function StudentAcademicRecordPage() {
  const { studentId, academicRegistrationId } = useParams();
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const [section, setSection] = useState<RecordSection>("overview");
  const [semesterId, setSemesterId] = useState("");
  const studentQuery = useQuery({ queryKey: studentRegistrationKeys.student(studentId ?? "missing"), queryFn: () => getStudent(studentId!), enabled: Boolean(studentId) });
  const registrationsQuery = useQuery({ queryKey: studentRegistrationKeys.studentRegistrations(studentId ?? "missing"), queryFn: () => getStudentAcademicRegistrations(studentId!), enabled: Boolean(studentId) });
  const registration = (registrationsQuery.data ?? []).find((item) => item.id === academicRegistrationId);
  const yearsQuery = useQuery({ queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"), queryFn: () => getAcademicYears(establishmentId!), enabled: Boolean(establishmentId) });
  const programQuery = useQuery({ queryKey: academicStructureKeys.programFiliere(registration?.programFiliereId ?? "missing"), queryFn: () => getProgramFiliere(registration!.programFiliereId), enabled: Boolean(registration) });
  const levelsQuery = useQuery({ queryKey: academicStructureKeys.academicLevels(registration?.programFiliereId ?? "missing"), queryFn: () => getAcademicLevels(registration!.programFiliereId), enabled: Boolean(registration) });
  const studyQuery = useQuery({ queryKey: studentRegistrationKeys.studyContext(academicRegistrationId ?? "missing"), queryFn: () => getRegistrationStudyContext(academicRegistrationId!), enabled: Boolean(academicRegistrationId) });
  const gradesQuery = useQuery({ queryKey: studentAcademicRecordKeys.grades(studentId ?? "", registration?.academicYearId ?? "", registration?.academicLevelId ?? ""), queryFn: () => getManagedStudentGrades(studentId!, registration!.academicYearId, registration!.academicLevelId), enabled: Boolean(studentId && registration) });
  const absencesQuery = useQuery({ queryKey: studentAcademicRecordKeys.absences(studentId ?? "", registration?.academicYearId ?? ""), queryFn: () => getManagedStudentAbsences(establishmentId!, studentId!, registration!.academicYearId), enabled: Boolean(establishmentId && studentId && registration) });
  const scheduleQuery = useQuery({ queryKey: studentAcademicRecordKeys.schedule(studentId ?? ""), queryFn: () => getManagedStudentSchedule(studentId!), enabled: Boolean(studentId) });
  const progressionQuery = useQuery({ queryKey: studentAcademicRecordKeys.progression(academicRegistrationId ?? ""), queryFn: () => getManagedStudentProgression(academicRegistrationId!), enabled: Boolean(academicRegistrationId), retry: false });
  const semesters = studyQuery.data ?? [];

  useEffect(() => {
    if (semesters.length && !semesters.some((item) => item.semester.semesterId === semesterId)) setSemesterId(semesters[0].semester.semesterId);
  }, [semesterId, semesters.map((item) => item.semester.semesterId).join(",")]);

  if (!studentId || !academicRegistrationId || !workspacePath) return <div className="management-state management-state--error"><h1>Academic context unavailable</h1></div>;
  if (studentQuery.isPending || registrationsQuery.isPending) return <div className="management-state">Loading academic record...</div>;
  if (studentQuery.isError || registrationsQuery.isError || !registration) return <div className="management-state management-state--error"><h1>Academic record unavailable</h1><p>{message(studentQuery.error ?? registrationsQuery.error)}</p></div>;

  const student = studentQuery.data;
  const year = (yearsQuery.data ?? []).find((item) => item.id === registration.academicYearId);
  const level = (levelsQuery.data ?? []).find((item) => item.id === registration.academicLevelId);
  const selectedSemester = semesters.find((item) => item.semester.semesterId === semesterId);
  const modules = selectedSemester?.modules ?? [];
  const grades = (gradesQuery.data ?? []).filter((grade) => grade.semesterId === semesterId);
  const finalGrades = Array.from(new Map(grades.filter((grade) => grade.finalGradeValue !== null).map((grade) => [grade.subjectModuleId, grade])).values());
  const absences = (absencesQuery.data ?? []).filter((absence) => absence.semesterId === semesterId);
  const schedule: WeeklyTimetableEntry[] = (scheduleQuery.data ?? []).filter((entry) => entry.academicYearId === registration.academicYearId && entry.academicLevelId === registration.academicLevelId && entry.semesterId === semesterId).map((entry) => ({
    id: entry.id, dayOfWeek: entry.dayOfWeek, startTime: entry.startTime, endTime: entry.endTime,
    title: entry.subjectModuleTitle, context: `${entry.componentType === "COURSE" ? "Course" : entry.componentType} · ${entry.teachingGroupName}`,
    detail: entry.professorName, room: [entry.blockCode, entry.roomCode].filter(Boolean).join(" · ") || "Room not assigned", componentType: entry.componentType,
  }));
  const loadingSection = studyQuery.isPending || gradesQuery.isPending || absencesQuery.isPending || scheduleQuery.isPending;

  return <div className="management-page managed-student-record-page professor-schedule-page">
    <div className="admin-page-toolbar"><Link className="record-back-link" to={`${workspacePath}/students/${studentId}`}>Back to {student.firstName} {student.lastName}</Link><span>{year?.label ?? "Academic year"} · {level?.name ?? "Level"}</span></div>
    <header className="management-page-header student-grades-header"><div><p className="management-kicker">Student academic record</p><h1>{student.firstName} {student.lastName}</h1><p>{programQuery.data?.name ?? "Program"} · {level?.name ?? "Level"}</p></div><div className="student-grades-current"><span>Selected registration</span><strong>{year?.label ?? "Academic year"}</strong><small>Apogee {student.apogeeCode} · {registration.status}</small></div></header>
    <section className="management-panel student-grades-panel managed-student-record-workspace">
      <nav aria-label="Academic record sections" className="managed-student-record-primary-tabs" role="tablist">{(["overview", "grades", "attendance", "schedule", "decision"] as RecordSection[]).map((item) => <button aria-selected={section === item} key={item} onClick={() => setSection(item)} role="tab" type="button">{item[0].toUpperCase() + item.slice(1)}</button>)}</nav>
      {section !== "decision" && <div className="managed-student-record-period"><span>Semester</span><div role="tablist">{semesters.map((item) => <button aria-selected={semesterId === item.semester.semesterId} key={item.semester.semesterId} onClick={() => setSemesterId(item.semester.semesterId)} role="tab" type="button">{item.semester.semesterName}</button>)}</div></div>}

    {loadingSection ? <div className="panel-empty">Loading academic information...</div> : section === "overview" ? <section className="managed-student-record-overview"><header className="student-grades-context"><div className="student-grades-context__identity"><span>Registration context</span><strong>{programQuery.data?.name ?? "Program"}</strong><small>{level?.name ?? "Level"} · {selectedSemester?.semester.semesterName ?? "Semester"} · {year?.label ?? "Academic year"}</small></div></header><div className="managed-student-record-facts"><div><span>Program / Filière</span><strong>{programQuery.data?.name ?? "—"}</strong></div><div><span>Academic level</span><strong>{level?.name ?? "—"}</strong></div><div><span>Academic year</span><strong>{year?.label ?? "—"}</strong></div><div><span>Registration status</span><strong>{registration.status}</strong></div></div><div className="managed-student-module-list"><header><h3>Registered modules</h3><span>{modules.length} modules</span></header><div className="resource-table-wrapper"><table className="resource-table managed-student-module-table"><thead><tr><th>Module</th><th>Inscription</th><th>Status</th></tr></thead><tbody>{modules.map((module) => <tr key={module.id}><td><div className="table-contact"><strong>{module.subjectModuleTitle}</strong><small>{module.subjectModuleCode}</small></div></td><td>{module.inscriptionNumber > 1 ? <span className="second-inscription-badge">{module.inscriptionNumber === 2 ? "2nd" : `${module.inscriptionNumber}th`} inscription</span> : "First inscription"}</td><td><span className="managed-module-status">{module.status}</span></td></tr>)}</tbody></table></div></div></section>
      : section === "grades" ? <section className="managed-student-record-table"><header className="student-grades-context"><div className="student-grades-context__identity"><span>Published results</span><strong>{programQuery.data?.name ?? "Grades"}</strong><small>{level?.name ?? "Level"} · {selectedSemester?.semester.semesterName ?? "Semester"} · {year?.label ?? "Academic year"}</small></div></header>{finalGrades.length === 0 ? <div className="panel-empty"><strong>No finalized grades for this semester.</strong></div> : <div className="student-grades-table-wrap"><table className="student-grades-table managed-student-grades-table"><thead><tr><th>Module</th><th>Normal</th><th>Rattrapage</th><th>Final</th><th>Result</th></tr></thead><tbody>{finalGrades.map((finalGrade) => { const moduleGrades = grades.filter((item) => item.subjectModuleId === finalGrade.subjectModuleId); return <tr key={finalGrade.subjectModuleId}><td><span>{finalGrade.subjectModuleCode}</span><strong>{finalGrade.subjectModuleTitle}</strong></td><td><span className="student-grade-value"><strong>{moduleGrades.find((item) => item.sessionType === "NORMAL")?.gradeValue?.toFixed(2) ?? "—"}</strong><span>/ 20</span></span></td><td><span className="student-grade-value"><strong>{moduleGrades.find((item) => item.sessionType === "RATTRAPAGE")?.gradeValue?.toFixed(2) ?? "—"}</strong><span>/ 20</span></span></td><td><span className="student-grade-value"><strong>{finalGrade.finalGradeValue?.toFixed(2)}</strong><span>/ 20</span></span></td><td><span className={`student-result-status student-result-status--${finalGrade.moduleResultStatus?.toLowerCase()}`}>{finalGrade.moduleResultStatus ? resultLabels[finalGrade.moduleResultStatus] : "Pending"}</span></td></tr>; })}</tbody></table></div>}</section>
      : section === "attendance" ? <section className="managed-student-record-table"><header className="student-grades-context"><div className="student-grades-context__identity"><span>Attendance history</span><strong>Recorded absences</strong><small>{level?.name ?? "Level"} · {selectedSemester?.semester.semesterName ?? "Semester"} · {year?.label ?? "Academic year"}</small></div><div className="student-grades-context__summary"><span>Total</span><strong>{absences.length}</strong><small>absence{absences.length === 1 ? "" : "s"}</small></div></header>{absences.length === 0 ? <div className="panel-empty"><strong>No absence recorded for this semester.</strong></div> : <div className="resource-table-wrapper"><table className="resource-table"><thead><tr><th>Date</th><th>Module</th><th>Status</th><th>Note</th></tr></thead><tbody>{absences.map((absence) => <tr key={absence.id}><td>{new Date(`${absence.absenceDate}T00:00:00`).toLocaleDateString("en-GB")}</td><td><div className="table-contact"><strong>{absence.subjectModuleTitle}</strong><small>{absence.subjectModuleCode}</small></div></td><td>{absence.justified ? "Justified" : "Unjustified"}</td><td>{absence.justificationNote ?? "—"}</td></tr>)}</tbody></table></div>}</section>
      : section === "schedule" ? <section className="managed-student-record-schedule"><header className="panel-header panel-header--bordered professor-schedule-panel-header"><div><p className="management-kicker">Published planning</p><h2>Weekly Schedule</h2><p>{programQuery.data?.name ?? "Program"} · {level?.name ?? "Level"} · {selectedSemester?.semester.semesterName ?? "Semester"}</p></div></header>{schedule.length === 0 ? <div className="panel-empty"><strong>No published schedule for this semester.</strong></div> : <div className="managed-student-timetable-frame"><WeeklyTimetable entries={schedule} /></div>}</section>
      : <section className="managed-student-record-decision"><header className="student-grades-context"><div className="student-grades-context__identity"><span>Annual decision</span><strong>Progression</strong><small>{level?.name ?? "Level"} · {year?.label ?? "Academic year"}</small></div></header>{progressionQuery.isPending ? <div className="panel-empty">Loading progression decision...</div> : progressionQuery.isError || !progressionQuery.data ? <div className="panel-empty"><strong>No progression decision is available.</strong></div> : <div className="managed-student-decision-card"><div><span>Decision</span><strong>{decisionLabels[progressionQuery.data.decisionStatus]}</strong></div><div><span>Annual average</span><strong>{progressionQuery.data.annualAverage.toFixed(2)} / 20</strong></div><div><span>Outstanding modules</span><strong>{progressionQuery.data.outstandingModuleCount}</strong></div><div><span>Decision date</span><strong>{new Date(progressionQuery.data.decidedAt).toLocaleDateString("en-GB")}</strong></div></div>}</section>}
    </section>
  </div>;
}
