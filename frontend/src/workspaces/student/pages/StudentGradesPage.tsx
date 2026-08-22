import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getMyAcademicContexts, getMyStudentGrades, studentOverviewKeys, type StudentAcademicContext, type StudentOverviewGrade } from "../api/student-overview-api";
import { getMyGraduationDecisions, getMyProgressionDecisions, studentDecisionKeys } from "../api/student-decisions-api";
import { StudentAcademicYearResult } from "../components/StudentAcademicYearResult";

type GradeView = "NORMAL" | "RATTRAPAGE" | "FINAL" | "ACADEMIC_YEAR";

const resultLabels = { V: "Validated", AV: "Compensated", NV: "Not validated" } as const;

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "Your results could not be loaded.";
}

function currentRegistration(contexts: StudentAcademicContext[], today: string) {
  const active = contexts.filter((context) => context.academicYearStatus === "ACTIVE");
  return active.find((context) => context.semesterStartDate <= today && context.semesterEndDate >= today)
    ?? [...active].filter((context) => context.semesterStartDate > today).sort((left, right) => left.semesterStartDate.localeCompare(right.semesterStartDate))[0]
    ?? [...active].filter((context) => context.semesterEndDate < today).sort((left, right) => right.semesterEndDate.localeCompare(left.semesterEndDate))[0]
    ?? contexts[0];
}

function uniqueFinalGrades(grades: StudentOverviewGrade[]) {
  const byModule = new Map<string, StudentOverviewGrade>();
  grades.forEach((grade) => {
    const previous = byModule.get(grade.subjectModuleId);
    if (!previous || (!previous.moduleResultStatus && grade.moduleResultStatus) || grade.publishedAt > previous.publishedAt) {
      byModule.set(grade.subjectModuleId, grade);
    }
  });
  return Array.from(byModule.values()).filter((grade) => grade.finalGradeValue !== null || grade.moduleResultStatus !== null);
}

function GradeValue({ grade, final }: { grade: StudentOverviewGrade; final?: boolean }) {
  const value = final ? grade.finalGradeValue : grade.gradeValue;
  if (grade.zeroGradeReason === "ABSENT" && !final) return <div className="student-grade-value student-grade-value--absent"><strong>0.00</strong><span>Absent</span></div>;
  return <div className="student-grade-value"><strong>{value?.toFixed(2) ?? "—"}</strong><span>/ 20</span></div>;
}

