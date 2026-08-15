import { z } from "zod";
import type { components } from "@/shared/api/generated/schema";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { env } from "@/shared/config/env";

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
  terminalLevel: z.boolean(),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

const teachingGroupPolicySchema = z.object({
  id: z.string().uuid(),
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  groupType: z.enum(["TD", "TP"]),
  minimumGroupSize: z.number().int().positive(),
  maximumGroupSize: z.number().int().positive(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

const semesterSchema = z.object({
  id: z.string().uuid(),
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  name: z.string(),
  semesterOrder: z.number().int(),
  termType: z.enum(["AUTUMN", "SPRING"]),
  startDate: z.string(),
  endDate: z.string(),
  lifecycleStatus: z.enum(["PLANNED", "ACTIVE", "FINISHED"]),
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
  requiredRoomType: z.enum(["LECTURE_HALL", "CLASSROOM", "COMPUTER_LAB"]),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

export type AcademicMetric = "MODULE_FINAL_GRADE" | "MODULE_INSCRIPTION_NUMBER" | "SEMESTER_AVERAGE" | "INDIVIDUALLY_VALIDATED_MODULE_COUNT" | "NON_VALIDATED_MODULE_COUNT" | "MINIMUM_NON_VALIDATED_MODULE_GRADE" | "ANNUAL_AVERAGE" | "NON_VALIDATED_SEMESTER_COUNT" | "ACADEMIC_LEVEL_VALIDATED" | "OUTSTANDING_MODULE_COUNT" | "EXHAUSTED_MODULE_INSCRIPTION_COUNT";
export type ProfileVariable = "MODULE_VALIDATION_THRESHOLD" | "COMPENSATION_MINIMUM_THRESHOLD" | "SEMESTER_VALIDATION_AVERAGE" | "ANNUAL_VALIDATION_AVERAGE" | "MINIMUM_INDIVIDUALLY_VALIDATED_MODULES_PER_SEMESTER" | "MAXIMUM_NON_VALIDATED_MODULES_PER_SEMESTER" | "MINIMUM_INDIVIDUALLY_VALIDATED_MODULES_PER_ACADEMIC_LEVEL" | "MAXIMUM_MODULE_INSCRIPTIONS" | "MAXIMUM_CARRIED_MODULES";
export type ComparisonOperator = "GREATER_THAN" | "GREATER_THAN_OR_EQUAL" | "LESS_THAN" | "LESS_THAN_OR_EQUAL" | "EQUAL" | "NOT_EQUAL";
export type AcademicRuleOutcome = "MODULE_VALIDATED" | "MODULE_NON_VALIDATED" | "SEMESTER_VALIDATED" | "SEMESTER_VALIDATED_BY_COMPENSATION" | "SEMESTER_NON_VALIDATED" | "ACADEMIC_LEVEL_VALIDATED" | "ACADEMIC_LEVEL_VALIDATED_BY_COMPENSATION" | "ACADEMIC_LEVEL_NON_VALIDATED" | "PROMOTED" | "PROMOTED_WITH_DEBT" | "REPEAT" | "FAILED";
export type RuleExpression = { type: "COMPARISON"; left: AcademicMetric; operator: ComparisonOperator; rightProfileVariable?: ProfileVariable | null; literalValue?: number | null } | { type: "LOGICAL"; operator: "AND" | "OR"; children: RuleExpression[] };
export interface AcademicDecisionRule { name: string; priority: number; outcome: AcademicRuleOutcome; enabled: boolean; expression: RuleExpression; }
export interface AcademicRuleSet {
  moduleRules: AcademicDecisionRule[];
  semesterRules: AcademicDecisionRule[];
  academicLevelRules: AcademicDecisionRule[];
  progressionRules: AcademicDecisionRule[];
  useSharedSemesterRules: boolean;
  autumnSemesterRules: AcademicDecisionRule[];
  springSemesterRules: AcademicDecisionRule[];
}

const comparisonExpressionSchema: z.ZodType<RuleExpression> = z.lazy(() => z.union([
  z.object({ type: z.literal("COMPARISON"), left: z.string() as z.ZodType<AcademicMetric>, operator: z.string() as z.ZodType<ComparisonOperator>, rightProfileVariable: (z.string() as z.ZodType<ProfileVariable>).nullable().optional(), literalValue: z.number().nullable().optional() }),
  z.object({ type: z.literal("LOGICAL"), operator: z.enum(["AND", "OR"]), children: z.array(comparisonExpressionSchema) }),
]));
const academicDecisionRuleSchema = z.object({ name: z.string(), priority: z.number().int(), outcome: z.string() as z.ZodType<AcademicRuleOutcome>, enabled: z.boolean(), expression: comparisonExpressionSchema });
const academicRuleSetSchema = z.object({
  moduleRules: z.array(academicDecisionRuleSchema),
  semesterRules: z.array(academicDecisionRuleSchema),
  academicLevelRules: z.array(academicDecisionRuleSchema),
  progressionRules: z.array(academicDecisionRuleSchema),
  useSharedSemesterRules: z.boolean(),
  autumnSemesterRules: z.array(academicDecisionRuleSchema),
  springSemesterRules: z.array(academicDecisionRuleSchema),
});

const academicRuleProfileSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  name: z.string(),
  version: z.number().int(),
  moduleValidationThreshold: z.number(),
  compensationMinimumThreshold: z.number(),
  semesterValidationAverage: z.number(),
  annualValidationAverage: z.number().nullable().optional(),
  minimumIndividuallyValidatedModulesPerSemester: z.number().int(),
  maximumNonValidatedModulesPerSemester: z.number().int(),
  allowInterSemesterCompensation: z.boolean(),
  minimumIndividuallyValidatedModulesPerAcademicLevel: z.number().int(),
  maximumModuleInscriptions: z.number().int(),
  sessionGradePolicy: z.enum(["BEST_GRADE", "RATTRAPAGE_REPLACES_NORMAL", "RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD"]),
  allowProgressionWithDebt: z.boolean(),
  maximumCarriedModules: z.number().int(),
  maximumUnjustifiedAbsences: z.number().int(),
  absenceExclusionPolicy: z.enum(["NORMAL_ONLY", "NORMAL_AND_RATTRAPAGE"]),
  status: z.enum(["ACTIVE", "INACTIVE"]),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
  ruleDefinition: academicRuleSetSchema,
});

const academicLevelRuleAssignmentSchema = z.object({
  id: z.string().uuid(),
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  academicRuleProfileId: z.string().uuid(),
  status: z.enum(["ACTIVE", "INACTIVE"]),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});


const academicDomainSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  code: z.string(),
  name: z.string(),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

const academicRankSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  code: z.string(),
  name: z.string(),
  seniorityOrder: z.number().int().positive(),
  canHoldModuleResponsibility: z.boolean(),
  status: z.enum(["ACTIVE", "INACTIVE"]),
});

const teachingAssignmentRankPreferenceSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  componentType: z.enum(["COURSE", "TD", "TP"]),
  academicRankId: z.string().uuid(),
  academicRankCode: z.string(),
  academicRankName: z.string(),
  priority: z.number().int().positive(),
});

