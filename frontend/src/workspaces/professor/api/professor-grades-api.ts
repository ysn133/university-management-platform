import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const gradeItemSchema = z.object({
  gradeRecordId: z.string().uuid().nullable(),
  moduleRegistrationId: z.string().uuid(),
  studentId: z.string().uuid(),
  apogeeCode: z.string(),
  universityEmail: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  inscriptionNumber: z.number().int(),
  gradeValue: z.number().nullable(),
  zeroGradeReason: z.enum(["ABSENT", "EARNED_ZERO"]).nullable(),
  workflowStatus: z.enum(["DRAFT", "SUBMITTED", "REVIEWED", "APPROVED", "PUBLISHED"]),
  publishedAt: z.string().nullable(),
});

const gradeSheetSchema = z.object({
  moduleExamId: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  classGroupId: z.string().uuid(),
  workflowStatus: z.enum(["DRAFT", "SUBMITTED", "REVIEWED", "APPROVED", "PUBLISHED"]),
  grades: z.array(gradeItemSchema),
});

export type GradeSheet = z.infer<typeof gradeSheetSchema>;
export type GradeDraftItem = { moduleRegistrationId: string; gradeValue: number; zeroGradeReason?: "ABSENT" | "EARNED_ZERO" };

export const professorGradeKeys = {
  sheet: (moduleExamId: string) => ["professor-grades", "sheet", moduleExamId] as const,
};

export async function getGradeSheet(moduleExamId: string): Promise<GradeSheet> {
  const result = await apiClient.GET("/api/v1/module-exams/{moduleExamId}/grade-sheet", { params: { path: { moduleExamId } } });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return gradeSheetSchema.parse(result.data);
}

export async function saveGradeSheet(moduleExamId: string, grades: GradeDraftItem[]): Promise<GradeSheet> {
  const result = await apiClient.PUT("/api/v1/module-exams/{moduleExamId}/grade-sheet", { params: { path: { moduleExamId } }, body: { grades } });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return gradeSheetSchema.parse(result.data);
}

export async function submitGradeSheet(moduleExamId: string): Promise<GradeSheet> {
  const result = await apiClient.POST("/api/v1/module-exams/{moduleExamId}/grade-sheet/submit", { params: { path: { moduleExamId } } });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return gradeSheetSchema.parse(result.data);
}
