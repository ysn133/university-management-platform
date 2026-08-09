import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const teachingGroupMemberSchema = z.object({
  semesterRegistrationId: z.string().uuid(),
  studentId: z.string().uuid(),
  apogeeCode: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  secondInscription: z.boolean(),
});

const teachingGroupSchema = z.object({
  id: z.string().uuid(),
  semesterId: z.string().uuid(),
  sourceClassGroupId: z.string().uuid(),
  sourceClassGroupName: z.string(),
  name: z.string(),
  groupType: z.enum(["TD", "TP"]),
  members: z.array(teachingGroupMemberSchema),
});

const teachingGroupRosterSchema = z.object({
  semesterId: z.string().uuid(),
  groups: z.array(teachingGroupSchema),
});

export type TeachingGroup = z.infer<typeof teachingGroupSchema>;
export type TeachingGroupMember = z.infer<typeof teachingGroupMemberSchema>;
export type TeachingGroupRoster = z.infer<typeof teachingGroupRosterSchema>;

export const teachingGroupKeys = {
  roster: (semesterId: string) => ["teaching-groups", semesterId] as const,
};

async function parseRoster(result: { response: Response; data?: unknown; error?: unknown }): Promise<TeachingGroupRoster> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return teachingGroupRosterSchema.parse(result.data);
}

export async function getTeachingGroups(semesterId: string): Promise<TeachingGroupRoster> {
  return parseRoster(await apiClient.GET("/api/v1/semesters/{semesterId}/teaching-groups", {
    params: { path: { semesterId } },
  }));
}

export async function generateTeachingGroups(semesterId: string): Promise<TeachingGroupRoster> {
  return parseRoster(await apiClient.POST("/api/v1/semesters/{semesterId}/teaching-groups/generate", {
    params: { path: { semesterId } },
  }));
}

export async function moveTeachingGroupMember(teachingGroupId: string, semesterRegistrationId: string): Promise<TeachingGroupRoster> {
  return parseRoster(await apiClient.PUT("/api/v1/teaching-groups/{teachingGroupId}/members/{semesterRegistrationId}", {
    params: { path: { teachingGroupId, semesterRegistrationId } },
  }));
}