export type NamedResource = z.infer<typeof namedResourceSchema>;
export type AcademicYear = z.infer<typeof academicYearSchema>;
export type ProgramFiliere = z.infer<typeof programFiliereSchema>;
export type AcademicLevel = z.infer<typeof academicLevelSchema>;
export type TeachingGroupPolicy = z.infer<typeof teachingGroupPolicySchema>;
export type Semester = z.infer<typeof semesterSchema>;
export type SubjectModule = z.infer<typeof subjectModuleSchema>;
export type ModuleTeachingComponent = z.infer<typeof moduleTeachingComponentSchema>;
export type AcademicRuleProfile = z.infer<typeof academicRuleProfileSchema>;
export type AcademicLevelRuleAssignment = z.infer<typeof academicLevelRuleAssignmentSchema>;
export type AcademicDomain = z.infer<typeof academicDomainSchema>;
export type AcademicRank = z.infer<typeof academicRankSchema>;
export type TeachingAssignmentRankPreference = z.infer<typeof teachingAssignmentRankPreferenceSchema>;
export type TeachingComponentType = TeachingAssignmentRankPreference["componentType"];
export type AcademicYearStatus = AcademicYear["status"];

export type CreateAcademicYearRequest = components["schemas"]["CreateAcademicYearRequest"];
export type UpdateAcademicYearRequest = components["schemas"]["UpdateAcademicYearRequest"];
export type CreateProgramFiliereRequest = components["schemas"]["CreateProgramFiliereRequest"];
export type UpdateProgramFiliereRequest = components["schemas"]["UpdateProgramFiliereRequest"];
export type CreateAcademicLevelRequest = components["schemas"]["CreateAcademicLevelRequest"];
export type UpdateAcademicLevelRequest = components["schemas"]["UpdateAcademicLevelRequest"];
export interface CreateAcademicRuleProfileRequest {
  name: string;
  moduleValidationThreshold: number;
  compensationMinimumThreshold: number;
  semesterValidationAverage: number;
  annualValidationAverage?: number;
  minimumIndividuallyValidatedModulesPerSemester: number;
  maximumNonValidatedModulesPerSemester: number;
  allowInterSemesterCompensation: boolean;
  minimumIndividuallyValidatedModulesPerAcademicLevel: number;
  maximumModuleInscriptions: number;
  sessionGradePolicy: AcademicRuleProfile["sessionGradePolicy"];
  allowProgressionWithDebt: boolean;
  maximumCarriedModules: number;
  maximumUnjustifiedAbsences: number;
  absenceExclusionPolicy: AcademicRuleProfile["absenceExclusionPolicy"];
  status: AcademicRuleProfile["status"];
  ruleDefinition?: AcademicRuleSet;
}
export type UpdateAcademicRuleProfileRequest = CreateAcademicRuleProfileRequest;
export type CreateAcademicDomainRequest = components["schemas"]["CreateAcademicDomainRequest"];
export type UpdateAcademicDomainRequest = components["schemas"]["UpdateAcademicDomainRequest"];
export type CreateSemesterRequest = components["schemas"]["CreateSemesterRequest"];
export type UpdateSemesterRequest = components["schemas"]["UpdateSemesterRequest"];
export type CreateSubjectModuleRequest = components["schemas"]["CreateSubjectModuleRequest"];
export type UpdateSubjectModuleRequest = components["schemas"]["UpdateSubjectModuleRequest"];
export type ReplaceModuleTeachingComponentsRequest = components["schemas"]["ReplaceModuleTeachingComponentsRequest"];
export type AcademicRankRequest = components["schemas"]["AcademicRankRequest"];
export type ReplaceRankPreferencesRequest = components["schemas"]["ReplaceRankPreferencesRequest"];

