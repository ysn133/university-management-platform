import { afterEach, describe, expect, it, vi } from "vitest";
import { getMyModuleResponsibilities } from "./professor-overview-api";

describe("Professor overview API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads module responsibilities for the authenticated Professor", async () => {
    const responsibilities = [{
      id: "00000000-0000-4000-8000-000000000001",
      subjectModuleId: "00000000-0000-4000-8000-000000000002",
      subjectModuleCode: "ALG",
      subjectModuleTitle: "Algorithms",
      classGroupId: "00000000-0000-4000-8000-000000000003",
      classGroupName: "Group A",
      academicYearId: "00000000-0000-4000-8000-000000000004",
      academicYearLabel: "2026-2027",
      semesterId: "00000000-0000-4000-8000-000000000005",
      semesterName: "S1",
      status: "ACTIVE",
    }];
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(responsibilities), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getMyModuleResponsibilities()).resolves.toEqual(responsibilities);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain("/api/v1/me/module-class-responsibilities");
  });
});
