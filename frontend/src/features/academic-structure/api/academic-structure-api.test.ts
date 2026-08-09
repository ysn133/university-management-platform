import { afterEach, describe, expect, it, vi } from "vitest";
import { createAcademicDomain, createAcademicRuleProfile, createAcademicYear, createProgramFiliere, getAcademicLevels, getDepartments, getModuleTeachingComponents, getSemesters, replaceModuleTeachingComponents, replaceTeachingGroupPolicies, updateAcademicRuleProfile } from "./academic-structure-api";

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

  it("loads academic levels through their program", async () => {
    const programId = "00000000-0000-4000-8000-000000000007";
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([{
      id: "00000000-0000-4000-8000-000000000008",
      programFiliereId: programId,
      establishmentId,
      name: "M1",
      levelOrder: 1,
    }]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await getAcademicLevels(programId);

    expect((fetchMock.mock.calls[0][0] as Request).url).toContain(`/api/v1/program-filieres/${programId}/academic-levels`);
  });

  it("loads semesters in the selected academic year", async () => {
    const levelId = "00000000-0000-4000-8000-000000000009";
    const academicYearId = "00000000-0000-4000-8000-000000000010";
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([{
      id: "00000000-0000-4000-8000-000000000011",
      academicLevelId: levelId,
      academicYearId,
      establishmentId,
      name: "S1",
      semesterOrder: 1,
    }]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await getSemesters(levelId, academicYearId);

    const requestUrl = new URL((fetchMock.mock.calls[0][0] as Request).url);
    expect(requestUrl.pathname).toContain(`/api/v1/academic-levels/${levelId}/semesters`);
    expect(requestUrl.searchParams.get("academicYearId")).toBe(academicYearId);
  });

  it("replaces annual TD and TP group sizes for an academic level", async () => {
    const levelId = "00000000-0000-4000-8000-000000000021";
    const academicYearId = "00000000-0000-4000-8000-000000000022";
    const policies = [
      { groupType: "TD" as const, minimumGroupSize: 20, maximumGroupSize: 40 },
      { groupType: "TP" as const, minimumGroupSize: 12, maximumGroupSize: 24 },
    ];
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(policies.map((policy, index) => ({
      id: `00000000-0000-4000-8000-00000000023${index}`,
      academicLevelId: levelId,
      academicYearId,
      ...policy,
      createdAt: "2026-08-05T12:00:00Z",
      updatedAt: "2026-08-05T12:00:00Z",
    }))), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await replaceTeachingGroupPolicies(levelId, academicYearId, policies);

    const sent = fetchMock.mock.calls[0][0] as Request;
    expect(sent.method).toBe("PUT");
    expect(sent.url).toContain(`/api/v1/academic-levels/${levelId}/teaching-group-policies`);
    await expect(sent.clone().json()).resolves.toEqual({ policies });
  });

  it("creates an active academic rule profile in establishment scope", async () => {
    const request = {
      name: "Master standard rules",
      moduleValidationThreshold: 10,
      compensationMinimumThreshold: 7,
      semesterValidationAverage: 10,
      annualValidationAverage: 10,
      maximumModuleInscriptions: 2,
      sessionGradePolicy: "RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD" as const,
      allowProgressionWithDebt: true,
      maximumCarriedModules: 2,
      maximumUnjustifiedAbsences: 3,
      absenceExclusionPolicy: "NORMAL_AND_RATTRAPAGE" as const,
      status: "ACTIVE" as const,
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      id: "00000000-0000-4000-8000-000000000012",
      establishmentId,
      version: 1,
      ...request,
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await createAcademicRuleProfile(establishmentId, request);

    const submittedRequest = fetchMock.mock.calls[0][0] as Request;
    expect(submittedRequest.url).toContain(`/api/v1/establishments/${establishmentId}/academic-rule-profiles`);
    await expect(submittedRequest.clone().json()).resolves.toEqual(request);
  });

  it("updates an academic rule profile", async () => {
    const academicRuleProfileId = "00000000-0000-4000-8000-000000000012";
    const request = {
      name: "Master standard rules",
      moduleValidationThreshold: 10,
      compensationMinimumThreshold: 7,
      semesterValidationAverage: 10,
      annualValidationAverage: 10,
      maximumModuleInscriptions: 2,
      sessionGradePolicy: "BEST_GRADE" as const,
      allowProgressionWithDebt: true,
      maximumCarriedModules: 2,
      maximumUnjustifiedAbsences: 3,
      absenceExclusionPolicy: "NORMAL_AND_RATTRAPAGE" as const,
      status: "INACTIVE" as const,
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      id: academicRuleProfileId,
      establishmentId,
      version: 1,
      ...request,
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await updateAcademicRuleProfile(academicRuleProfileId, request);

    const submittedRequest = fetchMock.mock.calls[0][0] as Request;
    expect(submittedRequest.method).toBe("PUT");
    expect(submittedRequest.url).toContain(`/api/v1/academic-rule-profiles/${academicRuleProfileId}`);
    await expect(submittedRequest.clone().json()).resolves.toEqual(request);
  });

  it("creates an academic domain in establishment scope", async () => {
    const request = { code: "CS", name: "Computer Science" };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      id: "00000000-0000-4000-8000-000000000013",
      establishmentId,
      ...request,
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await createAcademicDomain(establishmentId, request);

    const submittedRequest = fetchMock.mock.calls[0][0] as Request;
    expect(submittedRequest.url).toContain(`/api/v1/establishments/${establishmentId}/academic-domains`);
    await expect(submittedRequest.clone().json()).resolves.toEqual(request);
  });

  it("loads a subject module teaching configuration", async () => {
    const subjectModuleId = "00000000-0000-4000-8000-000000000014";
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([{
      id: "00000000-0000-4000-8000-000000000015",
      subjectModuleId,
      componentType: "COURSE",
      sessionsPerWeek: 1,
      sessionDurationMinutes: 120,
      audienceMode: "WHOLE_COHORT",
      requiredRoomType: "LECTURE_HALL",
    }]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await getModuleTeachingComponents(subjectModuleId);

    expect((fetchMock.mock.calls[0][0] as Request).url).toContain(`/api/v1/subject-modules/${subjectModuleId}/teaching-components`);
  });

  it("replaces the complete subject module teaching configuration", async () => {
    const subjectModuleId = "00000000-0000-4000-8000-000000000016";
    const request = {
      components: [{
        componentType: "TP" as const,
        sessionsPerWeek: 1,
        sessionDurationMinutes: 120,
        audienceMode: "SUBGROUP" as const,
        requiredRoomType: "COMPUTER_LAB" as const,
      }],
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([{
      id: "00000000-0000-4000-8000-000000000017",
      subjectModuleId,
      ...request.components[0],
    }]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await replaceModuleTeachingComponents(subjectModuleId, request);

    const submittedRequest = fetchMock.mock.calls[0][0] as Request;
    expect(submittedRequest.method).toBe("PUT");
    await expect(submittedRequest.clone().json()).resolves.toEqual(request);
  });
});
