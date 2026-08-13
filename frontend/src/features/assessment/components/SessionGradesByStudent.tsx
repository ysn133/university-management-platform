import { useDeferredValue, useState } from "react";
import { Link } from "react-router-dom";
import { getStudent } from "@/features/student-registration/api/student-registration-api";
import type { ManagedGradeSheet } from "../api/grade-management-api";
import { saveStudentSessionGradesPdf, type GradeDocumentContext } from "../utils/save-student-session-grades-pdf";

interface ModuleSheet {
  code: string;
  id: string;
  sheet?: ManagedGradeSheet;
  title: string;
}

interface Props {
  context: GradeDocumentContext;
  moduleValidationThreshold?: number;
  modules: ModuleSheet[];
  session: "Normal" | "Rattrapage";
  studentDetailsPath?: (studentId: string) => string;
}

export function SessionGradesByStudent({ context, moduleValidationThreshold, modules, session, studentDetailsPath }: Props) {
  const [search, setSearch] = useState("");
  const [exporting, setExporting] = useState("");
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const students = Array.from(new Map(modules.flatMap((module) => module.sheet?.grades ?? []).map((grade) => [grade.studentId, {
    apogeeCode: grade.apogeeCode,
    firstName: grade.firstName,
    id: grade.studentId,
    lastName: grade.lastName,
  }])).values());
  const visibleStudents = deferredSearch
    ? students.filter((student) => `${student.firstName} ${student.lastName} ${student.apogeeCode}`.toLowerCase().includes(deferredSearch))
    : students;

  async function downloadStudent(studentId: string) {
    setExporting(studentId);
    try { await saveStudentSessionGradesPdf(studentId, modules, session, context, await getStudent(studentId), moduleValidationThreshold); } finally { setExporting(""); }
  }

  return <div className="final-result-matrix-wrap session-grade-matrix-wrap">
    <div className="final-result-matrix-heading"><div><span>{session} session</span><strong>Grades by student</strong></div><span className="session-grade-count">{visibleStudents.length} of {students.length} students</span></div>
    <div className="final-result-matrix-controls"><label className="final-result-student-search"><span>Search student</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Search by name or Apogee" type="search" value={search} /></label></div>
    <table className="final-result-matrix session-grade-matrix"><thead><tr><th className="final-result-matrix__student">Student</th><th className="final-result-matrix__apogee">Apogee</th>{modules.map((module) => <th key={module.id} title={module.title}><strong>{module.code}</strong></th>)}<th aria-label="Download" className="session-grade-matrix__action" /></tr></thead><tbody>
      {visibleStudents.map((student) => <tr key={student.id}><td className="final-result-matrix__student">{studentDetailsPath ? <Link className="final-result-student-link" to={studentDetailsPath(student.id)}><span className="final-result-student-monogram">{student.firstName[0]}{student.lastName[0]}</span><strong>{student.firstName} {student.lastName}</strong></Link> : <span className="final-result-student-link"><span className="final-result-student-monogram">{student.firstName[0]}{student.lastName[0]}</span><strong>{student.firstName} {student.lastName}</strong></span>}</td><td className="final-result-matrix__apogee">{student.apogeeCode}</td>{modules.map((module) => { const grade = module.sheet?.grades.find((item) => item.studentId === student.id); const requiresRattrapage = session === "Normal" && grade?.gradeValue !== null && grade?.gradeValue !== undefined && (moduleValidationThreshold === undefined || grade.gradeValue < moduleValidationThreshold); return <td key={module.id}>{grade?.gradeValue !== null && grade?.gradeValue !== undefined ? <div className={`session-grade-cell${requiresRattrapage ? " session-grade-cell--rattrapage" : ""}`}><strong>{grade.gradeValue.toFixed(2)}</strong>{session === "Normal" ? <span>{requiresRattrapage ? "Rattrapage" : "Validated"}</span> : null}</div> : <span>—</span>}</td>; })}<td className="session-grade-matrix__action"><button className="record-open-link" disabled={exporting === student.id} onClick={() => downloadStudent(student.id)} type="button">{exporting === student.id ? "Preparing..." : "Download"}</button></td></tr>)}
      {visibleStudents.length === 0 && <tr><td className="final-result-matrix-empty" colSpan={modules.length + 3}>No students match this search.</td></tr>}
    </tbody></table>
  </div>;
}
