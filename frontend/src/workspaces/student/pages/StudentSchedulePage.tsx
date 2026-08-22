import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { WeeklyTimetable } from "@/features/scheduling/components/WeeklyTimetable";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getStudentScheduleEntries, studentScheduleKeys } from "../api/student-schedule-api";
import { getMyExamInvitations, studentOverviewKeys, type StudentExamInvitation } from "../api/student-overview-api";
import { saveStudentClassSchedulePdf, saveStudentExamSchedulePdf } from "../utils/save-student-schedule-pdf";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The schedule could not be loaded.";
}

function examDateParts(value: string) {
  const date = new Date(`${value}T00:00:00`);
  return {
    day: new Intl.DateTimeFormat("en-GB", { weekday: "long" }).format(date),
    date: new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "2-digit", year: "numeric" }).format(date),
  };
}

function StudentExamTable({ exams }: { exams: StudentExamInvitation[] }) {
  const examsByDate = Array.from(exams.reduce((dates, exam) => {
    const day = dates.get(exam.examDate) ?? [];
    day.push(exam);
    dates.set(exam.examDate, day);
    return dates;
  }, new Map<string, StudentExamInvitation[]>()).entries());

  return <div className="exam-plan-table-wrap student-exam-plan-table"><table className="exam-plan-table"><thead><tr><th>Day</th><th>Module</th><th>Time</th><th>Room</th><th>Group</th></tr></thead>{examsByDate.map(([date, dayExams]) => {
    const label = examDateParts(date);
    return <tbody key={date}>{dayExams.sort((left, right) => left.startTime.localeCompare(right.startTime)).map((exam, index) => <tr key={exam.id}>
      {index === 0 && <th className="exam-plan-date" rowSpan={dayExams.length}><span>{label.day}</span><strong>{label.date}</strong></th>}
      <td className="exam-plan-module"><span>{exam.subjectModuleCode}</span>{exam.subjectModuleTitle}</td>
      <td className="exam-plan-time">{exam.startTime.slice(0, 5)} - {exam.endTime?.slice(0, 5) ?? "—"}</td>
      <td>{exam.roomCode ?? "Room not assigned"}</td>
      <td>{exam.examGroupLabel ?? "Assigned candidate"}</td>
    </tr>)}</tbody>;
  })}</table></div>;
}