export const academicStructureKeys = {
  departments: (establishmentId: string) => ["academic-structure", "departments", establishmentId] as const,
  programPaths: (establishmentId: string) => ["academic-structure", "program-paths", establishmentId] as const,
  degreeCycles: (establishmentId: string) => ["academic-structure", "degree-cycles", establishmentId] as const,
  academicYears: (establishmentId: string) => ["academic-structure", "academic-years", establishmentId] as const,
  programFilieres: (departmentId: string) => ["academic-structure", "program-filieres", departmentId] as const,
  programFiliere: (programFiliereId: string) => ["academic-structure", "program-filiere", programFiliereId] as const,
  academicLevels: (programFiliereId: string) => ["academic-structure", "academic-levels", programFiliereId] as const,
  academicLevel: (academicLevelId: string) => ["academic-structure", "academic-level", academicLevelId] as const,
  teachingGroupPolicies: (academicLevelId: string, academicYearId: string) => ["academic-structure", "teaching-group-policies", academicLevelId, academicYearId] as const,
  semesters: (academicLevelId: string, academicYearId: string) => ["academic-structure", "semesters", academicLevelId, academicYearId] as const,
  semester: (semesterId: string) => ["academic-structure", "semester", semesterId] as const,
  subjectModules: (semesterId: string) => ["academic-structure", "subject-modules", semesterId] as const,
  subjectModule: (subjectModuleId: string) => ["academic-structure", "subject-module", subjectModuleId] as const,
  moduleTeachingComponents: (subjectModuleId: string) => ["academic-structure", "module-teaching-components", subjectModuleId] as const,
  ruleProfiles: (establishmentId: string) => ["academic-structure", "rule-profiles", establishmentId] as const,
  levelRuleAssignments: (academicLevelId: string) => ["academic-structure", "level-rule-assignments", academicLevelId] as const,
  academicDomains: (establishmentId: string) => ["academic-structure", "academic-domains", establishmentId] as const,
  academicRanks: (establishmentId: string) => ["academic-structure", "academic-ranks", establishmentId] as const,
  teachingAssignmentRankPreferences: (establishmentId: string) => ["teaching-planning", "rank-preferences", establishmentId] as const,
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

export async function getAcademicLevel(id: string): Promise<AcademicLevel> {
  return parseResponse(await apiClient.GET("/api/v1/academic-levels/{academicLevelId}", { params: { path: { academicLevelId: id } } }), academicLevelSchema);
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

export async function getTeachingGroupPolicies(academicLevelId: string, academicYearId: string): Promise<TeachingGroupPolicy[]> {
  return parseResponse(await apiClient.GET("/api/v1/academic-levels/{academicLevelId}/teaching-group-policies", {
    params: { path: { academicLevelId }, query: { academicYearId } },
  }), z.array(teachingGroupPolicySchema));
}

export async function replaceTeachingGroupPolicies(
  academicLevelId: string,
  academicYearId: string,
  policies: Array<{ groupType: "TD" | "TP"; minimumGroupSize: number; maximumGroupSize: number }>,
): Promise<TeachingGroupPolicy[]> {
  return parseResponse(await apiClient.PUT("/api/v1/academic-levels/{academicLevelId}/teaching-group-policies", {
    params: { path: { academicLevelId }, query: { academicYearId } },
    body: { policies },
  }), z.array(teachingGroupPolicySchema));
}

export async function getSemesters(academicLevelId: string, academicYearId: string): Promise<Semester[]> {
  return parseResponse(await apiClient.GET("/api/v1/academic-levels/{academicLevelId}/semesters", { params: { path: { academicLevelId }, query: { academicYearId } } }), z.array(semesterSchema));
}

export async function getSemester(id: string): Promise<Semester> {
  return parseResponse(await apiClient.GET("/api/v1/semesters/{semesterId}", { params: { path: { semesterId: id } } }), semesterSchema);
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

export async function getAcademicLevelRuleAssignments(academicLevelId: string): Promise<AcademicLevelRuleAssignment[]> {
  return parseResponse(await apiClient.GET("/api/v1/academic-levels/{academicLevelId}/rule-assignments", { params: { path: { academicLevelId } } }), z.array(academicLevelRuleAssignmentSchema));
}

export async function updateAcademicLevelRuleAssignment(assignmentId: string, academicRuleProfileId: string): Promise<AcademicLevelRuleAssignment> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/academic-level-rule-assignments/${assignmentId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ academicRuleProfileId }),
  });
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) throw apiRequestError(response, body);
  return academicLevelRuleAssignmentSchema.parse(body);
}

