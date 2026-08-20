import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { checkInToAttendance } from "../api/student-attendance-api";

export function StudentAttendanceCheckInPage() {
  const [params] = useSearchParams();
  const sessionId = params.get("sessionId") ?? "";
  const token = params.get("token") ?? "";
  const validLink = Boolean(sessionId && token);
  const checkInQuery = useQuery({
    queryKey: ["student-attendance-check-in", sessionId, token],
    queryFn: () => checkInToAttendance(sessionId, token),
    enabled: validLink,
    retry: false,
  });
  const message = checkInQuery.error instanceof ApiRequestError
    ? checkInQuery.error.message
    : "Attendance could not be confirmed.";

  return <main className="student-check-in-page">
    <section className={`student-check-in-card${checkInQuery.isSuccess ? " is-success" : checkInQuery.isError || !validLink ? " is-error" : ""}`}>
      <div className="student-check-in-mark" aria-hidden="true">{checkInQuery.isSuccess ? "✓" : checkInQuery.isError || !validLink ? "!" : ""}</div>
      <p className="management-kicker">Class attendance</p>
      <h1>{checkInQuery.isPending && validLink ? "Confirming your presence" : checkInQuery.isSuccess ? "You are checked in" : "Check-in unavailable"}</h1>
      <p>{!validLink ? "This attendance link is incomplete." : checkInQuery.isPending ? "Keep this page open for a moment." : checkInQuery.isSuccess ? "Your professor can now see you in the live attendance roster." : message}</p>
      {checkInQuery.isError && <small>The code may have changed. Scan the latest QR code displayed by your professor.</small>}
    </section>
  </main>;
}
