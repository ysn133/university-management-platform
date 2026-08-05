import { z } from "zod";
import type { components } from "@/shared/api/generated/schema";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const namedResourceSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  name: z.string().min(1),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

const academicYearSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  label: z.string(),
  startYear: z.number().int(),
  endYear: z.number().int(),
  status: z.enum(["PLANNED", "ACTIVE", "CLOSED"]),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

const programFiliereSchema = z.object({
  id: z.string().uuid(),
  departmentId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  degreeCycleId: z.string().uuid(),
  programPathId: z.string().uuid(),
  code: z.string(),
  name: z.string(),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

export type NamedResource = z.infer<typeof namedResourceSchema>;
export type AcademicYear = z.infer<typeof academicYearSchema>;
export type ProgramFiliere = z.infer<typeof programFiliereSchema>;
export type AcademicYearStatus = AcademicYear["status"];

export type CreateAcademicYearRequest = components["schemas"]["CreateAcademicYearRequest"];
export type UpdateAcademicYearRequest = components["schemas"]["UpdateAcademicYearRequest"];
export type CreateProgramFiliereRequest = components["schemas"]["CreateProgramFiliereRequest"];
export type UpdateProgramFiliereRequest = components["schemas"]["UpdateProgramFiliereRequest"];

export const academicStructureKeys = {
  departments: (establishmentId: string) => ["academic-structure", "departments", establishmentId] as const,
  programPaths: (establishmentId: string) => ["academic-structure", "program-paths", establishmentId] as const,
  degreeCycles: (establishmentId: string) => ["academic-structure", "degree-cycles", establishmentId] as const,
  academicYears: (establishmentId: string) => ["academic-structure", "academic-years", establishmentId] as const,
  programFilieres: (departmentId: string) => ["academic-structure", "program-filieres", departmentId] as const,
};

async function parseResponse<T>(result: { response: Response; data?: unknown; error?: unknown }, schema: z.ZodType<T>): Promise<T> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return schema.parse(result.data);
}

async function ensureSuccess(result: { response: Response; error?: unknown }): Promise<void> {
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

export async function getDepartments(establishmentId: string): Promise<NamedResource[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/departments", { params: { path: { establishmentId } } }), z.array(namedResourceSchema));
}

export async function createDepartment(establishmentId: string, name: string): Promise<NamedResource> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/departments", { params: { path: { establishmentId } }, body: { name } }), namedResourceSchema);
}

export async function updateDepartment(id: string, name: string): Promise<NamedResource> {
  return parseResponse(await apiClient.PATCH("/api/v1/departments/{departmentId}", { params: { path: { departmentId: id } }, body: { name } }), namedResourceSchema);
}

export async function deleteDepartment(id: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/departments/{departmentId}", { params: { path: { departmentId: id } } }));
}

export async function getProgramPaths(establishmentId: string): Promise<NamedResource[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/program-paths", { params: { path: { establishmentId } } }), z.array(namedResourceSchema));
}

export async function createProgramPath(establishmentId: string, name: string): Promise<NamedResource> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/program-paths", { params: { path: { establishmentId } }, body: { name } }), namedResourceSchema);
}

export async function updateProgramPath(id: string, name: string): Promise<NamedResource> {
  return parseResponse(await apiClient.PATCH("/api/v1/program-paths/{programPathId}", { params: { path: { programPathId: id } }, body: { name } }), namedResourceSchema);
}

export async function deleteProgramPath(id: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/program-paths/{programPathId}", { params: { path: { programPathId: id } } }));
}

export async function getDegreeCycles(establishmentId: string): Promise<NamedResource[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/degree-cycles", { params: { path: { establishmentId } } }), z.array(namedResourceSchema));
}

export async function createDegreeCycle(establishmentId: string, name: string): Promise<NamedResource> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/degree-cycles", { params: { path: { establishmentId } }, body: { name } }), namedResourceSchema);
}

export async function updateDegreeCycle(id: string, name: string): Promise<NamedResource> {
  return parseResponse(await apiClient.PATCH("/api/v1/degree-cycles/{degreeCycleId}", { params: { path: { degreeCycleId: id } }, body: { name } }), namedResourceSchema);
}

export async function deleteDegreeCycle(id: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/degree-cycles/{degreeCycleId}", { params: { path: { degreeCycleId: id } } }));
}

export async function getAcademicYears(establishmentId: string): Promise<AcademicYear[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/academic-years", { params: { path: { establishmentId } } }), z.array(academicYearSchema));
}

export async function createAcademicYear(establishmentId: string, request: CreateAcademicYearRequest): Promise<AcademicYear> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/academic-years", { params: { path: { establishmentId } }, body: request }), academicYearSchema);
}

export async function updateAcademicYear(id: string, request: UpdateAcademicYearRequest): Promise<AcademicYear> {
  return parseResponse(await apiClient.PUT("/api/v1/academic-years/{academicYearId}", { params: { path: { academicYearId: id } }, body: request }), academicYearSchema);
}

export async function deleteAcademicYear(id: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/academic-years/{academicYearId}", { params: { path: { academicYearId: id } } }));
}

export async function getProgramFilieres(departmentId: string): Promise<ProgramFiliere[]> {
  return parseResponse(await apiClient.GET("/api/v1/departments/{departmentId}/program-filieres", { params: { path: { departmentId } } }), z.array(programFiliereSchema));
}

export async function createProgramFiliere(departmentId: string, request: CreateProgramFiliereRequest): Promise<ProgramFiliere> {
  return parseResponse(await apiClient.POST("/api/v1/departments/{departmentId}/program-filieres", { params: { path: { departmentId } }, body: request }), programFiliereSchema);
}

export async function updateProgramFiliere(id: string, request: UpdateProgramFiliereRequest): Promise<ProgramFiliere> {
  return parseResponse(await apiClient.PATCH("/api/v1/program-filieres/{programFiliereId}", { params: { path: { programFiliereId: id } }, body: request }), programFiliereSchema);
}

export async function deleteProgramFiliere(id: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/program-filieres/{programFiliereId}", { params: { path: { programFiliereId: id } } }));
}
