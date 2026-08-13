export async function saveCandidateListPdf(documentElement: HTMLElement, fileName: string, fitSinglePage = false) {
  const exportDocument = documentElement.cloneNode(true) as HTMLElement;
  exportDocument.classList.add("pdf-candidate-page");
  document.body.append(exportDocument);

  try {
    await document.fonts.ready;
    const [{ default: html2canvas }, { jsPDF }] = await Promise.all([
      import("html2canvas"),
      import("jspdf"),
    ]);
    const canvas = await html2canvas(exportDocument, {
      backgroundColor: "#ffffff",
      logging: false,
      scale: 2,
      useCORS: true,
      width: exportDocument.scrollWidth,
      height: exportDocument.scrollHeight,
      windowWidth: exportDocument.scrollWidth,
    });
    const pdf = new jsPDF({ orientation: "portrait", unit: "mm", format: "a4" });
    const margin = 10;
    const availableWidth = pdf.internal.pageSize.getWidth() - margin * 2;
    const availableHeight = pdf.internal.pageSize.getHeight() - margin * 2;
    const naturalHeight = canvas.height * (availableWidth / canvas.width);
    const scale = fitSinglePage ? Math.min(1, availableHeight / naturalHeight) : 1;
    const width = availableWidth * scale;
    const height = naturalHeight * scale;
    const pageCount = Math.max(1, Math.ceil(height / availableHeight));

    for (let page = 0; page < pageCount; page += 1) {
      if (page > 0) pdf.addPage();
      pdf.addImage(canvas.toDataURL("image/png"), "PNG", margin, margin - page * availableHeight, width, height, undefined, "FAST");
      pdf.setFillColor(255, 255, 255);
      pdf.rect(0, pdf.internal.pageSize.getHeight() - margin, pdf.internal.pageSize.getWidth(), margin, "F");
      pdf.setFontSize(8);
      pdf.setTextColor(90);
      pdf.text(`${page + 1} / ${pageCount}`, pdf.internal.pageSize.getWidth() - margin, pdf.internal.pageSize.getHeight() - 5, { align: "right" });
    }

    pdf.save(fileName);
  } finally {
    exportDocument.remove();
  }
}

export async function saveExamGroupRosterPdf(documentElement: HTMLElement, fileName: string) {
  const header = documentElement.querySelector<HTMLElement>(":scope > header");
  const groups = Array.from(documentElement.querySelectorAll<HTMLElement>(":scope > .exam-candidate-group"));
  if (!header || !groups.length) return;

  const [{ default: html2canvas }, { jsPDF }] = await Promise.all([
    import("html2canvas"),
    import("jspdf"),
  ]);
  const pdf = new jsPDF({ orientation: "portrait", unit: "mm", format: "a4" });
  const margin = 10;

  for (let index = 0; index < groups.length; index += 1) {
    const page = document.createElement("article");
    page.className = "exam-candidate-document pdf-candidate-page";
    page.append(header.cloneNode(true), groups[index].cloneNode(true));
    document.body.append(page);
    try {
      await document.fonts.ready;
      const canvas = await html2canvas(page, { backgroundColor: "#ffffff", logging: false, scale: 2, useCORS: true });
      const availableWidth = pdf.internal.pageSize.getWidth() - margin * 2;
      const availableHeight = pdf.internal.pageSize.getHeight() - margin * 2;
      const naturalHeight = canvas.height * (availableWidth / canvas.width);
      const scale = Math.min(1, availableHeight / naturalHeight);
      if (index > 0) pdf.addPage();
      pdf.addImage(canvas.toDataURL("image/png"), "PNG", margin, margin, availableWidth * scale, naturalHeight * scale, undefined, "FAST");
      pdf.setFontSize(8);
      pdf.setTextColor(90);
      pdf.text(`${index + 1} / ${groups.length}`, pdf.internal.pageSize.getWidth() - margin, pdf.internal.pageSize.getHeight() - 5, { align: "right" });
    } finally {
      page.remove();
    }
  }

  pdf.save(fileName);
}
