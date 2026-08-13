import { afterEach, describe, expect, it, vi } from "vitest";
import { getGradeSheet, saveGradeSheet } from "./professor-grades-api";

const examId = "00000000-0000-4000-8000-000000000001";
const registrationId = "00000000-0000-4000-8000-000000000002";

const gradeSheet = {
  moduleExamId: examId,
  subjectModuleId: "00000000-0000-4000-8000-000000000003",
  classGroupId: "00000000-0000-4000-8000-000000000004",
  workflowStatus: "DRAFT",
  grades: [{
    gradeRecordId: null,
    moduleRegistrationId: registrationId,
    studentId: "00000000-0000-4000-8000-000000000005",
    apogeeCode: "22000123",
    universityEmail: "student@uiz.ac.ma",
    firstName: "Salma",
    lastName: "Amrani",
    inscriptionNumber: 1,
    gradeValue: null,
    zeroGradeReason: null,
    workflowStatus: "DRAFT",
    publishedAt: null,
  }],
};

describe("Professor grades API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads the complete grade roster for an exam", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(gradeSheet), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getGradeSheet(examId)).resolves.toEqual(gradeSheet);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain(`/api/v1/module-exams/${examId}/grade-sheet`);
  });

  it("sends the complete draft as one bulk request", async () => {
    const savedSheet = {
      ...gradeSheet,
      grades: [{ ...gradeSheet.grades[0], gradeRecordId: "00000000-0000-4000-8000-000000000006", gradeValue: 15.5 }],
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(savedSheet), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    }));
    vi.stubGlobal("fetch", fetchMock);

    await saveGradeSheet(examId, [{ moduleRegistrationId: registrationId, gradeValue: 15.5 }]);

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("PUT");
    await expect(request.clone().json()).resolves.toEqual({
      grades: [{ moduleRegistrationId: registrationId, gradeValue: 15.5 }],
    });
  });
});
