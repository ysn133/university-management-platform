import { afterEach, describe, expect, it, vi } from "vitest";
import { generateTeachingGroups, getTeachingGroups, moveTeachingGroupMember } from "./teaching-group-api";

const semesterId = "00000000-0000-4000-8000-000000000301";
const groupId = "00000000-0000-4000-8000-000000000302";
const classGroupId = "00000000-0000-4000-8000-000000000303";
const semesterRegistrationId = "00000000-0000-4000-8000-000000000304";
const studentId = "00000000-0000-4000-8000-000000000305";

const roster = {
  semesterId,
  groups: [{
    id: groupId,
    semesterId,
    sourceClassGroupId: classGroupId,
    sourceClassGroupName: "A",
    name: "A TP1",
    groupType: "TP",
    members: [{ semesterRegistrationId, studentId, apogeeCode: "APO-1", firstName: "Lina", lastName: "Amrani", secondInscription: true }],
  }],
};

describe("teaching group API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads and generates the semester roster", async () => {
    const fetchMock = vi.fn().mockImplementation(async () => new Response(JSON.stringify(roster), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getTeachingGroups(semesterId)).resolves.toEqual(roster);
    await expect(generateTeachingGroups(semesterId)).resolves.toEqual(roster);

    expect((fetchMock.mock.calls[0][0] as Request).method).toBe("GET");
    expect((fetchMock.mock.calls[1][0] as Request).method).toBe("POST");
  });

  it("moves a semester registration to a target group", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(roster), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await moveTeachingGroupMember(groupId, semesterRegistrationId);

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("PUT");
    expect(request.url).toContain(`/teaching-groups/${groupId}/members/${semesterRegistrationId}`);
  });
});
