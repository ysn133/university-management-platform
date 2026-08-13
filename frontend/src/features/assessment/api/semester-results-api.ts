import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";

const semesterResultSchema = z.object({
  id: z.string().uuid(),
  semesterRegistrationId: z.string().uuid(),
  studentId: z.string().uuid(),
  firstName: z.string(),
  lastName: z.string(),
  apogeeCode: z.string(),
  semesterAverage: z.number(),
  resultStatus: z.enum(["VALIDATED", "NON_VALIDATED"]),
  validatedModuleCount: z.number().int(),
  compensatedModuleCount: z.number().int(),
  nonValidatedModuleCount: z.number().int(),
  evaluatedAt: z.string(),
  secondInscriptionOnly: z.boolean(),
  originalAcademicYearId: z.string().uuid().nullable(),
  originalAcademicLevelId: z.string().uuid().nullable(),
  originalSemesterId: z.string().uuid().nullable(),
  originalClassGroupId: z.string().uuid().nullable(),
  originalAcademicYearLabel: z.string().nullable(),
  originalAcademicLevelName: z.string().nullable(),
  originalSemesterName: z.string().nullable(),
});

export type SemesterResult = z.infer<typeof semesterResultSchema>;

export async function getSemesterResults(semesterId: string, classGroupId: string) {
  const result = await apiClient.GET("/api/v1/semesters/{semesterId}/class-groups/{classGroupId}/semester-results", { params: { path: { semesterId, classGroupId } } });
  return z.array(semesterResultSchema).parse(result.data);
}

export async function generateSemesterResults(semesterId: string, classGroupId: string) {
  const result = await apiClient.POST("/api/v1/semesters/{semesterId}/class-groups/{classGroupId}/semester-results/generate", { params: { path: { semesterId, classGroupId } } });
  return z.array(semesterResultSchema).parse(result.data);
}
