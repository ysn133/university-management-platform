import { afterEach, describe, expect, it, vi } from "vitest";
import { generateTeachingPlan, getTeachingPlan } from "./teaching-plan-api";

const semesterId = "00000000-0000-4000-8000-000000000001";
const plan = [{
  id: "00000000-0000-4000-8000-000000000002",
  subjectModuleId: "00000000-0000-4000-8000-000000000003",
  moduleTeachingComponentId: "00000000-0000-4000-8000-000000000004",
  componentType: "TD",
  teachingGroupId: "00000000-0000-4000-8000-000000000005",
  teachingGroupName: "A",
  audienceType: "CLASS_GROUP",
  status: "ACTIVE",
}];

describe("teaching plan API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads the semester Teaching Plan", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(plan), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getTeachingPlan(semesterId)).resolves.toEqual(plan);

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("GET");
    expect(request.url).toContain(`/api/v1/semesters/${semesterId}/teaching-requirements`);
  });

  it("generates the semester Teaching Plan", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(plan), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(generateTeachingPlan(semesterId)).resolves.toEqual(plan);

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("POST");
    expect(request.url).toContain(`/api/v1/semesters/${semesterId}/teaching-requirements/generate`);
  });
});
