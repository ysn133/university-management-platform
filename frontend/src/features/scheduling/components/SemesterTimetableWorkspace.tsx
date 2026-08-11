import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useRef, useState } from "react";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { academicStructureKeys, getModuleTeachingComponents, type Semester, type SubjectModule } from "@/features/academic-structure/api/academic-structure-api";
import { facilityKeys, getBlocks, getRooms } from "@/features/facility-management/api/facility-api";
import { getProfessors, professorManagementKeys } from "@/features/professor-management/api/professor-management-api";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { classGroupKeys, getClassGroups } from "@/features/student-registration/api/class-group-api";
import { getTeachingAssignments, getTeachingPlan, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { createScheduleEntry, createSemesterSchedule, deleteScheduleEntry, getScheduleEntries, getSemesterSchedules, publishSemesterSchedule, scheduleKeys, updateScheduleEntry, type ScheduleDay, type ScheduleEntry, type ScheduleEntryInput } from "../api/schedule-api";
import { saveSchedulePdf } from "../utils/save-schedule-pdf";

interface SemesterTimetableWorkspaceProps {
  academicLevelName?: string;
  academicLevelId: string;
  academicYearId: string;
  academicYearLabel?: string;
  establishmentId: string;
  modules: SubjectModule[];
  semesterId: string;
  semesterName?: string;
  semesters: Semester[];
  onSelectSemester: (semesterId: string) => void;
}

interface EntryForm extends ScheduleEntryInput {}
const days: ScheduleDay[] = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SUNDAY"];
const dayLabels: Record<ScheduleDay, string> = { MONDAY: "Monday", TUESDAY: "Tuesday", WEDNESDAY: "Wednesday", THURSDAY: "Thursday", FRIDAY: "Friday", SATURDAY: "Saturday", SUNDAY: "Sunday" };
const standaloneRooms = "STANDALONE";
const roomTypeLabels = { LECTURE_HALL: "Lecture hall", CLASSROOM: "Classroom", COMPUTER_LAB: "Computer lab" } as const;
const gridStart = 8 * 60;
const gridEnd = 18 * 60 + 30;
const slotMinutes = 30;
const slotCount = (gridEnd - gridStart) / slotMinutes;
const slots = Array.from({ length: slotCount }, (_, index) => gridStart + index * slotMinutes);
const hourLabels = Array.from({ length: 11 }, (_, index) => 8 + index);

function errorMessage(error: unknown): string { return error instanceof ApiRequestError ? error.message : "The request could not be completed."; }
function timeToMinutes(value: string): number { const [hours, minutes] = value.split(":").map(Number); return hours * 60 + minutes; }
function minutesToTime(value: number): string { return `${String(Math.floor(value / 60)).padStart(2, "0")}:${String(value % 60).padStart(2, "0")}`; }
function displayTime(value: string): string { return value.slice(0, 5); }

function assignLanes(entries: ScheduleEntry[]) {
  const laneEnds: number[] = [];
  return [...entries].sort((left, right) => timeToMinutes(left.startTime) - timeToMinutes(right.startTime)).map((entry) => {
    const start = timeToMinutes(entry.startTime);
    let lane = laneEnds.findIndex((end) => end <= start);
    if (lane < 0) { lane = laneEnds.length; laneEnds.push(0); }
    laneEnds[lane] = timeToMinutes(entry.endTime);
    return { entry, lane, laneCount: laneEnds.length };
  });
}

export function SemesterTimetableWorkspace({ academicLevelName, academicLevelId, academicYearId, academicYearLabel, establishmentId, modules, semesterId, semesterName, semesters, onSelectSemester }: SemesterTimetableWorkspaceProps) {
  const queryClient = useQueryClient();
  const scheduleRootRef = useRef<HTMLElement>(null);
  const [exportingPdf, setExportingPdf] = useState(false);
  const [entryForm, setEntryForm] = useState<EntryForm | null>(null);
  const [selectedBlockId, setSelectedBlockId] = useState("");
  const [editingEntry, setEditingEntry] = useState<ScheduleEntry | null>(null);
  const [deletingEntry, setDeletingEntry] = useState<ScheduleEntry | null>(null);
  const [confirmingPublish, setConfirmingPublish] = useState(false);
  const [selectedClassGroupId, setSelectedClassGroupId] = useState("");
  const schedulesQuery = useQuery({ queryKey: scheduleKeys.schedules(establishmentId), queryFn: () => getSemesterSchedules(establishmentId) });
  const schedule = schedulesQuery.data?.find((item) => item.semesterId === semesterId);
  const entriesQuery = useQuery({ queryKey: scheduleKeys.entries(schedule?.id ?? "missing"), queryFn: () => getScheduleEntries(schedule!.id), enabled: Boolean(schedule) });
  const planQuery = useQuery({ queryKey: teachingPlanKeys.semester(semesterId || "missing"), queryFn: () => getTeachingPlan(semesterId), enabled: Boolean(semesterId) });
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.assignments(establishmentId), queryFn: () => getTeachingAssignments(establishmentId), enabled: Boolean(establishmentId) });
  const professorsQuery = useQuery({ queryKey: professorManagementKeys.professors(establishmentId), queryFn: () => getProfessors(establishmentId), enabled: Boolean(establishmentId) });
  const blocksQuery = useQuery({ queryKey: facilityKeys.blocks(establishmentId), queryFn: () => getBlocks(establishmentId), enabled: Boolean(establishmentId) });
  const roomsQuery = useQuery({ queryKey: facilityKeys.rooms(establishmentId), queryFn: () => getRooms(establishmentId), enabled: Boolean(establishmentId) });
  const classGroupsQuery = useQuery({ queryKey: classGroupKeys.groups(academicLevelId || "missing", academicYearId || "missing"), queryFn: () => getClassGroups(academicLevelId, academicYearId), enabled: Boolean(academicLevelId && academicYearId) });
  const componentQueries = useQueries({ queries: modules.map((module) => ({ queryKey: academicStructureKeys.moduleTeachingComponents(module.id), queryFn: () => getModuleTeachingComponents(module.id), enabled: Boolean(semesterId) })) });

  const classGroups = (classGroupsQuery.data ?? []).filter((group) => group.status === "ACTIVE");
  const plan = (planQuery.data ?? []).filter((requirement) => requirement.audienceType === "WHOLE_COHORT" || requirement.sourceClassGroupId === selectedClassGroupId);
  const requirementById = new Map(plan.map((requirement) => [requirement.id, requirement]));
  const requirementIds = new Set(requirementById.keys());
  const assignments = (assignmentsQuery.data ?? []).filter((assignment) => assignment.status === "ACTIVE" && requirementIds.has(assignment.teachingRequirementId));
  const assignmentById = new Map(assignments.map((assignment) => [assignment.id, assignment]));
  const moduleById = new Map(modules.map((module) => [module.id, module]));
  const professorById = new Map((professorsQuery.data ?? []).map((professor) => [professor.professorId, professor]));
  const componentById = new Map(componentQueries.flatMap((query) => query.data ?? []).map((component) => [component.id, component]));
  const allEntries = entriesQuery.data ?? [];
  const entries = allEntries.filter((entry) => entry.audienceType === "WHOLE_COHORT" || entry.sourceClassGroupId === selectedClassGroupId);
  const scheduledCounts = new Map<string, number>();
  entries.forEach((entry) => scheduledCounts.set(entry.teachingAssignmentId, (scheduledCounts.get(entry.teachingAssignmentId) ?? 0) + 1));
  const remainingAssignments = assignments.filter((assignment) => {
    const requirement = requirementById.get(assignment.teachingRequirementId);
    const component = requirement ? componentById.get(requirement.moduleTeachingComponentId) : undefined;
    return component && (scheduledCounts.get(assignment.id) ?? 0) < component.sessionsPerWeek;
  });
  const activeBlocks = (blocksQuery.data ?? []).filter((block) => block.status === "ACTIVE").sort((left, right) => left.name.localeCompare(right.name));
  const activeBlockIds = new Set(activeBlocks.map((block) => block.id));
  const activeRooms = (roomsQuery.data ?? []).filter((room) => room.status === "ACTIVE" && (!room.blockId || activeBlockIds.has(room.blockId)));
  const visibleRooms = selectedBlockId === standaloneRooms
    ? activeRooms.filter((room) => !room.blockId)
    : activeRooms.filter((room) => room.blockId === selectedBlockId);
  const selectedAssignment = entryForm ? assignmentById.get(entryForm.teachingAssignmentId) : undefined;
  const selectedRequirement = selectedAssignment ? requirementById.get(selectedAssignment.teachingRequirementId) : undefined;
  const selectedComponent = selectedRequirement ? componentById.get(selectedRequirement.moduleTeachingComponentId) : undefined;

  useEffect(() => {
    if (!classGroups.some((group) => group.id === selectedClassGroupId)) {
      setSelectedClassGroupId(classGroups[0]?.id ?? "");
    }
  }, [classGroups, selectedClassGroupId]);

  async function refreshSchedule() {
    if (schedule) await queryClient.invalidateQueries({ queryKey: scheduleKeys.entries(schedule.id) });
  }

  const createScheduleMutation = useMutation({ mutationFn: () => createSemesterSchedule(establishmentId, academicYearId, semesterId), onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: scheduleKeys.schedules(establishmentId) }); } });
  const entryMutation = useMutation({
    mutationFn: (input: EntryForm) => editingEntry ? updateScheduleEntry(editingEntry.id, input) : createScheduleEntry(schedule!.id, input),
    onSuccess: async () => { setEntryForm(null); setEditingEntry(null); await refreshSchedule(); },
  });
  const deleteMutation = useMutation({ mutationFn: () => deleteScheduleEntry(deletingEntry!.id), onSuccess: async () => { setDeletingEntry(null); setEntryForm(null); setEditingEntry(null); await refreshSchedule(); } });
  const publishMutation = useMutation({ mutationFn: () => publishSemesterSchedule(schedule!.id), onSuccess: async () => { setConfirmingPublish(false); await queryClient.invalidateQueries({ queryKey: scheduleKeys.schedules(establishmentId) }); } });

  function openEmpty(day: ScheduleDay, startMinutes: number) {
    if (!schedule || remainingAssignments.length === 0) return;
    const assignment = remainingAssignments[0];
    const requirement = requirementById.get(assignment.teachingRequirementId);
    const component = requirement ? componentById.get(requirement.moduleTeachingComponentId) : undefined;
    setEditingEntry(null);
    setSelectedBlockId("");
    setEntryForm({ teachingAssignmentId: assignment.id, dayOfWeek: day, startTime: minutesToTime(startMinutes), endTime: minutesToTime(Math.min(startMinutes + (component?.sessionDurationMinutes ?? 120), gridEnd)), roomId: "" });
  }

  function openEntry(entry: ScheduleEntry) {
    if (!schedule) return;
    setSelectedBlockId(entry.blockId ?? standaloneRooms);
    setEditingEntry(entry);
    setEntryForm({ teachingAssignmentId: entry.teachingAssignmentId, dayOfWeek: entry.dayOfWeek, startTime: displayTime(entry.startTime), endTime: displayTime(entry.endTime), roomId: entry.roomId });
  }

  function updateAssignment(assignmentId: string) {
    const assignment = assignmentById.get(assignmentId);
    const requirement = assignment ? requirementById.get(assignment.teachingRequirementId) : undefined;
    const component = requirement ? componentById.get(requirement.moduleTeachingComponentId) : undefined;
    const start = timeToMinutes(entryForm!.startTime);
    setSelectedBlockId("");
    setEntryForm({ ...entryForm!, teachingAssignmentId: assignmentId, endTime: minutesToTime(Math.min(start + (component?.sessionDurationMinutes ?? 120), gridEnd)), roomId: "" });
  }

  const loading = schedulesQuery.isPending || planQuery.isPending || assignmentsQuery.isPending || professorsQuery.isPending || blocksQuery.isPending || roomsQuery.isPending || classGroupsQuery.isPending || componentQueries.some((query) => query.isPending);
  const loadError = schedulesQuery.error ?? planQuery.error ?? assignmentsQuery.error ?? professorsQuery.error ?? blocksQuery.error ?? roomsQuery.error ?? classGroupsQuery.error ?? componentQueries.find((query) => query.error)?.error;

  async function exportPdf() {
    if (!scheduleRootRef.current) return;
    setExportingPdf(true);
    try {
      await saveSchedulePdf(scheduleRootRef.current, ".weekly-timetable", `semester-schedule-${semesterName ?? semesterId}.pdf`);
    } finally {
      setExportingPdf(false);
    }
  }

  return <section className="management-panel semester-timetable-panel print-schedule" ref={scheduleRootRef}>
    <div className="print-schedule-header"><div><strong>Université Ibn Zohr</strong><span>Academic timetable</span></div><div><strong>{semesterName} · {classGroups.find((group) => group.id === selectedClassGroupId)?.name ?? "Class Group"}</strong><span>{academicLevelName} · {academicYearLabel}</span></div></div>
    <div className="teaching-plan-context"><label><span>Semester</span><select onChange={(event) => onSelectSemester(event.target.value)} value={semesterId}>{semesters.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label><p>{academicLevelName} · {academicYearLabel}</p></div>
    <nav aria-label="Class group schedules" className="schedule-class-tabs">{classGroups.map((group) => <button aria-selected={selectedClassGroupId === group.id} key={group.id} onClick={() => setSelectedClassGroupId(group.id)} type="button"><strong>{group.name}</strong><span>Class schedule</span></button>)}</nav>
    <header className="panel-header panel-header--bordered"><div><p className="management-kicker">{semesterName} · {classGroups.find((group) => group.id === selectedClassGroupId)?.name ?? "Class Group"}</p><h2>Weekly Schedule</h2><p>Click empty timetable space to place a session for this class or one of its teaching groups.</p></div>{schedule && <div className="timetable-header-actions"><span className={`status-badge status-badge--${schedule.publicationStatus === "PUBLISHED" ? "active" : "inactive"}`}>{schedule.publicationStatus === "PUBLISHED" ? "Published" : "Draft"}</span><button className="secondary-button" disabled={!entries.length || exportingPdf} onClick={exportPdf} type="button">{exportingPdf ? "Preparing PDF..." : "Save as PDF"}</button>{schedule.publicationStatus === "DRAFT" && <button className="management-primary-button" disabled={!allEntries.length} onClick={() => setConfirmingPublish(true)} type="button">Publish schedule</button>}</div>}</header>
    {loading ? <div className="panel-empty">Loading timetable context...</div> : loadError ? <div className="panel-empty panel-empty--error">{errorMessage(loadError)}</div> : !semesterId ? <div className="panel-empty"><strong>Select a semester.</strong></div> : !selectedClassGroupId ? <div className="panel-empty"><strong>No class group is available.</strong><p>Create and assign the semester Class Groups before building their schedules.</p></div> : !schedule ? <div className="timetable-empty-state"><span>Weekly timetable</span><h3>No schedule created for {semesterName}.</h3><p>Create the semester draft, then plan each Class Group through its own view.</p><button className="management-primary-button" disabled={createScheduleMutation.isPending} onClick={() => createScheduleMutation.mutate()} type="button">{createScheduleMutation.isPending ? "Creating..." : "Create draft schedule"}</button>{createScheduleMutation.isError && <div className="management-alert management-alert--error">{errorMessage(createScheduleMutation.error)}</div>}</div> : <>
      <div className="timetable-progress"><span><strong>{entries.length}</strong> scheduled sessions</span><span><strong>{remainingAssignments.length}</strong> requirements remaining</span><span><strong>{assignments.length}</strong> assigned requirements</span></div>
      <div className="timetable-scroll"><div className="weekly-timetable"><div className="timetable-time-header"><span>Days</span><div>{hourLabels.map((hour) => <span key={hour} style={{ left: `${((hour * 60 - gridStart) / (gridEnd - gridStart)) * 100}%` }}>{hour}h</span>)}</div></div>{days.map((day) => {
        const dayEntries = entries.filter((entry) => entry.dayOfWeek === day);
        const laidOut = assignLanes(dayEntries);
        const laneCount = Math.max(1, ...laidOut.map((item) => item.lane + 1));
        return <div className="timetable-day-row" key={day}><strong>{dayLabels[day]}</strong><div className="timetable-day-track" style={{ minHeight: `${Math.max(104, laneCount * 112)}px` }}><div className="timetable-slots">{slots.map((start) => <button aria-label={`Add session on ${dayLabels[day]} at ${minutesToTime(start)}`} className={start >= 12 * 60 + 30 && start < 14 * 60 + 30 ? "is-break" : ""} disabled={remainingAssignments.length === 0} key={start} onClick={() => openEmpty(day, start)} type="button" />)}</div>{laidOut.map(({ entry, lane }) => {
          const assignment = assignmentById.get(entry.teachingAssignmentId);
          const requirement = assignment ? requirementById.get(assignment.teachingRequirementId) : undefined;
          const module = moduleById.get(entry.subjectModuleId);
          const professor = professorById.get(entry.professorId);
          const start = timeToMinutes(entry.startTime);
          const end = timeToMinutes(entry.endTime);
          const componentType = assignment?.componentType ?? "COURSE";
          return <button className={`timetable-session timetable-session--${componentType.toLowerCase()}`} key={entry.id} onClick={() => openEntry(entry)} style={{ left: `${((start - gridStart) / (gridEnd - gridStart)) * 100}%`, top: `${lane * 108 + 6}px`, width: `${((end - start) / (gridEnd - gridStart)) * 100}%` }} type="button"><strong>{module?.title ?? "Subject Module"}</strong><span>{professor ? `${professor.firstName} ${professor.lastName}` : "Professor"}</span><span>{componentType === "COURSE" ? "Course" : componentType} · {entry.teachingGroupName}</span><small>{entry.roomCode} · {displayTime(entry.startTime)}–{displayTime(entry.endTime)}</small></button>;
        })}</div></div>;
      })}</div></div>
    </>}
    {entryForm && <ManagementModal description={editingEntry ? "Adjust the assigned session, time, or room." : "Choose an assigned teaching requirement and place it in the timetable."} onClose={() => { setEntryForm(null); setEditingEntry(null); setSelectedBlockId(""); entryMutation.reset(); }} title={editingEntry ? "Edit Scheduled Session" : "Add Scheduled Session"}><form className="management-form schedule-entry-form" onSubmit={(event) => { event.preventDefault(); entryMutation.mutate(entryForm); }}><div className="form-field schedule-assignment-field"><label htmlFor="schedule-assignment">Teaching requirement</label><select disabled={Boolean(editingEntry)} id="schedule-assignment" onChange={(event) => updateAssignment(event.target.value)} value={entryForm.teachingAssignmentId}>{(editingEntry ? assignments : remainingAssignments).map((assignment) => { const module = moduleById.get(assignment.subjectModuleId); const professor = professorById.get(assignment.professorId); return <option key={assignment.id} value={assignment.id}>{module?.code} · {assignment.componentType} · {assignment.teachingGroupName} · {professor?.firstName} {professor?.lastName}</option>; })}</select></div><div className="form-field schedule-day-field"><label htmlFor="schedule-day">Day</label><select id="schedule-day" onChange={(event) => setEntryForm({ ...entryForm, dayOfWeek: event.target.value as ScheduleDay })} value={entryForm.dayOfWeek}>{days.map((day) => <option key={day} value={day}>{dayLabels[day]}</option>)}</select></div><div className="form-field schedule-block-field"><label htmlFor="schedule-block">Block</label><select id="schedule-block" onChange={(event) => { setSelectedBlockId(event.target.value); setEntryForm({ ...entryForm, roomId: "" }); }} required value={selectedBlockId}><option value="">Select a block</option>{activeBlocks.map((block) => <option key={block.id} value={block.id}>{block.code} · {block.name}</option>)}{activeRooms.some((room) => !room.blockId) && <option value={standaloneRooms}>Standalone / Amphitheatres</option>}</select></div><div className="form-field schedule-room-field"><label htmlFor="schedule-room">Room</label><select disabled={!selectedBlockId} id="schedule-room" onChange={(event) => setEntryForm({ ...entryForm, roomId: event.target.value })} required value={entryForm.roomId}><option value="">{selectedBlockId ? "Select a room" : "Select block first"}</option>{visibleRooms.map((room) => <option key={room.id} value={room.id}>{room.code} · {room.name} · {room.capacity} seats · {roomTypeLabels[room.roomType]}</option>)}</select>{selectedComponent && <small className="schedule-room-guidance">Preferred: {roomTypeLabels[selectedComponent.requiredRoomType]}</small>}</div><div className="form-field schedule-start-field"><label htmlFor="schedule-start">Start time</label><input id="schedule-start" max="18:00" min="08:00" onChange={(event) => setEntryForm({ ...entryForm, startTime: event.target.value })} step="1800" type="time" value={entryForm.startTime} /></div><div className="form-field schedule-end-field"><label htmlFor="schedule-end">End time</label><input id="schedule-end" max="18:30" min="08:30" onChange={(event) => setEntryForm({ ...entryForm, endTime: event.target.value })} step="1800" type="time" value={entryForm.endTime} /></div>{entryMutation.isError && <div className="management-alert management-alert--error">{errorMessage(entryMutation.error)}</div>}<footer className="form-actions">{editingEntry && <button className="danger-ghost-button schedule-delete-action" onClick={() => setDeletingEntry(editingEntry)} type="button">Remove session</button>}<button className="secondary-button" onClick={() => { setEntryForm(null); setEditingEntry(null); setSelectedBlockId(""); }} type="button">Cancel</button><button className="management-primary-button" disabled={entryMutation.isPending || !entryForm.roomId} type="submit">{entryMutation.isPending ? "Saving..." : editingEntry ? "Save changes" : "Add session"}</button></footer></form></ManagementModal>}
    {deletingEntry && <ConfirmActionModal actionLabel="Remove session" destructive description="Remove this session from the draft timetable?" error={deleteMutation.isError ? errorMessage(deleteMutation.error) : null} isSubmitting={deleteMutation.isPending} onCancel={() => { setDeletingEntry(null); deleteMutation.reset(); }} onConfirm={() => deleteMutation.mutate()} title="Remove Scheduled Session" />}
    {confirmingPublish && <ConfirmActionModal actionLabel="Publish schedule" description="Publish this timetable for student and Professor visibility? Later administrative changes will remain visible without republishing." error={publishMutation.isError ? errorMessage(publishMutation.error) : null} isSubmitting={publishMutation.isPending} onCancel={() => { setConfirmingPublish(false); publishMutation.reset(); }} onConfirm={() => publishMutation.mutate()} title="Publish Semester Schedule" />}
  </section>;
}
