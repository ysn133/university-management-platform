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

const academicLevelSchema = z.object({
  id: z.string().uuid(),
  programFiliereId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  name: z.string(),
  levelOrder: z.number().int(),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

const semesterSchema = z.object({
  id: z.string().uuid(),
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  name: z.string(),
  semesterOrder: z.number().int(),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

const subjectModuleSchema = z.object({
  id: z.string().uuid(),
  semesterId: z.string().uuid(),
  code: z.string(),
  title: z.string(),
  academicDomainIds: z.array(z.string().uuid()),
});

const moduleTeachingComponentSchema = z.object({
  id: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  componentType: z.enum(["COURSE", "TD", "TP"]),
  sessionsPerWeek: z.number().int().positive(),
  sessionDurationMinutes: z.number().int().positive(),
  audienceMode: z.enum(["WHOLE_COHORT", "CLASS_GROUP", "SUBGROUP"]),
  maximumGroupSize: z.number().int().positive().nullable().optional(),
  requiredRoomType: z.enum(["LECTURE_HALL", "CLASSROOM", "COMPUTER_LAB"]),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

const academicRuleProfileSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  name: z.string(),
  version: z.number().int(),
  status: z.enum(["ACTIVE", "INACTIVE"]),
}).passthrough();

const academicDomainSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  code: z.string(),
  name: z.string(),
});

export type NamedResource = z.infer<typeof namedResourceSchema>;
export type AcademicYear = z.infer<typeof academicYearSchema>;
export type ProgramFiliere = z.infer<typeof programFiliereSchema>;
export type AcademicLevel = z.infer<typeof academicLevelSchema>;
export type Semester = z.infer<typeof semesterSchema>;
export type SubjectModule = z.infer<typeof subjectModuleSchema>;
export type ModuleTeachingComponent = z.infer<typeof moduleTeachingComponentSchema>;
export type AcademicRuleProfile = z.infer<typeof academicRuleProfileSchema>;
export type AcademicDomain = z.infer<typeof academicDomainSchema>;
export type AcademicYearStatus = AcademicYear["status"];

export type CreateAcademicYearRequest = components["schemas"]["CreateAcademicYearRequest"];
export type UpdateAcademicYearRequest = components["schemas"]["UpdateAcademicYearRequest"];
export type CreateProgramFiliereRequest = components["schemas"]["CreateProgramFiliereRequest"];
export type UpdateProgramFiliereRequest = components["schemas"]["UpdateProgramFiliereRequest"];
export type CreateAcademicLevelRequest = components["schemas"]["CreateAcademicLevelRequest"];
export type UpdateAcademicLevelRequest = components["schemas"]["UpdateAcademicLevelRequest"];
export type CreateAcademicRuleProfileRequest = components["schemas"]["CreateAcademicRuleProfileRequest"];
export type CreateAcademicDomainRequest = components["schemas"]["CreateAcademicDomainRequest"];
export type CreateSemesterRequest = components["schemas"]["CreateSemesterRequest"];
export type UpdateSemesterRequest = components["schemas"]["UpdateSemesterRequest"];
export type CreateSubjectModuleRequest = components["schemas"]["CreateSubjectModuleRequest"];
export type UpdateSubjectModuleRequest = components["schemas"]["UpdateSubjectModuleRequest"];
export type ReplaceModuleTeachingComponentsRequest = components["schemas"]["ReplaceModuleTeachingComponentsRequest"];

export const academicStructureKeys = {
  departments: (establishmentId: string) => ["academic-structure", "departments", establishmentId] as const,
  programPaths: (establishmentId: string) => ["academic-structure", "program-paths", establishmentId] as const,
  degreeCycles: (establishmentId: string) => ["academic-structure", "degree-cycles", establishmentId] as const,
  academicYears: (establishmentId: string) => ["academic-structure", "academic-years", establishmentId] as const,
  programFilieres: (departmentId: string) => ["academic-structure", "program-filieres", departmentId] as const,
  programFiliere: (programFiliereId: string) => ["academic-structure", "program-filiere", programFiliereId] as const,
  academicLevels: (programFiliereId: string) => ["academic-structure", "academic-levels", programFiliereId] as const,
  semesters: (academicLevelId: string, academicYearId: string) => ["academic-structure", "semesters", academicLevelId, academicYearId] as const,
  subjectModules: (semesterId: string) => ["academic-structure", "subject-modules", semesterId] as const,
  subjectModule: (subjectModuleId: string) => ["academic-structure", "subject-module", subjectModuleId] as const,
  moduleTeachingComponents: (subjectModuleId: string) => ["academic-structure", "module-teaching-components", subjectModuleId] as const,
  ruleProfiles: (establishmentId: string) => ["academic-structure", "rule-profiles", establishmentId] as const,
  academicDomains: (establishmentId: string) => ["academic-structure", "academic-domains", establishmentId] as const,
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

export async function getProgramFiliere(id: string): Promise<ProgramFiliere> {
  return parseResponse(await apiClient.GET("/api/v1/program-filieres/{programFiliereId}", { params: { path: { programFiliereId: id } } }), programFiliereSchema);
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

export async function getAcademicLevels(programFiliereId: string): Promise<AcademicLevel[]> {
  return parseResponse(await apiClient.GET("/api/v1/program-filieres/{programFiliereId}/academic-levels", { params: { path: { programFiliereId } } }), z.array(academicLevelSchema));
}

export async function createAcademicLevel(programFiliereId: string, request: CreateAcademicLevelRequest): Promise<AcademicLevel> {
  return parseResponse(await apiClient.POST("/api/v1/program-filieres/{programFiliereId}/academic-levels", { params: { path: { programFiliereId } }, body: request }), academicLevelSchema);
}

export async function updateAcademicLevel(id: string, request: UpdateAcademicLevelRequest): Promise<AcademicLevel> {
  return parseResponse(await apiClient.PUT("/api/v1/academic-levels/{academicLevelId}", { params: { path: { academicLevelId: id } }, body: request }), academicLevelSchema);
}

export async function deleteAcademicLevel(id: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/academic-levels/{academicLevelId}", { params: { path: { academicLevelId: id } } }));
}

export async function getSemesters(academicLevelId: string, academicYearId: string): Promise<Semester[]> {
  return parseResponse(await apiClient.GET("/api/v1/academic-levels/{academicLevelId}/semesters", { params: { path: { academicLevelId }, query: { academicYearId } } }), z.array(semesterSchema));
}

export async function createSemester(academicLevelId: string, academicYearId: string, request: CreateSemesterRequest): Promise<Semester> {
  return parseResponse(await apiClient.POST("/api/v1/academic-levels/{academicLevelId}/semesters", { params: { path: { academicLevelId }, query: { academicYearId } }, body: request }), semesterSchema);
}

export async function updateSemester(id: string, request: UpdateSemesterRequest): Promise<Semester> {
  return parseResponse(await apiClient.PUT("/api/v1/semesters/{semesterId}", { params: { path: { semesterId: id } }, body: request }), semesterSchema);
}

export async function deleteSemester(id: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/semesters/{semesterId}", { params: { path: { semesterId: id } } }));
}

export async function getSubjectModules(semesterId: string): Promise<SubjectModule[]> {
  return parseResponse(await apiClient.GET("/api/v1/semesters/{semesterId}/subject-modules", { params: { path: { semesterId } } }), z.array(subjectModuleSchema));
}

export async function getSubjectModule(id: string): Promise<SubjectModule> {
  return parseResponse(await apiClient.GET("/api/v1/subject-modules/{subjectModuleId}", { params: { path: { subjectModuleId: id } } }), subjectModuleSchema);
}

export async function createSubjectModule(semesterId: string, request: CreateSubjectModuleRequest): Promise<SubjectModule> {
  return parseResponse(await apiClient.POST("/api/v1/semesters/{semesterId}/subject-modules", { params: { path: { semesterId } }, body: request }), subjectModuleSchema);
}

export async function updateSubjectModule(id: string, request: UpdateSubjectModuleRequest): Promise<SubjectModule> {
  return parseResponse(await apiClient.PUT("/api/v1/subject-modules/{subjectModuleId}", { params: { path: { subjectModuleId: id } }, body: request }), subjectModuleSchema);
}

export async function deleteSubjectModule(id: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/subject-modules/{subjectModuleId}", { params: { path: { subjectModuleId: id } } }));
}

export async function getModuleTeachingComponents(subjectModuleId: string): Promise<ModuleTeachingComponent[]> {
  return parseResponse(await apiClient.GET("/api/v1/subject-modules/{subjectModuleId}/teaching-components", { params: { path: { subjectModuleId } } }), z.array(moduleTeachingComponentSchema));
}

export async function replaceModuleTeachingComponents(
  subjectModuleId: string,
  request: ReplaceModuleTeachingComponentsRequest,
): Promise<ModuleTeachingComponent[]> {
  return parseResponse(await apiClient.PUT("/api/v1/subject-modules/{subjectModuleId}/teaching-components", { params: { path: { subjectModuleId } }, body: request }), z.array(moduleTeachingComponentSchema));
}

export async function getAcademicRuleProfiles(establishmentId: string): Promise<AcademicRuleProfile[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/academic-rule-profiles", { params: { path: { establishmentId } } }), z.array(academicRuleProfileSchema));
}

export async function createAcademicRuleProfile(establishmentId: string, request: CreateAcademicRuleProfileRequest): Promise<AcademicRuleProfile> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/academic-rule-profiles", { params: { path: { establishmentId } }, body: request }), academicRuleProfileSchema);
}

export async function getAcademicDomains(establishmentId: string): Promise<AcademicDomain[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/academic-domains", { params: { path: { establishmentId } } }), z.array(academicDomainSchema));
}

export async function createAcademicDomain(establishmentId: string, request: CreateAcademicDomainRequest): Promise<AcademicDomain> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/academic-domains", { params: { path: { establishmentId } }, body: request }), academicDomainSchema);
}
