export async function saveSchedulePdf(root: HTMLElement, contentSelector: string, fileName: string) {
  const header = root.querySelector<HTMLElement>(".print-schedule-header");
  const content = root.querySelector<HTMLElement>(contentSelector);
  if (!header || !content) return;

  const exportDocument = document.createElement("article");
  exportDocument.className = "pdf-export-document";
  exportDocument.append(header.cloneNode(true), content.cloneNode(true));
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

    const pdf = new jsPDF({ orientation: "landscape", unit: "mm", format: "a4" });
    const margin = 8;
    const availableWidth = pdf.internal.pageSize.getWidth() - margin * 2;
    const availableHeight = pdf.internal.pageSize.getHeight() - margin * 2;
    const scale = Math.min(availableWidth / canvas.width, availableHeight / canvas.height);
    const width = canvas.width * scale;
    const height = canvas.height * scale;
    pdf.addImage(
      canvas.toDataURL("image/png"),
      "PNG",
      (pdf.internal.pageSize.getWidth() - width) / 2,
      margin,
      width,
      height,
      undefined,
      "FAST",
    );
    pdf.save(fileName);
  } finally {
    exportDocument.remove();
  }
}
