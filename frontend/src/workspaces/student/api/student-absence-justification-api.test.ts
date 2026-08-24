import { afterEach, describe, expect, it, vi } from "vitest";
import { submitAbsenceJustification } from "./student-absence-justification-api";

describe("student absence justification API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("uploads evidence before linking it to the absence", async () => {
    const justification = {
      id: "00000000-0000-4000-8000-000000000001", absenceId: "00000000-0000-4000-8000-000000000002",
      teachingAssignmentId: "00000000-0000-4000-8000-000000000003", studentId: "00000000-0000-4000-8000-000000000004",
      studentApogeeCode: "2601001", studentFirstName: "Salma", studentLastName: "Bennani",
      subjectModuleId: "00000000-0000-4000-8000-000000000005", subjectModuleCode: "ALG", subjectModuleTitle: "Algorithms",
      absenceDate: "2026-10-10", reason: "Medical appointment", status: "PENDING",
      documentId: "00000000-0000-4000-8000-000000000006", documentFileName: "certificate.pdf", documentContentType: "application/pdf",
      decisionNote: null, submittedAt: "2026-10-11T10:00:00Z", reviewedAt: null,
    };
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify({ documentId: justification.documentId }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify(justification), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(submitAbsenceJustification(justification.absenceId, justification.reason, new File(["%PDF"], "certificate.pdf", { type: "application/pdf" }))).resolves.toEqual(justification);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain("/api/v1/documents?purpose=ABSENCE_JUSTIFICATION");
    expect((fetchMock.mock.calls[1][0] as Request).url).toContain(`/api/v1/absences/${justification.absenceId}/justifications`);
  });
});
