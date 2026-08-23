import { z } from "zod";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { env } from "@/shared/config/env";

const gradeSchema = z.object({
  gradeRecordId: z.string().uuid(), subjectModuleId: z.string().uuid(), subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(), academicYearId: z.string().uuid(), semesterId: z.string().uuid(),
  sessionType: z.enum(["NORMAL", "RATTRAPAGE"]), inscriptionNumber: z.number().int(), gradeValue: z.number().nullable(),
  zeroGradeReason: z.enum(["EARNED", "ABSENT"]).nullable().optional(), finalGradeValue: z.number().nullable(),
  moduleResultStatus: z.enum(["V", "AV", "NV"]).nullable(), publishedAt: z.string(), revised: z.boolean().default(false),
});

const absenceSchema = z.object({
  id: z.string().uuid(), subjectModuleId: z.string().uuid(), subjectModuleCode: z.string(), subjectModuleTitle: z.string(),
  academicYearId: z.string().uuid(), academicYearLabel: z.string(), semesterId: z.string().uuid(), semesterName: z.string(),
  teachingAssignmentId: z.string().uuid(), recordedByProfessorId: z.string().uuid(), absenceDate: z.string(),
  justified: z.boolean(), justificationNote: z.string().nullable(),
});

const scheduleEntrySchema = z.object({
  id: z.string().uuid(), academicYearId: z.string().uuid(), academicYearLabel: z.string(), academicYearStatus: z.string(),
  semesterId: z.string().uuid(), semesterName: z.string(), semesterTermType: z.enum(["AUTUMN", "SPRING"]),
  semesterStartDate: z.string(), semesterEndDate: z.string(), academicLevelId: z.string().uuid(), academicLevelName: z.string(),
  programFiliereId: z.string().uuid(), programFiliereCode: z.string(), programFiliereName: z.string(),
  subjectModuleId: z.string().uuid(), subjectModuleCode: z.string(), subjectModuleTitle: z.string(), componentType: z.string(),
  audienceType: z.string(), teachingGroupName: z.string(), professorName: z.string(), dayOfWeek: z.string(),
  startTime: z.string(), endTime: z.string(), roomCode: z.string().nullable(), roomName: z.string().nullable(),
  blockCode: z.string().nullable(), blockName: z.string().nullable(),
});

const progressionSchema = z.object({
  id: z.string().uuid(), academicRegistrationId: z.string().uuid(), academicRuleProfileId: z.string().uuid(),
  decisionStatus: z.enum(["PROMOTED", "PROMOTED_BY_COMPENSATION", "PROMOTED_WITH_DEBT", "LEVEL_VALIDATED", "REPEAT", "FAILED"]),
  annualAverage: z.number(), outstandingModuleCount: z.number().int(), decidedAt: z.string(),
});

export type ManagedStudentGrade = z.infer<typeof gradeSchema>;
export type ManagedStudentAbsence = z.infer<typeof absenceSchema>;
export type ManagedStudentScheduleEntry = z.infer<typeof scheduleEntrySchema>;
export type ManagedStudentProgression = z.infer<typeof progressionSchema>;

async function get<T>(path: string, schema: z.ZodType<T>): Promise<T> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}${path}`);
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) throw apiRequestError(response, body);
  return schema.parse(body);
}

export const studentAcademicRecordKeys = {
  grades: (studentId: string, academicYearId: string, academicLevelId: string) => ["managed-student-record", studentId, "grades", academicYearId, academicLevelId] as const,
  absences: (studentId: string, academicYearId: string) => ["managed-student-record", studentId, "absences", academicYearId] as const,
  schedule: (studentId: string) => ["managed-student-record", studentId, "schedule"] as const,
  progression: (registrationId: string) => ["managed-student-record", registrationId, "progression"] as const,
};

export function getManagedStudentGrades(studentId: string, academicYearId: string, academicLevelId: string) {
  return get(`/api/v1/students/${studentId}/grades?academicYearId=${academicYearId}&academicLevelId=${academicLevelId}`, z.array(gradeSchema));
}

export function getManagedStudentAbsences(establishmentId: string, studentId: string, academicYearId: string) {
  return get(`/api/v1/establishments/${establishmentId}/absences?studentId=${studentId}&academicYearId=${academicYearId}`, z.array(absenceSchema));
}

export function getManagedStudentSchedule(studentId: string) {
  return get(`/api/v1/students/${studentId}/schedule-entries`, z.array(scheduleEntrySchema));
}

export function getManagedStudentProgression(registrationId: string) {
  return get(`/api/v1/academic-registrations/${registrationId}/progression-decision`, progressionSchema);
}
