import { z } from "zod";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { env } from "@/shared/config/env";

const checkInResponseSchema = z.object({
  sessionId: z.string().uuid(),
  studentId: z.string().uuid(),
  checkedInAt: z.string(),
  message: z.string(),
});

export async function checkInToAttendance(sessionId: string, token: string) {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/attendance/qr-sessions/check-in`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ sessionId, token }),
  });
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) throw apiRequestError(response, body);
  return checkInResponseSchema.parse(body);
}
