import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getMyExams, professorOverviewKeys, type ProfessorExam } from "../api/professor-overview-api";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "Your exam schedule could not be loaded.";
}

function displayDate(value: string): string {
  return new Intl.DateTimeFormat("en-GB", { weekday: "long", day: "numeric", month: "long", year: "numeric" }).format(new Date(`${value}T00:00:00`));
}

function currentSemesterPeriod(exams: ProfessorExam[]): string | undefined {
  const today = new Date().toISOString().slice(0, 10);
  const periods = Array.from(new Set(exams.map((exam) => `${exam.semesterStartDate}:${exam.semesterEndDate}`)));
  return periods.find((period) => { const [start, end] = period.split(":"); return start <= today && today <= end; })
    ?? periods.filter((period) => period.split(":")[1] < today).sort().at(-1)
    ?? periods.sort()[0];
}

export function ProfessorExamCalendar({ exams, examHref }: { exams: ProfessorExam[]; examHref?: (exam: ProfessorExam) => string }) {
  const examsByDate = Array.from(exams.reduce((dates, exam) => {
    const day = dates.get(exam.examDate) ?? [];
    day.push(exam);
    dates.set(exam.examDate, day);
    return dates;
  }, new Map<string, ProfessorExam[]>()).entries());

  return <div className="professor-exam-calendar">{examsByDate.map(([date, dayExams]) => <section className="professor-exam-day" key={date}>
    <header><time dateTime={date}>{displayDate(date)}</time><span>{dayExams.length} {dayExams.length === 1 ? "exam" : "exams"}</span></header>
    <div>{dayExams.map((exam) => { const content = <>
      <div className="professor-exam-time"><strong>{exam.startTime.slice(0, 5)}</strong><span>{exam.endTime?.slice(0, 5)}</span></div>
      <div className="professor-exam-module"><span>{exam.subjectModuleCode}</span><strong>{exam.subjectModuleTitle}</strong><small>{exam.programFiliereCode} · {exam.academicLevelName} · {exam.semesterName}</small></div>
      <div className="professor-exam-class"><small>Class</small><strong>{exam.classGroupName}</strong></div>
      <div className="professor-exam-room"><small>{exam.rooms.length > 1 ? "Rooms" : "Room"}</small><strong>{exam.rooms.length ? exam.rooms.join(" · ") : "To be assigned"}</strong></div>
    </>; return examHref ? <Link className="professor-exam-card professor-exam-card--link" key={exam.id} to={examHref(exam)}>{content}<span className="professor-exam-open">Open grades →</span></Link> : <article className="professor-exam-card" key={exam.id}>{content}</article>; })}</div>
  </section>)}</div>;
}

