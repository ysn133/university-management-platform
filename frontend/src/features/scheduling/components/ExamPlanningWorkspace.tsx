import { useEffect, useRef, useState } from "react";
import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import type { Semester, SubjectModule } from "@/features/academic-structure/api/academic-structure-api";
import { facilityKeys, getBlocks, getRooms } from "@/features/facility-management/api/facility-api";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { classGroupKeys, getClassGroups } from "@/features/student-registration/api/class-group-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  createExamSchedule,
  createModuleExam,
  deleteModuleExam,
  examPlanningKeys,
  generateExamGroups,
  getExamGroupPlan,
  getExamRoomAllocations,
  getExamSchedules,
  getModuleExams,
  publishExamSchedule,
  updateExamSchedule,
  updateModuleExam,
  type ModuleExam,
  type ModuleExamInput,
} from "../api/exam-planning-api";
import { saveSchedulePdf } from "../utils/save-schedule-pdf";
import { ExamCandidateListModal } from "./ExamCandidateListModal";
import { ExamGroupRosterModal } from "./ExamGroupRosterModal";

interface Props {
  academicLevelId: string;
  academicYearId: string;
  academicYearLabel?: string;
  establishmentId: string;
  modules: SubjectModule[];
  programName?: string;
  semesterId: string;
  semesterName?: string;
  semesters: Semester[];
  onSelectSemester: (id: string) => void;
}

interface PeriodForm { startDate: string; endDate: string; }

const standalone = "STANDALONE";
const gridEnd = 18 * 60 + 30;

function errorMessage(error: unknown) {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function parseLocalDate(value: string) {
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function formatLocalDate(value: Date) {
  return `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, "0")}-${String(value.getDate()).padStart(2, "0")}`;
}

function dateParts(value: string) {
  const date = parseLocalDate(value);
  return {
    day: new Intl.DateTimeFormat("en-GB", { weekday: "long" }).format(date),
    date: new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "2-digit", year: "numeric" }).format(date),
  };
}

function minutesToTime(value: number) {
  return `${String(Math.floor(value / 60)).padStart(2, "0")}:${String(value % 60).padStart(2, "0")}`;
}


