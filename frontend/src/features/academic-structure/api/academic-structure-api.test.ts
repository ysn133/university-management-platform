import { afterEach, describe, expect, it, vi } from "vitest";
import { createAcademicYear, createProgramFiliere, getDepartments } from "./academic-structure-api";

const establishmentId = "00000000-0000-4000-8000-000000000001";
const departmentId = "00000000-0000-4000-8000-000000000002";

describe("academic structure API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads departments in establishment scope", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([{
      id: departmentId,
      establishmentId,
      name: "Computer Science",
    }]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getDepartments(establishmentId)).resolves.toEqual([{
      id: departmentId,
      establishmentId,
      name: "Computer Science",
    }]);
    expect(fetchMock.mock.calls[0][0].url).toContain(`/api/v1/establishments/${establishmentId}/departments`);
  });

  it("creates an academic year with its lifecycle status", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      id: "00000000-0000-4000-8000-000000000003",
      establishmentId,
      label: "2026-2027",
      startYear: 2026,
      endYear: 2027,
      status: "PLANNED",
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await createAcademicYear(establishmentId, { label: "2026-2027", status: "PLANNED" });

    const submittedRequest = fetchMock.mock.calls[0][0] as Request;
    await expect(submittedRequest.clone().json()).resolves.toEqual({ label: "2026-2027", status: "PLANNED" });
  });

  it("creates a program under the selected department", async () => {
    const programRequest = {
      code: "IL",
      name: "Software Engineering",
      degreeCycleId: "00000000-0000-4000-8000-000000000004",
      programPathId: "00000000-0000-4000-8000-000000000005",
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      id: "00000000-0000-4000-8000-000000000006",
      departmentId,
      establishmentId,
      ...programRequest,
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await createProgramFiliere(departmentId, programRequest);

    expect(fetchMock.mock.calls[0][0].url).toContain(`/api/v1/departments/${departmentId}/program-filieres`);
    const submittedRequest = fetchMock.mock.calls[0][0] as Request;
    await expect(submittedRequest.clone().json()).resolves.toEqual(programRequest);
  });
});
