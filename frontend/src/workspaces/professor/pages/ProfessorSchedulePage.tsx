import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { getMyScheduleEntries, scheduleKeys, type ScheduleEntry } from "@/features/scheduling/api/schedule-api";
import { getMyTeachingAssignments, teachingPlanKeys, type TeachingAssignment } from "@/features/teaching-planning/api/teaching-plan-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";

const days = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SUNDAY"] as const;
const dayLabels = { MONDAY: "Monday", TUESDAY: "Tuesday", WEDNESDAY: "Wednesday", THURSDAY: "Thursday", FRIDAY: "Friday", SATURDAY: "Saturday", SUNDAY: "Sunday" } as const;
const termLabels = { AUTUMN: "Autumn", SPRING: "Spring" } as const;
const gridStart = 8 * 60;
const gridEnd = 18 * 60 + 30;
const hourLabels = Array.from({ length: 11 }, (_, index) => 8 + index);

function timeToMinutes(value: string): number {
  const [hours, minutes] = value.split(":").map(Number);
  return hours * 60 + minutes;
}

function assignLanes(entries: ScheduleEntry[]) {
  const laneEnds: number[] = [];
  return [...entries].sort((left, right) => left.startTime.localeCompare(right.startTime)).map((entry) => {
    const start = timeToMinutes(entry.startTime);
    let lane = laneEnds.findIndex((end) => end <= start);
    if (lane < 0) {
      lane = laneEnds.length;
      laneEnds.push(0);
    }
    laneEnds[lane] = timeToMinutes(entry.endTime);
    return { entry, lane };
  });
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The schedule could not be loaded.";
}

function assignmentContext(assignment?: TeachingAssignment): string {
  if (!assignment) return "Academic context unavailable";
  return `${assignment.programFiliereName} · ${assignment.academicLevelName} · ${assignment.semesterName}`;
}

export function ProfessorSchedulePage() {
  const [academicYearId, setAcademicYearId] = useState("");
  const [termType, setTermType] = useState<"AUTUMN" | "SPRING" | "">("");

  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const entriesQuery = useQuery({ queryKey: scheduleKeys.myEntries(), queryFn: getMyScheduleEntries });
  const assignments = assignmentsQuery.data ?? [];
  const years = Array.from(new Map(assignments.map((assignment) => [assignment.academicYearId, {
    id: assignment.academicYearId,
    label: assignment.academicYearLabel,
    status: assignment.academicYearStatus,
  }])).values());
  const assignmentById = new Map(assignments.map((assignment) => [assignment.id, assignment]));

  useEffect(() => {
    if (academicYearId || !years.length) return;
    setAcademicYearId(years.find((year) => year.status === "ACTIVE")?.id ?? years[0].id);
  }, [academicYearId, years]);

  const yearAssignments = assignments.filter((assignment) => assignment.academicYearId === academicYearId);
  const availableTerms = Array.from(new Set(yearAssignments.map((assignment) => assignment.semesterTermType)));

  useEffect(() => {
    if (!academicYearId || !yearAssignments.length) return;
    const activeTerm = yearAssignments.find((assignment) => assignment.semesterLifecycleStatus === "ACTIVE")?.semesterTermType;
    setTermType((current) => current && availableTerms.includes(current) ? current : activeTerm ?? availableTerms[0] ?? "");
  }, [academicYearId, yearAssignments.length, availableTerms.join(",")]);

  const visibleAssignmentIds = new Set(assignments
    .filter((assignment) => assignment.academicYearId === academicYearId && assignment.semesterTermType === termType)
    .map((assignment) => assignment.id));
  const entries = (entriesQuery.data ?? []).filter((entry) => visibleAssignmentIds.has(entry.teachingAssignmentId));
  const selectedYear = years.find((year) => year.id === academicYearId);
  const loading = assignmentsQuery.isPending || entriesQuery.isPending;
  const loadError = assignmentsQuery.error ?? entriesQuery.error;

  return <div className="management-page professor-schedule-page">
    <header className="management-page-header professor-schedule-page-header">
      <div><p className="management-kicker">Teaching timetable</p><h1>My Schedule</h1><p>Your published teaching sessions across programs and classes.</p></div>
      <div className="professor-schedule-period"><span>Current view</span><strong>{selectedYear?.label ?? "Academic year"}</strong><small>{termType ? `${termLabels[termType]} semester` : "Semester"}</small></div>
    </header>

    <section className="management-panel professor-schedule-panel">
      <header className="panel-header panel-header--bordered professor-schedule-panel-header">
        <div><p className="management-kicker">Published planning</p><h2>Weekly Schedule</h2><p>All your scheduled sessions for the selected academic period.</p></div>
        <div className="professor-schedule-selectors">
          <label><span>Academic year</span><select disabled={assignmentsQuery.isPending} onChange={(event) => setAcademicYearId(event.target.value)} value={academicYearId}>{years.map((year) => <option key={year.id} value={year.id}>{year.label}</option>)}</select></label>
          <label><span>Semester</span><select disabled={!availableTerms.length} onChange={(event) => setTermType(event.target.value as "AUTUMN" | "SPRING")} value={termType}>{availableTerms.map((term) => <option key={term} value={term}>{termLabels[term]}</option>)}</select></label>
        </div>
      </header>

      {loading ? <div className="panel-empty">Loading your schedule...</div>
        : loadError ? <div className="panel-empty panel-empty--error">{errorMessage(loadError)}</div>
        : entries.length === 0 ? <div className="panel-empty"><strong>No published sessions for this period.</strong><p>Select another academic year or semester to view historical schedules.</p></div>
        : <div className="timetable-scroll professor-timetable-scroll"><div className="weekly-timetable professor-weekly-timetable"><div className="timetable-time-header"><span>Days</span><div>{hourLabels.map((hour) => <span key={hour} style={{ left: `${((hour * 60 - gridStart) / (gridEnd - gridStart)) * 100}%` }}>{hour}h</span>)}</div></div>{days.map((day) => {
          const laidOut = assignLanes(entries.filter((entry) => entry.dayOfWeek === day));
          const laneCount = Math.max(1, ...laidOut.map((item) => item.lane + 1));
          return <div className="timetable-day-row" key={day}><strong>{dayLabels[day]}</strong><div className="timetable-day-track professor-day-track" style={{ minHeight: `${Math.max(92, laneCount * 92)}px` }}>{laidOut.map(({ entry, lane }) => {
            const assignment = assignmentById.get(entry.teachingAssignmentId);
            const componentType = assignment?.componentType ?? "COURSE";
            const start = timeToMinutes(entry.startTime);
            const end = timeToMinutes(entry.endTime);
            return <article className={`timetable-session timetable-session--${componentType.toLowerCase()}`} key={entry.id} style={{ left: `${((start - gridStart) / (gridEnd - gridStart)) * 100}%`, top: `${lane * 88 + 4}px`, width: `${((end - start) / (gridEnd - gridStart)) * 100}%` }} title={assignmentContext(assignment)}><strong>{assignment?.subjectModuleTitle ?? "Scheduled session"}</strong><span>{assignmentContext(assignment)}</span><span>{componentType === "COURSE" ? "Course" : componentType} · {entry.audienceType === "WHOLE_COHORT" ? "Whole Cohort" : entry.teachingGroupName}</span><small>{entry.roomCode} · {entry.startTime.slice(0, 5)}–{entry.endTime.slice(0, 5)}</small></article>;
          })}</div></div>;
        })}</div></div>}
    </section>
  </div>;
}
