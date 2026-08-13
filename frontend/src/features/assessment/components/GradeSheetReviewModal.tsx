import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import type { ManagedGradeSheet } from "../api/grade-management-api";

interface Props {
  error?: string;
  isSubmitting: boolean;
  moduleCode?: string;
  moduleTitle: string;
  onApprove: () => void;
  onClose: () => void;
  onPublish: () => void;
  sheet: ManagedGradeSheet;
}

export function GradeSheetReviewModal({ error, isSubmitting, moduleCode, moduleTitle, onApprove, onClose, onPublish, sheet }: Props) {
  const canApprove = sheet.workflowStatus === "SUBMITTED" || sheet.workflowStatus === "REVIEWED";
  const canPublish = sheet.workflowStatus === "APPROVED";
  const absentCount = sheet.grades.filter((grade) => grade.zeroGradeReason === "ABSENT").length;
  const average = sheet.grades.length ? sheet.grades.reduce((total, grade) => total + (grade.gradeValue ?? 0), 0) / sheet.grades.length : 0;

  return <ManagementModal size="wide" title="Review Grade Sheet" description={`${moduleCode ? `${moduleCode} · ` : ""}${moduleTitle}`} onClose={onClose}>
    <div className="grade-review-modal">
      <div className="grade-review-summary"><div><span>Status</span><strong>{sheet.workflowStatus}</strong></div><div><span>Students</span><strong>{sheet.grades.length}</strong></div><div><span>Class average</span><strong>{average.toFixed(2)}</strong></div><div><span>Absent</span><strong>{absentCount}</strong></div></div>
      {error && <div className="management-alert management-alert--error">{error}</div>}
      <div className="grade-review-table-wrap"><table className="grade-review-table"><thead><tr><th>No.</th><th>Student</th><th>Apogee</th><th>Inscription</th><th>Grade / 20</th><th>Observation</th></tr></thead><tbody>{sheet.grades.map((grade, index) => <tr key={grade.moduleRegistrationId}><td>{index + 1}</td><td><strong>{grade.firstName} {grade.lastName}</strong><span>{grade.universityEmail}</span></td><td>{grade.apogeeCode}</td><td>{grade.inscriptionNumber}</td><td><strong>{grade.gradeValue?.toFixed(2) ?? "—"}</strong></td><td>{grade.zeroGradeReason === "ABSENT" ? "Absent" : grade.zeroGradeReason === "EARNED_ZERO" ? "Earned zero" : "—"}</td></tr>)}</tbody></table></div>
      <footer className="form-actions"><button className="secondary-button" onClick={onClose} type="button">Close</button>{canApprove && <button className="management-primary-button" disabled={isSubmitting} onClick={onApprove} type="button">{isSubmitting ? "Approving..." : "Approve Sheet"}</button>}{canPublish && <button className="management-primary-button" disabled={isSubmitting} onClick={onPublish} type="button">{isSubmitting ? "Publishing..." : "Publish Grades"}</button>}</footer>
    </div>
  </ManagementModal>;
}
