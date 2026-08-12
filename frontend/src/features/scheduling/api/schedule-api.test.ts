import { afterEach, describe, expect, it, vi } from "vitest";
import { createScheduleEntry, createSemesterSchedule, deleteScheduleEntry, getMyScheduleEntries, getScheduleEntries, getSemesterSchedules } from "./schedule-api";

const establishmentId = "00000000-0000-4000-8000-000000000001";
const scheduleId = "00000000-0000-4000-8000-000000000002";
const semesterId = "00000000-0000-4000-8000-000000000003";
const academicYearId = "00000000-0000-4000-8000-000000000004";
const assignmentId = "00000000-0000-4000-8000-000000000005";
const roomId = "00000000-0000-4000-8000-000000000006";
const entry = { id: "00000000-0000-4000-8000-000000000007", semesterScheduleId: scheduleId, teachingAssignmentId: assignmentId, professorId: "00000000-0000-4000-8000-000000000008", subjectModuleId: "00000000-0000-4000-8000-000000000009", teachingGroupId: "00000000-0000-4000-8000-000000000010", teachingGroupName: "IL-G1", sourceClassGroupId: "00000000-0000-4000-8000-000000000010", sourceClassGroupName: "IL-G1", audienceType: "CLASS_GROUP", dayOfWeek: "MONDAY", startTime: "08:30:00", endTime: "10:30:00", roomId, roomCode: "A-01", roomName: "Classroom A1" };

afterEach(() => vi.unstubAllGlobals());

describe("schedule API", () => {
  it("loads and creates semester schedules", async () => {
    const schedule = { id: scheduleId, establishmentId, academicYearId, semesterId, publicationStatus: "DRAFT" };
    const fetchMock = vi.fn().mockResolvedValueOnce(new Response(JSON.stringify([schedule]), { status: 200, headers: { "Content-Type": "application/json" } })).mockResolvedValueOnce(new Response(JSON.stringify(schedule), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);
    await expect(getSemesterSchedules(establishmentId)).resolves.toEqual([schedule]);
    await expect(createSemesterSchedule(establishmentId, academicYearId, semesterId)).resolves.toEqual(schedule);
    expect(await (fetchMock.mock.calls[1][0] as Request).json()).toEqual({ academicYearId, semesterId });
  });

  it("creates and lists timetable entries", async () => {
    const input = { teachingAssignmentId: assignmentId, dayOfWeek: "MONDAY" as const, startTime: "08:30", endTime: "10:30", roomId };
    const fetchMock = vi.fn().mockResolvedValueOnce(new Response(JSON.stringify([entry]), { status: 200, headers: { "Content-Type": "application/json" } })).mockResolvedValueOnce(new Response(JSON.stringify(entry), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);
    await expect(getScheduleEntries(scheduleId)).resolves.toEqual([entry]);
    await expect(createScheduleEntry(scheduleId, input)).resolves.toEqual(entry);
  });

  it("loads the authenticated Professor schedule", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([entry]), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);
    await expect(getMyScheduleEntries()).resolves.toEqual([entry]);
    expect((fetchMock.mock.calls[0][0] as Request).url).toContain("/api/v1/me/schedule-entries");
  });

  it("deletes an entry", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: true, message: "Schedule entry deleted" }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);
    await expect(deleteScheduleEntry(entry.id)).resolves.toBeUndefined();
    expect((fetchMock.mock.calls[0][0] as Request).method).toBe("DELETE");
  });
});
