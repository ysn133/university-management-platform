import { z } from "zod";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { env } from "@/shared/config/env";

const graduationDecisionSchema = z.object({
  id: z.string().uuid(),
  studentId: z.string().uuid(),
  firstName: z.string(),
  lastName: z.string(),
  apogeeCode: z.string(),
  nationalStudentCode: z.string().nullable(),
  cin: z.string().nullable(),
  programName: z.string(),
  programPathName: z.string(),
  degreeCycleName: z.string(),
  terminalAcademicLevelName: z.string(),
  academicYearLabel: z.string(),
  decisionStatus: z.literal("GRADUATED"),
  graduationAverage: z.number(),
  decidedAt: z.string(),
});

export type GraduationDecision = z.infer<typeof graduationDecisionSchema>;

async function request(academicLevelId: string, academicYearId: string, method: "GET" | "POST") {
  const response = await authenticatedFetch(
    `${env.apiBaseUrl}/api/v1/academic-levels/${academicLevelId}/academic-years/${academicYearId}/graduation-decisions${method === "POST" ? "/generate" : ""}`,
    { method },
  );
  const body = await response.json().catch(() => null);
  if (!response.ok) throw apiRequestError(response, body);
  return z.array(graduationDecisionSchema).parse(body);
}

export function getGraduationDecisions(academicLevelId: string, academicYearId: string) {
  return request(academicLevelId, academicYearId, "GET");
}

export function generateGraduationDecisions(academicLevelId: string, academicYearId: string) {
  return request(academicLevelId, academicYearId, "POST");
}
