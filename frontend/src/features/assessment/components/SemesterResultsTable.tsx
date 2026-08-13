import { useDeferredValue, useState } from "react";
import { Link } from "react-router-dom";
import { getStudent } from "@/features/student-registration/api/student-registration-api";
import { getFinalResults, type FinalResult } from "../api/final-results-api";
import { getSemesterResults, type SemesterResult } from "../api/semester-results-api";
import type { GradeDocumentContext } from "../utils/save-student-session-grades-pdf";
import { saveSemesterResultPdf } from "../utils/save-semester-result-pdf";

interface Props {
  context: GradeDocumentContext;
  finalResults: FinalResult[];
  isLoading: boolean;
  results: SemesterResult[];
  onOpenOriginalSemester?: (academicYearId: string, academicLevelId: string, semesterId: string) => void;
  studentDetailsPath?: (studentId: string) => string;
}

export function SemesterResultsTable({ context, finalResults, isLoading, onOpenOriginalSemester, results, studentDetailsPath }: Props) {
  const [search, setSearch] = useState("");
  const [exporting, setExporting] = useState("");
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());

  if (isLoading) return <div className="panel-empty">Loading semester results...</div>;
  if (!results.length) {
    return <div className="panel-empty"><strong>Semester results have not been generated.</strong><p>Generate them after the final module results are complete.</p></div>;
  }

  const modules = Array.from(new Map(finalResults.map((result) => [result.subjectModuleId, {
    code: result.subjectModuleCode,
    id: result.subjectModuleId,
    title: result.subjectModuleTitle,
  }])).values());
  const resultByStudentModule = new Map(finalResults.map((result) => [`${result.studentId}:${result.subjectModuleId}`, result]));
  const visible = deferredSearch
    ? results.filter((result) => `${result.firstName} ${result.lastName} ${result.apogeeCode}`.toLowerCase().includes(deferredSearch))
    : results;

  async function download(result: SemesterResult) {
    setExporting(result.id);
    try {
      await saveSemesterResultPdf(
        result,
        finalResults.filter((item) => item.studentId === result.studentId),
        context,
        await getStudent(result.studentId),
      );
    } finally {
      setExporting("");
    }
  }

  async function downloadUpdated(result: SemesterResult, currentModuleResults: FinalResult[]) {
    if (!result.originalSemesterId || !result.originalClassGroupId) return;
    setExporting(result.id);
    try {
      const [originalResults, originalSemesterResults, profile] = await Promise.all([
        getFinalResults(result.originalSemesterId, result.originalClassGroupId),
        getSemesterResults(result.originalSemesterId, result.originalClassGroupId),
        getStudent(result.studentId),
      ]);
      const originalStudentResult = originalSemesterResults.find((item) => item.studentId === result.studentId);
      if (!originalStudentResult) throw new Error("Original semester result not found");
      const replacements = new Map(currentModuleResults.map((item) => [item.subjectModuleCode.toLowerCase(), item]));
      const effectiveResults = originalResults.filter((item) => item.studentId === result.studentId).map((item) => replacements.get(item.subjectModuleCode.toLowerCase()) ?? item);
      const graded = effectiveResults.filter((item) => item.finalGrade !== null);
      const average = graded.reduce((sum, item) => sum + item.finalGrade!, 0) / graded.length;
      const validated = effectiveResults.every((item) => item.resultStatus === "V" || item.resultStatus === "AV");
      const updatedResult: SemesterResult = {
        ...originalStudentResult,
        compensatedModuleCount: effectiveResults.filter((item) => item.resultStatus === "AV").length,
        nonValidatedModuleCount: effectiveResults.filter((item) => item.resultStatus === "NV").length,
        resultStatus: validated ? "VALIDATED" : "NON_VALIDATED",
        semesterAverage: average,
        validatedModuleCount: effectiveResults.filter((item) => item.resultStatus === "V").length,
      };
      await saveSemesterResultPdf(updatedResult, effectiveResults, {
        academicLevel: result.originalAcademicLevelName ?? undefined,
        academicYear: result.originalAcademicYearLabel ?? undefined,
        label: [context.program, result.originalAcademicLevelName, result.originalSemesterName, result.originalAcademicYearLabel].filter(Boolean).join(" · "),
        program: context.program,
        programPath: context.programPath,
        semester: result.originalSemesterName ?? undefined,
      }, profile);
    } finally {
      setExporting("");
    }
  }

  return <div className="final-result-matrix-wrap semester-result-matrix-wrap">
    <div className="final-result-matrix-heading">
      <div><span>Semester results</span><strong>Results by student</strong></div>
      <div className="final-result-matrix-legend"><span><i className="is-v" />Validated</span><span><i className="is-av" />Compensated</span><span><i className="is-nv" />Not validated</span></div>
    </div>
    <div className="final-result-matrix-controls">
      <label className="final-result-student-search"><span>Search student</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Search by name or Apogee" type="search" value={search} /></label>
      <span>{visible.length} of {results.length} students</span>
    </div>
    <table className="final-result-matrix semester-result-matrix">
      <thead><tr>
        <th className="final-result-matrix__student">Student</th>
        <th className="final-result-matrix__apogee">Apogee</th>
        {modules.map((module) => <th key={module.id} title={module.title}><strong>{module.code}</strong></th>)}
        <th className="final-result-matrix__average">Average</th>
        <th className="semester-result-matrix__status">Semester result</th>
        <th aria-label="Download" className="final-result-matrix__download" />
      </tr></thead>
      <tbody>
        {visible.map((semesterResult) => {
          const studentModuleResults = finalResults.filter((result) => result.studentId === semesterResult.studentId);
          const secondInscriptionOnly = semesterResult.secondInscriptionOnly;
          const identity = <><span className="final-result-student-monogram">{semesterResult.firstName[0]}{semesterResult.lastName[0]}</span><span className="semester-result-student-identity"><strong title={`${semesterResult.firstName} ${semesterResult.lastName}`}>{semesterResult.firstName} {semesterResult.lastName}</strong>{secondInscriptionOnly && <small>Second inscription</small>}</span></>;
          return <tr key={semesterResult.id}>
            <td className="final-result-matrix__student">{studentDetailsPath ? <Link className="final-result-student-link" to={studentDetailsPath(semesterResult.studentId)}>{identity}</Link> : <span className="final-result-student-link">{identity}</span>}</td>
            <td className="final-result-matrix__apogee">{semesterResult.apogeeCode}</td>
            {modules.map((module) => {
              const result = resultByStudentModule.get(`${semesterResult.studentId}:${module.id}`);
              return <td key={module.id} title={module.title}>{result ? <div className={`final-result-matrix-cell final-result-matrix-cell--${result.resultStatus?.toLowerCase() ?? "pending"}`}><strong>{result.finalGrade?.toFixed(2) ?? "—"}</strong><span>{result.resultStatus ?? "Pending"}</span></div> : <span>—</span>}</td>;
            })}
            <td className="final-result-matrix__average"><strong>{secondInscriptionOnly ? "—" : semesterResult.semesterAverage?.toFixed(2) ?? "—"}</strong></td>
            <td className="semester-result-matrix__status">{secondInscriptionOnly ? <span className="semester-result-carried-label">Carried module</span> : semesterResult.resultStatus ? <span className={`semester-result-status semester-result-status--${semesterResult.resultStatus.toLowerCase().replace("_", "-")}`}>{semesterResult.resultStatus === "VALIDATED" ? "Validated" : "Not validated"}</span> : <span>—</span>}</td>
            <td className="final-result-matrix__download">{secondInscriptionOnly ? semesterResult.originalAcademicYearId && semesterResult.originalAcademicLevelId && semesterResult.originalSemesterId && semesterResult.originalClassGroupId ? <span className="semester-result-actions"><button className="record-open-link" disabled={exporting === semesterResult.id} onClick={() => downloadUpdated(semesterResult, studentModuleResults)} type="button">{exporting === semesterResult.id ? "Preparing..." : "Download"}</button>{onOpenOriginalSemester && <button className="record-open-link semester-result-origin-link" onClick={() => onOpenOriginalSemester(semesterResult.originalAcademicYearId!, semesterResult.originalAcademicLevelId!, semesterResult.originalSemesterId!)} type="button">Original result</button>}</span> : <span className="semester-result-no-download" title="The original registration could not be resolved.">Original unavailable</span> : <button className="record-open-link" disabled={exporting === semesterResult.id} onClick={() => download(semesterResult)} type="button">{exporting === semesterResult.id ? "Preparing..." : "Download"}</button>}</td>
          </tr>;
        })}
        {visible.length === 0 && <tr><td className="final-result-matrix-empty" colSpan={modules.length + 5}>No students match this search.</td></tr>}
      </tbody>
    </table>
  </div>;
}
