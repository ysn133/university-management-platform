import { saveStudentAcademicYearResultPdf } from "@/features/assessment/utils/save-academic-year-results-pdf";
import type { StudentAcademicContext } from "../api/student-overview-api";
import type { StudentGraduationDecision, StudentProgressionDecision } from "../api/student-decisions-api";

const decisionLabels: Record<StudentProgressionDecision["decisionStatus"], string> = {
  PROMOTED: "Promoted",
  PROMOTED_BY_COMPENSATION: "Promoted by compensation",
  PROMOTED_WITH_DEBT: "Promoted with module debt",
  LEVEL_VALIDATED: "Academic level validated",
  REPEAT: "Repeat academic level",
  FAILED: "Failed",
};

export function StudentAcademicYearResult({
  context,
  progression,
  graduation,
  loading,
}: {
  context?: StudentAcademicContext;
  progression?: StudentProgressionDecision;
  graduation?: StudentGraduationDecision;
  loading: boolean;
}) {
  if (loading) return <div className="panel-empty">Loading academic decision...</div>;
  if (!context) return <div className="panel-empty"><strong>No academic registration is available.</strong></div>;
  if (!progression && !graduation) return <div className="panel-empty"><strong>No academic-year decision has been published.</strong><p>The result will appear after the academic administration finalizes it.</p></div>;

  if (graduation) {
    return <div className="student-year-decision student-year-decision--graduation">
      <header><div><span>Graduation decision</span><h2>Graduated</h2><p>{graduation.degreeCycleName} · {graduation.programName}</p></div><strong>{graduation.graduationAverage.toFixed(2)}<small>/ 20</small></strong></header>
      <div className="student-year-decision-details"><div><span>Programme</span><strong>{graduation.programName}</strong></div><div><span>Final level</span><strong>{graduation.terminalAcademicLevelName}</strong></div><div><span>Academic year</span><strong>{graduation.academicYearLabel}</strong></div><div><span>Decision date</span><strong>{new Date(graduation.decidedAt).toLocaleDateString("en-GB")}</strong></div></div>
      <footer><p>Your graduation decision has been finalized by the academic administration.</p></footer>
    </div>;
  }

  if (!progression) return null;
  return <div className="student-year-decision">
    <header><div><span>Academic-year decision</span><h2>{decisionLabels[progression.decisionStatus]}</h2><p>{progression.academicLevelName} · {progression.academicYearLabel}</p></div><strong>{progression.annualAverage.toFixed(2)}<small>/ 20</small></strong></header>
    <div className="student-semester-decisions">{progression.semesterResults.map((semester) => <article key={semester.semesterId}><div><span>{semester.semesterName}</span><strong>{semester.semesterAverage.toFixed(2)} / 20</strong></div><i className={semester.resultStatus === "VALIDATED" ? "is-validated" : "is-not-validated"}>{semester.resultStatus === "VALIDATED" ? "Validated" : "Not validated"}</i><small>{semester.moduleResults.filter((module) => module.resultStatus === "V" || module.resultStatus === "AV").length} of {semester.moduleResults.length} modules validated</small></article>)}</div>
    <div className="student-year-decision-details"><div><span>Programme</span><strong>{progression.programName}</strong></div><div><span>Annual average</span><strong>{progression.annualAverage.toFixed(2)} / 20</strong></div><div><span>Outstanding modules</span><strong>{progression.outstandingModuleCount}</strong></div><div><span>Decision date</span><strong>{new Date(progression.decidedAt).toLocaleDateString("en-GB")}</strong></div></div>
    <footer><p>This decision combines the finalized results of the academic year.</p><button onClick={() => void saveStudentAcademicYearResultPdf(progression)} type="button">Download academic-year result</button></footer>
  </div>;
}
