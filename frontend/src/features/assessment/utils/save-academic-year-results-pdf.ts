import type { ProgressionDecision } from "../api/progression-decisions-api";
import {
  addAcademicDocumentFooters,
  drawAcademicDocumentClosing,
  drawAcademicDocumentHeader,
  drawAcademicTableHeader,
  drawAcademicTableRow,
} from "./academic-document-pdf";

const decisionLabels: Record<ProgressionDecision["decisionStatus"], string> = {
  PROMOTED: "Promoted",
  PROMOTED_BY_COMPENSATION: "Promoted by compensation",
  PROMOTED_WITH_DEBT: "Promoted with module debt",
  LEVEL_VALIDATED: "Academic level validated",
  REPEAT: "Repeat academic level",
  FAILED: "Failed",
};

function semesterStatus(status: "VALIDATED" | "NON_VALIDATED") {
  return status === "VALIDATED" ? "Validated" : "Not validated";
}

function drawCohortHeader(pdf: import("jspdf").jsPDF, result: ProgressionDecision) {
  pdf.setProperties({ author: "Universite Ibn Zohr", creator: "University Management Platform", subject: "Academic Year Results Register", title: "Academic Year Results Register" });
  pdf.setFillColor(18, 91, 145);
  pdf.rect(0, 0, 210, 5, "F");
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
  pdf.text("DOCUMENT REFERENCE", 196, 14, { align: "right" });
  pdf.setTextColor(23, 58, 85);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(8);
  pdf.text(`AYR-${result.academicLevelName}-${result.academicYearLabel}`, 196, 19, { align: "right" });
  pdf.setDrawColor(198, 211, 222);
  pdf.line(14, 30, 196, 30);
  pdf.setFontSize(15);
  pdf.text("ACADEMIC YEAR RESULTS REGISTER", 105, 42, { align: "center" });
  pdf.setFillColor(18, 91, 145);
  pdf.rect(91, 46, 28, 0.8, "F");
  pdf.setTextColor(94, 113, 130);
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(8.5);
  pdf.text(`${result.programName} · ${result.academicLevelName} · ${result.academicYearLabel}`, 105, 53, { align: "center" });
  const fields = [["Programme", result.programName], ["Path", result.programPathName], ["Level", result.academicLevelName], ["Academic year", result.academicYearLabel]];
  fields.forEach(([label, value], index) => {
    const x = 14 + index * 45.5;
    pdf.setTextColor(94, 113, 130);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(7);
    pdf.text(label.toUpperCase(), x, 68);
    pdf.setTextColor(23, 58, 85);
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(8.5);
    pdf.text(value, x, 74, { maxWidth: 41 });
  });
  pdf.setDrawColor(198, 211, 222);
  pdf.line(14, 82, 196, 82);
}

function drawAnnualDecision(pdf: import("jspdf").jsPDF, result: ProgressionDecision, y: number) {
  const safeY = y > 205 ? (pdf.addPage(), 24) : y;
  pdf.setDrawColor(198, 211, 222);
  pdf.setFillColor(242, 247, 250);
  pdf.roundedRect(14, safeY, 182, 30, 1.5, 1.5, "FD");
  pdf.setTextColor(94, 113, 130);
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(7.3);
  pdf.text("ANNUAL AVERAGE", 21, safeY + 8);
  pdf.text("PROGRESSION DECISION", 104, safeY + 8);
  pdf.setTextColor(23, 58, 85);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(11);
  pdf.text(`${result.annualAverage.toFixed(2)} / 20`, 21, safeY + 19);
  pdf.text(decisionLabels[result.decisionStatus], 104, safeY + 19, { maxWidth: 85 });
  return safeY + 34;
}

