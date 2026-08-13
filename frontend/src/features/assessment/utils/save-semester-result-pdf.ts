import type { Student } from "@/features/student-registration/api/student-registration-api";
import type { FinalResult } from "../api/final-results-api";
import type { SemesterResult } from "../api/semester-results-api";
import { addAcademicDocumentFooters, drawAcademicDocumentClosing, drawAcademicDocumentHeader, drawAcademicTableHeader, drawAcademicTableRow } from "./academic-document-pdf";
import type { GradeDocumentContext } from "./save-student-session-grades-pdf";

export async function saveSemesterResultPdf(result: SemesterResult, moduleResults: FinalResult[], context: GradeDocumentContext, profile: Student) {
  const { jsPDF } = await import("jspdf");
  const pdf = new jsPDF({ orientation: "portrait", unit: "mm", format: "a4" });
  drawAcademicDocumentHeader(pdf, { academicLevel: context.academicLevel, academicYear: context.academicYear, apogeeCode: result.apogeeCode, cin: profile.cin, cne: profile.nationalStudentCode, context: context.label, firstName: result.firstName, lastName: result.lastName, program: context.program, programPath: context.programPath, reference: `SR-${result.apogeeCode}-${new Date().getFullYear()}`, semester: context.semester, title: "Semester Result Statement" });
  const widths = [28, 92, 25, 28];
  let y = drawAcademicTableHeader(pdf, 111, ["Module", "Module title", "Grade / 20", "Result"], widths);
  moduleResults.forEach((module, index) => { y = drawAcademicTableRow(pdf, y, [module.subjectModuleCode, module.subjectModuleTitle, module.finalGrade?.toFixed(2) ?? "-", module.resultStatus ?? "Pending"], widths, index % 2 === 1); });
  y += 8;
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(10);
  pdf.text(`Semester average: ${result.semesterAverage.toFixed(2)} / 20`, 14, y);
  if (result.resultStatus === "VALIDATED") pdf.setTextColor(25, 112, 70);
  else pdf.setTextColor(180, 55, 55);
  pdf.text(result.resultStatus === "VALIDATED" ? "SEMESTER VALIDATED" : "SEMESTER NOT VALIDATED", 196, y, { align: "right" });
  drawAcademicDocumentClosing(pdf, y);
  addAcademicDocumentFooters(pdf);
  pdf.save(`${result.apogeeCode.toLowerCase()}-semester-result.pdf`);
}
