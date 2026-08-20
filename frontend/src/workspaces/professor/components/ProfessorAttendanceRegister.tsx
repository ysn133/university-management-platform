import { useDeferredValue, useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getMyTeachingAssignments, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { confirmAttendance, getAttendanceRoster, getTeachingAssignmentAbsences, professorAttendanceKeys, startAttendanceQrSession, type AttendanceQrSession } from "../api/professor-attendance-api";
import { AttendanceQrModal } from "./AttendanceQrModal";

type ProfessorAttendanceRegisterProps = {
  subjectModuleId?: string;
  semesterId?: string;
  classGroupName?: string;
  teachingAssignmentId?: string;
};

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "Attendance could not be processed.";
}

export function ProfessorAttendanceRegister({ subjectModuleId, semesterId, classGroupName, teachingAssignmentId: fixedAssignmentId }: ProfessorAttendanceRegisterProps) {
  const queryClient = useQueryClient();
  const today = new Date().toISOString().slice(0, 10);
  const [attendanceDate, setAttendanceDate] = useState(today);
  const [requestedAssignmentId, setRequestedAssignmentId] = useState("");
  const [search, setSearch] = useState("");
  const [absentStudentIds, setAbsentStudentIds] = useState<Set<string>>(new Set());
  const [qrSession, setQrSession] = useState<AttendanceQrSession | null>(null);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const className = classGroupName?.trim().toLowerCase() ?? "";
  const assignments = (assignmentsQuery.data ?? []).filter((assignment) => assignment.status === "ACTIVE" && (fixedAssignmentId
    ? assignment.id === fixedAssignmentId
    : assignment.subjectModuleId === subjectModuleId
      && assignment.semesterId === semesterId
      && (assignment.componentType === "COURSE" || assignment.teachingGroupName.toLowerCase().startsWith(className))));
  const teachingAssignmentId = fixedAssignmentId && assignments.some((assignment) => assignment.id === fixedAssignmentId)
    ? fixedAssignmentId
    : assignments.some((assignment) => assignment.id === requestedAssignmentId) ? requestedAssignmentId : assignments[0]?.id ?? "";
  const selectedAssignment = assignments.find((assignment) => assignment.id === teachingAssignmentId);
  const rosterQuery = useQuery({ queryKey: professorAttendanceKeys.roster(teachingAssignmentId), queryFn: () => getAttendanceRoster(teachingAssignmentId), enabled: Boolean(teachingAssignmentId) });
  const absencesQuery = useQuery({ queryKey: professorAttendanceKeys.absences(teachingAssignmentId), queryFn: () => getTeachingAssignmentAbsences(teachingAssignmentId), enabled: Boolean(teachingAssignmentId) });

  useEffect(() => {
    const recorded = (absencesQuery.data ?? []).filter((absence) => absence.absenceDate === attendanceDate).map((absence) => absence.studentId);
    setAbsentStudentIds(new Set(recorded));
  }, [attendanceDate, teachingAssignmentId, absencesQuery.data]);

  const saveMutation = useMutation({
    mutationFn: () => confirmAttendance(teachingAssignmentId, attendanceDate, Array.from(absentStudentIds)),
    onSuccess: (saved) => {
      const previous = queryClient.getQueryData<Awaited<ReturnType<typeof getTeachingAssignmentAbsences>>>(professorAttendanceKeys.absences(teachingAssignmentId)) ?? [];
      queryClient.setQueryData(professorAttendanceKeys.absences(teachingAssignmentId), [...previous.filter((absence) => absence.absenceDate !== attendanceDate), ...saved]);
    },
  });
  const qrMutation = useMutation({
    mutationFn: () => startAttendanceQrSession(teachingAssignmentId, attendanceDate),
    onSuccess: (session) => {
      setAbsentStudentIds(new Set((rosterQuery.data ?? []).map((student) => student.studentId)));
      setQrSession(session);
    },
  });
  const roster = (rosterQuery.data ?? []).filter((student) => !deferredSearch || `${student.firstName} ${student.lastName} ${student.apogeeCode} ${student.nationalStudentCode ?? ""} ${student.universityEmail}`.toLowerCase().includes(deferredSearch));

  function toggleStudent(studentId: string) {
    setAbsentStudentIds((current) => {
      const updated = new Set(current);
      if (updated.has(studentId)) updated.delete(studentId);
      else updated.add(studentId);
      return updated;
    });
  }

  function applyQrCheckIns(checkedInStudentIds: string[]) {
    const checkedIn = new Set(checkedInStudentIds);
    setAbsentStudentIds(new Set((rosterQuery.data ?? []).filter((student) => !checkedIn.has(student.studentId)).map((student) => student.studentId)));
  }

  const loadError = assignmentsQuery.error ?? rosterQuery.error ?? absencesQuery.error;
  return <section className="management-panel professor-attendance-panel professor-attendance-panel--embedded">
    <header className="professor-attendance-panel-header"><div><p className="management-kicker">Attendance register</p><h2>{selectedAssignment?.subjectModuleTitle ?? "Attendance"}</h2><p>{selectedAssignment ? `${classGroupName ? `${classGroupName} · ` : ""}${selectedAssignment.componentType === "COURSE" ? "Course" : selectedAssignment.componentType} · ${selectedAssignment.teachingGroupName}` : "No teaching delivery is assigned for this class."}</p></div>{selectedAssignment && <div className="professor-attendance-header-actions"><button className="attendance-qr-start" disabled={attendanceDate !== today || qrMutation.isPending || !rosterQuery.data?.length} onClick={() => qrMutation.mutate()} type="button">{qrMutation.isPending ? "Opening..." : "Start QR check-in"}</button><div className="professor-attendance-summary"><span><strong>{rosterQuery.data?.length ?? 0}</strong> students</span><span className="is-absent"><strong>{absentStudentIds.size}</strong> absent</span></div></div>}</header>
    {loadError && <div className="management-alert management-alert--error">{errorMessage(loadError)}</div>}
    <div className={`professor-attendance-controls professor-attendance-controls--class${fixedAssignmentId ? " professor-attendance-controls--fixed" : ""}`}>{!fixedAssignmentId && <label className="professor-attendance-assignment"><span>Teaching delivery</span><select onChange={(event) => setRequestedAssignmentId(event.target.value)} value={teachingAssignmentId}>{assignments.map((assignment) => <option key={assignment.id} value={assignment.id}>{assignment.componentType === "COURSE" ? "Course" : assignment.componentType} · {assignment.teachingGroupName}</option>)}</select></label>}<label><span>Date</span><input max={today} onChange={(event) => setAttendanceDate(event.target.value)} type="date" value={attendanceDate} /></label></div>
    <div className="professor-attendance-toolbar"><label><span>Search roster</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Name, Apogee, code, or email" value={search} /></label><div><span><i /> Present</span><span><i /> Absent</span></div></div>
    {assignmentsQuery.isPending || rosterQuery.isPending || absencesQuery.isPending ? <div className="panel-empty">Loading attendance roster...</div> : !selectedAssignment ? <div className="panel-empty"><strong>No teaching delivery is available for this class.</strong></div> : roster.length === 0 ? <div className="panel-empty"><strong>{search ? "No student matches your search." : "No student belongs to this teaching audience."}</strong></div> : <div className="professor-attendance-table-wrap"><table className="professor-attendance-table"><thead><tr><th>Student</th><th>Apogee</th><th>National code</th><th>Status</th></tr></thead><tbody>{roster.map((student) => { const absent = absentStudentIds.has(student.studentId); return <tr className={absent ? "is-absent" : ""} key={student.studentId}><td><div><span>{student.firstName[0]}{student.lastName[0]}</span><div><strong>{student.firstName} {student.lastName}</strong><small>{student.universityEmail}</small></div></div></td><td>{student.apogeeCode}</td><td>{student.nationalStudentCode ?? "—"}</td><td><button aria-pressed={absent} onClick={() => toggleStudent(student.studentId)} type="button"><span>{absent ? "Absent" : "Present"}</span><i /></button></td></tr>; })}</tbody></table></div>}
    {saveMutation.isError && <div className="management-alert management-alert--error">{errorMessage(saveMutation.error)}</div>}
    {qrMutation.isError && <div className="management-alert management-alert--error">{errorMessage(qrMutation.error)}</div>}
    {saveMutation.isSuccess && <div className="management-alert management-alert--success">Attendance saved for {attendanceDate}.</div>}
    {selectedAssignment && <footer className="professor-attendance-actions"><p>Only students marked absent will be recorded.</p><button className="management-primary-button" disabled={saveMutation.isPending || rosterQuery.isPending || absencesQuery.isPending} onClick={() => saveMutation.mutate()} type="button">{saveMutation.isPending ? "Saving..." : "Confirm Attendance"}</button></footer>}
    {qrSession && <AttendanceQrModal initialSession={qrSession} onCheckInsChange={applyQrCheckIns} onClose={() => setQrSession(null)} />}
  </section>;
}