export function StudentGradesPage() {
  const [academicYearId, setAcademicYearId] = useState("");
  const [semesterId, setSemesterId] = useState("");
  const [view, setView] = useState<GradeView>("NORMAL");
  const gradesQuery = useQuery({ queryKey: studentOverviewKeys.grades(), queryFn: getMyStudentGrades });
  const contextsQuery = useQuery({ queryKey: studentOverviewKeys.academicContexts(), queryFn: getMyAcademicContexts });
  const progressionQuery = useQuery({ queryKey: studentDecisionKeys.progression(), queryFn: getMyProgressionDecisions });
  const graduationQuery = useQuery({ queryKey: studentDecisionKeys.graduation(), queryFn: getMyGraduationDecisions });
  const contexts = contextsQuery.data ?? [];
  const grades = gradesQuery.data ?? [];
  const today = new Date().toISOString().slice(0, 10);
  const defaultContext = currentRegistration(contexts, today);
  const years = Array.from(new Map(contexts.map((context) => [context.academicYearId, { id: context.academicYearId, label: context.academicYearLabel }])).values());

  useEffect(() => {
    if (!academicYearId && defaultContext) setAcademicYearId(defaultContext.academicYearId);
  }, [academicYearId, defaultContext?.academicYearId]);

  const yearContexts = contexts.filter((context) => context.academicYearId === academicYearId);
  const semesters = [...yearContexts].sort((left, right) => left.semesterStartDate.localeCompare(right.semesterStartDate));

  useEffect(() => {
    if (!academicYearId || !semesters.length) return;
    setSemesterId((current) => semesters.some((context) => context.semesterId === current)
      ? current
      : defaultContext?.academicYearId === academicYearId ? defaultContext.semesterId : semesters[0].semesterId);
  }, [academicYearId, semesters.map((context) => context.semesterId).join(","), defaultContext?.semesterId]);

  const selectedContext = contexts.find((context) => context.academicYearId === academicYearId && context.semesterId === semesterId);
  const selectedYearContext = yearContexts[0];
  const progression = (progressionQuery.data ?? []).find((decision) => decision.academicRegistrationId === selectedYearContext?.academicRegistrationId);
  const graduation = (graduationQuery.data ?? []).find((decision) => decision.academicYearLabel === selectedYearContext?.academicYearLabel && decision.terminalAcademicLevelName === selectedYearContext?.academicLevelName);
  const contextGrades = grades.filter((grade) => grade.academicYearId === academicYearId && grade.semesterId === semesterId);
  const visibleGrades = (view === "FINAL"
    ? uniqueFinalGrades(contextGrades)
    : view === "NORMAL" || view === "RATTRAPAGE"
      ? contextGrades.filter((grade) => grade.sessionType === view)
      : [])
    .sort((left, right) => left.subjectModuleTitle.localeCompare(right.subjectModuleTitle));
  const numericGrades = view === "FINAL"
    ? visibleGrades.map((grade) => grade.finalGradeValue).filter((grade): grade is number => grade !== null)
    : [];
  const average = numericGrades.length ? numericGrades.reduce((sum, grade) => sum + grade, 0) / numericGrades.length : null;
  const error = gradesQuery.error ?? contextsQuery.error ?? progressionQuery.error ?? graduationQuery.error;

  return <div className="management-page student-grades-page">
    <header className="management-page-header student-grades-header"><div><p className="management-kicker">Academic record</p><h1>Grades</h1><p>Published results for your academic registrations.</p></div>{selectedContext && <div className="student-grades-current"><span>Selected context</span><strong>{selectedContext.academicYearLabel}</strong><small>{selectedContext.academicLevelName} · {selectedContext.semesterName}</small></div>}</header>

    {error && <div className="management-alert management-alert--error">{errorMessage(error)}</div>}

    <section className="management-panel student-grades-panel">
      <div className="student-grades-toolbar">
        <div className="professor-grade-view-tabs" role="tablist"><button aria-selected={view === "NORMAL"} onClick={() => setView("NORMAL")} role="tab" type="button">Normal</button><button aria-selected={view === "RATTRAPAGE"} onClick={() => setView("RATTRAPAGE")} role="tab" type="button">Rattrapage</button><button aria-selected={view === "FINAL"} onClick={() => setView("FINAL")} role="tab" type="button">Final</button><button aria-selected={view === "ACADEMIC_YEAR"} onClick={() => setView("ACADEMIC_YEAR")} role="tab" type="button">Academic year</button></div>
        <div className="student-grades-selectors"><label><span>Academic year</span><select disabled={contextsQuery.isPending || !years.length} onChange={(event) => { setAcademicYearId(event.target.value); setSemesterId(""); }} value={academicYearId}>{years.map((year) => <option key={year.id} value={year.id}>{year.label}</option>)}</select></label>{view !== "ACADEMIC_YEAR" && <label><span>Semester</span><select disabled={!semesters.length} onChange={(event) => setSemesterId(event.target.value)} value={semesterId}>{semesters.map((semester) => <option key={semester.semesterRegistrationId} value={semester.semesterId}>{semester.semesterName}</option>)}</select></label>}</div>
      </div>

      {view !== "ACADEMIC_YEAR" && <header className="student-grades-context"><div><span>{view === "NORMAL" ? "Normal session" : view === "RATTRAPAGE" ? "Rattrapage session" : "Final module results"}</span><strong>{selectedContext?.programFiliereName ?? "Academic results"}</strong><small>{selectedContext ? `${selectedContext.academicLevelName} · ${selectedContext.semesterName} · ${selectedContext.academicYearLabel}` : "Select an academic context"}</small></div>{view === "FINAL" && <div><span>Average</span><strong>{average?.toFixed(2) ?? "—"}</strong><small>{visibleGrades.length} module{visibleGrades.length === 1 ? "" : "s"}</small></div>}</header>}

      {view === "ACADEMIC_YEAR" ? <StudentAcademicYearResult context={selectedYearContext} graduation={graduation} loading={contextsQuery.isPending || progressionQuery.isPending || graduationQuery.isPending} progression={progression} /> : gradesQuery.isPending || contextsQuery.isPending ? <div className="panel-empty">Loading results...</div> : !selectedContext ? <div className="panel-empty"><strong>No academic registration is available.</strong></div> : visibleGrades.length === 0 ? <div className="panel-empty"><strong>No published {view === "NORMAL" ? "Normal Session" : view === "RATTRAPAGE" ? "Rattrapage" : "Final"} result for this semester.</strong></div> : <div className="student-grades-table-wrap"><table className="student-grades-table"><thead><tr><th>Module</th><th>Session</th><th>Inscription</th><th>Grade</th>{view === "FINAL" && <th>Result</th>}</tr></thead><tbody>{visibleGrades.map((grade) => <tr key={view === "FINAL" ? grade.subjectModuleId : grade.gradeRecordId}><td><span>{grade.subjectModuleCode}</span><strong>{grade.subjectModuleTitle}</strong></td><td>{view === "FINAL" ? "Final" : grade.sessionType === "NORMAL" ? "Normal" : "Rattrapage"}</td><td>{grade.inscriptionNumber === 1 ? "First" : `${grade.inscriptionNumber}${grade.inscriptionNumber === 2 ? "nd" : "th"}`}</td><td><GradeValue final={view === "FINAL"} grade={grade} /></td>{view === "FINAL" && <td><span className={`student-result-status student-result-status--${grade.moduleResultStatus?.toLowerCase() ?? "pending"}`}>{grade.moduleResultStatus ? resultLabels[grade.moduleResultStatus] : "Pending"}</span></td>}</tr>)}</tbody></table></div>}
    </section>
  </div>;
}
