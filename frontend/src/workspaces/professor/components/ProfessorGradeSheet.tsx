import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useRef, useState } from "react";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getGradeSheet, professorGradeKeys, saveGradeSheet, submitGradeSheet, type GradeSheet } from "../api/professor-grades-api";
import type { ProfessorExam } from "../api/professor-overview-api";

type RowDraft = { grade: string; zeroReason: "" | "EARNED_ZERO" | "ABSENT" };

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The grade sheet could not be processed.";
}

function initialRows(sheet: GradeSheet): Record<string, RowDraft> {
  return Object.fromEntries(sheet.grades.map((item) => [item.moduleRegistrationId, {
    grade: item.gradeValue === null ? "" : String(item.gradeValue),
    zeroReason: item.zeroGradeReason ?? "",
  }]));
}

export function ProfessorGradeSheet({ exam }: { exam: ProfessorExam }) {
  const queryClient = useQueryClient();
  const sheetQuery = useQuery({ queryKey: professorGradeKeys.sheet(exam.id), queryFn: () => getGradeSheet(exam.id) });
  const [rows, setRows] = useState<Record<string, RowDraft>>({});
  const initializedExam = useRef("");
  const sheet = sheetQuery.data;

  useEffect(() => {
    if (!sheet || initializedExam.current === exam.id) return;
    setRows(initialRows(sheet));
    initializedExam.current = exam.id;
  }, [exam.id, sheet]);

  const completed = sheet?.grades.filter((item) => {
    const row = rows[item.moduleRegistrationId];
    const value = Number(row?.grade);
    return row?.grade !== "" && value >= 0 && value <= 20 && (value !== 0 || Boolean(row.zeroReason));
  }).length ?? 0;
  const absent = sheet?.grades.filter((item) => rows[item.moduleRegistrationId]?.zeroReason === "ABSENT").length ?? 0;
  const complete = Boolean(sheet?.grades.length) && completed === sheet?.grades.length;
  const editable = sheet?.workflowStatus === "DRAFT";

  const saveMutation = useMutation({
    mutationFn: () => saveGradeSheet(exam.id, sheet!.grades.map((item) => {
      const row = rows[item.moduleRegistrationId];
      const gradeValue = Number(row.grade);
      return { moduleRegistrationId: item.moduleRegistrationId, gradeValue, ...(gradeValue === 0 ? { zeroGradeReason: row.zeroReason as "ABSENT" | "EARNED_ZERO" } : {}) };
    })),
    onSuccess: (saved) => { queryClient.setQueryData(professorGradeKeys.sheet(exam.id), saved); setRows(initialRows(saved)); },
  });
  const submitMutation = useMutation({
    mutationFn: () => submitGradeSheet(exam.id),
    onSuccess: (submitted) => queryClient.setQueryData(professorGradeKeys.sheet(exam.id), submitted),
  });

  if (sheetQuery.isPending) return <div className="panel-empty">Loading grade sheet...</div>;
  if (sheetQuery.isError) return <div className="management-alert management-alert--error professor-grade-load-error">{errorMessage(sheetQuery.error)}</div>;
  if (!sheet) return null;

  return <div className="professor-grade-sheet">
    <header className="professor-grade-summary">
      <div><span className="professor-grade-status">{sheet.workflowStatus}</span><strong>{exam.subjectModuleTitle}</strong><small>{exam.classGroupName} · {exam.sessionType === "NORMAL" ? "Normal session" : "Rattrapage"} · {exam.examDate}</small></div>
      <dl><div><dt>Students</dt><dd>{sheet.grades.length}</dd></div><div><dt>Completed</dt><dd>{completed}</dd></div><div><dt>Missing</dt><dd>{sheet.grades.length - completed}</dd></div><div><dt>Absent</dt><dd>{absent}</dd></div></dl>
    </header>
    {(saveMutation.isError || submitMutation.isError) && <div className="management-alert management-alert--error">{errorMessage(saveMutation.error ?? submitMutation.error)}</div>}
    <div className="professor-grade-table-wrap"><table className="professor-grade-table"><thead><tr><th>Student</th><th>Apogee</th><th>Inscription</th><th>Grade / 20</th><th>Zero reason</th></tr></thead><tbody>{sheet.grades.map((item, index) => {
      const row = rows[item.moduleRegistrationId] ?? { grade: "", zeroReason: "" };
      const isZero = row.grade !== "" && Number(row.grade) === 0;
      return <tr key={item.moduleRegistrationId}><td><strong>{item.firstName} {item.lastName}</strong><span>{item.universityEmail}</span></td><td>{item.apogeeCode}</td><td>{item.inscriptionNumber === 1 ? "First" : `${item.inscriptionNumber}${item.inscriptionNumber === 2 ? "nd" : "th"}`}</td><td><input aria-label={`Grade for ${item.firstName} ${item.lastName}`} disabled={!editable} max="20" min="0" onChange={(event) => { const grade = event.target.value; setRows((current) => ({ ...current, [item.moduleRegistrationId]: { grade, zeroReason: grade !== "" && Number(grade) === 0 ? row.zeroReason : "" } })); }} onKeyDown={(event) => { if (event.key === "Enter" || event.key === "ArrowDown") { event.preventDefault(); document.querySelector<HTMLInputElement>(`[data-grade-index="${index + 1}"]`)?.focus(); } }} data-grade-index={index} placeholder="0.00" step="0.25" type="number" value={row.grade} /></td><td>{isZero ? <select aria-label={`Zero reason for ${item.firstName} ${item.lastName}`} disabled={!editable} onChange={(event) => setRows((current) => ({ ...current, [item.moduleRegistrationId]: { ...row, zeroReason: event.target.value as RowDraft["zeroReason"] } }))} value={row.zeroReason}><option value="">Select reason</option><option value="EARNED_ZERO">Earned zero</option><option value="ABSENT">Absent</option></select> : <span className="professor-grade-not-applicable">Not required</span>}</td></tr>;
    })}</tbody></table></div>
    <footer className="professor-grade-actions"><p>{editable ? "Complete every row before saving the draft." : `This sheet is ${sheet.workflowStatus.toLowerCase()} and can no longer be edited.`}</p>{editable && <div><button className="secondary-button" disabled={!complete || saveMutation.isPending} onClick={() => saveMutation.mutate()} type="button">{saveMutation.isPending ? "Saving..." : "Save Draft"}</button><button className="management-primary-button" disabled={!complete || saveMutation.isPending || submitMutation.isPending || !sheet.grades.every((item) => item.gradeRecordId)} onClick={() => submitMutation.mutate()} type="button">{submitMutation.isPending ? "Submitting..." : "Submit Sheet"}</button></div>}</footer>
  </div>;
}
