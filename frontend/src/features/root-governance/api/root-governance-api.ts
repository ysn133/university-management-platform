import { z } from "zod";
import type { components } from "@/shared/api/generated/schema";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const universitySchema = z.object({
  universityId: z.string().uuid(),
  universityName: z.string().min(1),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

const establishmentSchema = z.object({
  id: z.string().uuid(),
  universityId: z.string().uuid(),
  name: z.string().min(1),
  type: z.enum(["SCHOOL", "FACULTY", "INSTITUTE"]),
  status: z.enum(["ACTIVE", "INACTIVE", "ARCHIVED"]),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

const superAdminSchema = z.object({
  id: z.string().uuid(),
  accountId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  email: z.string().email(),
  role: z.literal("SUPER_ADMIN"),
  status: z.enum(["ACTIVE", "LOCKED", "DEACTIVATED", "ARCHIVED"]),
  firstName: z.string(),
  lastName: z.string(),
  birthDate: z.string(),
  cin: z.string().optional().nullable(),
  sex: z.enum(["MALE", "FEMALE"]),
  phoneNumber: z.string().optional().nullable(),
});

export type University = z.infer<typeof universitySchema>;
export type Establishment = z.infer<typeof establishmentSchema>;
export type SuperAdmin = z.infer<typeof superAdminSchema>;
export type EstablishmentType = Establishment["type"];
export type EstablishmentStatus = Establishment["status"];
export type AccountStatus = SuperAdmin["status"];

export type CreateEstablishmentRequest = components["schemas"]["CreateEstablishmentRequest"];
export type UpdateEstablishmentRequest = components["schemas"]["UpdateEstablishmentRequest"];
export type CreateSuperAdminRequest = components["schemas"]["CreateSuperAdminRequest"];
export type UpdateSuperAdminRequest = components["schemas"]["UpdateSuperAdminRequest"];

export interface EstablishmentFilters {
  query?: string;
  type?: EstablishmentType;
  status?: EstablishmentStatus;
}

export interface SuperAdminFilters {
  query?: string;
  status?: AccountStatus;
}

export const rootGovernanceKeys = {
  university: ["root-governance", "university"] as const,
  establishments: (universityId: string, filters: EstablishmentFilters = {}) =>
    ["root-governance", "establishments", universityId, filters] as const,
  establishment: (establishmentId: string) =>
    ["root-governance", "establishment", establishmentId] as const,
  superAdmins: (establishmentId: string, filters: SuperAdminFilters = {}) =>
    ["root-governance", "super-admins", establishmentId, filters] as const,
};

export async function getUniversity(): Promise<University> {
  const result = await apiClient.GET("/api/v1/university");
  if (!result.response.ok || !result.data) {
    throw apiRequestError(result.response, result.error);
  }
  return universitySchema.parse(result.data);
}

export async function getEstablishments(
  universityId: string,
  filters: EstablishmentFilters = {},
): Promise<Establishment[]> {
  const result = await apiClient.GET("/api/v1/university/{universityId}/establishments", {
    params: { path: { universityId }, query: filters },
  });
  if (!result.response.ok || !result.data) {
    throw apiRequestError(result.response, result.error);
  }
  return z.array(establishmentSchema).parse(result.data);
}

export async function getEstablishment(establishmentId: string): Promise<Establishment> {
  const result = await apiClient.GET("/api/v1/establishments/{id}", {
    params: { path: { id: establishmentId } },
  });
  if (!result.response.ok || !result.data) {
    throw apiRequestError(result.response, result.error);
  }
  return establishmentSchema.parse(result.data);
}

export async function createEstablishment(
  request: CreateEstablishmentRequest,
): Promise<Establishment> {
  const result = await apiClient.POST("/api/v1/establishments", { body: request });
  if (!result.response.ok || !result.data) {
    throw apiRequestError(result.response, result.error);
  }
  return establishmentSchema.parse(result.data);
}

export async function updateEstablishment(
  establishmentId: string,
  request: UpdateEstablishmentRequest,
): Promise<Establishment> {
  const result = await apiClient.PUT("/api/v1/establishments/{id}", {
    params: { path: { id: establishmentId } },
    body: request,
  });
  if (!result.response.ok || !result.data) {
    throw apiRequestError(result.response, result.error);
  }
  return establishmentSchema.parse(result.data);
}

async function changeEstablishmentStatus(
  establishmentId: string,
  action: "activate" | "deactivate",
): Promise<void> {
  const path = action === "activate"
    ? "/api/v1/establishments/{id}/activate" as const
    : "/api/v1/establishments/{id}/deactivate" as const;
  const result = await apiClient.POST(path, { params: { path: { id: establishmentId } } });
  if (!result.response.ok) {
    throw apiRequestError(result.response, result.error);
  }
}

export function activateEstablishment(establishmentId: string): Promise<void> {
  return changeEstablishmentStatus(establishmentId, "activate");
}

export function deactivateEstablishment(establishmentId: string): Promise<void> {
  return changeEstablishmentStatus(establishmentId, "deactivate");
}

export async function getSuperAdmins(
  establishmentId: string,
  filters: SuperAdminFilters = {},
): Promise<SuperAdmin[]> {
  const result = await apiClient.GET("/api/v1/establishments/{establishmentId}/super-admins", {
    params: { path: { establishmentId }, query: filters },
  });
  if (!result.response.ok || !result.data) {
    throw apiRequestError(result.response, result.error);
  }
  return z.array(superAdminSchema).parse(result.data);
}

export async function createSuperAdmin(
  establishmentId: string,
  request: CreateSuperAdminRequest,
): Promise<void> {
  const result = await apiClient.POST("/api/v1/establishments/{id}/super-admins", {
    params: { path: { id: establishmentId } },
    body: request,
  });
  if (!result.response.ok) {
    throw apiRequestError(result.response, result.error);
  }
}

export async function updateSuperAdmin(
  superAdminId: string,
  request: UpdateSuperAdminRequest,
): Promise<SuperAdmin> {
  const result = await apiClient.PUT("/api/v1/super-admins/{superAdminId}", {
    params: { path: { superAdminId } },
    body: request,
  });
  if (!result.response.ok || !result.data) {
    throw apiRequestError(result.response, result.error);
  }
  return superAdminSchema.parse(result.data);
}

export async function resetSuperAdminPassword(
  superAdminId: string,
  newPassword: string,
): Promise<void> {
  const result = await apiClient.POST("/api/v1/super-admins/{id}/password-reset", {
    params: { path: { id: superAdminId } },
    body: { newPassword },
  });
  if (!result.response.ok) {
    throw apiRequestError(result.response, result.error);
  }
}

export type SuperAdminLifecycleAction =
  | "lock"
  | "unlock"
  | "deactivate"
  | "activate"
  | "archive"
  | "restore";

export async function changeSuperAdminStatus(
  superAdminId: string,
  action: SuperAdminLifecycleAction,
): Promise<void> {
  const paths = {
    lock: "/api/v1/super-admins/{id}/lock",
    unlock: "/api/v1/super-admins/{id}/unlock",
    deactivate: "/api/v1/super-admins/{id}/deactivate",
    activate: "/api/v1/super-admins/{id}/activate",
    archive: "/api/v1/super-admins/{id}/archive",
    restore: "/api/v1/super-admins/{id}/restore",
  } as const;
  const result = await apiClient.POST(paths[action], {
    params: { path: { id: superAdminId } },
  });
  if (!result.response.ok) {
    throw apiRequestError(result.response, result.error);
  }
}
