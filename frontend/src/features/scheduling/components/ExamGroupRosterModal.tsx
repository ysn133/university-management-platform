import { useRef, useState } from "react";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import type { ExamGroupPlan } from "../api/exam-planning-api";
import { saveExamGroupRosterPdf } from "../utils/save-candidate-list-pdf";

interface Props { academicYearLabel?: string; classGroupName: string; plan: ExamGroupPlan; programName?: string; semesterName?: string; sessionType: "NORMAL" | "RATTRAPAGE"; onClose: () => void; }

export function ExamGroupRosterModal({ academicYearLabel, classGroupName, plan, programName, semesterName, sessionType, onClose }: Props) {
  const documentRef = useRef<HTMLDivElement>(null);
  const [exporting, setExporting] = useState(false);
  async function exportPdf() {
    if (!documentRef.current) return;
    setExporting(true);
    try { await saveExamGroupRosterPdf(documentRef.current, `exam-groups-${semesterName ?? "semester"}.pdf`); }
    finally { setExporting(false); }
  }
  return <ManagementModal size="wide" title="Exam Groups" description={`${semesterName} · ${classGroupName}`} onClose={onClose}><div className="exam-candidate-workspace"><div className="exam-candidate-toolbar"><div className="exam-candidate-toolbar__summary"><span>Grouping roster</span><div><strong>{plan.totalStudentCount}</strong><p>students across {plan.splitCount} exam {plan.splitCount === 1 ? "group" : "groups"}</p></div></div><div className="exam-candidate-toolbar__actions"><button className="management-primary-button" disabled={!plan.groups.length || exporting} onClick={exportPdf} type="button">{exporting ? "Preparing PDF..." : "Save as PDF"}</button></div></div><div className="exam-candidate-document" ref={documentRef}><header><div><strong>Université Ibn Zohr</strong><span>{programName ?? "Academic programme"}</span><span>{academicYearLabel ?? "Academic year"}</span></div><div><span>{sessionType === "NORMAL" ? "Normal Examination Session" : "Rattrapage Examination Session"}</span><strong>{semesterName} · {classGroupName}</strong><span>Exam group assignments</span></div></header>{plan.groups.map((group) => <section className="exam-candidate-group" key={group.id}><div className="exam-candidate-group-heading"><div><span>Exam group</span><strong>{group.label}</strong></div><div><span>Students</span><strong>{group.studentCount}</strong></div></div><table><thead><tr><th>No.</th><th>Apogee</th><th>CNE</th><th>CIN</th><th>Last name</th><th>First name</th></tr></thead><tbody>{group.members.map((member, index) => <tr key={member.studentId}><td>{index + 1}</td><td>{member.apogeeCode}</td><td>{member.nationalStudentCode ?? "—"}</td><td>{member.cin ?? "—"}</td><td>{member.lastName}</td><td>{member.firstName}</td></tr>)}</tbody></table></section>)}</div></div></ManagementModal>;
}
