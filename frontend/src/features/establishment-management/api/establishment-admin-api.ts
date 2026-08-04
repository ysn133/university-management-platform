import { z } from "zod";
import type { components } from "@/shared/api/generated/schema";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const adminSchema = z.object({
  id: z.string().uuid(),
  accountId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  email: z.string().email(),
  role: z.literal("ADMIN"),
  status: z.enum(["ACTIVE", "LOCKED", "DEACTIVATED", "ARCHIVED"]),
  firstName: z.string(),
  lastName: z.string(),
  birthDate: z.string(),
  cin: z.string().optional().nullable(),
  sex: z.enum(["MALE", "FEMALE"]),
  phoneNumber: z.string().optional().nullable(),
});

const permissionSchema = z.object({
  code: z.string().min(1),
  name: z.string().min(1),
});

const grantsSchema = z.object({
  adminId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  permissions: z.array(z.string()),
});

const createAdminResponseSchema = z.object({
  adminId: z.string().uuid(),
  userAccountId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  roleType: z.literal("ADMIN"),
});

export type AdminAccount = z.infer<typeof adminSchema>;
export type AccountStatus = AdminAccount["status"];
export type Permission = z.infer<typeof permissionSchema>;
export type PermissionCode = components["schemas"]["ReplaceAdminPermissionGrantsRequest"]["permissions"][number];
export type CreateAdminRequest = components["schemas"]["CreateAdminRequest"];
export type UpdateAdminRequest = components["schemas"]["UpdateAdminRequest"];
export type CreateAdminResponse = z.infer<typeof createAdminResponseSchema>;

export interface AdminFilters {
  query?: string;
  status?: AccountStatus;
  createdFrom?: string;
  createdTo?: string;
}

export const establishmentAdminKeys = {
  admins: (establishmentId: string, filters: AdminFilters = {}) =>
    ["establishment-management", "admins", establishmentId, filters] as const,
  admin: (adminId: string) => ["establishment-management", "admin", adminId] as const,
  permissions: ["establishment-management", "permissions"] as const,
  grants: (adminId: string) => ["establishment-management", "admin-grants", adminId] as const,
};

export async function getAdmin(adminId: string): Promise<AdminAccount> {
  const result = await apiClient.GET("/api/v1/admins/{id}", {
    params: { path: { id: adminId } },
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return adminSchema.parse(result.data);
}

export async function getAdmins(establishmentId: string, filters: AdminFilters = {}): Promise<AdminAccount[]> {
  const result = await apiClient.GET("/api/v1/establishments/{id}/admins", {
    params: { path: { id: establishmentId }, query: filters },
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(adminSchema).parse(result.data);
}

export async function createAdmin(establishmentId: string, request: CreateAdminRequest): Promise<CreateAdminResponse> {
  const result = await apiClient.POST("/api/v1/establishments/{id}/admins", {
    params: { path: { id: establishmentId } },
    body: request,
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return createAdminResponseSchema.parse(result.data);
}

export async function updateAdmin(adminId: string, request: UpdateAdminRequest): Promise<AdminAccount> {
  const result = await apiClient.PUT("/api/v1/admins/{id}", {
    params: { path: { id: adminId } },
    body: request,
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return adminSchema.parse(result.data);
}

export async function resetAdminPassword(adminId: string, newPassword: string): Promise<void> {
  const result = await apiClient.POST("/api/v1/admins/{id}/password-reset", {
    params: { path: { id: adminId } },
    body: { newPassword },
  });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

export type AdminLifecycleAction = "lock" | "unlock" | "deactivate" | "activate" | "archive" | "restore";

export async function changeAdminStatus(adminId: string, action: AdminLifecycleAction): Promise<void> {
  const paths = {
    lock: "/api/v1/admins/{id}/lock",
    unlock: "/api/v1/admins/{id}/unlock",
    deactivate: "/api/v1/admins/{id}/deactivate",
    activate: "/api/v1/admins/{id}/activate",
    archive: "/api/v1/admins/{id}/archive",
    restore: "/api/v1/admins/{id}/restore",
  } as const;
  const result = await apiClient.POST(paths[action], { params: { path: { id: adminId } } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

export async function getPermissionCatalog(): Promise<Permission[]> {
  const result = await apiClient.GET("/api/v1/permissions");
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(permissionSchema).parse(result.data);
}

export async function getAdminGrants(adminId: string): Promise<PermissionCode[]> {
  const result = await apiClient.GET("/api/v1/admins/{id}/permission-grants", {
    params: { path: { id: adminId } },
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return grantsSchema.parse(result.data).permissions as PermissionCode[];
}

export async function replaceAdminGrants(adminId: string, permissions: PermissionCode[]): Promise<PermissionCode[]> {
  const result = await apiClient.PUT("/api/v1/admins/{id}/permission-grants", {
    params: { path: { id: adminId } },
    body: { permissions },
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return grantsSchema.parse(result.data).permissions as PermissionCode[];
}
