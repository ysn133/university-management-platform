import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const gradeItemSchema = z.object({
  gradeRecordId: z.string().uuid().nullable(), moduleRegistrationId: z.string().uuid(), studentId: z.string().uuid(),
  apogeeCode: z.string(), universityEmail: z.string(), firstName: z.string(), lastName: z.string(), inscriptionNumber: z.number().int(),
  gradeValue: z.number().nullable(), zeroGradeReason: z.enum(["ABSENT", "EARNED_ZERO"]).nullable(),
  workflowStatus: z.enum(["DRAFT", "SUBMITTED", "REVIEWED", "APPROVED", "PUBLISHED"]), publishedAt: z.string().nullable(),
});
const gradeSheetSchema = z.object({
  moduleExamId: z.string().uuid(), subjectModuleId: z.string().uuid(), classGroupId: z.string().uuid(),
  workflowStatus: z.enum(["DRAFT", "SUBMITTED", "REVIEWED", "APPROVED", "PUBLISHED"]), grades: z.array(gradeItemSchema),
});

export type ManagedGradeSheet = z.infer<typeof gradeSheetSchema>;
export type GradeWorkflowStatus = ManagedGradeSheet["workflowStatus"];

async function parse(result: { response: Response; data?: unknown; error?: unknown }) {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return gradeSheetSchema.parse(result.data);
}

export function getManagedGradeSheet(moduleExamId: string) {
  return apiClient.GET("/api/v1/module-exams/{moduleExamId}/grade-sheet", { params: { path: { moduleExamId } } }).then(parse);
}

export function reviewGradeSheet(moduleExamId: string) {
  return apiClient.POST("/api/v1/module-exams/{moduleExamId}/grade-sheet/review", { params: { path: { moduleExamId } } }).then(parse);
}

export function approveGradeSheet(moduleExamId: string) {
  return apiClient.POST("/api/v1/module-exams/{moduleExamId}/grade-sheet/approve", { params: { path: { moduleExamId } } }).then(parse);
}

export function publishGradeSheet(moduleExamId: string) {
  return apiClient.POST("/api/v1/module-exams/{moduleExamId}/grade-sheet/publish", { params: { path: { moduleExamId } } }).then(parse);
}
