import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { env } from "@/shared/config/env";

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

export const professorAttendanceKeys = {
  roster: (teachingAssignmentId: string) => ["professor-attendance", "roster", teachingAssignmentId] as const,
  absences: (teachingAssignmentId: string) => ["professor-attendance", "absences", teachingAssignmentId] as const,
  qrSession: (sessionId: string) => ["professor-attendance", "qr-session", sessionId] as const,
};

export async function getAttendanceRoster(teachingAssignmentId: string): Promise<AttendanceStudent[]> {
  const result = await apiClient.GET("/api/v1/teaching-assignments/{teachingAssignmentId}/students", {
    params: { path: { teachingAssignmentId } },
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(attendanceStudentSchema).parse(result.data);
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
