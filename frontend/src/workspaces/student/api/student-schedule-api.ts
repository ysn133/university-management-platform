import { z } from "zod";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { env } from "@/shared/config/env";

const studentScheduleEntrySchema = z.object({
  id: z.string().uuid(), academicYearId: z.string().uuid(), academicYearLabel: z.string(),
  academicYearStatus: z.string(), semesterId: z.string().uuid(), semesterName: z.string(),
  semesterTermType: z.enum(["AUTUMN", "SPRING"]), semesterStartDate: z.string(), semesterEndDate: z.string(),
  academicLevelId: z.string().uuid(), academicLevelName: z.string(), programFiliereId: z.string().uuid(),
  programFiliereCode: z.string(), programFiliereName: z.string(), subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(), subjectModuleTitle: z.string(), componentType: z.string(),
  audienceType: z.string(), teachingGroupName: z.string(), professorName: z.string(), dayOfWeek: z.string(),
  startTime: z.string(), endTime: z.string(), roomCode: z.string().nullable(), roomName: z.string().nullable(),
  blockCode: z.string().nullable(), blockName: z.string().nullable(),
});

export type StudentScheduleEntry = z.infer<typeof studentScheduleEntrySchema>;

export const studentScheduleKeys = { entries: () => ["student", "schedule"] as const };

export async function getStudentScheduleEntries(): Promise<StudentScheduleEntry[]> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/me/student-schedule-entries`);
  if (!response.ok) throw new ApiRequestError("The schedule could not be loaded.", response.status);
  return z.array(studentScheduleEntrySchema).parse(await response.json());
}
