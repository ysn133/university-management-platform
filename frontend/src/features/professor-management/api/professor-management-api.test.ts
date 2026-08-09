import { afterEach, describe, expect, it, vi } from "vitest";
import { changeProfessorStatus, createProfessor, getProfessor, getProfessors, replaceProfessorExpertise, resetProfessorPassword, updateProfessor } from "./professor-management-api";

const establishmentId = "00000000-0000-4000-8000-000000000001";
const professorId = "00000000-0000-4000-8000-000000000002";
const userAccountId = "00000000-0000-4000-8000-000000000003";
const academicDomainId = "00000000-0000-4000-8000-000000000004";

const professor = {
  professorId,
  userAccountId,
  establishmentId,
  employeeNumber: "EMP-100",
  academicRank: "Assistant Professor",
  hireDate: "2025-09-01",
  maximumWeeklyTeachingMinutes: 720,
  universityEmail: "professor@uiz.ac.ma",
  roleType: "PROFESSOR",
  accountStatus: "ACTIVE",
  firstName: "Leila",
  lastName: "Amrani",
  birthDate: "1988-05-12",
  placeOfBirth: "Agadir",
  nationality: "Moroccan",
  cin: "J123456",
  sex: "FEMALE",
  phoneNumber: "0600000000",
  profilePicturePath: null,
};

describe("professor management API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("lists Professors using establishment filters", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([professor]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getProfessors(establishmentId, { query: "Leila", status: "ACTIVE", academicDomainId })).resolves.toHaveLength(1);

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.url).toContain(`/api/v1/establishments/${establishmentId}/professors`);
    expect(request.url).toContain("query=Leila");
    expect(request.url).toContain("status=ACTIVE");
    expect(request.url).toContain(`academicDomainId=${academicDomainId}`);
  });

  it("creates the Professor identity and employment profile", async () => {
    const body = {
      employeeNumber: "EMP-100",
      academicRank: "Assistant Professor",
      hireDate: "2025-09-01",
      maximumWeeklyTeachingMinutes: 720,
      cin: "J123456",
      universityEmail: "professor@uiz.ac.ma",
      password: "temporary-password",
      firstName: "Leila",
      lastName: "Amrani",
      birth_date: "1988-05-12",
      placeOfBirth: "Agadir",
      nationality: "Moroccan",
      sex: "FEMALE" as const,
      phone_number: "0600000000",
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ professorId, userAccountId, establishmentId, employeeNumber: body.employeeNumber, roleType: "PROFESSOR" }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await createProfessor(establishmentId, body);

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("POST");
    await expect(request.clone().json()).resolves.toEqual(body);
  });

  it("loads and updates a Professor record", async () => {
    const update = {
      employeeNumber: professor.employeeNumber,
      academicRank: "Professor",
      hireDate: professor.hireDate,
      maximumWeeklyTeachingMinutes: 600,
      cin: professor.cin,
      universityEmail: professor.universityEmail,
      firstName: professor.firstName,
      lastName: professor.lastName,
      birth_date: professor.birthDate,
      placeOfBirth: professor.placeOfBirth,
      nationality: professor.nationality,
      sex: "FEMALE" as const,
      phone_number: professor.phoneNumber,
    };
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify(professor), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ ...professor, academicRank: "Professor", maximumWeeklyTeachingMinutes: 600 }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getProfessor(professorId)).resolves.toEqual(professor);
    await updateProfessor(professorId, update);

    const getRequest = fetchMock.mock.calls[0][0] as Request;
    const updateRequest = fetchMock.mock.calls[1][0] as Request;
    expect(getRequest.url).toContain(`/api/v1/professors/${professorId}`);
    expect(updateRequest.method).toBe("PUT");
    await expect(updateRequest.clone().json()).resolves.toEqual(update);
  });

  it("replaces the Professor expertise domains", async () => {
    const response = { professorId, academicDomains: [{ academicDomainId, code: "CS", name: "Computer Science" }] };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(response), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(replaceProfessorExpertise(professorId, [academicDomainId])).resolves.toEqual(response);

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("PUT");
    await expect(request.clone().json()).resolves.toEqual({ academicDomainIds: [academicDomainId] });
  });

  it("resets a Professor password", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: true, message: "Password reset" }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await resetProfessorPassword(professorId, "new-password");

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("POST");
    expect(request.url).toContain(`/api/v1/professors/${professorId}/password-reset`);
    await expect(request.clone().json()).resolves.toEqual({ newPassword: "new-password" });
  });

  it.each(["lock", "unlock", "deactivate", "archive"] as const)("performs the %s Professor lifecycle action", async (action) => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: true, message: action }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await changeProfessorStatus(professorId, action);

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("POST");
    expect(request.url).toContain(`/api/v1/professors/${professorId}/${action}`);
  });
});
