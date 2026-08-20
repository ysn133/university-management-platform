import { useQuery } from "@tanstack/react-query";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { getFinalResults } from "@/features/assessment/api/final-results-api";
import { getMyTeachingAssignments, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ProfessorFinalGradeSheet } from "../components/ProfessorFinalGradeSheet";
import { ProfessorGradeSheet } from "../components/ProfessorGradeSheet";
import { getMyExams, getMyModuleResponsibilities, professorOverviewKeys } from "../api/professor-overview-api";

type GradeView = "NORMAL" | "RATTRAPAGE" | "FINAL";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The grade workspace could not be loaded.";
}

export function ProfessorGradeDetailsPage() {
  const { subjectModuleId = "", classGroupId = "" } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const requestedSession = searchParams.get("session");
  const gradeView: GradeView = requestedSession === "RATTRAPAGE" ? "RATTRAPAGE" : requestedSession === "FINAL" ? "FINAL" : "NORMAL";
  const requestedExamId = searchParams.get("examId") ?? "";
  const responsibilitiesQuery = useQuery({ queryKey: professorOverviewKeys.responsibilities(), queryFn: getMyModuleResponsibilities });
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const examsQuery = useQuery({ queryKey: professorOverviewKeys.exams(), queryFn: getMyExams });
  const responsibility = (responsibilitiesQuery.data ?? []).find((item) => item.status === "ACTIVE" && item.subjectModuleId === subjectModuleId && item.classGroupId === classGroupId);
  const assignment = (assignmentsQuery.data ?? []).find((item) => item.status === "ACTIVE" && item.subjectModuleId === subjectModuleId && item.semesterId === responsibility?.semesterId);
  const classExams = (examsQuery.data ?? []).filter((exam) => exam.subjectModuleId === subjectModuleId && exam.classGroupId === classGroupId && exam.academicYearId === responsibility?.academicYearId && exam.semesterId === responsibility?.semesterId);
  const sessionExams = classExams.filter((exam) => exam.sessionType === gradeView);
  const selectedExam = sessionExams.find((exam) => exam.id === requestedExamId) ?? sessionExams[0];
  const finalResultsQuery = useQuery({
    queryKey: ["professor-final-results", responsibility?.semesterId, classGroupId, subjectModuleId],
    queryFn: () => getFinalResults(responsibility!.semesterId, classGroupId, subjectModuleId),
    enabled: gradeView === "FINAL" && Boolean(responsibility),
  });
  const loading = responsibilitiesQuery.isPending || assignmentsQuery.isPending || examsQuery.isPending;
  const loadError = responsibilitiesQuery.error ?? assignmentsQuery.error ?? examsQuery.error;
  const fromExamSchedule = searchParams.get("from") === "exams";

  function selectView(view: GradeView) {
    setSearchParams({ session: view, ...(fromExamSchedule ? { from: "exams" } : { from: "grades" }) });
  }

  return <div className="management-page professor-grade-details-page">
    <Link className="management-back-link" to={fromExamSchedule ? "/professor/exams" : "/professor/grades"}>← {fromExamSchedule ? "Back to Exam Schedule" : "Back to Grades"}</Link>
    {loadError && <div className="management-alert management-alert--error">{errorMessage(loadError)}</div>}
    {loading ? <div className="management-panel panel-empty">Loading grade workspace...</div> : !responsibility ? <div className="management-panel panel-empty"><strong>Grade context unavailable.</strong><p>This module and Class Group are not assigned to your account.</p></div> : <>
      <header className="curriculum-header professor-grade-details-header"><span className="curriculum-program-code">{responsibility.subjectModuleCode}</span><div><p className="management-kicker">Grades · Class Group</p><h1>{responsibility.subjectModuleTitle}</h1><p>{responsibility.classGroupName}</p></div><div className="professor-class-context"><strong>{assignment?.programFiliereName}</strong><span>{assignment?.academicLevelName} · {responsibility.semesterName} · {responsibility.academicYearLabel}</span></div></header>
      <section className="management-panel professor-class-workspace professor-class-grades professor-grade-details-sheet">
        <div className="professor-grade-view-tabs" role="tablist"><button aria-selected={gradeView === "NORMAL"} onClick={() => selectView("NORMAL")} role="tab" type="button">Normal</button><button aria-selected={gradeView === "RATTRAPAGE"} onClick={() => selectView("RATTRAPAGE")} role="tab" type="button">Rattrapage</button><button aria-selected={gradeView === "FINAL"} onClick={() => selectView("FINAL")} role="tab" type="button">Final</button></div>
        <header className="panel-header panel-header--bordered"><div><p className="management-kicker">Assessment · {gradeView === "FINAL" ? "Final Result" : gradeView === "NORMAL" ? "Normal Session" : "Rattrapage"}</p><h2>{gradeView === "FINAL" ? "Final Grades" : "Grade Sheet"}</h2><p>{gradeView === "FINAL" ? "Read-only results generated by academic administration." : "Enter and review all student results for this examination session."}</p></div>{gradeView !== "FINAL" && sessionExams.length > 1 && <label className="professor-grade-exam-select"><span>Exam</span><select onChange={(event) => setSearchParams({ session: gradeView, examId: event.target.value, ...(fromExamSchedule ? { from: "exams" } : { from: "grades" }) })} value={selectedExam?.id}>{sessionExams.map((exam) => <option key={exam.id} value={exam.id}>{exam.examDate}</option>)}</select></label>}</header>
        {gradeView === "FINAL" ? <ProfessorFinalGradeSheet context={{ academicLevel: assignment?.academicLevelName, academicYear: responsibility.academicYearLabel, label: `${assignment?.programFiliereName ?? "Program"} · ${responsibility.semesterName} · ${responsibility.classGroupName}`, program: assignment?.programFiliereName, semester: responsibility.semesterName }} isLoading={finalResultsQuery.isPending} results={finalResultsQuery.data ?? []} /> : !selectedExam ? <div className="panel-empty"><strong>No published {gradeView === "NORMAL" ? "Normal Session" : "Rattrapage"} exam is available.</strong></div> : <ProfessorGradeSheet exam={selectedExam} />}
      </section>
    </>}
  </div>;
}
