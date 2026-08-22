import type { WeeklyTimetableEntry } from "@/features/scheduling/components/WeeklyTimetable";
import type { StudentExamInvitation } from "../api/student-overview-api";

interface ScheduleDocumentContext {
  academicYear: string;
  semester: string;
}

const days = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SUNDAY"] as const;
const dayLabels = { MONDAY: "Monday", TUESDAY: "Tuesday", WEDNESDAY: "Wednesday", THURSDAY: "Thursday", FRIDAY: "Friday", SUNDAY: "Sunday" } as const;

function minutes(value: string) {
  const [hour, minute] = value.split(":").map(Number);
  return hour * 60 + minute;
}

function drawHeader(pdf: import("jspdf").jsPDF, title: string, context: ScheduleDocumentContext) {
  const pageWidth = pdf.internal.pageSize.getWidth();
  pdf.setTextColor(23, 50, 74);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(15);
  pdf.text("Universite Ibn Zohr", 10, 13);
  pdf.setFontSize(13);
  pdf.text(title, pageWidth - 10, 13, { align: "right" });
  pdf.setFont("helvetica", "normal");
  pdf.setFontSize(9);
  pdf.setTextColor(80, 104, 122);
  pdf.text(`${context.semester} - ${context.academicYear}`, pageWidth - 10, 19, { align: "right" });
  pdf.setDrawColor(23, 50, 74);
  pdf.setLineWidth(0.6);
  pdf.line(10, 23, pageWidth - 10, 23);
}

export async function saveStudentClassSchedulePdf(entries: WeeklyTimetableEntry[], context: ScheduleDocumentContext) {
  const { jsPDF } = await import("jspdf");
  const pdf = new jsPDF({ orientation: "landscape", unit: "mm", format: "a4" });
  drawHeader(pdf, "Student Class Schedule", context);

  const left = 10;
  const top = 30;
  const dayWidth = 25;
  const timelineWidth = pdf.internal.pageSize.getWidth() - left * 2 - dayWidth;
  const headerHeight = 10;
  const rowHeight = 25;
  const startMinute = 8 * 60;
  const endMinute = 18 * 60 + 30;
  const duration = endMinute - startMinute;

  pdf.setFillColor(25, 68, 104);
  pdf.rect(left, top, dayWidth + timelineWidth, headerHeight, "F");
  pdf.setDrawColor(93, 117, 136);
  pdf.setLineWidth(0.35);
  pdf.rect(left, top, dayWidth + timelineWidth, headerHeight + rowHeight * days.length);
  pdf.line(left + dayWidth, top, left + dayWidth, top + headerHeight + rowHeight * days.length);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(8);
  pdf.setTextColor(255, 255, 255);
  pdf.text("Days", left + dayWidth / 2, top + 6.5, { align: "center" });

  for (let hour = 8; hour <= 18; hour += 1) {
    const x = left + dayWidth + ((hour * 60 - startMinute) / duration) * timelineWidth;
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(7);
    pdf.setTextColor(255, 255, 255);
    pdf.text(`${hour}h`, x + 1, top + 6.5);
  }

  days.forEach((day, dayIndex) => {
    const y = top + headerHeight + dayIndex * rowHeight;
    pdf.setFillColor(255, 255, 255);
    pdf.rect(left, y, dayWidth + timelineWidth, rowHeight, "F");
    pdf.setFillColor(242, 246, 249);
    pdf.rect(left, y, dayWidth, rowHeight, "F");
    pdf.setDrawColor(177, 193, 205);
    pdf.setLineWidth(0.2);
    pdf.line(left, y, left + dayWidth + timelineWidth, y);
    pdf.line(left + dayWidth, y, left + dayWidth, y + rowHeight);

    for (let hour = 8; hour <= 18; hour += 1) {
      const x = left + dayWidth + ((hour * 60 - startMinute) / duration) * timelineWidth;
      pdf.setDrawColor(221, 228, 233);
      pdf.setLineWidth(0.12);
      pdf.line(x, y, x, y + rowHeight);
    }
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(8);
    pdf.setTextColor(23, 50, 74);
    pdf.text(dayLabels[day], left + dayWidth / 2, y + rowHeight / 2 + 1, { align: "center" });

    entries.filter((entry) => entry.dayOfWeek === day).forEach((entry) => {
      const x = left + dayWidth + ((minutes(entry.startTime) - startMinute) / duration) * timelineWidth;
      const width = Math.max(17, ((minutes(entry.endTime) - minutes(entry.startTime)) / duration) * timelineWidth);
      const palette = entry.componentType === "TP"
        ? { fill: [235, 246, 249], accent: [35, 112, 139] }
        : entry.componentType === "TD"
          ? { fill: [252, 246, 230], accent: [169, 113, 29] }
          : { fill: [237, 246, 239], accent: [45, 113, 75] };
      const cardWidth = Math.min(width - 1.8, left + dayWidth + timelineWidth - x - 1);
      pdf.setFillColor(palette.fill[0], palette.fill[1], palette.fill[2]);
      pdf.setDrawColor(183, 198, 208);
      pdf.setLineWidth(0.15);
      pdf.roundedRect(x + 0.9, y + 1.8, cardWidth, rowHeight - 3.6, 0.8, 0.8, "FD");
      pdf.setFillColor(palette.accent[0], palette.accent[1], palette.accent[2]);
      pdf.rect(x + 0.9, y + 1.8, 1, rowHeight - 3.6, "F");
      pdf.setTextColor(23, 50, 74);
      pdf.setFont("helvetica", "bold");
      pdf.setFontSize(7);
      pdf.text(pdf.splitTextToSize(entry.title, Math.max(12, cardWidth - 5)).slice(0, 2), x + 3, y + 6);
      pdf.setFont("helvetica", "normal");
      pdf.setFontSize(5.8);
      pdf.setTextColor(65, 87, 104);
      pdf.text(pdf.splitTextToSize(entry.detail, Math.max(12, cardWidth - 5)).slice(0, 2), x + 3, y + 13);
      pdf.setFont("helvetica", "bold");
      pdf.text(`${entry.room} | ${entry.startTime.slice(0, 5)}-${entry.endTime.slice(0, 5)}`, x + 3, y + 21);
    });
  });
  pdf.setDrawColor(177, 193, 205);
  pdf.setLineWidth(0.2);
  pdf.line(left, top + headerHeight + rowHeight * days.length, left + dayWidth + timelineWidth, top + headerHeight + rowHeight * days.length);
  pdf.setDrawColor(93, 117, 136);
  pdf.setLineWidth(0.35);
  pdf.rect(left, top, dayWidth + timelineWidth, headerHeight + rowHeight * days.length);

  pdf.save(`class-schedule-${context.academicYear}-${context.semester}.pdf`.toLowerCase());
}

