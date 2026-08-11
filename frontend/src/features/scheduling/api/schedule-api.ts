import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const daySchema = z.enum(["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]);
const scheduleSchema = z.object({
  id: z.string().uuid(), establishmentId: z.string().uuid(), academicYearId: z.string().uuid(), semesterId: z.string().uuid(),
  publicationStatus: z.enum(["DRAFT", "PUBLISHED"]), publishedAt: z.string().nullable().optional(),
});
const entrySchema = z.object({
  id: z.string().uuid(), semesterScheduleId: z.string().uuid(), teachingAssignmentId: z.string().uuid(), professorId: z.string().uuid(),
  subjectModuleId: z.string().uuid(), teachingGroupId: z.string().uuid(), teachingGroupName: z.string(),
  sourceClassGroupId: z.string().uuid().nullable(), sourceClassGroupName: z.string().nullable(), audienceType: z.enum(["WHOLE_COHORT", "CLASS_GROUP", "SUBGROUP"]), dayOfWeek: daySchema,
  startTime: z.string(), endTime: z.string(), roomId: z.string().uuid(), roomCode: z.string(), roomName: z.string(), blockId: z.string().uuid().nullable().optional(),
});

export type SemesterSchedule = z.infer<typeof scheduleSchema>;
export type ScheduleEntry = z.infer<typeof entrySchema>;
export type ScheduleDay = z.infer<typeof daySchema>;
export interface ScheduleEntryInput { teachingAssignmentId: string; dayOfWeek: ScheduleDay; startTime: string; endTime: string; roomId: string; }

export const scheduleKeys = {
  schedules: (establishmentId: string) => ["scheduling", "semester-schedules", establishmentId] as const,
  entries: (scheduleId: string) => ["scheduling", "entries", scheduleId] as const,
};

async function parse<T>(result: { response: Response; data?: unknown; error?: unknown }, schema: z.ZodType<T>): Promise<T> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return schema.parse(result.data);
}

export async function getSemesterSchedules(establishmentId: string): Promise<SemesterSchedule[]> {
  return parse(await apiClient.GET("/api/v1/establishments/{establishmentId}/semester-schedules", { params: { path: { establishmentId } } }), z.array(scheduleSchema));
}

export async function createSemesterSchedule(establishmentId: string, academicYearId: string, semesterId: string): Promise<SemesterSchedule> {
  return parse(await apiClient.POST("/api/v1/establishments/{establishmentId}/semester-schedules", { params: { path: { establishmentId } }, body: { academicYearId, semesterId } }), scheduleSchema);
}

export async function publishSemesterSchedule(scheduleId: string): Promise<SemesterSchedule> {
  return parse(await apiClient.POST("/api/v1/semester-schedules/{scheduleId}/publish", { params: { path: { scheduleId } } }), scheduleSchema);
}

export async function getScheduleEntries(scheduleId: string): Promise<ScheduleEntry[]> {
  return parse(await apiClient.GET("/api/v1/semester-schedules/{scheduleId}/entries", { params: { path: { scheduleId } } }), z.array(entrySchema));
}

export async function createScheduleEntry(scheduleId: string, input: ScheduleEntryInput): Promise<ScheduleEntry> {
  return parse(await apiClient.POST("/api/v1/semester-schedules/{scheduleId}/entries", { params: { path: { scheduleId } }, body: input }), entrySchema);
}

export async function updateScheduleEntry(entryId: string, input: ScheduleEntryInput): Promise<ScheduleEntry> {
  return parse(await apiClient.PUT("/api/v1/schedule-entries/{scheduleEntryId}", { params: { path: { scheduleEntryId: entryId } }, body: input }), entrySchema);
}

export async function deleteScheduleEntry(entryId: string): Promise<void> {
  const result = await apiClient.DELETE("/api/v1/schedule-entries/{scheduleEntryId}", { params: { path: { scheduleEntryId: entryId } } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}
