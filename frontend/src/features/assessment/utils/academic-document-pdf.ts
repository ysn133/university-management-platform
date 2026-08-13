import type { jsPDF } from "jspdf";

const BLUE = [18, 91, 145] as const;
const NAVY = [23, 58, 85] as const;
const MUTED = [94, 113, 130] as const;
const LINE = [198, 211, 222] as const;

function displayValue(value?: string | null) {
  if (!value) return "Not provided";
  return value.replace(/_/g, " ").replace(/\b\w/g, (letter) => letter.toUpperCase());
}

export interface AcademicDocumentIdentity {
  apogeeCode: string;
  academicLevel?: string;
  academicYear?: string;
  cin?: string | null;
  context: string;
  cne?: string | null;
  firstName: string;
  lastName: string;
  program?: string;
  programPath?: string;
  reference: string;
  semester?: string;
  title: string;
}

export function drawAcademicDocumentHeader(pdf: jsPDF, identity: AcademicDocumentIdentity) {
  pdf.setProperties({ author: "Universite Ibn Zohr", creator: "University Management Platform", subject: identity.title, title: identity.title });
  pdf.setFillColor(...BLUE);
  pdf.rect(0, 0, 210, 5, "F");
  pdf.setDrawColor(...LINE);
  pdf.line(14, 30, 196, 30);

  pdf.setFillColor(...BLUE);
  pdf.roundedRect(14, 10, 17, 14, 1.5, 1.5, "F");
  pdf.setTextColor(255, 255, 255);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(10);
  pdf.text("UIZ", 22.5, 18.8, { align: "center" });

  pdf.setTextColor(...NAVY);
  pdf.setFontSize(12);
  pdf.text("UNIVERSITE IBN ZOHR", 36, 15);
  pdf.setFont("helvetica", "normal");
  pdf.setTextColor(...MUTED);
  pdf.setFontSize(7.5);
  pdf.text("ACADEMIC ADMINISTRATION", 36, 20);

  pdf.setTextColor(...MUTED);
  pdf.setFontSize(7);
  pdf.text("DOCUMENT REFERENCE", 196, 14, { align: "right" });
  pdf.setTextColor(...NAVY);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(8);
  pdf.text(identity.reference, 196, 19, { align: "right" });

  pdf.setFontSize(15);
  pdf.text(identity.title.toUpperCase(), 105, 42, { align: "center" });
  pdf.setFillColor(...BLUE);
  pdf.rect(91, 46, 28, 0.8, "F");
  pdf.setTextColor(...MUTED);
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(8.5);
  pdf.text(identity.context, 105, 53, { align: "center", maxWidth: 170 });

  const leftFields = [
    ["Student", `${identity.lastName.toUpperCase()} ${identity.firstName}`],
    ["Apogee", identity.apogeeCode],
    ["CNE", identity.cne || "Not provided"],
    ["CIN", identity.cin || "Not provided"],
  ];
  const rightFields = [
    ["Filiere", displayValue(identity.program)],
    ["Path", displayValue(identity.programPath)],
    ["Academic level", displayValue(identity.academicLevel)],
    ["Semester", displayValue(identity.semester)],
    ["Academic year", displayValue(identity.academicYear)],
  ];
  const drawField = (label: string, value: string, x: number, y: number, valueX: number, maxWidth: number) => {
    pdf.setFont("helvetica", "bold");
    pdf.setTextColor(...NAVY);
    pdf.setFontSize(8.2);
    pdf.text(`${label}:`, x, y);
    pdf.setFont("helvetica", "normal");
    pdf.text(value, valueX, y, { maxWidth });
  };
  leftFields.forEach(([label, value], index) => drawField(label, value, 14, 66 + index * 8, 38, 57));
  rightFields.forEach(([label, value], index) => drawField(label, value, 108, 66 + index * 8, 137, 59));
  pdf.setDrawColor(...LINE);
  pdf.line(14, 103, 196, 103);
}

export function drawAcademicTableHeader(pdf: jsPDF, y: number, labels: string[], widths: number[]) {
  pdf.setFillColor(...NAVY);
  pdf.rect(14, y, widths.reduce((sum, width) => sum + width, 0), 9, "F");
  pdf.setTextColor(255, 255, 255);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(7.5);
  let x = 14;
  labels.forEach((label, index) => { pdf.text(label.toUpperCase(), x + 2, y + 5.8); x += widths[index]; });
  return y + 9;
}

export function drawAcademicTableRow(pdf: jsPDF, y: number, values: string[], widths: number[], shaded: boolean, dangerColumns: number[] = []) {
  const totalWidth = widths.reduce((sum, width) => sum + width, 0);
  if (shaded) { pdf.setFillColor(247, 250, 252); pdf.rect(14, y, totalWidth, 10, "F"); }
  pdf.setDrawColor(...LINE);
  pdf.rect(14, y, totalWidth, 10);
  pdf.setTextColor(...NAVY);
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(8);
  let x = 14;
  values.forEach((value, index) => {
    if (index > 0) pdf.line(x, y, x, y + 10);
    const isDanger = dangerColumns.includes(index);
    if (isDanger) pdf.setTextColor(180, 55, 55);
    else pdf.setTextColor(...NAVY);
    pdf.setFont("helvetica", isDanger ? "bold" : "normal");
    pdf.text(value, x + 2, y + 6.3, { maxWidth: widths[index] - 4 });
    x += widths[index];
  });
  return y + 10;
}

export function drawAcademicDocumentClosing(pdf: jsPDF, y: number) {
  const closingY = Math.max(y + 14, 224);
  pdf.setTextColor(...MUTED);
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(7.5);
  pdf.text(`Issued on ${new Date().toLocaleDateString("en-GB")}`, 14, closingY);
  pdf.setDrawColor(...LINE);
  pdf.roundedRect(132, closingY - 5, 64, 28, 1.5, 1.5);
  pdf.setTextColor(...NAVY);
  pdf.setFont("helvetica", "bold");
  pdf.text("ACADEMIC ADMINISTRATION", 164, closingY + 2, { align: "center" });
  pdf.setFont("helvetica", "normal");
  pdf.setTextColor(...MUTED);
  pdf.text("Authorized signature and stamp", 164, closingY + 16, { align: "center" });
}

export function addAcademicDocumentFooters(pdf: jsPDF) {
  const pages = pdf.getNumberOfPages();
  for (let page = 1; page <= pages; page += 1) {
    pdf.setPage(page);
    pdf.setDrawColor(...LINE);
    pdf.line(14, 284, 196, 284);
    pdf.setTextColor(...MUTED);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(6.8);
    pdf.text("This document was generated by the Universite Ibn Zohr academic management platform.", 14, 289);
    pdf.text(`Page ${page} / ${pages}`, 196, 289, { align: "right" });
  }
}
