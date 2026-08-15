import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";

const academicYearModuleResultSchema = z.object({
  subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(),
  finalGrade: z.number(),
  resultStatus: z.enum(["V", "AV", "NV"]),
  inscriptionNumber: z.number().int(),
});

const academicYearSemesterResultSchema = z.object({
  semesterId: z.string().uuid(),
  semesterName: z.string(),
  semesterOrder: z.number().int(),
  semesterAverage: z.number(),
  resultStatus: z.enum(["VALIDATED", "NON_VALIDATED"]),
  moduleResults: z.array(academicYearModuleResultSchema),
});

const progressionDecisionSchema = z.object({
  id: z.string().uuid(),
  academicRegistrationId: z.string().uuid(),
  studentId: z.string().uuid(),
  firstName: z.string(),
  lastName: z.string(),
  apogeeCode: z.string(),
  nationalStudentCode: z.string().nullable(),
  cin: z.string().nullable(),
  programName: z.string(),
  programPathName: z.string(),
  academicLevelName: z.string(),
  academicYearLabel: z.string(),
  semesterResults: z.array(academicYearSemesterResultSchema),
  decisionStatus: z.enum(["PROMOTED", "PROMOTED_BY_COMPENSATION", "PROMOTED_WITH_DEBT", "LEVEL_VALIDATED", "REPEAT", "FAILED"]),
  annualAverage: z.number(),
  outstandingModuleCount: z.number().int(),
  decidedAt: z.string(),
});

export type ProgressionDecision = z.infer<typeof progressionDecisionSchema>;

export async function getProgressionDecisions(academicLevelId: string, academicYearId: string) {
  const result = await apiClient.GET("/api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/progression-decisions", {
    params: { path: { academicLevelId, academicYearId } },
  });
  return z.array(progressionDecisionSchema).parse(result.data);
}

export async function generateProgressionDecisions(academicLevelId: string, academicYearId: string) {
  const result = await apiClient.POST("/api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/progression-decisions/generate", {
    params: { path: { academicLevelId, academicYearId } },
  });
  return z.array(progressionDecisionSchema).parse(result.data);
}
