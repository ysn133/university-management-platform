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
  audienceType: z.enum(["WHOLE_COHORT", "CLASS_GROUP", "SUBGROUP"]),
  status: z.enum(["ACTIVE", "INACTIVE"]),
});

export type TeachingPlanItem = z.infer<typeof teachingPlanItemSchema>;

export const teachingPlanKeys = {
  semester: (semesterId: string) => ["teaching-plan", "semester", semesterId] as const,
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
