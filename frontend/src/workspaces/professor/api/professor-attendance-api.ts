import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { env } from "@/shared/config/env";

const absenceJustificationSchema = z.object({
  id: z.string().uuid(), absenceId: z.string().uuid(), teachingAssignmentId: z.string().uuid(), studentId: z.string().uuid(),
  studentApogeeCode: z.string(), studentFirstName: z.string(), studentLastName: z.string(), subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(), subjectModuleTitle: z.string(), absenceDate: z.string(), reason: z.string(),
  status: z.enum(["PENDING", "ACCEPTED", "REJECTED"]), documentId: z.string().uuid(), documentFileName: z.string(),
  documentContentType: z.string(), decisionNote: z.string().nullable(), submittedAt: z.string(), reviewedAt: z.string().nullable(),
});

const attendanceStudentSchema = z.object({
  studentId: z.string().uuid(),
  apogeeCode: z.string(),
  nationalStudentCode: z.string().nullable(),
  universityEmail: z.string(),
  firstName: z.string(),
  lastName: z.string(),
});

const absenceRecordSchema = z.object({
  id: z.string().uuid(),
  moduleRegistrationId: z.string().uuid(),
  studentId: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  teachingAssignmentId: z.string().uuid(),
  recordedByProfessorId: z.string().uuid(),
  absenceDate: z.string(),
  justified: z.boolean(),
  justificationNote: z.string().nullable(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

const attendanceQrSessionSchema = z.object({
  sessionId: z.string().uuid(),
  teachingAssignmentId: z.string().uuid(),
  attendanceDate: z.string(),
  token: z.string(),
  tokenExpiresAt: z.string(),
  closesAt: z.string(),
  checkedInStudentIds: z.array(z.string().uuid()),
});

export type AttendanceStudent = z.infer<typeof attendanceStudentSchema>;
export type AbsenceRecord = z.infer<typeof absenceRecordSchema>;
export type AttendanceQrSession = z.infer<typeof attendanceQrSessionSchema>;
export type AbsenceJustification = z.infer<typeof absenceJustificationSchema>;

export const professorAttendanceKeys = {
  roster: (teachingAssignmentId: string) => ["professor-attendance", "roster", teachingAssignmentId] as const,
  absences: (teachingAssignmentId: string) => ["professor-attendance", "absences", teachingAssignmentId] as const,
  qrSession: (sessionId: string) => ["professor-attendance", "qr-session", sessionId] as const,
  justifications: (teachingAssignmentId: string) => ["professor-attendance", "justifications", teachingAssignmentId] as const,
};

export async function getAttendanceRoster(teachingAssignmentId: string): Promise<AttendanceStudent[]> {
  const result = await apiClient.GET("/api/v1/teaching-assignments/{teachingAssignmentId}/students", {
    params: { path: { teachingAssignmentId } },
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(attendanceStudentSchema).parse(result.data);
}

export async function getTeachingAssignmentJustifications(teachingAssignmentId: string): Promise<AbsenceJustification[]> {
  return z.array(absenceJustificationSchema).parse(await qrRequest(`/api/v1/teaching-assignments/${teachingAssignmentId}/absence-justifications`));
}

export async function reviewAbsenceJustification(id: string, decision: "ACCEPTED" | "REJECTED", note: string): Promise<AbsenceJustification> {
  return absenceJustificationSchema.parse(await qrRequest(`/api/v1/absence-justifications/${id}/decision`, { method: "PUT", body: JSON.stringify({ decision, note: note || null }) }));
}

export async function downloadJustificationDocument(id: string, filename: string): Promise<void> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/absence-justifications/${id}/document`);
  if (!response.ok) throw apiRequestError(response, await response.json().catch(() => null));
  const url = URL.createObjectURL(await response.blob()); const anchor = document.createElement("a"); anchor.href = url; anchor.download = filename; anchor.click(); URL.revokeObjectURL(url);
}

async function qrRequest(path: string, init?: RequestInit): Promise<unknown> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}${path}`, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
  });
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) throw apiRequestError(response, body);
  return body;
}

export async function startAttendanceQrSession(teachingAssignmentId: string, attendanceDate: string): Promise<AttendanceQrSession> {
  return attendanceQrSessionSchema.parse(await qrRequest(`/api/v1/attendance/qr-sessions/teaching-assignments/${teachingAssignmentId}`, {
    method: "POST",
    body: JSON.stringify({ attendanceDate }),
  }));
}

export async function getAttendanceQrSession(sessionId: string): Promise<AttendanceQrSession> {
  return attendanceQrSessionSchema.parse(await qrRequest(`/api/v1/attendance/qr-sessions/${sessionId}`));
}

export async function closeAttendanceQrSession(sessionId: string): Promise<void> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/attendance/qr-sessions/${sessionId}`, { method: "DELETE" });
  if (!response.ok) {
    const body: unknown = await response.json().catch(() => null);
    throw apiRequestError(response, body);
  }
}

export async function getTeachingAssignmentAbsences(teachingAssignmentId: string): Promise<AbsenceRecord[]> {
  const result = await apiClient.GET("/api/v1/teaching-assignments/{teachingAssignmentId}/absences", {
    params: { path: { teachingAssignmentId } },
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(absenceRecordSchema).parse(result.data);
}

export async function confirmAttendance(teachingAssignmentId: string, attendanceDate: string, absentStudentIds: string[]): Promise<AbsenceRecord[]> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/teaching-assignments/${teachingAssignmentId}/attendance`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ attendanceDate, absentStudentIds }),
  });
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) throw apiRequestError(response, body);
  return z.array(absenceRecordSchema).parse(body);
}
