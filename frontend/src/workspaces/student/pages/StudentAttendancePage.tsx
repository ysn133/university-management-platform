import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { AttendanceQrScanner } from "../components/AttendanceQrScanner";
import { getMyAbsences, getMyAcademicContexts, studentOverviewKeys } from "../api/student-overview-api";
import { downloadAbsenceJustification, getMyAbsenceJustifications, studentJustificationKeys, submitAbsenceJustification } from "../api/student-absence-justification-api";

function errorMessage(error: unknown) {
  return error instanceof ApiRequestError ? error.message : "Your attendance record could not be loaded.";
}

function displayDate(value: string) {
  return new Intl.DateTimeFormat("en-GB", { weekday: "short", day: "numeric", month: "short", year: "numeric" }).format(new Date(`${value}T00:00:00`));
}

export function StudentAttendancePage() {
  const queryClient = useQueryClient();
  const [academicYearId, setAcademicYearId] = useState("");
  const [semesterId, setSemesterId] = useState("");
  const [scannerOpen, setScannerOpen] = useState(false);
  const [justificationAbsenceId, setJustificationAbsenceId] = useState<string | null>(null);
  const [reason, setReason] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const absencesQuery = useQuery({ queryKey: studentOverviewKeys.absences(), queryFn: getMyAbsences });
  const contextsQuery = useQuery({ queryKey: studentOverviewKeys.academicContexts(), queryFn: getMyAcademicContexts });
  const justificationsQuery = useQuery({ queryKey: studentJustificationKeys.mine(), queryFn: getMyAbsenceJustifications });
  const contexts = contextsQuery.data ?? [];
  const absences = absencesQuery.data ?? [];
  const years = Array.from(new Map(contexts.map((context) => [context.academicYearId, { id: context.academicYearId, label: context.academicYearLabel, status: context.academicYearStatus }])).values());

  useEffect(() => {
    if (!academicYearId && years.length) setAcademicYearId(years.find((year) => year.status === "ACTIVE")?.id ?? years[0].id);
  }, [academicYearId, years.map((year) => year.id).join(",")]);

  const semesters = contexts.filter((context) => context.academicYearId === academicYearId).sort((left, right) => left.semesterStartDate.localeCompare(right.semesterStartDate));
  useEffect(() => {
    if (!semesters.length) return;
    const today = new Date().toISOString().slice(0, 10);
    const current = semesters.find((semester) => semester.semesterStartDate <= today && semester.semesterEndDate >= today)
      ?? semesters.find((semester) => semester.semesterStartDate > today)
      ?? semesters.at(-1);
    setSemesterId((selected) => semesters.some((semester) => semester.semesterId === selected) ? selected : current?.semesterId ?? "");
  }, [academicYearId, semesters.map((semester) => semester.semesterId).join(",")]);

  const visibleAbsences = absences.filter((absence) => absence.academicYearId === academicYearId && absence.semesterId === semesterId).sort((left, right) => right.absenceDate.localeCompare(left.absenceDate));
  const justified = visibleAbsences.filter((absence) => absence.justified).length;
  const selectedContext = contexts.find((context) => context.academicYearId === academicYearId && context.semesterId === semesterId);
  const selectedAbsence = visibleAbsences.find((absence) => absence.id === justificationAbsenceId);
  const error = absencesQuery.error ?? contextsQuery.error ?? justificationsQuery.error;
  const latestJustification = new Map<string, NonNullable<typeof justificationsQuery.data>[number]>();
  (justificationsQuery.data ?? []).forEach((item) => { if (!latestJustification.has(item.absenceId)) latestJustification.set(item.absenceId, item); });
  const submitMutation = useMutation({
    mutationFn: () => submitAbsenceJustification(justificationAbsenceId!, reason, file!),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: studentJustificationKeys.mine() }); closeJustificationModal(); },
  });

  function closeJustificationModal() {
    setJustificationAbsenceId(null);
    setReason("");
    setFile(null);
  }

  return <div className="management-page student-attendance-page">
    <header className="management-page-header student-attendance-header"><div><p className="management-kicker">Attendance</p><h1>Absence record</h1><p>Confirmed absences and justification status for your modules.</p></div><button className="management-primary-button student-scan-button" onClick={() => setScannerOpen(true)} type="button"><svg aria-hidden="true" fill="none" viewBox="0 0 24 24"><path d="M4 9V5a1 1 0 0 1 1-1h4M15 4h4a1 1 0 0 1 1 1v4M20 15v4a1 1 0 0 1-1 1h-4M9 20H5a1 1 0 0 1-1-1v-4M8 12h8" /></svg>Scan QR code</button></header>

    {error && <div className="management-alert management-alert--error">{errorMessage(error)}</div>}

    <section className="student-attendance-summary-row">
      <article><span>Total absences</span><strong>{absencesQuery.isPending ? "—" : visibleAbsences.length}</strong></article>
      <article><span>Unjustified</span><strong>{absencesQuery.isPending ? "—" : visibleAbsences.length - justified}</strong></article>
      <article><span>Justified</span><strong>{absencesQuery.isPending ? "—" : justified}</strong></article>
      <div><span>Current view</span><strong>{selectedContext ? `${selectedContext.academicYearLabel} · ${selectedContext.semesterName}` : "No registration"}</strong></div>
    </section>

    <section className="management-panel student-attendance-records">
      <header><div><p className="management-kicker">Recorded absences</p><h2>{selectedContext?.programFiliereName ?? "Attendance history"}</h2></div><div className="student-grades-selectors"><label><span>Academic year</span><select disabled={!years.length} onChange={(event) => { setAcademicYearId(event.target.value); setSemesterId(""); }} value={academicYearId}>{years.map((year) => <option key={year.id} value={year.id}>{year.label}</option>)}</select></label><label><span>Semester</span><select disabled={!semesters.length} onChange={(event) => setSemesterId(event.target.value)} value={semesterId}>{semesters.map((semester) => <option key={semester.semesterRegistrationId} value={semester.semesterId}>{semester.semesterName}</option>)}</select></label></div></header>
      {absencesQuery.isPending || contextsQuery.isPending ? <div className="panel-empty">Loading attendance...</div> : visibleAbsences.length === 0 ? <div className="panel-empty"><strong>No absence is recorded for this semester.</strong></div> : <div className="student-attendance-table-wrap"><table className="student-attendance-table"><thead><tr><th>Date</th><th>Module</th><th>Status</th><th>Justification</th><th>Action</th></tr></thead><tbody>{visibleAbsences.map((absence) => { const justification = latestJustification.get(absence.id); return <tr key={absence.id}><td><strong>{displayDate(absence.absenceDate)}</strong></td><td><span>{absence.subjectModuleCode}</span><strong>{absence.subjectModuleTitle}</strong></td><td><span className={`student-attendance-state ${absence.justified ? "is-justified" : "is-unjustified"}`}>{absence.justified ? "Justified" : "Unjustified"}</span></td><td>{justification ? <div className="absence-justification-state"><strong data-status={justification.status}>{justification.status.toLowerCase()}</strong><span>{justification.decisionNote ?? justification.reason}</span></div> : "—"}</td><td>{justification?.status === "PENDING" || absence.justified ? <>{justification && <button className="management-text-action" onClick={() => downloadAbsenceJustification(justification.id, justification.documentFileName)} type="button">Document</button>}</> : <button className="management-text-action" onClick={() => setJustificationAbsenceId(absence.id)} type="button">{justification?.status === "REJECTED" ? "Resubmit" : "Justify"}</button>}</td></tr>; })}</tbody></table></div>}
    </section>
    {scannerOpen && <AttendanceQrScanner onClose={() => setScannerOpen(false)} />}
    {justificationAbsenceId && <ManagementModal title="Submit absence justification" description={selectedAbsence ? `${selectedAbsence.subjectModuleTitle} · ${displayDate(selectedAbsence.absenceDate)}` : "Send your explanation for professor review."} onClose={() => !submitMutation.isPending && closeJustificationModal()}><form className="absence-justification-form" onSubmit={(event) => { event.preventDefault(); if (reason.trim() && file) submitMutation.mutate(); }}><label className="absence-reason-field"><span>Reason for absence</span><textarea autoFocus maxLength={1500} onChange={(event) => setReason(event.target.value)} placeholder="Briefly explain why you were absent..." rows={4} value={reason} /><small>{reason.length} / 1500</small></label><div className={`absence-document-field${file ? " has-file" : ""}`}><div className="absence-document-copy"><span>Supporting document</span><small>PDF, JPG, or PNG · up to 5 MB</small></div><label className="absence-document-picker"><input accept="application/pdf,image/jpeg,image/png" className="absence-document-input" onChange={(event) => setFile(event.target.files?.[0] ?? null)} type="file" /><span>{file ? "Replace file" : "Choose file"}</span></label>{file && <div className="absence-selected-document"><svg aria-hidden="true" fill="none" viewBox="0 0 24 24"><path d="M8 3h6l4 4v14H8zM14 3v5h5M11 13h4M11 17h4" /></svg><div><strong>{file.name}</strong><small>{(file.size / 1024 / 1024).toFixed(2)} MB</small></div><button aria-label="Remove selected document" disabled={submitMutation.isPending} onClick={() => setFile(null)} type="button">×</button></div>}</div>{submitMutation.isError && <div className="management-alert management-alert--error">{errorMessage(submitMutation.error)}</div>}<footer><button className="management-secondary-button" disabled={submitMutation.isPending} onClick={closeJustificationModal} type="button">Cancel</button><button className="management-primary-button" disabled={!reason.trim() || !file || submitMutation.isPending} type="submit">{submitMutation.isPending ? "Submitting..." : "Submit for review"}</button></footer></form></ManagementModal>}
  </div>;
}
