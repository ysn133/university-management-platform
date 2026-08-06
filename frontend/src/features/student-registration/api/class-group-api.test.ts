import { afterEach, describe, expect, it, vi } from "vitest";
import { bulkAssignStudentClasses, generateClassGroups, getClassGroupRoster, rebalanceClassGroups, replaceTeachingGroupPolicies } from "./class-group-api";

const academicLevelId = "00000000-0000-4000-8000-000000000201";
const academicYearId = "00000000-0000-4000-8000-000000000202";
const semesterId = "00000000-0000-4000-8000-000000000203";
const registrationId = "00000000-0000-4000-8000-000000000204";
const classGroupId = "00000000-0000-4000-8000-000000000205";

describe("class group API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads the semester-aware roster", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      academicLevelId,
      academicYearId,
      semesterId,
      totalStudents: 1,
      unassignedAcademicRegistrationIds: [],
      groups: [{ classGroupId, name: "Group A", academicRegistrationIds: [registrationId] }],
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    const roster = await getClassGroupRoster(academicLevelId, academicYearId, semesterId);

    expect(roster.groups[0].academicRegistrationIds).toEqual([registrationId]);
    const url = (fetchMock.mock.calls[0][0] as Request).url;
    expect(url).toContain(`academicYearId=${academicYearId}`);
    expect(url).toContain(`semesterId=${semesterId}`);
  });

  it("generates balanced groups", async () => {
    const request = { minimumGroupSize: 30, maximumGroupSize: 100 };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      academicLevelId,
      academicYearId,
      totalStudents: 80,
      semesterAssignmentsCreated: 160,
      groups: [{ classGroupId, name: "Group A", studentCount: 80 }],
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await generateClassGroups(academicLevelId, academicYearId, request);

    const sent = fetchMock.mock.calls[0][0] as Request;
    expect(sent.method).toBe("POST");
    await expect(sent.clone().json()).resolves.toEqual(request);
  });

  it("submits annual bulk assignments", async () => {
    const request = { assignments: [{ academicRegistrationId: registrationId, classGroupId }] };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      academicLevelId,
      academicYearId,
      studentsProcessed: 1,
      semesterAssignmentsCreated: 2,
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await bulkAssignStudentClasses(academicLevelId, academicYearId, request);

    const sent = fetchMock.mock.calls[0][0] as Request;
    expect(sent.method).toBe("PUT");
    await expect(sent.clone().json()).resolves.toEqual(request);
  });

  it("rebalances existing class groups", async () => {
    const request = { minimumGroupSize: 30, maximumGroupSize: 100 };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      academicLevelId,
      academicYearId,
      totalStudents: 160,
      semesterAssignmentsChanged: 160,
      groups: [
        { classGroupId, name: "Group A", studentCount: 80 },
        { classGroupId: "00000000-0000-4000-8000-000000000206", name: "Group B", studentCount: 80 },
      ],
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await rebalanceClassGroups(academicLevelId, academicYearId, request);

    const sent = fetchMock.mock.calls[0][0] as Request;
    expect(sent.method).toBe("PUT");
    expect(sent.url).toContain("/class-groups/rebalance");
  });

  it("replaces the annual TD and TP group policy", async () => {
    const policies = [
      { groupType: "TD" as const, maximumGroupSize: 40 },
      { groupType: "TP" as const, maximumGroupSize: 24 },
    ];
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(policies.map((policy, index) => ({
      id: `00000000-0000-4000-8000-00000000021${index}`,
      academicLevelId,
      academicYearId,
      ...policy,
      createdAt: "2026-08-05T12:00:00Z",
      updatedAt: "2026-08-05T12:00:00Z",
    }))), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await replaceTeachingGroupPolicies(academicLevelId, academicYearId, policies);

    const sent = fetchMock.mock.calls[0][0] as Request;
    expect(sent.method).toBe("PUT");
    expect(sent.url).toContain("/teaching-group-policies");
    await expect(sent.clone().json()).resolves.toEqual({ policies });
  });
});
