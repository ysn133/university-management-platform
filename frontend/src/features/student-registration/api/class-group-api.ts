import { z } from "zod";
import type { components } from "@/shared/api/generated/schema";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const classGroupSchema = z.object({
  id: z.string().uuid(),
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  programFiliereId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  name: z.string(),
  status: z.enum(["ACTIVE", "INACTIVE", "ARCHIVED"]),
});

const rosterSchema = z.object({
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  semesterId: z.string().uuid(),
  totalStudents: z.number().int(),
  unassignedAcademicRegistrationIds: z.array(z.string().uuid()),
  groups: z.array(z.object({
    classGroupId: z.string().uuid(),
    name: z.string(),
    academicRegistrationIds: z.array(z.string().uuid()),
  })),
});

const generationSchema = z.object({
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  totalStudents: z.number().int(),
  semesterAssignmentsCreated: z.number().int(),
  groups: z.array(z.object({
    classGroupId: z.string().uuid(),
    name: z.string(),
    studentCount: z.number().int(),
  })),
});

const rebalanceSchema = z.object({
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  totalStudents: z.number().int(),
  semesterAssignmentsChanged: z.number().int(),
  groups: z.array(z.object({
    classGroupId: z.string().uuid(),
    name: z.string(),
    studentCount: z.number().int(),
  })),
});

const bulkAssignmentSchema = z.object({
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  studentsProcessed: z.number().int(),
  semesterAssignmentsCreated: z.number().int(),
});

const teachingGroupPolicySchema = z.object({
  id: z.string().uuid(),
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  groupType: z.enum(["TD", "TP"]),
  maximumGroupSize: z.number().int().positive(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export type ClassGroup = z.infer<typeof classGroupSchema>;
export type ClassGroupRoster = z.infer<typeof rosterSchema>;
export type ClassGroupGeneration = z.infer<typeof generationSchema>;
export type ClassGroupRebalance = z.infer<typeof rebalanceSchema>;
export type TeachingGroupPolicy = z.infer<typeof teachingGroupPolicySchema>;
export type GenerateClassGroupsRequest = components["schemas"]["GenerateClassGroupsRequest"];
export type BulkAssignStudentClassesRequest = components["schemas"]["BulkAssignStudentClassesRequest"];

export const classGroupKeys = {
  groups: (academicLevelId: string, academicYearId: string) => ["class-groups", academicLevelId, academicYearId] as const,
  roster: (academicLevelId: string, academicYearId: string, semesterId: string) => ["class-group-roster", academicLevelId, academicYearId, semesterId] as const,
  teachingPolicies: (academicLevelId: string, academicYearId: string) => ["teaching-group-policies", academicLevelId, academicYearId] as const,
};

async function parseResponse<T>(result: { response: Response; data?: unknown; error?: unknown }, schema: z.ZodType<T>): Promise<T> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return schema.parse(result.data);
}

export async function getClassGroups(academicLevelId: string, academicYearId: string): Promise<ClassGroup[]> {
  return parseResponse(await apiClient.GET("/api/v1/academic-levels/{academicLevelId}/class-groups", {
    params: { path: { academicLevelId }, query: { academicYearId } },
  }), z.array(classGroupSchema));
}

export async function getClassGroupRoster(academicLevelId: string, academicYearId: string, semesterId: string): Promise<ClassGroupRoster> {
  return parseResponse(await apiClient.GET("/api/v1/academic-levels/{academicLevelId}/class-groups/roster", {
    params: { path: { academicLevelId }, query: { academicYearId, semesterId } },
  }), rosterSchema);
}

export async function generateClassGroups(academicLevelId: string, academicYearId: string, request: GenerateClassGroupsRequest): Promise<ClassGroupGeneration> {
  return parseResponse(await apiClient.POST("/api/v1/academic-levels/{academicLevelId}/class-groups/generate", {
    params: { path: { academicLevelId }, query: { academicYearId } },
    body: request,
  }), generationSchema);
}

export async function rebalanceClassGroups(academicLevelId: string, academicYearId: string, request: GenerateClassGroupsRequest): Promise<ClassGroupRebalance> {
  return parseResponse(await apiClient.PUT("/api/v1/academic-levels/{academicLevelId}/class-groups/rebalance", {
    params: { path: { academicLevelId }, query: { academicYearId } },
    body: request,
  }), rebalanceSchema);
}

export async function createClassGroup(academicLevelId: string, academicYearId: string, name: string): Promise<ClassGroup> {
  return parseResponse(await apiClient.POST("/api/v1/academic-levels/{academicLevelId}/class-groups", {
    params: { path: { academicLevelId }, query: { academicYearId } },
    body: { name, status: "ACTIVE" },
  }), classGroupSchema);
}

export async function bulkAssignStudentClasses(academicLevelId: string, academicYearId: string, request: BulkAssignStudentClassesRequest) {
  return parseResponse(await apiClient.PUT("/api/v1/academic-levels/{academicLevelId}/class-groups/assignments", {
    params: { path: { academicLevelId }, query: { academicYearId } },
    body: request,
  }), bulkAssignmentSchema);
}

export async function getTeachingGroupPolicies(academicLevelId: string, academicYearId: string): Promise<TeachingGroupPolicy[]> {
  return parseResponse(await apiClient.GET("/api/v1/academic-levels/{academicLevelId}/teaching-group-policies", {
    params: { path: { academicLevelId }, query: { academicYearId } },
  }), z.array(teachingGroupPolicySchema));
}

export async function replaceTeachingGroupPolicies(
  academicLevelId: string,
  academicYearId: string,
  policies: Array<{ groupType: "TD" | "TP"; maximumGroupSize: number }>,
): Promise<TeachingGroupPolicy[]> {
  return parseResponse(await apiClient.PUT("/api/v1/academic-levels/{academicLevelId}/teaching-group-policies", {
    params: { path: { academicLevelId }, query: { academicYearId } },
    body: { policies },
  }), z.array(teachingGroupPolicySchema));
}
