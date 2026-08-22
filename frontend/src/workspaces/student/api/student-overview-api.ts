import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { env } from "@/shared/config/env";

const gradeSchema = z.object({
  gradeRecordId: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(),
  academicYearId: z.string().uuid(),
  semesterId: z.string().uuid(),
  sessionType: z.enum(["NORMAL", "RATTRAPAGE"]),
  inscriptionNumber: z.number().int(),
  gradeValue: z.number().nullable(),
  zeroGradeReason: z.enum(["EARNED", "ABSENT"]).nullable().optional(),
  finalGradeValue: z.number().nullable(),
  moduleResultStatus: z.enum(["V", "AV", "NV"]).nullable(),
  publishedAt: z.string(),
});

const invitationSchema = z.object({
  id: z.string().uuid(),
  moduleExamId: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(),
  academicYearId: z.string().uuid(),
  academicYearLabel: z.string(),
  academicYearStatus: z.string(),
  semesterId: z.string().uuid(),
  semesterName: z.string(),
  semesterStartDate: z.string(),
  semesterEndDate: z.string(),
  academicLevelId: z.string().uuid(),
  academicLevelName: z.string(),
  programFiliereId: z.string().uuid(),
  programFiliereCode: z.string(),
  programFiliereName: z.string(),
  sessionType: z.enum(["NORMAL", "RATTRAPAGE"]),
  examDate: z.string(),
  startTime: z.string(),
  endTime: z.string().nullable(),
  roomCode: z.string().nullable(),
  examGroupLabel: z.string().nullable(),
});

const absenceSchema = z.object({
  id: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(),
  academicYearId: z.string().uuid(),
  academicYearLabel: z.string(),
  semesterId: z.string().uuid(),
  semesterName: z.string(),
  teachingAssignmentId: z.string().uuid(),
  recordedByProfessorId: z.string().uuid(),
  absenceDate: z.string(),
  justified: z.boolean(),
  justificationNote: z.string().nullable(),
});

const academicContextSchema = z.object({
  academicRegistrationId: z.string().uuid(),
  semesterRegistrationId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  academicYearLabel: z.string(),
  academicYearStatus: z.string(),
  programPathId: z.string().uuid(),
  programPathName: z.string(),
  programFiliereId: z.string().uuid(),
  programFiliereCode: z.string(),
  programFiliereName: z.string(),
  academicLevelId: z.string().uuid(),
  academicLevelName: z.string(),
  semesterId: z.string().uuid(),
  semesterName: z.string(),
  semesterStartDate: z.string(),
  semesterEndDate: z.string(),
  registrationStatus: z.string(),
  classGroupId: z.string().uuid().nullable(),
  classGroupName: z.string().nullable(),
  tdGroups: z.array(z.string()),
  tpGroups: z.array(z.string()),
});

export type StudentOverviewGrade = z.infer<typeof gradeSchema>;
export type StudentExamInvitation = z.infer<typeof invitationSchema>;
export type StudentAbsence = z.infer<typeof absenceSchema>;
export type StudentAcademicContext = z.infer<typeof academicContextSchema>;

export const studentOverviewKeys = {
  grades: () => ["student-overview", "grades"] as const,
  exams: () => ["student-overview", "exam-invitations"] as const,
  absences: () => ["student-overview", "absences"] as const,
  academicContexts: () => ["student-overview", "academic-contexts"] as const,
};

async function parse<T>(result: { response: Response; data?: unknown; error?: unknown }, schema: z.ZodType<T>): Promise<T> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return schema.parse(result.data);
}

export async function getMyStudentGrades(): Promise<StudentOverviewGrade[]> {
  return parse(await apiClient.GET("/api/v1/me/grades"), z.array(gradeSchema));
}

export async function getMyExamInvitations(): Promise<StudentExamInvitation[]> {
  return parse(await apiClient.GET("/api/v1/me/exam-invitations"), z.array(invitationSchema));
}

export async function getMyAbsences(): Promise<StudentAbsence[]> {
  return parse(await apiClient.GET("/api/v1/me/absences"), z.array(absenceSchema));
}

export async function getMyAcademicContexts(): Promise<StudentAcademicContext[]> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/me/academic-contexts`);
  if (!response.ok) throw apiRequestError(response, undefined);
  return z.array(academicContextSchema).parse(await response.json());
}