export async function saveStudentAcademicYearResultPdf(result: ProgressionDecision) {
  const { jsPDF } = await import("jspdf");
  const pdf = new jsPDF({ orientation: "portrait", unit: "mm", format: "a4" });
  const widths = [25, 78, 25, 27, 28];
  let y = 111;
  result.semesterResults.forEach((semester, semesterIndex) => {
    if (semesterIndex > 0) {
      pdf.addPage();
    }
    drawAcademicDocumentHeader(pdf, {
      academicLevel: result.academicLevelName,
      academicYear: result.academicYearLabel,
      apogeeCode: result.apogeeCode,
      cin: result.cin,
      cne: result.nationalStudentCode,
      context: `${result.programName} · ${result.academicLevelName} · ${semester.semesterName} · ${result.academicYearLabel}`,
      firstName: result.firstName,
      lastName: result.lastName,
      program: result.programName,
      programPath: result.programPathName,
      reference: `AYR-${result.apogeeCode}-${result.academicYearLabel}`,
      semester: semester.semesterName,
      title: "Academic Year Result Statement",
    });
    y = 111;
    const drawSemesterHeader = (continued = false) => {
      pdf.setTextColor(23, 58, 85);
      pdf.setFont("helvetica", "bold");
      pdf.setFontSize(10.5);
      pdf.text(`${semester.semesterName} result${continued ? " (continued)" : ""}`, 14, y);
      pdf.setFontSize(8.5);
      pdf.text(`${semester.semesterAverage.toFixed(2)} / 20 · ${semesterStatus(semester.resultStatus)}`, 196, y, { align: "right" });
      y = drawAcademicTableHeader(pdf, y + 5, ["Module", "Title", "Grade", "Result", "Inscription"], widths);
    };
    drawSemesterHeader();
    semester.moduleResults.forEach((module, index) => {
      if (y + 10 > 270) {
        pdf.addPage();
        y = 20;
        drawSemesterHeader(true);
      }
      y = drawAcademicTableRow(pdf, y, [module.subjectModuleCode, module.subjectModuleTitle, module.finalGrade.toFixed(2), module.resultStatus, String(module.inscriptionNumber)], widths, index % 2 === 1);
    });
    y += 6;
  });
  y = drawAnnualDecision(pdf, result, y);
  drawAcademicDocumentClosing(pdf, y);
  addAcademicDocumentFooters(pdf);
  pdf.save(`${result.apogeeCode.toLowerCase()}-${result.academicYearLabel}-academic-year-result.pdf`);
}

export async function saveAllAcademicYearResultsPdf(results: ProgressionDecision[]) {
  if (!results.length) return;
  const { jsPDF } = await import("jspdf");
  const pdf = new jsPDF({ orientation: "portrait", unit: "mm", format: "a4" });
  const first = results[0];
  drawCohortHeader(pdf, first);
  const widths = [10, 46, 25, 22, 22, 22, 36];
  const labels = ["No.", "Student", "Apogee", "Semester 1", "Semester 2", "Annual", "Decision"];
  let y = drawAcademicTableHeader(pdf, 111, labels, widths);
  results.forEach((result, index) => {
    if (y > 269) {
      pdf.addPage();
      y = drawAcademicTableHeader(pdf, 16, labels, widths);
    }
    const semesters = [...result.semesterResults].sort((left, right) => left.semesterOrder - right.semesterOrder);
    y = drawAcademicTableRow(pdf, y, [
      String(index + 1),
      `${result.lastName} ${result.firstName}`,
      result.apogeeCode,
      semesters[0] ? `${semesters[0].semesterAverage.toFixed(2)} ${semesters[0].resultStatus === "VALIDATED" ? "V" : "NV"}` : "-",
      semesters[1] ? `${semesters[1].semesterAverage.toFixed(2)} ${semesters[1].resultStatus === "VALIDATED" ? "V" : "NV"}` : "-",
      result.annualAverage.toFixed(2),
      decisionLabels[result.decisionStatus],
    ], widths, index % 2 === 1);
  });
  addAcademicDocumentFooters(pdf);
  pdf.save(`${first.programName.toLowerCase().replaceAll(/[^a-z0-9]+/g, "-")}-${first.academicLevelName.toLowerCase()}-${first.academicYearLabel}-results.pdf`);
}
