import { z } from "zod";
import type { components } from "@/shared/api/generated/schema";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const professorSchema = z.object({
  professorId: z.string().uuid(),
  userAccountId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  employeeNumber: z.string(),
  academicRankId: z.string().uuid().nullable().optional(),
  academicRank: z.string().nullable().optional(),
  hireDate: z.string().nullable().optional(),
  maximumWeeklyTeachingMinutes: z.number().int(),
  universityEmail: z.string(),
  roleType: z.literal("PROFESSOR"),
  accountStatus: z.enum(["ACTIVE", "LOCKED", "DEACTIVATED", "ARCHIVED"]),
  firstName: z.string(),
  lastName: z.string(),
  birthDate: z.string(),
  placeOfBirth: z.string(),
  nationality: z.string(),
  cin: z.string().nullable().optional(),
  sex: z.enum(["MALE", "FEMALE"]),
  phoneNumber: z.string().nullable().optional(),
  profilePicturePath: z.string().nullable().optional(),
});

const createProfessorResponseSchema = z.object({
  professorId: z.string().uuid(),
  userAccountId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  employeeNumber: z.string(),
  roleType: z.literal("PROFESSOR"),
});

const professorExpertiseSchema = z.object({
  professorId: z.string().uuid(),
  academicDomains: z.array(z.object({
    academicDomainId: z.string().uuid(),
    code: z.string(),
    name: z.string(),
  })),
});

export type Professor = z.infer<typeof professorSchema>;
export type ProfessorAccountStatus = Professor["accountStatus"];
export type ProfessorExpertise = z.infer<typeof professorExpertiseSchema>;
export type CreateProfessorRequest = components["schemas"]["CreateProfessorRequest"];
export type UpdateProfessorRequest = components["schemas"]["UpdateProfessorRequest"];
export type ProfessorLifecycleAction = "lock" | "unlock" | "deactivate" | "archive";

export interface ProfessorDirectoryFilters {
  query?: string;
  status?: ProfessorAccountStatus;
  joinedFrom?: string;
  joinedTo?: string;
  academicDomainId?: string;
}

export const professorManagementKeys = {
  professors: (establishmentId: string, filters: ProfessorDirectoryFilters = {}) => ["professor-management", "professors", establishmentId, filters] as const,
  professor: (professorId: string) => ["professor-management", "professor", professorId] as const,
  expertise: (professorId: string) => ["professor-management", "expertise", professorId] as const,
};

async function parseResponse<T>(result: { response: Response; data?: unknown; error?: unknown }, schema: z.ZodType<T>): Promise<T> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return schema.parse(result.data);
}

export async function getProfessors(establishmentId: string, filters: ProfessorDirectoryFilters = {}): Promise<Professor[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/professors", { params: { path: { establishmentId }, query: filters } }), z.array(professorSchema));
}

export async function createProfessor(establishmentId: string, request: CreateProfessorRequest): Promise<{ professorId: string }> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/professors", { params: { path: { establishmentId } }, body: request }), createProfessorResponseSchema);
}

export async function getProfessor(professorId: string): Promise<Professor> {
  return parseResponse(await apiClient.GET("/api/v1/professors/{professorId}", { params: { path: { professorId } } }), professorSchema);
}

export async function updateProfessor(professorId: string, request: UpdateProfessorRequest): Promise<Professor> {
  return parseResponse(await apiClient.PUT("/api/v1/professors/{professorId}", { params: { path: { professorId } }, body: request }), professorSchema);
}

export async function resetProfessorPassword(professorId: string, newPassword: string): Promise<void> {
  const result = await apiClient.POST("/api/v1/professors/{professorId}/password-reset", { params: { path: { professorId } }, body: { newPassword } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

export async function changeProfessorStatus(professorId: string, action: ProfessorLifecycleAction): Promise<void> {
  const paths = {
    lock: "/api/v1/professors/{professorId}/lock",
    unlock: "/api/v1/professors/{professorId}/unlock",
    deactivate: "/api/v1/professors/{professorId}/deactivate",
    archive: "/api/v1/professors/{professorId}/archive",
  } as const;
  const result = await apiClient.POST(paths[action], { params: { path: { professorId } } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

export async function getProfessorExpertise(professorId: string): Promise<ProfessorExpertise> {
  return parseResponse(await apiClient.GET("/api/v1/professors/{professorId}/expertise", { params: { path: { professorId } } }), professorExpertiseSchema);
}

export async function replaceProfessorExpertise(professorId: string, academicDomainIds: string[]): Promise<ProfessorExpertise> {
  return parseResponse(await apiClient.PUT("/api/v1/professors/{professorId}/expertise", { params: { path: { professorId } }, body: { academicDomainIds } }), professorExpertiseSchema);
}