export function ProfessorExamSchedulePage() {
  const [requestedYearId, setRequestedYearId] = useState("");
  const [requestedLevelId, setRequestedLevelId] = useState("");
  const [requestedSemesterId, setRequestedSemesterId] = useState("");
  const [requestedSession, setRequestedSession] = useState<"" | ProfessorExam["sessionType"]>("");
  const examsQuery = useQuery({ queryKey: professorOverviewKeys.exams(), queryFn: getMyExams });
  const exams = examsQuery.data ?? [];
  const academicYears = Array.from(new Map(exams.map((exam) => [exam.academicYearId, exam.academicYearLabel])).entries());
  const academicYearId = academicYears.some(([id]) => id === requestedYearId)
    ? requestedYearId
    : exams.find((exam) => exam.academicYearStatus === "ACTIVE")?.academicYearId ?? academicYears.at(-1)?.[0] ?? "";
  const yearExams = exams.filter((exam) => exam.academicYearId === academicYearId);
  const academicLevels = Array.from(new Map(yearExams.map((exam) => [exam.academicLevelId, exam.academicLevelName])).entries());
  const academicLevelId = academicLevels.some(([id]) => id === requestedLevelId) ? requestedLevelId : academicLevels[0]?.[0] ?? "";
  const levelExams = yearExams.filter((exam) => exam.academicLevelId === academicLevelId);
  const semesters = Array.from(new Map(levelExams.map((exam) => [exam.semesterId, exam.semesterName])).entries());
  const currentPeriod = currentSemesterPeriod(levelExams);
  const defaultSemesterId = levelExams.find((exam) => `${exam.semesterStartDate}:${exam.semesterEndDate}` === currentPeriod)?.semesterId ?? semesters[0]?.[0] ?? "";
  const semesterId = semesters.some(([id]) => id === requestedSemesterId) ? requestedSemesterId : defaultSemesterId;
  const semesterExams = levelExams.filter((exam) => exam.semesterId === semesterId);
  const sessions = Array.from(new Set(semesterExams.map((exam) => exam.sessionType)));
  const sessionType = sessions.includes(requestedSession as ProfessorExam["sessionType"])
    ? requestedSession as ProfessorExam["sessionType"]
    : sessions.includes("NORMAL") ? "NORMAL" : sessions[0];
  const filteredExams = semesterExams.filter((exam) => exam.sessionType === sessionType);
  const currentYearLabel = academicYears.find(([id]) => id === academicYearId)?.[1];
  const currentLevelLabel = academicLevels.find(([id]) => id === academicLevelId)?.[1];
  const currentSemesterLabel = semesters.find(([id]) => id === semesterId)?.[1];

  return <div className="management-page professor-exam-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Assessment calendar</p><h1>Exam Schedule</h1><p>Review one published examination schedule at a time.</p></div></header>
    {examsQuery.error && <div className="management-alert management-alert--error">{errorMessage(examsQuery.error)}</div>}
    <section className="management-panel professor-exam-panel">
      <header className="professor-exam-panel-header"><div><p className="management-kicker">Published planning</p><h2>{sessionType === "RATTRAPAGE" ? "Rattrapage Session" : "Normal Session"}</h2><p>{currentLevelLabel} · {currentSemesterLabel} · {currentYearLabel}</p></div><strong>{filteredExams.length}<span>exams</span></strong></header>
      <div className="professor-exam-context-selector">
        <label><span>Academic Year</span><select onChange={(event) => { setRequestedYearId(event.target.value); setRequestedLevelId(""); setRequestedSemesterId(""); setRequestedSession(""); }} value={academicYearId}>{academicYears.map(([id, label]) => <option key={id} value={id}>{label}</option>)}</select></label>
        <label><span>Academic Level</span><select onChange={(event) => { setRequestedLevelId(event.target.value); setRequestedSemesterId(""); setRequestedSession(""); }} value={academicLevelId}>{academicLevels.map(([id, name]) => <option key={id} value={id}>{name}</option>)}</select></label>
        <label><span>Semester</span><select onChange={(event) => { setRequestedSemesterId(event.target.value); setRequestedSession(""); }} value={semesterId}>{semesters.map(([id, name]) => <option key={id} value={id}>{name}</option>)}</select></label>
        <label><span>Session</span><select onChange={(event) => setRequestedSession(event.target.value as ProfessorExam["sessionType"])} value={sessionType}>{sessions.map((session) => <option key={session} value={session}>{session === "NORMAL" ? "Normal session" : "Rattrapage"}</option>)}</select></label>
      </div>
      {examsQuery.isPending ? <div className="panel-empty">Loading your exam schedule...</div> : exams.length === 0 ? <div className="panel-empty"><strong>No published exams yet.</strong><p>Exams will appear here after their schedule is published.</p></div> : filteredExams.length === 0 ? <div className="panel-empty"><strong>No published exam schedule for this context.</strong></div> : <ProfessorExamCalendar examHref={(exam) => `/professor/modules/${exam.subjectModuleId}/classes/${exam.classGroupId}?tab=grades&examId=${exam.id}`} exams={filteredExams} />}
    </section>
  </div>;
}
