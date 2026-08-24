import { afterEach, describe, expect, it, vi } from "vitest";
import { getMyModuleRegistrations } from "./student-overview-api";

describe("student overview API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads the authenticated student's module registrations", async () => {
    const registration = {
      moduleRegistrationId: "00000000-0000-4000-8000-000000000001",
      subjectModuleId: "00000000-0000-4000-8000-000000000002",
      subjectModuleCode: "ALG",
      subjectModuleTitle: "Algorithms",
      academicRegistrationId: "00000000-0000-4000-8000-000000000003",
      semesterRegistrationId: "00000000-0000-4000-8000-000000000004",
      academicYearId: "00000000-0000-4000-8000-000000000005",
      academicYearLabel: "2026-2027",
      academicYearStatus: "ACTIVE",
      programPathId: "00000000-0000-4000-8000-000000000006",
      programPathName: "Excellence",
      programFiliereId: "00000000-0000-4000-8000-000000000007",
      programFiliereCode: "IL",
      programFiliereName: "Software Engineering",
      academicLevelId: "00000000-0000-4000-8000-000000000008",
      academicLevelName: "M1",
      semesterId: "00000000-0000-4000-8000-000000000009",
      semesterName: "S1",
      semesterStartDate: "2026-09-01",
      semesterEndDate: "2027-01-31",
      originAcademicLevelId: null,
      originAcademicLevelName: null,
      inscriptionNumber: 1,
      status: "ACTIVE",
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([registration]), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getMyModuleRegistrations()).resolves.toEqual([registration]);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain("/api/v1/me/module-registrations");
  });
});
