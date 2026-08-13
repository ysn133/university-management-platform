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
    const requiresRattrapage = includeStatus && grade.gradeValue !== null && (moduleValidationThreshold === undefined || grade.gradeValue < moduleValidationThreshold);
    y = drawAcademicTableRow(pdf, y, values, widths, index % 2 === 1, requiresRattrapage ? [2, 3] : []);
  });
  drawAcademicDocumentClosing(pdf, y);
  addAcademicDocumentFooters(pdf);
  pdf.save(`${student.apogeeCode.toLowerCase()}-${session.toLowerCase()}-grades.pdf`);
}
