import type { GraduationDecision } from "../api/graduation-decisions-api";
import { addAcademicDocumentFooters } from "./academic-document-pdf";

export async function saveGraduationDecisionPdf(decision: GraduationDecision) {
  const { jsPDF } = await import("jspdf");
  const pdf = new jsPDF({ orientation: "portrait", unit: "mm", format: "a4" });
  pdf.setProperties({
    author: "Universite Ibn Zohr",
    creator: "University Management Platform",
    subject: "Graduation Acknowledgment",
    title: "Graduation Acknowledgment",
  });

  pdf.setFillColor(18, 91, 145);
  pdf.rect(0, 0, 210, 5, "F");
  pdf.roundedRect(14, 12, 17, 14, 1.5, 1.5, "F");
  pdf.setTextColor(255, 255, 255);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(10);
  pdf.text("UIZ", 22.5, 20.8, { align: "center" });
  pdf.setTextColor(23, 58, 85);
  pdf.setFontSize(12);
  pdf.text("UNIVERSITE IBN ZOHR", 36, 17);
  pdf.setFont("helvetica", "normal");
  pdf.setTextColor(94, 113, 130);
  pdf.setFontSize(7.5);
  pdf.text("ACADEMIC ADMINISTRATION", 36, 22);
  pdf.setFontSize(7);
  pdf.text("DOCUMENT REFERENCE", 196, 16, { align: "right" });
  pdf.setTextColor(23, 58, 85);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(8);
  pdf.text(`GRD-${decision.apogeeCode}-${decision.academicYearLabel}`, 196, 21, { align: "right" });
  pdf.setDrawColor(198, 211, 222);
  pdf.line(14, 33, 196, 33);

  pdf.setFontSize(15);
  pdf.text("GRADUATION DECISION", 105, 50, { align: "center" });
  pdf.setFillColor(18, 91, 145);
  pdf.rect(92, 55, 26, 0.6, "F");

  const fields = [
    ["Student", `${decision.firstName} ${decision.lastName}`],
    ["Apogee", decision.apogeeCode],
    ["CNE", decision.nationalStudentCode ?? "Not provided"],
    ["CIN", decision.cin ?? "Not provided"],
    ["Program / Filiere", decision.programName],
    ["Program path", decision.programPathName],
    ["Degree cycle", decision.degreeCycleName],
    ["Academic year", decision.academicYearLabel],
  ];
  fields.forEach(([label, value], index) => {
    const column = index % 2;
    const row = Math.floor(index / 2);
    const x = column === 0 ? 20 : 108;
    const y = 72 + row * 11;
    pdf.setTextColor(94, 113, 130);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(7);
    pdf.text(label.toUpperCase(), x, y);
    pdf.setTextColor(23, 58, 85);
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(8.5);
    pdf.text(value, x, y + 5, { maxWidth: 78 });
  });

  pdf.setDrawColor(198, 211, 222);
  pdf.line(20, 118, 190, 118);
  pdf.setTextColor(23, 58, 85);
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(10);
  const statement = `Following review of the academic record, the academic administration certifies that ${decision.firstName} ${decision.lastName} has fulfilled the requirements of the ${decision.degreeCycleName} in ${decision.programName}.`;
  pdf.text(pdf.splitTextToSize(statement, 170), 20, 135, { lineHeightFactor: 1.55 });

  pdf.setFillColor(247, 249, 251);
  pdf.setDrawColor(198, 211, 222);
  pdf.rect(20, 164, 170, 20, "FD");
  pdf.setTextColor(23, 58, 85);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(8);
  pdf.text("ACADEMIC DECISION", 26, 171.5);
  pdf.text("FINAL AVERAGE", 132, 171.5);
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(10);
  pdf.text("Graduated", 26, 179.5);
  pdf.setTextColor(23, 58, 85);
  pdf.text(`${decision.graduationAverage.toFixed(2)} / 20`, 132, 179.5);

  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(8);
  pdf.setTextColor(94, 113, 130);
  pdf.text(`Issued on ${new Date(decision.decidedAt).toLocaleDateString("en-GB")}`, 20, 204);
  pdf.setTextColor(23, 58, 85);
  pdf.setFont("helvetica", "bold");
  pdf.text("Academic Administration", 164, 230, { align: "center" });
  pdf.setDrawColor(94, 113, 130);
  pdf.line(137, 221, 191, 221);
  addAcademicDocumentFooters(pdf);
  pdf.save(`${decision.apogeeCode.toLowerCase()}-graduation-acknowledgment.pdf`);
}
