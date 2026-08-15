import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { env } from "@/shared/config/env";

const finalResultSchema = z.object({
  moduleRegistrationId: z.string().uuid(),
  studentId: z.string().uuid(),
  firstName: z.string(),
  lastName: z.string(),
  apogeeCode: z.string(),
  subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(),
  inscriptionNumber: z.number().int(),
  finalGrade: z.number().nullable(),
  resultStatus: z.enum(["V", "AV", "NV"]).nullable(),
});

export type FinalResult = z.infer<typeof finalResultSchema>;

export async function getFinalResults(semesterId: string, classGroupId: string, subjectModuleId?: string) {
  const result = await apiClient.GET("/api/v1/semesters/{semesterId}/class-groups/{classGroupId}/final-results", {
    params: { path: { semesterId, classGroupId }, query: { subjectModuleId } },
  });
  return z.array(finalResultSchema).parse(result.data);
}

export async function generateFinalResults(semesterId: string, classGroupId: string) {
  const result = await apiClient.POST("/api/v1/semesters/{semesterId}/class-groups/{classGroupId}/final-results/generate", {
    params: { path: { semesterId, classGroupId } },
  });
  return z.array(finalResultSchema).parse(result.data);
}

export async function clearFinalResults(semesterId: string, classGroupId: string): Promise<void> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/semesters/${semesterId}/class-groups/${classGroupId}/final-results`, { method: "DELETE" });
  if (!response.ok) {
    const body: unknown = await response.json().catch(() => null);
    throw apiRequestError(response, body);
  }
}
