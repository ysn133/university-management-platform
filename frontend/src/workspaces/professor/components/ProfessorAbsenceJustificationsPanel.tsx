import { useMutation, useQueries, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import type { TeachingAssignment } from "@/features/teaching-planning/api/teaching-plan-api";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  downloadJustificationDocument,
  getTeachingAssignmentJustifications,
  professorAttendanceKeys,
  reviewAbsenceJustification,
  type AbsenceJustification,
} from "../api/professor-attendance-api";

type JustificationView = "PENDING" | "REVIEWED" | "ALL";

type ProfessorAbsenceJustificationsPanelProps = {
  assignments: TeachingAssignment[];
  search?: string;
};

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "Absence justifications could not be loaded.";
}

function displayDate(value: string): string {
  return new Intl.DateTimeFormat("en-GB", { day: "numeric", month: "short", year: "numeric" }).format(new Date(`${value}T00:00:00`));
}

export function ProfessorAbsenceJustificationsPanel({ assignments, search }: ProfessorAbsenceJustificationsPanelProps) {
  const queryClient = useQueryClient();
  const [view, setView] = useState<JustificationView>("PENDING");
  const [reviewing, setReviewing] = useState<AbsenceJustification | null>(null);
  const [decisionNote, setDecisionNote] = useState("");
  const [localSearch, setLocalSearch] = useState("");
  const queries = useQueries({
    queries: assignments.map((assignment) => ({
      queryKey: professorAttendanceKeys.justifications(assignment.id),
      queryFn: () => getTeachingAssignmentJustifications(assignment.id),
    })),
  });
  const assignmentById = new Map(assignments.map((assignment) => [assignment.id, assignment]));
  const justifications = Array.from(new Map(queries.flatMap((query) => query.data ?? []).map((item) => [item.id, item])).values())
    .sort((left, right) => right.submittedAt.localeCompare(left.submittedAt));
  const pendingCount = justifications.filter((item) => item.status === "PENDING").length;
  const reviewedCount = justifications.length - pendingCount;
  const normalizedSearch = (search ?? localSearch).trim().toLowerCase();
  const visibleJustifications = justifications.filter((item) => {
    const matchesView = view === "ALL" || (view === "REVIEWED" ? item.status !== "PENDING" : item.status === "PENDING");
    const assignment = assignmentById.get(item.teachingAssignmentId);
    const searchable = `${item.studentFirstName} ${item.studentLastName} ${item.studentApogeeCode} ${item.subjectModuleCode} ${item.subjectModuleTitle} ${assignment?.teachingGroupName ?? ""}`.toLowerCase();
    return matchesView && (!normalizedSearch || searchable.includes(normalizedSearch));
  });
  const loadError = queries.find((query) => query.error)?.error;
  const isPending = queries.some((query) => query.isPending);
  const reviewMutation = useMutation({
    mutationFn: (decision: "ACCEPTED" | "REJECTED") => reviewAbsenceJustification(reviewing!.id, decision, decisionNote),
    onSuccess: async () => {
      const assignmentId = reviewing!.teachingAssignmentId;
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: professorAttendanceKeys.justifications(assignmentId) }),
        queryClient.invalidateQueries({ queryKey: professorAttendanceKeys.absences(assignmentId) }),
      ]);
      closeReviewModal();
    },
  });

  function closeReviewModal() {
    setReviewing(null);
    setDecisionNote("");
  }

  return <>
    <div className="professor-justification-toolbar">
      <div aria-label="Justification status" className="professor-justification-tabs" role="tablist">
        <button aria-selected={view === "PENDING"} onClick={() => setView("PENDING")} role="tab" type="button">Pending <span>{pendingCount}</span></button>
        <button aria-selected={view === "REVIEWED"} onClick={() => setView("REVIEWED")} role="tab" type="button">Reviewed <span>{reviewedCount}</span></button>
        <button aria-selected={view === "ALL"} onClick={() => setView("ALL")} role="tab" type="button">All <span>{justifications.length}</span></button>
      </div>
      <div className="professor-justification-toolbar-end">{search === undefined && <label><span className="sr-only">Search justifications</span><input onChange={(event) => setLocalSearch(event.target.value)} placeholder="Search student or module" value={localSearch} /></label>}<p>{visibleJustifications.length} {visibleJustifications.length === 1 ? "submission" : "submissions"}</p></div>
    </div>
    {loadError && <div className="management-alert management-alert--error">{errorMessage(loadError)}</div>}
    {isPending ? <div className="panel-empty">Loading absence justifications...</div>
      : assignments.length === 0 ? <div className="panel-empty"><strong>No teaching assignment is available in this academic period.</strong></div>
      : visibleJustifications.length === 0 ? <div className="professor-justification-empty"><span>✓</span><strong>{view === "PENDING" ? "No justification is waiting for review." : "No justification matches this view."}</strong><p>{view === "PENDING" ? "New student submissions will appear here." : "Try another status or search term."}</p></div>
      : <div className="professor-justification-list">{visibleJustifications.map((item) => { const assignment = assignmentById.get(item.teachingAssignmentId); return <article key={item.id}>
        <div className="professor-justification-student"><span>{item.studentFirstName[0]}{item.studentLastName[0]}</span><div><strong>{item.studentFirstName} {item.studentLastName}</strong><small>{item.studentApogeeCode}</small></div></div>
        <div className="professor-justification-context"><strong>{item.subjectModuleTitle}</strong><span>{item.subjectModuleCode} · {assignment?.teachingGroupName ?? "Teaching group"}</span></div>
        <div className="professor-justification-date"><span>Absent</span><strong>{displayDate(item.absenceDate)}</strong><small>Submitted {displayDate(item.submittedAt.slice(0, 10))}</small></div>
        <div className="professor-justification-reason"><p>{item.reason}</p>{item.decisionNote && <small>{item.decisionNote}</small>}</div>
        <div className="professor-justification-action"><span data-status={item.status}>{item.status.toLowerCase()}</span>{item.status === "PENDING" ? <button onClick={() => setReviewing(item)} type="button">Review</button> : <button onClick={() => downloadJustificationDocument(item.id, item.documentFileName)} type="button">Document</button>}</div>
      </article>; })}</div>}
    {reviewing && <ManagementModal title="Review absence justification" description={`${reviewing.studentFirstName} ${reviewing.studentLastName} · ${reviewing.subjectModuleTitle}`} onClose={() => !reviewMutation.isPending && closeReviewModal()}><div className="absence-review"><div className="absence-review-summary"><span>Absence date<strong>{new Date(`${reviewing.absenceDate}T00:00:00`).toLocaleDateString("en-GB", { day: "numeric", month: "long", year: "numeric" })}</strong></span><span>Apogee code<strong>{reviewing.studentApogeeCode}</strong></span></div><section className="absence-review-reason"><span>Student explanation</span><p>{reviewing.reason}</p></section><button className="absence-review-document" onClick={() => downloadJustificationDocument(reviewing.id, reviewing.documentFileName)} type="button"><svg aria-hidden="true" fill="none" viewBox="0 0 24 24"><path d="M8 3h6l4 4v14H8zM14 3v5h5M12 11v6M9.5 14.5 12 17l2.5-2.5" /></svg><span><strong>{reviewing.documentFileName}</strong><small>Supporting document</small></span><b>Download</b></button><label className="absence-review-response"><span>Response <small>Optional</small></span><textarea maxLength={1000} onChange={(event) => setDecisionNote(event.target.value)} placeholder="Add a note for the student..." rows={3} value={decisionNote} /></label>{reviewMutation.isError && <div className="management-alert management-alert--error">{errorMessage(reviewMutation.error)}</div>}<footer><button className="absence-review-reject" disabled={reviewMutation.isPending} onClick={() => reviewMutation.mutate("REJECTED")} type="button">Reject justification</button><button className="management-primary-button" disabled={reviewMutation.isPending} onClick={() => reviewMutation.mutate("ACCEPTED")} type="button">Accept justification</button></footer></div></ManagementModal>}
  </>;
}
