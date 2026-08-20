import type { ManagedGradeSheet } from "../api/grade-management-api";
import type { Student } from "@/features/student-registration/api/student-registration-api";
import { addAcademicDocumentFooters, drawAcademicDocumentClosing, drawAcademicDocumentHeader, drawAcademicTableHeader, drawAcademicTableRow } from "./academic-document-pdf";

export interface StudentSessionModule {
  code: string;
  title: string;
  sheet?: ManagedGradeSheet;
}

export interface GradeDocumentContext {
  academicLevel?: string;
  academicYear?: string;
  label: string;
  program?: string;
  programPath?: string;
  semester?: string;
}

export async function saveStudentSessionGradesPdf(studentId: string, modules: StudentSessionModule[], session: string, context: GradeDocumentContext, profile: Student, moduleValidationThreshold?: number) {
  const grades = modules.flatMap((module) => {
    const grade = module.sheet?.grades.find((item) => item.studentId === studentId);
    return grade ? [{ grade, module }] : [];
  });
  if (!grades.length) return;
  const { jsPDF } = await import("jspdf");
  const student = grades[0].grade;
  const pdf = new jsPDF({ orientation: "portrait", unit: "mm", format: "a4" });
  const includeStatus = session === "Normal";
  drawAcademicDocumentHeader(pdf, { academicLevel: context.academicLevel, academicYear: context.academicYear, apogeeCode: student.apogeeCode, cin: profile.cin, cne: profile.nationalStudentCode, context: context.label, firstName: student.firstName, lastName: student.lastName, program: context.program, programPath: context.programPath, reference: `GR-${student.apogeeCode}-${new Date().getFullYear()}`, semester: context.semester, title: `${session} Session Grade Statement` });
  const widths = includeStatus ? [28, 92, 25, 28] : [30, 118, 25];
  const labels = includeStatus ? ["Module", "Module title", "Grade / 20", "Status"] : ["Module", "Module title", "Grade / 20"];
  let y = drawAcademicTableHeader(pdf, 111, labels, widths);
  grades.forEach(({ grade, module }, index) => {
    const values = [module.code, module.title, grade.gradeValue?.toFixed(2) ?? "-"];
    if (includeStatus) values.push(grade.gradeValue === null ? "Pending" : moduleValidationThreshold !== undefined && grade.gradeValue >= moduleValidationThreshold ? "Validated" : "Rattrapage");
    const nonValidated = grade.gradeValue !== null && (moduleValidationThreshold === undefined || grade.gradeValue < moduleValidationThreshold);
    y = drawAcademicTableRow(pdf, y, values, widths, index % 2 === 1, nonValidated ? includeStatus ? [2, 3] : [2] : []);
  });
  drawAcademicDocumentClosing(pdf, y);
  addAcademicDocumentFooters(pdf);
  pdf.save(`${student.apogeeCode.toLowerCase()}-${session.toLowerCase()}-grades.pdf`);
}