export async function saveStudentExamSchedulePdf(exams: StudentExamInvitation[], context: ScheduleDocumentContext, session: string) {
  const { jsPDF } = await import("jspdf");
  const pdf = new jsPDF({ orientation: "landscape", unit: "mm", format: "a4" });
  drawHeader(pdf, session === "RATTRAPAGE" ? "Rattrapage Exam Schedule" : "Normal Exam Schedule", context);
  const columns = [10, 47, 132, 174, 218, 287];
  let y = 31;

  pdf.setFillColor(23, 74, 120);
  pdf.rect(columns[0], y, columns.at(-1)! - columns[0], 10, "F");
  ["Day", "Module", "Time", "Room", "Group"].forEach((label, index) => {
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(8);
    pdf.setTextColor(255, 255, 255);
    pdf.text(label, columns[index] + 2, y + 6.5);
  });
  y += 10;

  [...exams].sort((left, right) => left.examDate.localeCompare(right.examDate) || left.startTime.localeCompare(right.startTime)).forEach((exam, rowIndex) => {
    const date = new Intl.DateTimeFormat("en-GB", { weekday: "short", day: "2-digit", month: "2-digit", year: "numeric" }).format(new Date(`${exam.examDate}T00:00:00`));
    if (rowIndex % 2 === 0) {
      pdf.setFillColor(247, 250, 252);
      pdf.rect(columns[0], y, columns.at(-1)! - columns[0], 12, "F");
    }
    pdf.setDrawColor(205, 220, 230);
    pdf.setLineWidth(0.2);
    pdf.line(columns[0], y, columns.at(-1)!, y);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(8);
    pdf.setTextColor(23, 50, 74);
    const values = [date, `${exam.subjectModuleCode} - ${exam.subjectModuleTitle}`, `${exam.startTime.slice(0, 5)} - ${exam.endTime?.slice(0, 5) ?? "-"}`, exam.roomCode ?? "Not assigned", exam.examGroupLabel ?? "Assigned candidate"];
    values.forEach((value, index) => pdf.text(pdf.splitTextToSize(value, columns[index + 1] - columns[index] - 4).slice(0, 2), columns[index] + 2, y + 6));
    y += 12;
  });
  pdf.setDrawColor(151, 173, 188);
  pdf.setLineWidth(0.3);
  pdf.line(columns[0], y, columns.at(-1)!, y);
  columns.forEach((x) => pdf.line(x, 31, x, y));
  pdf.save(`exam-schedule-${session.toLowerCase()}-${context.academicYear}-${context.semester}.pdf`.toLowerCase());
}
