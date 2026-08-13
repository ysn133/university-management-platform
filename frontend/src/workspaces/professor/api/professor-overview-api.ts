import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const moduleResponsibilitySchema = z.object({
  id: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(),
  classGroupId: z.string().uuid(),
  classGroupName: z.string(),
  academicYearId: z.string().uuid(),
  academicYearLabel: z.string(),
  semesterId: z.string().uuid(),
  semesterName: z.string(),
  status: z.enum(["ACTIVE", "INACTIVE"]),
});

const professorClassStudentSchema = z.object({
  studentId: z.string().uuid(),
  apogeeCode: z.string(),
  nationalStudentCode: z.string().nullable(),
  universityEmail: z.string(),
  firstName: z.string(),
  lastName: z.string(),
});

const professorExamSchema = z.object({
  id: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(),
  classGroupId: z.string().uuid(),
  classGroupName: z.string(),
  academicYearId: z.string().uuid(),
  academicYearLabel: z.string(),
  academicYearStatus: z.enum(["PLANNED", "ACTIVE", "CLOSED"]),
  semesterId: z.string().uuid(),
  semesterName: z.string(),
  semesterStartDate: z.string(),
  semesterEndDate: z.string(),
  academicLevelId: z.string().uuid(),
  academicLevelName: z.string(),
  programFiliereCode: z.string(),
  programFiliereName: z.string(),
  sessionType: z.enum(["NORMAL", "RATTRAPAGE"]),
  examDate: z.string(),
  startTime: z.string(),
  endTime: z.string().nullable(),
  rooms: z.array(z.string()),
});

export type ModuleResponsibility = z.infer<typeof moduleResponsibilitySchema>;
export type ProfessorClassStudent = z.infer<typeof professorClassStudentSchema>;
export type ProfessorExam = z.infer<typeof professorExamSchema>;

export const professorOverviewKeys = {
  responsibilities: () => ["professor-overview", "responsibilities"] as const,
  classStudents: (subjectModuleId: string, classGroupId: string) => ["professor-overview", "class-students", subjectModuleId, classGroupId] as const,
  exams: () => ["professor-overview", "exams"] as const,
};

export async function getMyModuleResponsibilities(): Promise<ModuleResponsibility[]> {
  const result = await apiClient.GET("/api/v1/me/module-class-responsibilities");
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(moduleResponsibilitySchema).parse(result.data);
}

export async function getMyExams(): Promise<ProfessorExam[]> {
  const result = await apiClient.GET("/api/v1/me/exams");
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(professorExamSchema).parse(result.data);
}

export async function getMyClassStudents(subjectModuleId: string, classGroupId: string): Promise<ProfessorClassStudent[]> {
  const result = await apiClient.GET("/api/v1/me/modules/{subjectModuleId}/classes/{classGroupId}/students", {
    params: { path: { subjectModuleId, classGroupId } },
  });
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return z.array(professorClassStudentSchema).parse(result.data);
}
