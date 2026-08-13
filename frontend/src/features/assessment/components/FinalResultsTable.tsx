import { useDeferredValue, useState } from "react";
import { Link } from "react-router-dom";
import { getStudent } from "@/features/student-registration/api/student-registration-api";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import type { FinalResult } from "../api/final-results-api";
import { saveAllFinalResultsPdf, saveModuleFinalResultsPdf, saveStudentFinalResultsPdf } from "../utils/save-final-results-pdf";
import type { GradeDocumentContext } from "../utils/save-student-session-grades-pdf";

interface Props {
  context?: string;
  documentContext?: GradeDocumentContext;
  isLoading: boolean;
  results: FinalResult[];
  studentDetailsPath?: (studentId: string) => string;
}

const statusLabel = { V: "Validated", AV: "Compensated", NV: "Not validated" } as const;

export function FinalResultsTable({ context = "Final academic results", documentContext, isLoading, results, studentDetailsPath }: Props) {
  const [selectedModuleId, setSelectedModuleId] = useState("");
  const [exporting, setExporting] = useState("");
  const [studentSearch, setStudentSearch] = useState("");
  const [view, setView] = useState<"MODULE" | "STUDENT">("MODULE");
  const deferredStudentSearch = useDeferredValue(studentSearch.trim().toLowerCase());

  if (isLoading) return <div className="panel-empty">Loading final results...</div>;
  if (!results.length) return <div className="panel-empty"><strong>Final results have not been generated.</strong><p>Management generates them after the required session grades are published.</p></div>;

  const modules = Array.from(new Map(results.map((result) => [result.subjectModuleId, {
    id: result.subjectModuleId,
    code: result.subjectModuleCode,
    title: result.subjectModuleTitle,
  }])).values());
  const byModule = (moduleId: string) => results.filter((result) => result.subjectModuleId === moduleId);
  const selectedModule = modules.find((module) => module.id === selectedModuleId);
  const selectedResults = selectedModule ? byModule(selectedModule.id) : [];
  const students = Array.from(new Map(results.map((result) => [result.studentId, {
    id: result.studentId,
    firstName: result.firstName,
    lastName: result.lastName,
    apogeeCode: result.apogeeCode,
  }])).values());
  const visibleStudents = deferredStudentSearch
    ? students.filter((student) => `${student.firstName} ${student.lastName} ${student.apogeeCode}`.toLowerCase().includes(deferredStudentSearch))
    : students;
  const resultByStudentModule = new Map(results.map((result) => [`${result.studentId}:${result.subjectModuleId}`, result]));

  async function exportPdf(key: string, action: () => Promise<void>) {
    setExporting(key);
    try { await action(); } finally { setExporting(""); }
  }

  return <div className="final-results-workspace">
    <div className="final-results-toolbar"><div><strong>{view === "MODULE" ? modules.length : visibleStudents.length}</strong><span>{view === "MODULE" ? modules.length === 1 ? "module result sheet" : "module result sheets" : visibleStudents.length === 1 ? "student result" : "student results"}</span></div>{modules.length > 1 && <button className="secondary-button" disabled={Boolean(exporting)} onClick={() => exportPdf("all", () => saveAllFinalResultsPdf(results, documentContext ?? { label: context }))} type="button">{exporting === "all" ? "Preparing PDF..." : "Download All Results"}</button>}</div>
    {modules.length > 1 && <div className="final-result-view-tabs" role="tablist"><button aria-selected={view === "MODULE"} onClick={() => setView("MODULE")} role="tab" type="button">By Module</button><button aria-selected={view === "STUDENT"} onClick={() => setView("STUDENT")} role="tab" type="button">By Student</button></div>}
    {view === "MODULE" ? <div className="grade-management-list final-result-sheet-list">{modules.map((module) => {
      const moduleResults = byModule(module.id);
      const completed = moduleResults.filter((result) => result.finalGrade !== null).length;
      const validated = moduleResults.filter((result) => result.resultStatus === "V" || result.resultStatus === "AV").length;
      return <article key={module.id}><div className="grade-management-module"><span>{module.code}</span><div><strong>{module.title}</strong><small>Final module result</small></div></div><div className="grade-management-progress"><strong>{completed}/{moduleResults.length}</strong><span>results generated</span></div><span className="final-result-sheet-summary">{validated} validated</span><button className="secondary-button" onClick={() => setSelectedModuleId(module.id)} type="button">Open Sheet</button></article>;
    })}</div> : <div className="final-result-matrix-wrap"><div className="final-result-matrix-heading"><div><span>Consolidated results</span><strong>Students across all modules</strong></div><div className="final-result-matrix-legend"><span><i className="is-v" />Validated</span><span><i className="is-av" />Compensated</span><span><i className="is-nv" />Not validated</span></div></div><div className="final-result-matrix-controls"><label className="final-result-student-search"><span>Search student</span><input onChange={(event) => setStudentSearch(event.target.value)} placeholder="Search by name or Apogee" type="search" value={studentSearch} /></label><span>{visibleStudents.length} of {students.length} students</span></div><table className="final-result-matrix"><thead><tr><th className="final-result-matrix__student">Student</th><th className="final-result-matrix__apogee">Apogee</th>{modules.map((module) => <th key={module.id} title={module.title}><strong>{module.code}</strong></th>)}<th className="final-result-matrix__average">Average</th><th className="final-result-matrix__validated">Validated</th><th aria-label="Download" className="final-result-matrix__download" /></tr></thead><tbody>{visibleStudents.map((student) => { const studentResults = modules.map((module) => resultByStudentModule.get(`${student.id}:${module.id}`)); const graded = studentResults.filter((result): result is FinalResult => Boolean(result?.finalGrade !== null && result?.finalGrade !== undefined)); const average = graded.length ? graded.reduce((total, result) => total + result.finalGrade!, 0) / graded.length : null; const validated = studentResults.filter((result) => result?.resultStatus === "V" || result?.resultStatus === "AV").length; const identity = <><span className="final-result-student-monogram">{student.firstName[0]}{student.lastName[0]}</span><strong title={`${student.firstName} ${student.lastName}`}>{student.firstName} {student.lastName}</strong></>; return <tr key={student.id}><td className="final-result-matrix__student">{studentDetailsPath ? <Link className="final-result-student-link" to={studentDetailsPath(student.id)}>{identity}</Link> : <span className="final-result-student-link">{identity}</span>}</td><td className="final-result-matrix__apogee">{student.apogeeCode}</td>{studentResults.map((result, index) => <td key={modules[index].id} title={modules[index].title}>{result ? <div className={`final-result-matrix-cell final-result-matrix-cell--${result.resultStatus?.toLowerCase() ?? "pending"}`}><strong>{result.finalGrade?.toFixed(2) ?? "—"}</strong><span>{result.resultStatus ?? "Pending"}</span></div> : <span>—</span>}</td>)}<td className="final-result-matrix__average"><strong>{average?.toFixed(2) ?? "—"}</strong></td><td className="final-result-matrix__validated"><strong>{validated}</strong><span> / {modules.length}</span></td><td className="final-result-matrix__download"><button className="record-open-link" disabled={exporting === student.id} onClick={() => exportPdf(student.id, async () => saveStudentFinalResultsPdf(graded, documentContext ?? { label: context }, await getStudent(student.id)))} type="button">{exporting === student.id ? "Preparing..." : "Download"}</button></td></tr>; })}{visibleStudents.length === 0 && <tr><td className="final-result-matrix-empty" colSpan={modules.length + 5}>No students match this search.</td></tr>}</tbody></table></div>}
    {selectedModule && <ManagementModal size="wide" title="Final Grade Sheet" description={`${selectedModule.code} · ${selectedModule.title}`} onClose={() => setSelectedModuleId("")}><div className="grade-review-modal"><div className="grade-review-summary"><div><span>Students</span><strong>{selectedResults.length}</strong></div><div><span>Validated</span><strong>{selectedResults.filter((result) => result.resultStatus === "V").length}</strong></div><div><span>Compensated</span><strong>{selectedResults.filter((result) => result.resultStatus === "AV").length}</strong></div><div><span>Not validated</span><strong>{selectedResults.filter((result) => result.resultStatus === "NV").length}</strong></div></div><div className="grade-review-table-wrap"><table className="grade-review-table"><thead><tr><th>No.</th><th>Student</th><th>Apogee</th><th>Inscription</th><th>Final grade</th><th>Result</th></tr></thead><tbody>{selectedResults.map((result, index) => <tr key={result.moduleRegistrationId}><td>{index + 1}</td><td><strong>{result.firstName} {result.lastName}</strong></td><td>{result.apogeeCode}</td><td>{result.inscriptionNumber}</td><td><strong>{result.finalGrade?.toFixed(2) ?? "—"}</strong></td><td><span className={`final-result-status final-result-status--${result.resultStatus?.toLowerCase() ?? "pending"}`}>{result.resultStatus ? statusLabel[result.resultStatus] : "Pending"}</span></td></tr>)}</tbody></table></div><footer className="form-actions"><button className="secondary-button" onClick={() => setSelectedModuleId("")} type="button">Close</button><button className="management-primary-button" disabled={Boolean(exporting)} onClick={() => exportPdf(selectedModule.id, () => saveModuleFinalResultsPdf(selectedResults, documentContext ?? { label: context }))} type="button">{exporting === selectedModule.id ? "Preparing PDF..." : "Download PDF"}</button></footer></div></ManagementModal>}
  </div>;
}