export function StudentSchedulePage() {
  const [academicYearId, setAcademicYearId] = useState("");
  const [semesterId, setSemesterId] = useState("");
  const [activeTab, setActiveTab] = useState<"classes" | "exams">("classes");
  const [examSession, setExamSession] = useState<"NORMAL" | "RATTRAPAGE" | "">("");
  const [exportingPdf, setExportingPdf] = useState(false);
  const query = useQuery({ queryKey: studentScheduleKeys.entries(), queryFn: getStudentScheduleEntries });
  const examsQuery = useQuery({ queryKey: studentOverviewKeys.exams(), queryFn: getMyExamInvitations });
  const allEntries = query.data ?? [];
  const allExams = examsQuery.data ?? [];
  const years = Array.from(new Map([...allEntries.map((entry) => [entry.academicYearId, { id: entry.academicYearId, label: entry.academicYearLabel, status: entry.academicYearStatus }] as const), ...allExams.map((exam) => [exam.academicYearId, { id: exam.academicYearId, label: exam.academicYearLabel, status: exam.academicYearStatus }] as const)]).values());

  useEffect(() => {
    if (academicYearId || !years.length) return;
    setAcademicYearId(years.find((year) => year.status === "ACTIVE")?.id ?? years[0].id);
  }, [academicYearId, years]);

  const yearEntries = allEntries.filter((entry) => entry.academicYearId === academicYearId);
  const yearExams = allExams.filter((exam) => exam.academicYearId === academicYearId);
  const semesters = Array.from(new Map([...yearEntries.map((entry) => [entry.semesterId, {
    id: entry.semesterId,
    name: entry.semesterName,
    startDate: entry.semesterStartDate,
    endDate: entry.semesterEndDate,
  }] as const), ...yearExams.map((exam) => [exam.semesterId, { id: exam.semesterId, name: exam.semesterName, startDate: exam.semesterStartDate, endDate: exam.semesterEndDate }] as const)]).values()).sort((left, right) => left.name.localeCompare(right.name, undefined, { numeric: true }));

  useEffect(() => {
    if (!academicYearId || !semesters.length) return;
    const today = new Date().toISOString().slice(0, 10);
    const activeSemester = semesters.find((semester) => semester.startDate <= today && semester.endDate >= today);
    setSemesterId((current) => semesters.some((semester) => semester.id === current) ? current : activeSemester?.id ?? semesters[0].id);
  }, [academicYearId, semesters.map((semester) => semester.id).join(",")]);

  const entries = yearEntries.filter((entry) => entry.semesterId === semesterId);
  const semesterExams = yearExams.filter((exam) => exam.semesterId === semesterId);
  const examSessions = Array.from(new Set(semesterExams.map((exam) => exam.sessionType)));
  const selectedExamSession = examSessions.includes(examSession as "NORMAL" | "RATTRAPAGE") ? examSession as "NORMAL" | "RATTRAPAGE" : examSessions.includes("NORMAL") ? "NORMAL" : examSessions[0] ?? "";
  const visibleExams = semesterExams.filter((exam) => exam.sessionType === selectedExamSession);
  const selectedYear = years.find((year) => year.id === academicYearId);
  const selectedSemester = semesters.find((semester) => semester.id === semesterId);
  const timetableEntries = entries.map((entry) => ({
    id: entry.id, dayOfWeek: entry.dayOfWeek, startTime: entry.startTime, endTime: entry.endTime,
    title: entry.subjectModuleTitle,
    context: `${entry.subjectModuleCode} · ${entry.academicLevelName} · ${entry.semesterName}`,
    detail: `${entry.componentType === "COURSE" ? "Course" : entry.componentType} · ${entry.teachingGroupName} · ${entry.professorName}`,
    room: [entry.blockCode, entry.roomCode].filter(Boolean).join(" · ") || "Room pending",
    componentType: entry.componentType,
  }));

  async function downloadSchedulePdf() {
    setExportingPdf(true);
    try {
      const context = { academicYear: selectedYear?.label ?? "Academic year", semester: selectedSemester?.name ?? "Semester" };
      if (activeTab === "classes") await saveStudentClassSchedulePdf(timetableEntries, context);
      else await saveStudentExamSchedulePdf(visibleExams, context, selectedExamSession);
    } finally {
      setExportingPdf(false);
    }
  }

  return <div className="management-page professor-schedule-page student-schedule-page">
    <header className="management-page-header professor-schedule-page-header">
      <div><p className="management-kicker">Academic timetable</p><h1>My Schedule</h1><p>Your published classes for the selected academic period.</p></div>
      <div className="professor-schedule-period"><span>Current view</span><strong>{selectedYear?.label ?? "Academic year"}</strong><small>{selectedSemester?.name ?? "Semester"}</small></div>
    </header>
    <nav aria-label="Schedule views" className="curriculum-section-tabs student-schedule-tabs" role="tablist"><button aria-selected={activeTab === "classes"} onClick={() => setActiveTab("classes")} role="tab" type="button">Class Schedule</button><button aria-selected={activeTab === "exams"} onClick={() => setActiveTab("exams")} role="tab" type="button">Exam Schedule</button></nav>
    <section className="management-panel professor-schedule-panel">
      <header className="panel-header panel-header--bordered professor-schedule-panel-header">
        <div><p className="management-kicker">Published planning</p><h2>{activeTab === "classes" ? "Weekly Schedule" : selectedExamSession === "RATTRAPAGE" ? "Rattrapage Session" : "Normal Session"}</h2><p>{activeTab === "classes" ? "Course, TD, and TP sessions assigned to your groups." : "Your generated examination invitations and assigned rooms."}</p></div>
        <div className="student-schedule-header-actions"><div className="professor-schedule-selectors">
            <label><span>Academic year</span><select disabled={query.isPending} onChange={(event) => setAcademicYearId(event.target.value)} value={academicYearId}>{years.map((year) => <option key={year.id} value={year.id}>{year.label}</option>)}</select></label>
            <label><span>Semester</span><select disabled={!semesters.length} onChange={(event) => setSemesterId(event.target.value)} value={semesterId}>{semesters.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label>
            {activeTab === "exams" && <label><span>Session</span><select disabled={!examSessions.length} onChange={(event) => setExamSession(event.target.value as "NORMAL" | "RATTRAPAGE")} value={selectedExamSession}>{examSessions.map((session) => <option key={session} value={session}>{session === "NORMAL" ? "Normal session" : "Rattrapage"}</option>)}</select></label>}
          </div><button aria-label="Download schedule as PDF" className="student-schedule-download" disabled={exportingPdf || (activeTab === "classes" ? timetableEntries.length === 0 : visibleExams.length === 0)} onClick={() => void downloadSchedulePdf()} title="Download as PDF" type="button"><svg aria-hidden="true" fill="none" viewBox="0 0 24 24"><path d="M12 3v12m0 0 4-4m-4 4-4-4M5 20h14" /></svg><span>{exportingPdf ? "Preparing" : "Download PDF"}</span></button>
        </div>
      </header>
      {activeTab === "classes" ? query.isPending ? <div className="panel-empty">Loading your schedule...</div>
        : query.error ? <div className="panel-empty panel-empty--error">{errorMessage(query.error)}</div>
        : timetableEntries.length === 0 ? <div className="panel-empty"><strong>No published sessions for this period.</strong><p>Select another academic year or semester to view historical schedules.</p></div>
        : <WeeklyTimetable entries={timetableEntries} />
        : examsQuery.isPending ? <div className="panel-empty">Loading your exam schedule...</div>
        : examsQuery.error ? <div className="panel-empty panel-empty--error">{errorMessage(examsQuery.error)}</div>
        : visibleExams.length === 0 ? <div className="panel-empty"><strong>No published exam invitation for this context.</strong><p>Eligible exams appear after candidate lists are generated and published.</p></div>
        : <StudentExamTable exams={visibleExams} />}
    </section>
  </div>;
}
