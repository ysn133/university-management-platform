import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { env } from "@/shared/config/env";

const examScheduleSchema = z.object({
  id: z.string().uuid(), establishmentId: z.string().uuid(), academicYearId: z.string().uuid(), semesterId: z.string().uuid(),
  sessionType: z.enum(["NORMAL", "RATTRAPAGE"]), publicationStatus: z.enum(["DRAFT", "PUBLISHED"]), startDate: z.string(), endDate: z.string(),
});
const moduleExamSchema = z.object({
  id: z.string().uuid(), examScheduleId: z.string().uuid(), subjectModuleId: z.string().uuid(), classGroupId: z.string().uuid(),
  examDate: z.string(), startTime: z.string(), endTime: z.string().nullable().optional(), roomId: z.string().uuid().nullable().optional(),
  roomCode: z.string().nullable().optional(), roomName: z.string().nullable().optional(), candidateListGeneratedAt: z.string().nullable().optional(),
});

export type ExamSchedule = z.infer<typeof examScheduleSchema>;
export type ModuleExam = z.infer<typeof moduleExamSchema>;
export interface ExamScheduleInput { academicYearId: string; semesterId: string; sessionType: "NORMAL" | "RATTRAPAGE"; startDate: string; endDate: string; }
export interface ModuleExamInput {
  subjectModuleId: string;
  classGroupId: string;
  examDate: string;
  startTime: string;
  endTime?: string;
  roomId: string;
  roomAllocations?: Array<{ examGroupId: string; roomId: string }>;
}
export interface ExamGroup { id: string; label: string; groupOrder: number; studentCount: number; }
export interface ExamGroupPlan { examScheduleId: string; classGroupId: string; totalStudentCount: number; splitCount: number; groups: ExamGroup[]; }
export interface ExamRoomAllocation { id: string; examGroupId: string; examGroupLabel: string; studentCount: number; roomId: string; roomCode: string; roomName: string; roomCapacity: number; }

export const examPlanningKeys = {
  schedules: (establishmentId: string) => ["exam-planning", "schedules", establishmentId] as const,
  exams: (scheduleId: string) => ["exam-planning", "module-exams", scheduleId] as const,
};

async function parse<T>(result: { response: Response; data?: unknown; error?: unknown }, schema: z.ZodType<T>): Promise<T> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return schema.parse(result.data);
}

export async function getExamSchedules(establishmentId: string): Promise<ExamSchedule[]> {
  return parse(await apiClient.GET("/api/v1/establishments/{establishmentId}/exam-schedules", { params: { path: { establishmentId } } }), z.array(examScheduleSchema));
}
export async function createExamSchedule(establishmentId: string, input: ExamScheduleInput): Promise<ExamSchedule> {
  return parse(await apiClient.POST("/api/v1/establishments/{establishmentId}/exam-schedules", { params: { path: { establishmentId } }, body: input }), examScheduleSchema);
}
export async function updateExamSchedule(id: string, input: ExamScheduleInput): Promise<ExamSchedule> {
  return parse(await apiClient.PUT("/api/v1/exam-schedules/{examScheduleId}", { params: { path: { examScheduleId: id } }, body: input }), examScheduleSchema);
}
export async function publishExamSchedule(id: string): Promise<ExamSchedule> {
  return parse(await apiClient.POST("/api/v1/exam-schedules/{examScheduleId}/publish", { params: { path: { examScheduleId: id } } }), examScheduleSchema);
}
export async function getModuleExams(scheduleId: string): Promise<ModuleExam[]> {
  return parse(await apiClient.GET("/api/v1/exam-schedules/{examScheduleId}/module-exams", { params: { path: { examScheduleId: scheduleId } } }), z.array(moduleExamSchema));
}
export async function createModuleExam(scheduleId: string, input: ModuleExamInput): Promise<ModuleExam> {
  return parse(await apiClient.POST("/api/v1/exam-schedules/{examScheduleId}/module-exams", { params: { path: { examScheduleId: scheduleId } }, body: input }), moduleExamSchema);
}
export async function updateModuleExam(id: string, input: ModuleExamInput): Promise<ModuleExam> {
  return parse(await apiClient.PUT("/api/v1/module-exams/{moduleExamId}", { params: { path: { moduleExamId: id } }, body: input }), moduleExamSchema);
}
export async function deleteModuleExam(id: string): Promise<void> {
  const result = await apiClient.DELETE("/api/v1/module-exams/{moduleExamId}", { params: { path: { moduleExamId: id } } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

async function requestJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}${path}`, { ...init, headers: { "Content-Type": "application/json", ...init?.headers } });
  if (!response.ok) throw apiRequestError(response, await response.json().catch(() => undefined));
  return response.json() as Promise<T>;
}

export function getExamGroupPlan(scheduleId: string, classGroupId: string): Promise<ExamGroupPlan> {
  return requestJson(`/api/v1/exam-schedules/${scheduleId}/class-groups/${classGroupId}/exam-groups`);
}

export function generateExamGroups(scheduleId: string, classGroupId: string, splitCount: number): Promise<ExamGroupPlan> {
  return requestJson(`/api/v1/exam-schedules/${scheduleId}/class-groups/${classGroupId}/exam-groups/generate`, { method: "POST", body: JSON.stringify({ splitCount }) });
}

export function getExamRoomAllocations(moduleExamId: string): Promise<ExamRoomAllocation[]> {
  return requestJson(`/api/v1/module-exams/${moduleExamId}/room-allocations`);
}

export function replaceExamRoomAllocations(moduleExamId: string, allocations: Array<{ examGroupId: string; roomId: string }>): Promise<ExamRoomAllocation[]> {
  return requestJson(`/api/v1/module-exams/${moduleExamId}/room-allocations`, { method: "PUT", body: JSON.stringify({ allocations }) });
}
