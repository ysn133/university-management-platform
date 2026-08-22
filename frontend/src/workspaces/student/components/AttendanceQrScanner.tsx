import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useMutation } from "@tanstack/react-query";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { checkInToAttendance } from "../api/student-attendance-api";

type BarcodeResult = { rawValue: string };
type BarcodeDetectorInstance = { detect(source: HTMLVideoElement): Promise<BarcodeResult[]> };
type BarcodeDetectorConstructor = new (options: { formats: string[] }) => BarcodeDetectorInstance;

function qrParameters(value: string) {
  try {
    const url = new URL(value, window.location.origin);
    return { sessionId: url.searchParams.get("sessionId") ?? "", token: url.searchParams.get("token") ?? "" };
  } catch {
    return { sessionId: "", token: "" };
  }
}

export function AttendanceQrScanner({ onClose }: { onClose: () => void }) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const frameRef = useRef<number | null>(null);
  const [cameraError, setCameraError] = useState("");
  const [cameraReady, setCameraReady] = useState(false);
  const [detected, setDetected] = useState(false);
  const checkInMutation = useMutation({
    mutationFn: ({ sessionId, token }: { sessionId: string; token: string }) => checkInToAttendance(sessionId, token),
  });

  useEffect(() => {
    if (!window.isSecureContext) {
      setCameraError("Camera scanning requires HTTPS when the app is opened from another device.");
      return;
    }
    const BarcodeDetector = (window as Window & { BarcodeDetector?: BarcodeDetectorConstructor }).BarcodeDetector;
    if (!BarcodeDetector) {
      setCameraError("QR scanning is not supported by this browser.");
      return;
    }
    let active = true;
    const detector = new BarcodeDetector({ formats: ["qr_code"] });

    async function start() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: { ideal: "environment" } }, audio: false });
        if (!active) {
          stream.getTracks().forEach((track) => track.stop());
          return;
        }
        streamRef.current = stream;
        const video = videoRef.current;
        if (!video) return;
        video.srcObject = stream;
        await video.play();
        setCameraReady(true);

        async function scan() {
          if (!active || !videoRef.current) return;
          try {
            const codes = await detector.detect(videoRef.current);
            const parameters = codes[0] ? qrParameters(codes[0].rawValue) : null;
            if (parameters?.sessionId && parameters.token) {
              setDetected(true);
              navigator.vibrate?.(80);
              active = false;
              stream.getTracks().forEach((track) => track.stop());
              checkInMutation.mutate(parameters);
              return;
            }
          } catch {
            // A frame can fail while the camera is focusing; continue scanning.
          }
          frameRef.current = window.requestAnimationFrame(scan);
        }
        frameRef.current = window.requestAnimationFrame(scan);
      } catch {
        setCameraError("Camera access is unavailable. Check the browser permission and try again.");
      }
    }

    void start();
    return () => {
      active = false;
      if (frameRef.current !== null) window.cancelAnimationFrame(frameRef.current);
      streamRef.current?.getTracks().forEach((track) => track.stop());
    };
  }, []);

  const requestError = checkInMutation.error instanceof ApiRequestError
    ? checkInMutation.error.message
    : "Attendance could not be confirmed.";

  return createPortal(<div className="management-modal-backdrop student-qr-scanner-backdrop" role="presentation">
    <section aria-modal="true" className={`student-qr-scanner${checkInMutation.isSuccess ? " is-success" : checkInMutation.isError || cameraError ? " is-error" : ""}`} role="dialog">
      <header><div className="student-qr-scanner-heading"><span aria-hidden="true"><svg fill="none" viewBox="0 0 24 24"><path d="M4 9V5a1 1 0 0 1 1-1h4M15 4h4a1 1 0 0 1 1 1v4M20 15v4a1 1 0 0 1-1 1h-4M9 20H5a1 1 0 0 1-1-1v-4M8 12h8" /></svg></span><div><p className="management-kicker">Attendance check-in</p><h2>{checkInMutation.isSuccess ? "Check-in complete" : "Scan the classroom code"}</h2><p>{checkInMutation.isSuccess ? "Your attendance was sent successfully." : "Keep the QR code inside the frame until it is detected."}</p></div></div><button aria-label="Close scanner" onClick={onClose} type="button">×</button></header>
      <div className={`student-qr-scanner-view${detected ? " is-detected" : ""}${cameraError || checkInMutation.isSuccess || checkInMutation.isError ? " has-result" : ""}`}>
        {!cameraError && !checkInMutation.isSuccess && !checkInMutation.isError && <><video muted playsInline ref={videoRef} /><div className="student-qr-frame" aria-hidden="true"><i /><i /><i /><i /><b /></div><span aria-live="polite">{detected ? "Confirming attendance..." : cameraReady ? "Point your camera at the code" : "Starting camera..."}</span></>}
        {cameraError && <div className="student-qr-result is-error"><span className="student-qr-result-icon" aria-hidden="true">!</span><strong>Camera unavailable</strong><p>{cameraError}</p></div>}
        {checkInMutation.isSuccess && <div className="student-qr-result is-success"><span className="student-qr-result-icon" aria-hidden="true"><svg fill="none" viewBox="0 0 24 24"><path d="m6 12 4 4 8-9" /></svg></span><strong>Attendance confirmed</strong><p>{checkInMutation.data.message}</p><small>You can close this window and return to your attendance record.</small></div>}
        {checkInMutation.isError && <div className="student-qr-result is-error"><span className="student-qr-result-icon" aria-hidden="true">!</span><strong>Check-in failed</strong><p>{requestError}</p><small>The displayed code may have rotated. Close this window and scan the current code.</small></div>}
      </div>
      <footer><button className={checkInMutation.isSuccess ? "management-primary-button" : "secondary-button"} onClick={onClose} type="button">{checkInMutation.isSuccess ? "Return to attendance" : "Close scanner"}</button></footer>
    </section>
  </div>, document.body);
}
