import { useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { generateExamCandidates, getExamCandidates, type ModuleExam } from "../api/exam-planning-api";
import { saveCandidateListPdf } from "../utils/save-candidate-list-pdf";

interface Props {
  academicYearLabel?: string;
  classGroupName: string;
  exam: ModuleExam;
  moduleCode?: string;
  moduleTitle: string;
  programName?: string;
  semesterName?: string;
  sessionType: "NORMAL" | "RATTRAPAGE";
  onClose: () => void;
  onGenerated: () => void;
}

function errorMessage(error: unknown) {
  return error instanceof ApiRequestError ? error.message : "The candidate list could not be processed.";
}

export function ExamCandidateListModal({ academicYearLabel, classGroupName, exam, moduleCode, moduleTitle, programName, semesterName, sessionType, onClose, onGenerated }: Props) {
  const documentRef = useRef<HTMLDivElement>(null);
  const queryClient = useQueryClient();
  const [exporting, setExporting] = useState(false);
  const queryKey = ["exam-candidates", exam.id] as const;
  const candidatesQuery = useQuery({ queryKey, queryFn: () => getExamCandidates(exam.id) });
  const candidates = [...(candidatesQuery.data ?? [])].sort((left, right) => left.lastName.localeCompare(right.lastName) || left.firstName.localeCompare(right.firstName));
  const generateMutation = useMutation({ mutationFn: () => generateExamCandidates(exam.id), onSuccess: (result) => { queryClient.setQueryData(queryKey, result); onGenerated(); } });

  async function exportPdf() {
    if (!documentRef.current) return;
    setExporting(true);
    try {
      await saveCandidateListPdf(documentRef.current, `candidate-list-${moduleCode ?? "module"}-${sessionType.toLowerCase()}.pdf`, true);
    } finally {
      setExporting(false);
    }
  }

  return <ManagementModal size="wide" title="Exam Candidate List" description={`${moduleTitle} · ${classGroupName}`} onClose={onClose}>
    <div className="exam-candidate-workspace">
      <div className="exam-candidate-toolbar"><div className="exam-candidate-toolbar__summary"><span>Official candidate list</span><div><strong>{candidates.length}</strong><p>eligible students for this module exam</p></div></div><div className="exam-candidate-toolbar__actions"><button className="secondary-button" disabled={generateMutation.isPending} onClick={() => generateMutation.mutate()} type="button">{generateMutation.isPending ? "Generating..." : candidates.length ? "Regenerate List" : "Generate List"}</button><button className="management-primary-button" disabled={!candidates.length || exporting} onClick={exportPdf} type="button">{exporting ? "Preparing PDF..." : "Save as PDF"}</button></div></div>
      {(candidatesQuery.isError || generateMutation.isError) && <div className="management-alert management-alert--error">{errorMessage(candidatesQuery.error ?? generateMutation.error)}</div>}
      {candidatesQuery.isPending ? <div className="panel-empty">Loading candidate list...</div> : !candidates.length ? <div className="panel-empty"><strong>No candidate list generated.</strong><p>Generate the list after the examination plan is ready.</p></div> : <div className="exam-candidate-document" ref={documentRef}>
        <header><div><strong>Université Ibn Zohr</strong><span>{programName ?? "Academic programme"}</span><span>{academicYearLabel ?? "Academic year"}</span></div><div><span>{sessionType === "NORMAL" ? "Normal Examination Session" : "Rattrapage Examination Session"}</span><strong>{moduleCode ? `${moduleCode} · ` : ""}{moduleTitle}</strong><span>{semesterName} · {classGroupName}</span><span>{exam.examDate} · {exam.startTime.slice(0, 5)}–{exam.endTime?.slice(0, 5)}</span></div></header>
        <section className="exam-candidate-group exam-candidate-group--flat"><div className="exam-candidate-group-heading"><div><span>Class</span><strong>{classGroupName}</strong></div><div><span>Candidates</span><strong>{candidates.length}</strong></div></div><table><thead><tr><th>No.</th><th>Apogee</th><th>CNE</th><th>CIN</th><th>Last name</th><th>First name</th></tr></thead><tbody>{candidates.map((candidate, index) => <tr key={candidate.id}><td>{index + 1}</td><td>{candidate.apogeeCode}</td><td>{candidate.nationalStudentCode ?? "—"}</td><td>{candidate.cin ?? "—"}</td><td>{candidate.lastName}</td><td>{candidate.firstName}</td></tr>)}</tbody></table></section>
      </div>}
    </div>
  </ManagementModal>;
}
