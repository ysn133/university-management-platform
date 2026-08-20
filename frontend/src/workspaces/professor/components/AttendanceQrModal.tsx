import { useEffect, useEffectEvent, useState } from "react";
import { createPortal } from "react-dom";
import { useMutation, useQuery } from "@tanstack/react-query";
import { QRCodeSVG } from "qrcode.react";
import { getAttendanceQrSession, closeAttendanceQrSession, professorAttendanceKeys, type AttendanceQrSession } from "../api/professor-attendance-api";

type AttendanceQrModalProps = {
  initialSession: AttendanceQrSession;
  onCheckInsChange: (studentIds: string[]) => void;
  onClose: () => void;
};

export function AttendanceQrModal({ initialSession, onCheckInsChange, onClose }: AttendanceQrModalProps) {
  const [now, setNow] = useState(Date.now());
  const sessionQuery = useQuery({
    queryKey: professorAttendanceKeys.qrSession(initialSession.sessionId),
    queryFn: () => getAttendanceQrSession(initialSession.sessionId),
    initialData: initialSession,
    refetchInterval: 3_000,
  });
  const closeMutation = useMutation({
    mutationFn: () => closeAttendanceQrSession(initialSession.sessionId),
    onSettled: onClose,
  });
  const session = sessionQuery.data;
  const reportCheckIns = useEffectEvent(onCheckInsChange);
  const secondsLeft = Math.max(0, Math.ceil((new Date(session.tokenExpiresAt).getTime() - now) / 1_000));
  const checkInUrl = `${window.location.origin}/student/attendance/check-in?sessionId=${session.sessionId}&token=${session.token}`;

  useEffect(() => {
    reportCheckIns(session.checkedInStudentIds);
  }, [session.checkedInStudentIds]);

  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 1_000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previousOverflow;
    };
  }, []);

  return createPortal(<div className="management-modal-backdrop attendance-qr-backdrop" role="presentation">
    <section aria-modal="true" className="attendance-qr-modal" role="dialog">
      <header><div><p className="management-kicker">Live attendance</p><h2>Scan to check in</h2><p>Students scan using their university account.</p></div><button aria-label="Close QR attendance" onClick={() => closeMutation.mutate()} type="button">×</button></header>
      <div className="attendance-qr-content">
        <div className="attendance-qr-code"><QRCodeSVG bgColor="#ffffff" fgColor="#123b59" level="M" marginSize={2} size={280} value={checkInUrl} /></div>
        <div className="attendance-qr-live"><span><i /> Live session</span><span>New code in <strong>{secondsLeft}s</strong></span></div>
        <div className="attendance-qr-status"><div><span>Checked in</span><strong>{session.checkedInStudentIds.length}</strong></div><p>The attendance roster updates automatically. You can review and correct it after closing this window.</p></div>
        {sessionQuery.isError && <small className="attendance-qr-error">Live updates were interrupted. Reopen the QR session.</small>}
      </div>
      <footer><button disabled={closeMutation.isPending} onClick={() => closeMutation.mutate()} type="button">{closeMutation.isPending ? "Closing..." : "Finish check-in"}</button></footer>
    </section>
  </div>, document.body);
}