export async function createAcademicRuleProfile(establishmentId: string, request: CreateAcademicRuleProfileRequest): Promise<AcademicRuleProfile> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/academic-rule-profiles", { params: { path: { establishmentId } }, body: request as unknown as components["schemas"]["CreateAcademicRuleProfileRequest"] }), academicRuleProfileSchema);
}

export async function getAcademicRuleProfile(academicRuleProfileId: string): Promise<AcademicRuleProfile> {
  return parseResponse(await apiClient.GET("/api/v1/academic-rule-profiles/{academicRuleProfileId}", { params: { path: { academicRuleProfileId } } }), academicRuleProfileSchema);
}

export async function updateAcademicRuleProfile(academicRuleProfileId: string, request: UpdateAcademicRuleProfileRequest): Promise<AcademicRuleProfile> {
  return parseResponse(await apiClient.PUT("/api/v1/academic-rule-profiles/{academicRuleProfileId}", { params: { path: { academicRuleProfileId } }, body: request as unknown as components["schemas"]["UpdateAcademicRuleProfileRequest"] }), academicRuleProfileSchema);
}

export async function getAcademicRanks(establishmentId: string): Promise<AcademicRank[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/academic-ranks", { params: { path: { establishmentId } } }), z.array(academicRankSchema));
}

export async function createAcademicRank(establishmentId: string, request: AcademicRankRequest): Promise<AcademicRank> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/academic-ranks", { params: { path: { establishmentId } }, body: request }), academicRankSchema);
}

export async function updateAcademicRank(rankId: string, request: AcademicRankRequest): Promise<AcademicRank> {
  return parseResponse(await apiClient.PUT("/api/v1/academic-ranks/{rankId}", { params: { path: { rankId } }, body: request }), academicRankSchema);
}

export async function deleteAcademicRank(rankId: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/academic-ranks/{rankId}", { params: { path: { rankId } } }));
}

export async function getTeachingAssignmentRankPreferences(establishmentId: string): Promise<TeachingAssignmentRankPreference[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/teaching-assignment-rank-preferences", { params: { path: { establishmentId } } }), z.array(teachingAssignmentRankPreferenceSchema));
}

export async function replaceTeachingAssignmentRankPreferences(
  establishmentId: string,
  componentType: TeachingComponentType,
  request: ReplaceRankPreferencesRequest,
): Promise<TeachingAssignmentRankPreference[]> {
  return parseResponse(await apiClient.PUT("/api/v1/establishments/{establishmentId}/teaching-assignment-rank-preferences/{componentType}", { params: { path: { establishmentId, componentType } }, body: request }), z.array(teachingAssignmentRankPreferenceSchema));
}

export async function getAcademicDomains(establishmentId: string): Promise<AcademicDomain[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/academic-domains", { params: { path: { establishmentId } } }), z.array(academicDomainSchema));
}

export async function createAcademicDomain(establishmentId: string, request: CreateAcademicDomainRequest): Promise<AcademicDomain> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/academic-domains", { params: { path: { establishmentId } }, body: request }), academicDomainSchema);
}

export async function updateAcademicDomain(academicDomainId: string, request: UpdateAcademicDomainRequest): Promise<AcademicDomain> {
  return parseResponse(await apiClient.PUT("/api/v1/academic-domains/{academicDomainId}", { params: { path: { academicDomainId } }, body: request }), academicDomainSchema);
}

export async function deleteAcademicDomain(academicDomainId: string): Promise<void> {
  await ensureSuccess(await apiClient.DELETE("/api/v1/academic-domains/{academicDomainId}", { params: { path: { academicDomainId } } }));
}