export function ExamPlanningWorkspace({ academicLevelId, academicYearId, academicYearLabel, establishmentId, modules, programName, semesterId, semesterName, semesters, onSelectSemester }: Props) {
  const queryClient = useQueryClient();
  const scheduleRootRef = useRef<HTMLElement>(null);
  const [exportingPdf, setExportingPdf] = useState(false);
  const [sessionType, setSessionType] = useState<"NORMAL" | "RATTRAPAGE">("NORMAL");
  const [periodForm, setPeriodForm] = useState<PeriodForm | null>(null);
  const [editingPeriod, setEditingPeriod] = useState(false);
  const [examForm, setExamForm] = useState<ModuleExamInput | null>(null);
  const [editingExam, setEditingExam] = useState<ModuleExam | null>(null);
  const [deletingExam, setDeletingExam] = useState<ModuleExam | null>(null);
  const [candidateExam, setCandidateExam] = useState<ModuleExam | null>(null);
  const [showingExamGroups, setShowingExamGroups] = useState(false);
  const [confirmingPublish, setConfirmingPublish] = useState(false);
  const [selectedClassGroupId, setSelectedClassGroupId] = useState("");
  const [splitCount, setSplitCount] = useState(1);
  const [configuringGroups, setConfiguringGroups] = useState(false);
  const [allocationRooms, setAllocationRooms] = useState<Record<string, string>>({});
  const [allocationBlocks, setAllocationBlocks] = useState<Record<string, string>>({});

  const schedulesQuery = useQuery({ queryKey: examPlanningKeys.schedules(establishmentId), queryFn: () => getExamSchedules(establishmentId) });
  const schedule = schedulesQuery.data?.find((item) => item.semesterId === semesterId && item.sessionType === sessionType);
  const examsQuery = useQuery({ queryKey: examPlanningKeys.exams(schedule?.id ?? "missing"), queryFn: () => getModuleExams(schedule!.id), enabled: Boolean(schedule) });
  const groupsQuery = useQuery({ queryKey: classGroupKeys.groups(academicLevelId, academicYearId), queryFn: () => getClassGroups(academicLevelId, academicYearId), enabled: Boolean(academicLevelId && academicYearId) });
  const blocksQuery = useQuery({ queryKey: facilityKeys.blocks(establishmentId), queryFn: () => getBlocks(establishmentId) });
  const roomsQuery = useQuery({ queryKey: facilityKeys.rooms(establishmentId), queryFn: () => getRooms(establishmentId) });
  const examGroupPlanQuery = useQuery({ queryKey: ["exam-groups", schedule?.id, selectedClassGroupId], queryFn: () => getExamGroupPlan(schedule!.id, selectedClassGroupId), enabled: Boolean(schedule && selectedClassGroupId) });

  const groups = (groupsQuery.data ?? []).filter((group) => group.status === "ACTIVE");
  const rooms = (roomsQuery.data ?? []).filter((room) => room.status === "ACTIVE");
  const blocks = (blocksQuery.data ?? []).filter((block) => block.status === "ACTIVE");
  const moduleById = new Map(modules.map((module) => [module.id, module]));
  const groupById = new Map(groups.map((group) => [group.id, group]));
  const exams = examsQuery.data ?? [];
  const groupExams = exams.filter((exam) => exam.classGroupId === selectedClassGroupId);
  const allocationQueries = useQueries({
    queries: groupExams.map((exam) => ({
      queryKey: ["exam-room-allocations", exam.id],
      queryFn: () => getExamRoomAllocations(exam.id),
    })),
  });
  const allocationsByExamId = new Map(groupExams.map((exam, index) => [exam.id, allocationQueries[index]?.data ?? []]));
  const allocationLoadingByExamId = new Map(groupExams.map((exam, index) => [exam.id, allocationQueries[index]?.isPending ?? false]));

  useEffect(() => {
    if (!groups.some((group) => group.id === selectedClassGroupId)) {
      setSelectedClassGroupId(groups[0]?.id ?? "");
    }
  }, [groups, selectedClassGroupId]);

  useEffect(() => {
    setExamForm(null);
    setEditingExam(null);
  }, [semesterId, sessionType]);

  async function refresh() {
    if (schedule) await queryClient.invalidateQueries({ queryKey: examPlanningKeys.exams(schedule.id) });
  }

  const periodMutation = useMutation({
    mutationFn: () => {
      const input = { academicYearId, semesterId, sessionType, ...periodForm! };
      return editingPeriod && schedule ? updateExamSchedule(schedule.id, input) : createExamSchedule(establishmentId, input);
    },
    onSuccess: async () => {
      setPeriodForm(null);
      setEditingPeriod(false);
      await queryClient.invalidateQueries({ queryKey: examPlanningKeys.schedules(establishmentId) });
    },
  });
  const groupGenerationMutation = useMutation({
    mutationFn: () => generateExamGroups(schedule!.id, selectedClassGroupId, splitCount),
    onSuccess: async () => {
      setConfiguringGroups(false);
      await queryClient.invalidateQueries({ queryKey: ["exam-groups", schedule?.id, selectedClassGroupId] });
    },
  });
  const examMutation = useMutation({
    mutationFn: async (input: ModuleExamInput) => {
      const roomIds = Object.values(allocationRooms);
      const request = {
        ...input,
        roomId: roomIds[0] ?? "",
        roomAllocations: (examGroupPlanQuery.data?.groups ?? []).map((group) => ({
          examGroupId: group.id,
          roomId: allocationRooms[group.id],
        })),
      };
      return editingExam
        ? updateModuleExam(editingExam.id, request)
        : createModuleExam(schedule!.id, request);
    },
    onSuccess: async () => {
      setExamForm(null);
      setEditingExam(null);
      setAllocationRooms({});
      setAllocationBlocks({});
      await Promise.all([
        refresh(),
        queryClient.invalidateQueries({ queryKey: ["exam-room-allocations"] }),
      ]);
    },
  });
  const deleteMutation = useMutation({ mutationFn: () => deleteModuleExam(deletingExam!.id), onSuccess: async () => { setDeletingExam(null); setExamForm(null); setEditingExam(null); await refresh(); } });
  const publishMutation = useMutation({ mutationFn: () => publishExamSchedule(schedule!.id), onSuccess: async () => { setConfirmingPublish(false); await queryClient.invalidateQueries({ queryKey: examPlanningKeys.schedules(establishmentId) }); } });

  function openNew(date?: string, startMinutes = 9 * 60) {
    if (!examGroupPlanQuery.data?.splitCount) {
      setSplitCount(1);
      setConfiguringGroups(true);
      return;
    }
    setEditingExam(null);
    examMutation.reset();
    setExamForm({
      subjectModuleId: modules[0]?.id ?? "",
      classGroupId: selectedClassGroupId,
      examDate: date ?? schedule!.startDate,
      startTime: minutesToTime(startMinutes),
      endTime: minutesToTime(Math.min(startMinutes + 120, gridEnd)),
      roomId: "",
    });
    setAllocationRooms({});
    setAllocationBlocks({});
  }

  async function openEdit(exam: ModuleExam) {
    setEditingExam(exam);
    setExamForm({ subjectModuleId: exam.subjectModuleId, classGroupId: exam.classGroupId, examDate: exam.examDate, startTime: exam.startTime.slice(0, 5), endTime: exam.endTime?.slice(0, 5), roomId: exam.roomId ?? "" });
    const allocations = await getExamRoomAllocations(exam.id);
    setAllocationRooms(Object.fromEntries(allocations.map((allocation) => [allocation.examGroupId, allocation.roomId])));
    setAllocationBlocks(Object.fromEntries(allocations.map((allocation) => {
      const room = rooms.find((item) => item.id === allocation.roomId);
      return [allocation.examGroupId, room?.blockId ?? standalone];
    })));
  }

  const examDates = [...new Set(groupExams.map((exam) => exam.examDate))].sort();
  const earliestExamDate = exams.length ? [...exams].sort((left, right) => left.examDate.localeCompare(right.examDate))[0].examDate : undefined;
  const latestExamDate = exams.length ? [...exams].sort((left, right) => right.examDate.localeCompare(left.examDate))[0].examDate : undefined;
  const invalidPeriodOrder = Boolean(periodForm && periodForm.endDate < periodForm.startDate);
  const periodExcludesExam = Boolean(periodForm && ((earliestExamDate && periodForm.startDate > earliestExamDate) || (latestExamDate && periodForm.endDate < latestExamDate)));
  const invalidPeriod = invalidPeriodOrder || periodExcludesExam;
  const loading = schedulesQuery.isPending || groupsQuery.isPending || blocksQuery.isPending || roomsQuery.isPending;
  const allocationComplete = Boolean(examGroupPlanQuery.data?.groups.length) && examGroupPlanQuery.data!.groups.every((group) => allocationRooms[group.id]);

  async function exportPdf() {
    if (!scheduleRootRef.current) return;
    setExportingPdf(true);
    try {
      await saveSchedulePdf(scheduleRootRef.current, ".exam-plan-table", `exam-schedule-${semesterName ?? semesterId}-${sessionType.toLowerCase()}.pdf`);
    } finally {
      setExportingPdf(false);
    }
  }

  return <section className="management-panel exam-planning-workspace print-schedule" ref={scheduleRootRef}>
    <div className="print-schedule-header"><div><strong>Université Ibn Zohr</strong><span>{sessionType === "NORMAL" ? "Normal examination session" : "Rattrapage examination session"}</span></div><div><strong>{semesterName} · {groups.find((group) => group.id === selectedClassGroupId)?.name ?? "Class Group"}</strong><span>{schedule ? `${dateParts(schedule.startDate).date} - ${dateParts(schedule.endDate).date}` : "Exam planning"}</span></div></div>
    <div className="teaching-plan-context"><label><span>Semester</span><select onChange={(event) => onSelectSemester(event.target.value)} value={semesterId}>{semesters.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label><div className="exam-session-switch" role="group"><button aria-pressed={sessionType === "NORMAL"} onClick={() => setSessionType("NORMAL")} type="button">Normal Session</button><button aria-pressed={sessionType === "RATTRAPAGE"} onClick={() => setSessionType("RATTRAPAGE")} type="button">Rattrapage</button></div></div>
    {groups.length > 0 && <nav aria-label="Class group exam plans" className="schedule-class-tabs">{groups.map((group) => <button aria-selected={selectedClassGroupId === group.id} key={group.id} onClick={() => setSelectedClassGroupId(group.id)} type="button"><strong>{group.name}</strong><span>Exam timetable</span></button>)}</nav>}
    <header className="panel-header panel-header--bordered"><div><p className="management-kicker">{semesterName} · {groups.find((group) => group.id === selectedClassGroupId)?.name ?? "Class Group"} · {sessionType === "NORMAL" ? "Normal Session" : "Rattrapage Session"}</p><h2>Exam Planning</h2><p>Review exams by date, module, room, and exam group. Select a draft row to edit it.</p></div>{schedule && <div className="timetable-header-actions"><span className={`status-badge status-badge--${schedule.publicationStatus === "PUBLISHED" ? "active" : "inactive"}`}>{schedule.publicationStatus}</span><button className="secondary-button" disabled={!groupExams.length || exportingPdf} onClick={exportPdf} type="button">{exportingPdf ? "Preparing PDF..." : "Save as PDF"}</button>{schedule.publicationStatus === "DRAFT" && <><button className="secondary-button" onClick={() => { setEditingPeriod(true); setPeriodForm({ startDate: schedule.startDate, endDate: schedule.endDate }); }} type="button">Edit Period</button><button className="secondary-button" disabled={!selectedClassGroupId} onClick={() => openNew()} type="button">Add Exam</button><button className="management-primary-button" disabled={!exams.length} onClick={() => setConfirmingPublish(true)} type="button">Publish Plan</button></>}</div>}</header>
    {schedule && selectedClassGroupId && examGroupPlanQuery.data && <div className="exam-group-summary"><div className="exam-group-summary__context"><span>Exam room setup</span><strong>{examGroupPlanQuery.data.totalStudentCount} students across {examGroupPlanQuery.data.splitCount || "no configured"} {examGroupPlanQuery.data.splitCount === 1 ? "group" : "groups"}</strong></div><div className="exam-group-summary__groups">{examGroupPlanQuery.data.groups.map((group) => <span className="exam-group-size" key={group.id}><strong>{group.label}</strong><small>{group.studentCount} students</small></span>)}</div><div className="exam-group-summary__actions"><button className="secondary-button" disabled={!examGroupPlanQuery.data.groups.length} onClick={() => setShowingExamGroups(true)} type="button">View Group Roster</button>{schedule.publicationStatus === "DRAFT" && <button className="secondary-button" onClick={() => { setSplitCount(examGroupPlanQuery.data?.splitCount || 1); setConfiguringGroups(true); }} type="button">Configure Groups</button>}</div></div>}
    {loading ? <div className="panel-empty">Loading examination context...</div> : !selectedClassGroupId ? <div className="panel-empty"><strong>No class group is available.</strong><p>Create the semester Class Groups before planning their exams.</p></div> : !schedule ? <div className="timetable-empty-state"><span>Examination period</span><h3>No {sessionType === "NORMAL" ? "Normal" : "Rattrapage"} exam plan for {semesterName}.</h3><p>Define the shared period, then plan exams for each Class Group.</p><button className="management-primary-button" onClick={() => { const semester = semesters.find((item) => item.id === semesterId); const start = semester?.endDate ? parseLocalDate(semester.endDate) : new Date(); start.setDate(start.getDate() + 1); const end = new Date(start); end.setDate(end.getDate() + 13); setEditingPeriod(false); setPeriodForm({ startDate: formatLocalDate(start), endDate: formatLocalDate(end) }); }} type="button">Create Exam Plan</button></div> : groupExams.length === 0 ? <div className="panel-empty"><strong>No exams are scheduled for this Class Group.</strong><p>Use Add Exam to build the examination plan.</p></div> : <div className="exam-plan-table-wrap"><table className="exam-plan-table"><thead><tr><th>Day</th><th>Module</th><th>Time</th><th>Room</th><th>Group</th><th className="no-print">Candidate List</th></tr></thead>{examDates.map((date) => {
      const dateExams = groupExams.filter((exam) => exam.examDate === date).sort((left, right) => left.startTime.localeCompare(right.startTime));
      const dateRowCount = dateExams.reduce((total, exam) => total + Math.max(1, allocationsByExamId.get(exam.id)?.length ?? 0), 0);
      let dateRendered = false;
      const label = dateParts(date);
      return <tbody key={date}>{dateExams.map((exam) => {
        const allocations = allocationsByExamId.get(exam.id) ?? [];
        const rows = allocations.length > 0 ? allocations : [null];
        return rows.map((allocation, allocationIndex) => {
          const showDate = !dateRendered;
          dateRendered = true;
          return <tr className={schedule.publicationStatus === "DRAFT" ? "is-editable" : ""} key={`${exam.id}-${allocation?.id ?? "empty"}`} onClick={() => schedule.publicationStatus === "DRAFT" && openEdit(exam)}>{showDate && <th className="exam-plan-date" rowSpan={dateRowCount}><span>{label.day}</span><strong>{label.date}</strong></th>}{allocationIndex === 0 && <><td className="exam-plan-module" rowSpan={rows.length}>{moduleById.get(exam.subjectModuleId)?.title ?? "Module Exam"}</td><td className="exam-plan-time" rowSpan={rows.length}>{exam.startTime.slice(0, 5)} - {exam.endTime?.slice(0, 5)}</td></>}<td>{allocation ? `${allocation.roomCode} · ${allocation.roomName}` : allocationLoadingByExamId.get(exam.id) ? "Loading room..." : "Room not assigned"}</td><td>{allocation?.examGroupLabel ?? groupById.get(exam.classGroupId)?.name ?? "Class Group"}</td>{allocationIndex === 0 && <td className="exam-candidate-row-action no-print" rowSpan={rows.length}><button onClick={(event) => { event.stopPropagation(); setCandidateExam(exam); }} type="button">{exam.candidateListGeneratedAt ? "View List" : "Generate List"}</button></td>}</tr>;
        });
      })}</tbody>;
    })}</table></div>}
    {configuringGroups && examGroupPlanQuery.data && <ManagementModal title="Configure Exam Groups" description={`${examGroupPlanQuery.data.totalStudentCount} students · Choose how many room groups will share the same exam timetable.`} onClose={() => setConfiguringGroups(false)}><div className="management-form"><div className="form-field"><label>Number of exam groups</label><input max={Math.min(20, examGroupPlanQuery.data.totalStudentCount)} min="1" onChange={(event) => setSplitCount(Number(event.target.value))} type="number" value={splitCount} /></div><div className="exam-split-preview">{Array.from({ length: splitCount }, (_, index) => { const base = Math.floor(examGroupPlanQuery.data!.totalStudentCount / splitCount); const extra = index < examGroupPlanQuery.data!.totalStudentCount % splitCount ? 1 : 0; return <span key={index}><strong>{groups.find((group) => group.id === selectedClassGroupId)?.name}-E{index + 1}</strong>{base + extra} students</span>; })}</div>{groupGenerationMutation.isError && <div className="management-alert management-alert--error">{errorMessage(groupGenerationMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={() => setConfiguringGroups(false)} type="button">Cancel</button><button className="management-primary-button" disabled={groupGenerationMutation.isPending || splitCount < 1 || splitCount > examGroupPlanQuery.data.totalStudentCount} onClick={() => groupGenerationMutation.mutate()} type="button">{groupGenerationMutation.isPending ? "Generating..." : "Generate Groups"}</button></footer></div></ManagementModal>}
    {periodForm && <ManagementModal title={`${editingPeriod ? "Edit" : "Create"} ${sessionType === "NORMAL" ? "Normal" : "Rattrapage"} Exam Plan`} description={`${semesterName} · Define the examination period.`} onClose={() => { setPeriodForm(null); setEditingPeriod(false); }}><div className="management-form management-form--two-columns"><div className="form-field"><label>Start date</label><input onChange={(event) => setPeriodForm({ ...periodForm, startDate: event.target.value })} type="date" value={periodForm.startDate} /></div><div className="form-field"><label>End date</label><input onChange={(event) => setPeriodForm({ ...periodForm, endDate: event.target.value })} type="date" value={periodForm.endDate} /></div>{invalidPeriodOrder && <div className="management-alert management-alert--error">End date must be on or after the start date.</div>}{periodExcludesExam && <div className="management-alert management-alert--error">The period must include every exam already scheduled between {earliestExamDate} and {latestExamDate}.</div>}{periodMutation.isError && <div className="management-alert management-alert--error">{errorMessage(periodMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={() => { setPeriodForm(null); setEditingPeriod(false); }} type="button">Cancel</button><button className="management-primary-button" disabled={periodMutation.isPending || invalidPeriod} onClick={() => periodMutation.mutate()} type="button">{periodMutation.isPending ? "Saving..." : editingPeriod ? "Save Period" : "Create Plan"}</button></footer></div></ManagementModal>}
    {examForm && <ManagementModal size="wide" title={editingExam ? "Edit Module Exam" : "Add Module Exam"} description={`${semesterName} · ${groups.find((group) => group.id === examForm.classGroupId)?.name ?? "Class Group"} · ${sessionType === "NORMAL" ? "Normal Session" : "Rattrapage Session"}`} onClose={() => { setExamForm(null); setEditingExam(null); setAllocationRooms({}); setAllocationBlocks({}); }}>
      <form className="management-form management-form--two-columns exam-planning-form" onSubmit={(event) => { event.preventDefault(); examMutation.mutate(examForm); }}>
        <div className="form-field form-field--wide"><label>Subject Module</label><select onChange={(event) => setExamForm({ ...examForm, subjectModuleId: event.target.value })} value={examForm.subjectModuleId}>{modules.map((module) => <option key={module.id} value={module.id}>{module.code} · {module.title}</option>)}</select></div>
        <div className="form-field"><label>Class Group</label><select disabled value={examForm.classGroupId}>{groups.map((group) => <option key={group.id} value={group.id}>{group.name}</option>)}</select></div>
        <div className="form-field"><label>Exam date</label><input max={schedule?.endDate} min={schedule?.startDate} onChange={(event) => setExamForm({ ...examForm, examDate: event.target.value })} type="date" value={examForm.examDate} /></div>
        <div className="form-field"><label>Start time</label><input max="18:00" min="08:00" onChange={(event) => setExamForm({ ...examForm, startTime: event.target.value })} step="1800" type="time" value={examForm.startTime} /></div>
        <div className="form-field"><label>End time</label><input max="18:30" min={examForm.startTime} onChange={(event) => setExamForm({ ...examForm, endTime: event.target.value })} step="1800" type="time" value={examForm.endTime} /></div>
        <section className="exam-room-allocation-fields form-field--wide"><header><div><strong>Room allocation</strong><span>Assign one room to each exam group.</span></div><small>Same date and time</small></header><div className="exam-room-allocation-list">{examGroupPlanQuery.data?.groups.map((group) => {
          const selectedRoom = rooms.find((room) => room.id === allocationRooms[group.id]);
          const selectedBlock = allocationBlocks[group.id] ?? "";
          const blockRooms = rooms.filter((room) => (selectedBlock === standalone ? !room.blockId : room.blockId === selectedBlock) && room.capacity >= group.studentCount);
          return <div className="exam-room-allocation-row" key={group.id}><div className="exam-room-group"><span>{group.label}</span><strong>{group.studentCount}</strong><small>students</small></div><div className="exam-room-choice"><label htmlFor={`exam-block-${group.id}`}>Block</label><select id={`exam-block-${group.id}`} onChange={(event) => { setAllocationBlocks({ ...allocationBlocks, [group.id]: event.target.value }); setAllocationRooms({ ...allocationRooms, [group.id]: "" }); }} required value={selectedBlock}><option value="">Select a block</option>{blocks.map((block) => <option key={block.id} value={block.id}>{block.code} · {block.name}</option>)}{rooms.some((room) => !room.blockId) && <option value={standalone}>Outside blocks</option>}</select></div><div className="exam-room-choice"><label htmlFor={`exam-room-${group.id}`}>Room</label><select disabled={!selectedBlock} id={`exam-room-${group.id}`} onChange={(event) => setAllocationRooms({ ...allocationRooms, [group.id]: event.target.value })} required value={allocationRooms[group.id] ?? ""}><option value="">{selectedBlock ? "Select a suitable room" : "Select a block first"}</option>{blockRooms.map((room) => <option disabled={Object.entries(allocationRooms).some(([groupId, roomId]) => groupId !== group.id && roomId === room.id)} key={room.id} value={room.id}>{room.code} · {room.name} · {room.capacity} seats</option>)}</select></div><span className={`exam-room-fit${selectedRoom ? " is-selected" : ""}`}>{selectedRoom ? `${selectedRoom.capacity - group.studentCount} spare seats` : `Minimum ${group.studentCount} seats`}</span></div>;
        })}</div></section>
        {examMutation.isError && <div className="management-alert management-alert--error">{errorMessage(examMutation.error)}</div>}
        <footer className="form-actions">{editingExam && <button className="danger-ghost-button" onClick={() => setDeletingExam(editingExam)} type="button">Remove Exam</button>}<button className="secondary-button" onClick={() => { setExamForm(null); setAllocationRooms({}); setAllocationBlocks({}); }} type="button">Cancel</button><button className="management-primary-button" disabled={examMutation.isPending || !allocationComplete} type="submit">{examMutation.isPending ? "Saving..." : "Save Exam"}</button></footer>
      </form>
    </ManagementModal>}
    {deletingExam && <ConfirmActionModal actionLabel="Remove Exam" destructive description="Remove this module exam from the plan?" error={deleteMutation.isError ? errorMessage(deleteMutation.error) : null} isSubmitting={deleteMutation.isPending} onCancel={() => setDeletingExam(null)} onConfirm={() => deleteMutation.mutate()} title="Remove Module Exam" />}
    {confirmingPublish && <ConfirmActionModal actionLabel="Publish Plan" description="Publish this exam plan for academic visibility?" error={publishMutation.isError ? errorMessage(publishMutation.error) : null} isSubmitting={publishMutation.isPending} onCancel={() => setConfirmingPublish(false)} onConfirm={() => publishMutation.mutate()} title="Publish Exam Plan" />}
    {candidateExam && <ExamCandidateListModal academicYearLabel={academicYearLabel} classGroupName={groups.find((group) => group.id === candidateExam.classGroupId)?.name ?? "Class Group"} exam={candidateExam} moduleCode={moduleById.get(candidateExam.subjectModuleId)?.code} moduleTitle={moduleById.get(candidateExam.subjectModuleId)?.title ?? "Module Exam"} programName={programName} semesterName={semesterName} sessionType={sessionType} onClose={() => setCandidateExam(null)} onGenerated={refresh} />}
    {showingExamGroups && examGroupPlanQuery.data && <ExamGroupRosterModal academicYearLabel={academicYearLabel} classGroupName={groups.find((group) => group.id === selectedClassGroupId)?.name ?? "Class Group"} plan={examGroupPlanQuery.data} programName={programName} semesterName={semesterName} sessionType={sessionType} onClose={() => setShowingExamGroups(false)} />}
  </section>;
}
