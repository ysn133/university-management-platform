import { afterEach, describe, expect, it, vi } from "vitest";
import { getStudentScheduleEntries } from "./student-schedule-api";

describe("student schedule API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads the authenticated student's published schedule", async () => {
    const entry = {
      id: "00000000-0000-4000-8000-000000000001",
      academicYearId: "00000000-0000-4000-8000-000000000002", academicYearLabel: "2026-2027", academicYearStatus: "ACTIVE",
      semesterId: "00000000-0000-4000-8000-000000000003", semesterName: "S1", semesterTermType: "AUTUMN",
      semesterStartDate: "2026-09-01", semesterEndDate: "2027-01-31",
      academicLevelId: "00000000-0000-4000-8000-000000000004", academicLevelName: "M1",
      programFiliereId: "00000000-0000-4000-8000-000000000005", programFiliereCode: "IL", programFiliereName: "Software Engineering",
      subjectModuleId: "00000000-0000-4000-8000-000000000006", subjectModuleCode: "ALG", subjectModuleTitle: "Algorithms",
      componentType: "COURSE", audienceType: "CLASS_GROUP", teachingGroupName: "Group A", professorName: "Yassine Chouikh",
      dayOfWeek: "MONDAY", startTime: "08:30:00", endTime: "10:30:00", roomCode: "A1", roomName: "Room A1", blockCode: "A", blockName: "Block A",
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([entry]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getStudentScheduleEntries()).resolves.toEqual([entry]);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain("/api/v1/me/student-schedule-entries");
  });
});
