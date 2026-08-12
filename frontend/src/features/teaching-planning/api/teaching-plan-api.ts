import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const teachingPlanItemSchema = z.object({
  id: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  moduleTeachingComponentId: z.string().uuid(),
  componentType: z.enum(["COURSE", "TD", "TP"]),
  teachingGroupId: z.string().uuid(),
  teachingGroupName: z.string(),
  sourceClassGroupId: z.string().uuid().nullable(),
  sourceClassGroupName: z.string().nullable(),
  audienceType: z.enum(["WHOLE_COHORT", "CLASS_GROUP", "SUBGROUP"]),
  status: z.enum(["ACTIVE", "INACTIVE"]),
});

export type TeachingPlanItem = z.infer<typeof teachingPlanItemSchema>;

const teachingAssignmentSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  professorId: z.string().uuid(),
  teachingRequirementId: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(),
  componentType: z.enum(["COURSE", "TD", "TP"]),
  sessionsPerWeek: z.number().int(),
  sessionDurationMinutes: z.number().int(),
  teachingGroupId: z.string().uuid(),
  teachingGroupName: z.string(),
  semesterId: z.string().uuid(),
  semesterName: z.string(),
  academicYearId: z.string().uuid(),
  academicYearLabel: z.string(),
  academicLevelId: z.string().uuid(),
  academicLevelName: z.string(),
  programFiliereId: z.string().uuid(),
  programFiliereCode: z.string(),
  programFiliereName: z.string(),
  status: z.enum(["ACTIVE", "INACTIVE"]),
  assignmentSource: z.enum(["MANUAL", "AUTOMATIC"]),
});

const generationResponseSchema = z.object({
  semesterId: z.string().uuid(),
  preservedAssignmentCount: z.number().int(),
  createdAssignments: z.array(teachingAssignmentSchema),
  unresolvedRequirements: z.array(z.object({
    teachingRequirementId: z.string().uuid(),
    subjectModuleId: z.string().uuid(),
    componentType: z.enum(["COURSE", "TD", "TP"]),
    teachingGroupId: z.string().uuid(),
    teachingGroupName: z.string(),
    reason: z.enum([
      "NO_ACTIVE_PROFESSOR",
      "MISSING_ACADEMIC_DOMAIN_CONFIGURATION",
      "NO_MATCHING_EXPERTISE",
      "NO_ELIGIBLE_ACADEMIC_RANK",
      "WORKLOAD_CAPACITY_EXCEEDED",
    ]),
  })),
  professorWorkloads: z.array(z.object({
    professorId: z.string().uuid(),
    employeeNumber: z.string(),
    assignedWeeklyMinutes: z.number().int(),
    maximumWeeklyTeachingMinutes: z.number().int(),
  })),
});

export type TeachingAssignment = z.infer<typeof teachingAssignmentSchema>;
export type TeachingAssignmentGeneration = z.infer<typeof generationResponseSchema>;

export const teachingPlanKeys = {
  semester: (semesterId: string) => ["teaching-plan", "semester", semesterId] as const,
  assignments: (establishmentId: string) => ["teaching-plan", "assignments", establishmentId] as const,
  myAssignments: () => ["teaching-plan", "my-assignments"] as const,
};

async function parseItems(result: { response: Response; data?: unknown; error?: unknown }): Promise<TeachingPlanItem[]> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(teachingPlanItemSchema).parse(result.data);
}

export async function getTeachingPlan(semesterId: string): Promise<TeachingPlanItem[]> {
  return parseItems(await apiClient.GET("/api/v1/semesters/{semesterId}/teaching-requirements", { params: { path: { semesterId } } }));
}

export async function generateTeachingPlan(semesterId: string): Promise<TeachingPlanItem[]> {
  return parseItems(await apiClient.POST("/api/v1/semesters/{semesterId}/teaching-requirements/generate", { params: { path: { semesterId } } }));
}

export async function getTeachingAssignments(establishmentId: string): Promise<TeachingAssignment[]> {
  const result = await apiClient.GET("/api/v1/establishments/{establishmentId}/teaching-assignments", { params: { path: { establishmentId } } });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(teachingAssignmentSchema).parse(result.data);
}

export async function getMyTeachingAssignments(): Promise<TeachingAssignment[]> {
  const result = await apiClient.GET("/api/v1/me/teaching-assignments");
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(teachingAssignmentSchema).parse(result.data);
}

export async function generateTeachingAssignments(semesterId: string): Promise<TeachingAssignmentGeneration> {
  const result = await apiClient.POST("/api/v1/semesters/{semesterId}/teaching-assignments/generate", { params: { path: { semesterId } } });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return generationResponseSchema.parse(result.data);
}

export async function clearTeachingAssignments(semesterId: string): Promise<void> {
  const result = await apiClient.DELETE("/api/v1/semesters/{semesterId}/teaching-assignments", { params: { path: { semesterId } } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

export async function assignProfessor(establishmentId: string, professorId: string, teachingRequirementId: string): Promise<TeachingAssignment> {
  const result = await apiClient.POST("/api/v1/establishments/{establishmentId}/teaching-assignments", {
    params: { path: { establishmentId } },
    body: { professorId, teachingRequirementId },
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return teachingAssignmentSchema.parse(result.data);
}

export async function unassignProfessor(teachingAssignmentId: string): Promise<void> {
  const result = await apiClient.DELETE("/api/v1/teaching-assignments/{teachingAssignmentId}", {
    params: { path: { teachingAssignmentId } },
  });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}
