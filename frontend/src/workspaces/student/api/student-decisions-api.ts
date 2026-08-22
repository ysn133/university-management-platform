import { z } from "zod";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { env } from "@/shared/config/env";

const moduleResultSchema = z.object({
  subjectModuleId: z.string().uuid(), subjectModuleCode: z.string(), subjectModuleTitle: z.string(),
  finalGrade: z.number(), resultStatus: z.enum(["V", "AV", "NV"]), inscriptionNumber: z.number().int(),
});
const semesterResultSchema = z.object({
  semesterId: z.string().uuid(), semesterName: z.string(), semesterOrder: z.number().int(),
  semesterAverage: z.number(), resultStatus: z.enum(["VALIDATED", "NON_VALIDATED"]), moduleResults: z.array(moduleResultSchema),
});
const progressionDecisionSchema = z.object({
  id: z.string().uuid(), academicRegistrationId: z.string().uuid(), studentId: z.string().uuid(),
  firstName: z.string(), lastName: z.string(), apogeeCode: z.string(), nationalStudentCode: z.string().nullable(),
  cin: z.string().nullable(), programName: z.string(), programPathName: z.string(), academicLevelName: z.string(),
  academicYearLabel: z.string(), semesterResults: z.array(semesterResultSchema),
  decisionStatus: z.enum(["PROMOTED", "PROMOTED_BY_COMPENSATION", "PROMOTED_WITH_DEBT", "LEVEL_VALIDATED", "REPEAT", "FAILED"]),
  annualAverage: z.number(), outstandingModuleCount: z.number().int(), decidedAt: z.string(),
});
const graduationDecisionSchema = z.object({
  id: z.string().uuid(), studentId: z.string().uuid(), firstName: z.string(), lastName: z.string(),
  apogeeCode: z.string(), nationalStudentCode: z.string().nullable(), cin: z.string().nullable(),
  programName: z.string(), programPathName: z.string(), degreeCycleName: z.string(), terminalAcademicLevelName: z.string(),
  academicYearLabel: z.string(), decisionStatus: z.literal("GRADUATED"), graduationAverage: z.number(), decidedAt: z.string(),
});

export type StudentProgressionDecision = z.infer<typeof progressionDecisionSchema>;
export type StudentGraduationDecision = z.infer<typeof graduationDecisionSchema>;

async function get<T>(path: string, schema: z.ZodType<T>): Promise<T> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}${path}`);
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) throw apiRequestError(response, body);
  return schema.parse(body);
}

export const studentDecisionKeys = {
  progression: () => ["student-decisions", "progression"] as const,
  graduation: () => ["student-decisions", "graduation"] as const,
};
export const getMyProgressionDecisions = () => get("/api/v1/me/progression-decisions", z.array(progressionDecisionSchema));
export const getMyGraduationDecisions = () => get("/api/v1/me/graduation-decisions", z.array(graduationDecisionSchema));
