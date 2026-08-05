import { z } from "zod";
import type { components } from "@/shared/api/generated/schema";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const studentSchema = z.object({
  studentId: z.string().uuid(),
  userAccountId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  apogeeCode: z.string(),
  nationalStudentCode: z.string().nullable().optional(),
  initialEnrollmentDate: z.string(),
  universityEmail: z.string(),
  roleType: z.literal("STUDENT"),
  accountStatus: z.enum(["ACTIVE", "LOCKED", "DEACTIVATED", "ARCHIVED"]),
  firstName: z.string(),
  lastName: z.string(),
  birthDate: z.string(),
  placeOfBirth: z.string(),
  nationality: z.string(),
  cin: z.string().nullable().optional(),
  sex: z.enum(["MALE", "FEMALE"]),
  phoneNumber: z.string().nullable().optional(),
  profilePicturePath: z.string().nullable().optional(),
});

const createStudentResponseSchema = z.object({
  studentId: z.string().uuid(),
  userAccountId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  apogeeCode: z.string(),
  roleType: z.literal("STUDENT"),
});

const academicRegistrationSchema = z.object({
  id: z.string().uuid(),
  studentId: z.string().uuid(),
  establishmentId: z.string().uuid(),
  programFiliereId: z.string().uuid(),
  academicLevelId: z.string().uuid(),
  academicYearId: z.string().uuid(),
  status: z.enum(["ACTIVE", "COMPLETED", "CANCELLED", "SUSPENDED"]),
  createdAt: z.string(),
  updatedAt: z.string(),
});

const semesterRegistrationSchema = z.object({
  id: z.string().uuid(),
  academicRegistrationId: z.string().uuid(),
  semesterId: z.string().uuid(),
  semesterName: z.string(),
  semesterOrder: z.number().int(),
});

const moduleRegistrationSchema = z.object({
  id: z.string().uuid(),
  semesterRegistrationId: z.string().uuid(),
  subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(),
  subjectModuleTitle: z.string(),
  originAcademicLevelId: z.string().uuid().nullable().optional(),
  inscriptionNumber: z.number().int(),
  status: z.enum(["ACTIVE", "COMPLETED", "CANCELLED"]),
});

export type Student = z.infer<typeof studentSchema>;
export type StudentAccountStatus = Student["accountStatus"];
export interface StudentDirectoryFilters {
  query?: string;
  status?: StudentAccountStatus;
  enrolledFrom?: string;
  enrolledTo?: string;
}
export interface AcademicRegistrationFilters {
  academicYearId?: string;
  programFiliereId?: string;
  academicLevelId?: string;
  semesterId?: string;
  status?: AcademicRegistration["status"];
}
export type UpdateStudentRequest = components["schemas"]["UpdateStudentRequest"];
export type StudentLifecycleAction = "lock" | "unlock" | "deactivate" | "archive";
export type AcademicRegistration = z.infer<typeof academicRegistrationSchema>;
export type SemesterRegistration = z.infer<typeof semesterRegistrationSchema>;
export type ModuleRegistration = z.infer<typeof moduleRegistrationSchema>;
export type CreateStudentRequest = components["schemas"]["CreateStudentRequest"];
export type CreateAcademicRegistrationRequest = components["schemas"]["CreateAcademicRegistrationRequest"];

export const studentRegistrationKeys = {
  students: (establishmentId: string, filters: StudentDirectoryFilters = {}) => ["student-registration", "students", establishmentId, filters] as const,
  student: (studentId: string) => ["student-registration", "student", studentId] as const,
  registrations: (establishmentId: string, filters: AcademicRegistrationFilters = {}) => ["student-registration", "registrations", establishmentId, filters] as const,
  studentRegistrations: (studentId: string) => ["student-registration", "student-registrations", studentId] as const,
  studyContext: (registrationId: string) => ["student-registration", "study-context", registrationId] as const,
};

async function parseResponse<T>(result: { response: Response; data?: unknown; error?: unknown }, schema: z.ZodType<T>): Promise<T> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return schema.parse(result.data);
}

export async function getStudents(establishmentId: string, filters: StudentDirectoryFilters = {}): Promise<Student[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/students", { params: { path: { establishmentId }, query: filters } }), z.array(studentSchema));
}

export async function createStudent(establishmentId: string, request: CreateStudentRequest): Promise<{ studentId: string }> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/students", { params: { path: { establishmentId } }, body: request }), createStudentResponseSchema);
}

export async function getStudent(studentId: string): Promise<Student> {
  return parseResponse(await apiClient.GET("/api/v1/students/{studentId}", { params: { path: { studentId } } }), studentSchema);
}

export async function updateStudent(studentId: string, request: UpdateStudentRequest): Promise<Student> {
  return parseResponse(await apiClient.PUT("/api/v1/students/{studentId}", { params: { path: { studentId } }, body: request }), studentSchema);
}

export async function resetStudentPassword(studentId: string, newPassword: string): Promise<void> {
  const result = await apiClient.POST("/api/v1/students/{studentId}/password-reset", { params: { path: { studentId } }, body: { newPassword } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

export async function changeStudentStatus(studentId: string, action: StudentLifecycleAction): Promise<void> {
  const paths = {
    lock: "/api/v1/students/{studentId}/lock",
    unlock: "/api/v1/students/{studentId}/unlock",
    deactivate: "/api/v1/students/{studentId}/deactivate",
    archive: "/api/v1/students/{studentId}/archive",
  } as const;
  const result = await apiClient.POST(paths[action], { params: { path: { studentId } } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

export async function getAcademicRegistrations(establishmentId: string, filters: AcademicRegistrationFilters = {}): Promise<AcademicRegistration[]> {
  return parseResponse(await apiClient.GET("/api/v1/establishments/{establishmentId}/academic-registrations", { params: { path: { establishmentId }, query: filters } }), z.array(academicRegistrationSchema));
}

export async function getStudentAcademicRegistrations(studentId: string): Promise<AcademicRegistration[]> {
  return parseResponse(await apiClient.GET("/api/v1/students/{studentId}/academic-registrations", { params: { path: { studentId } } }), z.array(academicRegistrationSchema));
}

export async function createAcademicRegistration(establishmentId: string, request: CreateAcademicRegistrationRequest): Promise<AcademicRegistration> {
  return parseResponse(await apiClient.POST("/api/v1/establishments/{establishmentId}/academic-registrations", { params: { path: { establishmentId } }, body: request }), academicRegistrationSchema);
}

export async function getRegistrationStudyContext(registrationId: string): Promise<Array<{ semester: SemesterRegistration; modules: ModuleRegistration[] }>> {
  const semesters = await parseResponse(await apiClient.GET("/api/v1/academic-registrations/{academicRegistrationId}/semester-registrations", { params: { path: { academicRegistrationId: registrationId } } }), z.array(semesterRegistrationSchema));
  return Promise.all(semesters.map(async (semester) => ({
    semester,
    modules: await parseResponse(await apiClient.GET("/api/v1/semester-registrations/{semesterRegistrationId}/module-registrations", { params: { path: { semesterRegistrationId: semester.id } } }), z.array(moduleRegistrationSchema)),
  })));
}
