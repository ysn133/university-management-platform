import { afterEach, describe, expect, it, vi } from "vitest";
import { changeStudentStatus, createAcademicRegistration, createStudent, getAcademicRegistrations, getRegistrationStudyContext, getStudents } from "./student-registration-api";

const establishmentId = "00000000-0000-4000-8000-000000000101";
const studentId = "00000000-0000-4000-8000-000000000102";

describe("student registration API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads establishment students", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([{
      studentId,
      userAccountId: "00000000-0000-4000-8000-000000000103",
      establishmentId,
      apogeeCode: "220001",
      initialEnrollmentDate: "2026-09-01",
      universityEmail: "student@uiz.ac.ma",
      roleType: "STUDENT",
      accountStatus: "ACTIVE",
      firstName: "Sara",
      lastName: "Amrani",
      birthDate: "2004-03-12",
      placeOfBirth: "Agadir",
      nationality: "Moroccan",
      sex: "FEMALE",
    }]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getStudents(establishmentId, { query: "Sara", status: "ACTIVE", enrolledFrom: "2025-09-01" })).resolves.toHaveLength(1);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain(`/api/v1/establishments/${establishmentId}/students`);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain("query=Sara");
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain("status=ACTIVE");
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain("enrolledFrom=2025-09-01");
  });

  it("creates a Student account", async () => {
    const request = {
      apogeeCode: "220001",
      initialEnrollmentDate: "2026-09-01",
      universityEmail: "student@uiz.ac.ma",
      password: "Temporary123!",
      firstName: "Sara",
      lastName: "Amrani",
      birth_date: "2004-03-12",
      placeOfBirth: "Agadir",
      nationality: "Moroccan",
      sex: "FEMALE" as const,
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      studentId,
      userAccountId: "00000000-0000-4000-8000-000000000103",
      establishmentId,
      apogeeCode: request.apogeeCode,
      roleType: "STUDENT",
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await createStudent(establishmentId, request);

    await expect((fetchMock.mock.calls[0][0] as Request).clone().json()).resolves.toEqual(request);
  });

  it("creates the annual academic registration", async () => {
    const request = {
      studentId,
      programFiliereId: "00000000-0000-4000-8000-000000000104",
      academicLevelId: "00000000-0000-4000-8000-000000000105",
      academicYearId: "00000000-0000-4000-8000-000000000106",
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      id: "00000000-0000-4000-8000-000000000107",
      establishmentId,
      ...request,
      status: "ACTIVE",
      createdAt: "2026-09-01T10:00:00Z",
      updatedAt: "2026-09-01T10:00:00Z",
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await createAcademicRegistration(establishmentId, request);

    await expect((fetchMock.mock.calls[0][0] as Request).clone().json()).resolves.toEqual(request);
  });

  it("forwards academic roster filters", async () => {
    const semesterId = "00000000-0000-4000-8000-000000000109";
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await getAcademicRegistrations(establishmentId, { semesterId, status: "ACTIVE" });

    const requestUrl = (fetchMock.mock.calls[0][0] as Request).url;
    expect(requestUrl).toContain(`semesterId=${semesterId}`);
    expect(requestUrl).toContain("status=ACTIVE");
  });

  it("sends Student lifecycle actions to the selected record", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: true }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await changeStudentStatus(studentId, "lock");

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("POST");
    expect(request.url).toContain(`/api/v1/students/${studentId}/lock`);
  });

  it("loads semester modules with inscription history", async () => {
    const registrationId = "00000000-0000-4000-8000-000000000107";
    const semesterRegistrationId = "00000000-0000-4000-8000-000000000108";
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify([{
        id: semesterRegistrationId,
        academicRegistrationId: registrationId,
        semesterId: "00000000-0000-4000-8000-000000000109",
        semesterName: "S3",
        semesterOrder: 1,
      }]), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify([{
        id: "00000000-0000-4000-8000-000000000110",
        semesterRegistrationId,
        subjectModuleId: "00000000-0000-4000-8000-000000000111",
        subjectModuleCode: "ML",
        subjectModuleTitle: "Machine Learning",
        originAcademicLevelId: "00000000-0000-4000-8000-000000000112",
        inscriptionNumber: 2,
        status: "ACTIVE",
      }]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    const result = await getRegistrationStudyContext(registrationId);

    expect(result[0].modules[0].inscriptionNumber).toBe(2);
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});
