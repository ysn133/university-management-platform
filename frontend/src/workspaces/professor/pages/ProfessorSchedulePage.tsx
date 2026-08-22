import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { getMyScheduleEntries, scheduleKeys } from "@/features/scheduling/api/schedule-api";
import { getMyTeachingAssignments, teachingPlanKeys, type TeachingAssignment } from "@/features/teaching-planning/api/teaching-plan-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { WeeklyTimetable } from "@/features/scheduling/components/WeeklyTimetable";

const termLabels = { AUTUMN: "Autumn", SPRING: "Spring" } as const;

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
  const timetableEntries = entries.map((entry) => {
    const assignment = assignmentById.get(entry.teachingAssignmentId);
    const componentType = assignment?.componentType ?? "COURSE";
    return {
      id: entry.id, dayOfWeek: entry.dayOfWeek, startTime: entry.startTime, endTime: entry.endTime,
      title: assignment?.subjectModuleTitle ?? "Scheduled session", context: assignmentContext(assignment),
      detail: `${componentType === "COURSE" ? "Course" : componentType} · ${entry.audienceType === "WHOLE_COHORT" ? "Whole Cohort" : entry.teachingGroupName}`,
      room: entry.roomCode ?? "Room pending", componentType,
    };
  });

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
        : <WeeklyTimetable entries={timetableEntries} />}
    </section>
  </div>;
}
