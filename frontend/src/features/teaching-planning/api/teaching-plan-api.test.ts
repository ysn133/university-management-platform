import { afterEach, describe, expect, it, vi } from "vitest";
import { assignProfessor, clearTeachingAssignments, generateTeachingAssignments, generateTeachingPlan, getMyTeachingAssignments, getTeachingAssignments, getTeachingPlan, unassignProfessor } from "./teaching-plan-api";

const semesterId = "00000000-0000-4000-8000-000000000001";
const establishmentId = "00000000-0000-4000-8000-000000000010";
const assignmentContext = {
  subjectModuleCode: "ALG",
  subjectModuleTitle: "Algorithms",
  sessionsPerWeek: 1,
  sessionDurationMinutes: 120,
  semesterId,
  semesterName: "S1",
  semesterTermType: "AUTUMN",
  semesterLifecycleStatus: "ACTIVE",
  academicYearId: "00000000-0000-4000-8000-000000000020",
  academicYearLabel: "2026-2027",
  academicYearStatus: "ACTIVE",
  academicLevelId: "00000000-0000-4000-8000-000000000021",
  academicLevelName: "M1",
  programFiliereId: "00000000-0000-4000-8000-000000000022",
  programFiliereCode: "IL",
  programFiliereName: "Software Engineering",
};
const plan = [{
  id: "00000000-0000-4000-8000-000000000002",
  subjectModuleId: "00000000-0000-4000-8000-000000000003",
  moduleTeachingComponentId: "00000000-0000-4000-8000-000000000004",
  componentType: "TD",
  teachingGroupId: "00000000-0000-4000-8000-000000000005",
  teachingGroupName: "A",
  sourceClassGroupId: "00000000-0000-4000-8000-000000000005",
  sourceClassGroupName: "A",
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

  it("loads establishment teaching assignments", async () => {
    const assignments = [{
      id: "00000000-0000-4000-8000-000000000011",
      establishmentId,
      professorId: "00000000-0000-4000-8000-000000000012",
      teachingRequirementId: plan[0].id,
      subjectModuleId: plan[0].subjectModuleId,
      ...assignmentContext,
      componentType: "TD",
      teachingGroupId: plan[0].teachingGroupId,
      teachingGroupName: "A",
      status: "ACTIVE",
      assignmentSource: "AUTOMATIC",
    }];
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(assignments), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getTeachingAssignments(establishmentId)).resolves.toEqual(assignments);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain(`/api/v1/establishments/${establishmentId}/teaching-assignments`);
  });

  it("loads the authenticated Professor teaching assignments", async () => {
    const assignments = [{
      id: "00000000-0000-4000-8000-000000000011",
      establishmentId,
      professorId: "00000000-0000-4000-8000-000000000012",
      teachingRequirementId: plan[0].id,
      subjectModuleId: plan[0].subjectModuleId,
      ...assignmentContext,
      componentType: "TD",
      teachingGroupId: plan[0].teachingGroupId,
      teachingGroupName: "A",
      status: "ACTIVE",
      assignmentSource: "AUTOMATIC",
    }];
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(assignments), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getMyTeachingAssignments()).resolves.toEqual(assignments);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain("/api/v1/me/teaching-assignments");
  });

  it("generates and reports semester teaching assignments", async () => {
    const result = {
      semesterId,
      preservedAssignmentCount: 2,
      createdAssignments: [],
      unresolvedRequirements: [],
      professorWorkloads: [],
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(result), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(generateTeachingAssignments(semesterId)).resolves.toEqual(result);
    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("POST");
    expect(request.url).toContain(`/api/v1/semesters/${semesterId}/teaching-assignments/generate`);
  });

  it("clears semester teaching assignments", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: true, message: "3 teaching assignments cleared" }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(clearTeachingAssignments(semesterId)).resolves.toBeUndefined();
    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("DELETE");
    expect(request.url).toContain(`/api/v1/semesters/${semesterId}/teaching-assignments`);
  });

  it("assigns a professor manually", async () => {
    const assignment = {
      id: "00000000-0000-4000-8000-000000000011",
      establishmentId,
      professorId: "00000000-0000-4000-8000-000000000012",
      teachingRequirementId: plan[0].id,
      subjectModuleId: plan[0].subjectModuleId,
      ...assignmentContext,
      componentType: "TD",
      teachingGroupId: plan[0].teachingGroupId,
      teachingGroupName: "A",
      status: "ACTIVE",
      assignmentSource: "MANUAL",
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(assignment), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(assignProfessor(establishmentId, assignment.professorId, assignment.teachingRequirementId)).resolves.toEqual(assignment);
    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("POST");
    expect(await request.json()).toEqual({ professorId: assignment.professorId, teachingRequirementId: assignment.teachingRequirementId });
  });

  it("unassigns one professor", async () => {
    const assignmentId = "00000000-0000-4000-8000-000000000011";
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: true, message: "Professor unassigned" }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(unassignProfessor(assignmentId)).resolves.toBeUndefined();
    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("DELETE");
    expect(request.url).toContain(`/api/v1/teaching-assignments/${assignmentId}`);
  });
});
