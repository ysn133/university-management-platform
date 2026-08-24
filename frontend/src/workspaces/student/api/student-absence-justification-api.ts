import { z } from "zod";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { env } from "@/shared/config/env";

export const absenceJustificationSchema = z.object({
  id: z.string().uuid(), absenceId: z.string().uuid(), teachingAssignmentId: z.string().uuid(), studentId: z.string().uuid(),
  studentApogeeCode: z.string(), studentFirstName: z.string(), studentLastName: z.string(), subjectModuleId: z.string().uuid(),
  subjectModuleCode: z.string(), subjectModuleTitle: z.string(), absenceDate: z.string(), reason: z.string(),
  status: z.enum(["PENDING", "ACCEPTED", "REJECTED"]), documentId: z.string().uuid(), documentFileName: z.string(),
  documentContentType: z.string(), decisionNote: z.string().nullable(), submittedAt: z.string(), reviewedAt: z.string().nullable(),
});
export type AbsenceJustification = z.infer<typeof absenceJustificationSchema>;
export const studentJustificationKeys = { mine: () => ["student", "absence-justifications"] as const };

async function body(response: Response) { const data: unknown = await response.json().catch(() => null); if (!response.ok) throw apiRequestError(response, data); return data; }

export async function getMyAbsenceJustifications(): Promise<AbsenceJustification[]> {
  return z.array(absenceJustificationSchema).parse(await body(await authenticatedFetch(`${env.apiBaseUrl}/api/v1/me/absence-justifications`)));
}

export async function submitAbsenceJustification(absenceId: string, reason: string, file: File): Promise<AbsenceJustification> {
  const form = new FormData(); form.append("file", file);
  const upload = await body(await authenticatedFetch(`${env.apiBaseUrl}/api/v1/documents?purpose=ABSENCE_JUSTIFICATION`, { method: "POST", body: form })) as { documentId?: string };
  if (!upload.documentId) throw new Error("Document upload did not return an identifier.");
  return absenceJustificationSchema.parse(await body(await authenticatedFetch(`${env.apiBaseUrl}/api/v1/absences/${absenceId}/justifications`, {
    method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ reason, documentId: upload.documentId }),
  })));
}

export async function downloadAbsenceJustification(id: string, filename: string): Promise<void> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/absence-justifications/${id}/document`);
  if (!response.ok) throw apiRequestError(response, await response.json().catch(() => null));
  const url = URL.createObjectURL(await response.blob()); const anchor = document.createElement("a"); anchor.href = url; anchor.download = filename; anchor.click(); URL.revokeObjectURL(url);
}