export async function saveAllSessionGradesPdf(modules: StudentSessionModule[], session: string, context: GradeDocumentContext, moduleValidationThreshold?: number) {
  const students = Array.from(new Map(modules.flatMap((module) => module.sheet?.grades ?? []).map((grade) => [grade.studentId, grade])).values());
  if (!students.length) return;
  const { jsPDF } = await import("jspdf");
  const pdf = new jsPDF({ orientation: "landscape", unit: "mm", format: "a4" });
  const pageWidth = 297;
  const pageHeight = 210;
  const title = `${session} Session Grade Register`;
  const reference = `SR-${session.slice(0, 3).toUpperCase()}-${new Date().getFullYear()}`;
  const drawHeader = () => {
    pdf.setFillColor(18, 91, 145);
    pdf.rect(0, 0, pageWidth, 5, "F");
    pdf.roundedRect(14, 10, 17, 14, 1.5, 1.5, "F");
    pdf.setTextColor(255, 255, 255);
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(10);
    pdf.text("UIZ", 22.5, 18.8, { align: "center" });
    pdf.setTextColor(23, 58, 85);
    pdf.setFontSize(12);
    pdf.text("UNIVERSITE IBN ZOHR", 36, 15);
    pdf.setFont("helvetica", "normal");
    pdf.setTextColor(94, 113, 130);
    pdf.setFontSize(7.5);
    pdf.text("ACADEMIC ADMINISTRATION", 36, 20);
    pdf.setFontSize(7);
    pdf.text("DOCUMENT REFERENCE", 283, 14, { align: "right" });
    pdf.setTextColor(23, 58, 85);
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(8);
    pdf.text(reference, 283, 19, { align: "right" });
    pdf.setDrawColor(198, 211, 222);
    pdf.line(14, 30, 283, 30);
    pdf.setFontSize(15);
    pdf.text(title.toUpperCase(), 148.5, 42, { align: "center" });
    pdf.setFont("helvetica", "normal");
    pdf.setTextColor(94, 113, 130);
    pdf.setFontSize(8.5);
    pdf.text(context.label, 148.5, 52, { align: "center", maxWidth: 250 });
    const details = [context.program, context.programPath, context.academicLevel, context.semester, context.academicYear].filter(Boolean).join("  ·  ");
    pdf.setTextColor(23, 58, 85);
    pdf.setFontSize(8);
    pdf.text(details, 148.5, 61, { align: "center", maxWidth: 260 });
  };
  const studentWidth = 58;
  const apogeeWidth = 28;
  const moduleWidth = (269 - studentWidth - apogeeWidth) / Math.max(modules.length, 1);
  let y = 72;
  const drawTableHeader = () => {
    pdf.setFillColor(23, 58, 85);
    pdf.rect(14, y, 269, 9, "F");
    pdf.setTextColor(255, 255, 255);
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(7.2);
    pdf.text("STUDENT", 15.5, y + 5.8);
    pdf.text("APOGEE", 14 + studentWidth + 1.5, y + 5.8);
    modules.forEach((module, index) => pdf.text(module.code.slice(0, 10), 14 + studentWidth + apogeeWidth + index * moduleWidth + 1.5, y + 5.8, { maxWidth: moduleWidth - 3 }));
    y += 9;
  };
  drawHeader();
  drawTableHeader();
  students.forEach((student, index) => {
    if (y > 187) {
      pdf.addPage();
      drawHeader();
      y = 72;
      drawTableHeader();
    }
    if (index % 2 === 1) { pdf.setFillColor(247, 250, 252); pdf.rect(14, y, 269, 9, "F"); }
    pdf.setDrawColor(198, 211, 222);
    pdf.rect(14, y, 269, 9);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(7.4);
    pdf.setTextColor(23, 58, 85);
    pdf.text(`${student.lastName} ${student.firstName}`.slice(0, 34), 15.5, y + 5.7);
    pdf.text(student.apogeeCode.slice(0, 16), 14 + studentWidth + 1.5, y + 5.7);
    modules.forEach((module, moduleIndex) => {
      const grade = module.sheet?.grades.find((item) => item.studentId === student.studentId)?.gradeValue;
      const nonValidated = grade !== null && grade !== undefined && (moduleValidationThreshold === undefined || grade < moduleValidationThreshold);
      pdf.setTextColor(nonValidated ? 180 : 23, nonValidated ? 55 : 58, nonValidated ? 55 : 85);
      pdf.setFont("helvetica", nonValidated ? "bold" : "normal");
      pdf.text(grade === null || grade === undefined ? "-" : grade.toFixed(2), 14 + studentWidth + apogeeWidth + moduleIndex * moduleWidth + 1.5, y + 5.7);
    });
    y += 9;
  });
  const pages = pdf.getNumberOfPages();
  for (let page = 1; page <= pages; page += 1) {
    pdf.setPage(page);
    pdf.setDrawColor(198, 211, 222);
    pdf.line(14, pageHeight - 13, 283, pageHeight - 13);
    pdf.setTextColor(94, 113, 130);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(6.8);
    pdf.text("Official academic record generated by the Universite Ibn Zohr management platform.", 14, pageHeight - 8);
    pdf.text(`Page ${page} / ${pages}`, 283, pageHeight - 8, { align: "right" });
  }
  pdf.save(`${session.toLowerCase()}-session-all-grades.pdf`);
}
