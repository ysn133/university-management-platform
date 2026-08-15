import { useState } from "react";
import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import type { Semester, SubjectModule } from "@/features/academic-structure/api/academic-structure-api";
import { classGroupKeys, getClassGroups } from "@/features/student-registration/api/class-group-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { examPlanningKeys, getExamSchedules, getModuleExams } from "@/features/scheduling/api/exam-planning-api";
import { approveGradeSheet, getManagedGradeSheet, publishGradeSheet, reviewGradeSheet, type GradeWorkflowStatus } from "../api/grade-management-api";
import { GradeSheetReviewModal } from "./GradeSheetReviewModal";
import { clearFinalResults, generateFinalResults, getFinalResults } from "../api/final-results-api";
import { FinalResultsTable } from "./FinalResultsTable";
import { SessionGradesByStudent } from "./SessionGradesByStudent";
import { generateSemesterResults, getSemesterResults } from "../api/semester-results-api";
import { SemesterResultsTable } from "./SemesterResultsTable";

interface Props { academicLevelId: string; academicLevelName?: string; academicYearId: string; academicYearLabel?: string; establishmentId: string; moduleValidationThreshold?: number; modules: SubjectModule[]; programName?: string; programPathName?: string; semesterId: string; semesterName?: string; semesters: Semester[]; onOpenOriginalSemester?: (academicYearId: string, academicLevelId: string, semesterId: string) => void; onSelectSemester: (id: string) => void; studentDetailsPath?: (studentId: string) => string; }

function errorMessage(error: unknown) { return error instanceof ApiRequestError ? error.message : "The grade workflow could not be completed."; }
export function GradeManagementWorkspace({ academicLevelId, academicLevelName, academicYearId, academicYearLabel, establishmentId, moduleValidationThreshold, modules, onOpenOriginalSemester, programName, programPathName, semesterId, semesterName, semesters, onSelectSemester, studentDetailsPath }: Props) {
  const queryClient = useQueryClient();
  const [gradeView, setGradeView] = useState<"NORMAL" | "RATTRAPAGE" | "FINAL_MODULES" | "SEMESTER">("NORMAL");
  const [resultView, setResultView] = useState<"MODULE" | "STUDENT">("MODULE");
  const [classGroupId, setClassGroupId] = useState("");
  const [selectedExamId, setSelectedExamId] = useState("");
  const [confirmingClearResults, setConfirmingClearResults] = useState(false);
  const schedulesQuery = useQuery({ queryKey: examPlanningKeys.schedules(establishmentId), queryFn: () => getExamSchedules(establishmentId) });
  const schedule = gradeView === "FINAL_MODULES" || gradeView === "SEMESTER" ? undefined : schedulesQuery.data?.find((item) => item.semesterId === semesterId && item.sessionType === gradeView);
  const examsQuery = useQuery({ queryKey: examPlanningKeys.exams(schedule?.id ?? "missing"), queryFn: () => getModuleExams(schedule!.id), enabled: Boolean(schedule) });
  const groupsQuery = useQuery({ queryKey: classGroupKeys.groups(academicLevelId, academicYearId), queryFn: () => getClassGroups(academicLevelId, academicYearId), enabled: Boolean(academicLevelId && academicYearId) });
  const groups = (groupsQuery.data ?? []).filter((group) => group.status === "ACTIVE");
  const selectedGroupId = groups.some((group) => group.id === classGroupId) ? classGroupId : groups[0]?.id ?? "";
  const exams = (examsQuery.data ?? []).filter((exam) => exam.classGroupId === selectedGroupId && exam.candidateListGeneratedAt);
  const sheetQueries = useQueries({ queries: exams.map((exam) => ({ queryKey: ["managed-grade-sheet", exam.id], queryFn: () => getManagedGradeSheet(exam.id) })) });
  const moduleById = new Map(modules.map((module) => [module.id, module]));
  const workflowMutation = useMutation({
    mutationFn: async ({ examId, action, status }: { examId: string; action: "approve" | "publish"; status: GradeWorkflowStatus }) => {
      if (action === "publish") return publishGradeSheet(examId);
      if (status === "SUBMITTED") await reviewGradeSheet(examId);
      return approveGradeSheet(examId);
    },
    onSuccess: (sheet) => queryClient.setQueryData(["managed-grade-sheet", sheet.moduleExamId], sheet),
  });
  const selectedExamIndex = exams.findIndex((exam) => exam.id === selectedExamId);
  const selectedExam = selectedExamIndex >= 0 ? exams[selectedExamIndex] : undefined;
  const selectedSheet = selectedExamIndex >= 0 ? sheetQueries[selectedExamIndex]?.data : undefined;
  const selectedModule = selectedExam ? moduleById.get(selectedExam.subjectModuleId) : undefined;
  const finalResultsQuery = useQuery({ queryKey: ["final-results", semesterId, selectedGroupId], queryFn: () => getFinalResults(semesterId, selectedGroupId), enabled: (gradeView === "FINAL_MODULES" || gradeView === "SEMESTER") && Boolean(semesterId && selectedGroupId) });
  const generateMutation = useMutation({ mutationFn: () => generateFinalResults(semesterId, selectedGroupId), onSuccess: (results) => queryClient.setQueryData(["final-results", semesterId, selectedGroupId], results) });
  const semesterResultsQuery = useQuery({ queryKey: ["semester-results", semesterId, selectedGroupId], queryFn: () => getSemesterResults(semesterId, selectedGroupId), enabled: gradeView === "SEMESTER" && Boolean(semesterId && selectedGroupId) });
  const generateSemesterMutation = useMutation({ mutationFn: () => generateSemesterResults(semesterId, selectedGroupId), onSuccess: async (results) => { queryClient.setQueryData(["semester-results", semesterId, selectedGroupId], results); await queryClient.invalidateQueries({ queryKey: ["final-results", semesterId, selectedGroupId] }); } });
  const clearResultsMutation = useMutation({ mutationFn: () => clearFinalResults(semesterId, selectedGroupId), onSuccess: async () => { queryClient.setQueryData(["final-results", semesterId, selectedGroupId], []); queryClient.setQueryData(["semester-results", semesterId, selectedGroupId], []); setConfirmingClearResults(false); } });
  const sessionModules = exams.map((exam, index) => ({
    code: moduleById.get(exam.subjectModuleId)?.code ?? "Module",
    id: exam.subjectModuleId,
    sheet: sheetQueries[index]?.data,
    title: moduleById.get(exam.subjectModuleId)?.title ?? "Module Exam",
  }));
  const documentContext = { academicLevel: academicLevelName, academicYear: academicYearLabel, label: [programName, academicLevelName, semesterName, academicYearLabel].filter(Boolean).join(" · "), program: programName, programPath: programPathName, semester: semesterName };

  return <section className="management-panel grade-management-workspace">
    <div className="teaching-plan-context"><label><span>Semester</span><select onChange={(event) => onSelectSemester(event.target.value)} value={semesterId}>{semesters.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label><div className="exam-session-switch" role="tablist"><button aria-selected={gradeView === "NORMAL"} onClick={() => setGradeView("NORMAL")} role="tab" type="button">Normal</button><button aria-selected={gradeView === "RATTRAPAGE"} onClick={() => setGradeView("RATTRAPAGE")} role="tab" type="button">Rattrapage</button><button aria-selected={gradeView === "FINAL_MODULES"} onClick={() => setGradeView("FINAL_MODULES")} role="tab" type="button">Final Modules</button><button aria-selected={gradeView === "SEMESTER"} onClick={() => setGradeView("SEMESTER")} role="tab" type="button">Semester Results</button></div></div>
    {groups.length > 0 && <nav className="schedule-class-tabs">{groups.map((group) => <button aria-selected={selectedGroupId === group.id} key={group.id} onClick={() => setClassGroupId(group.id)} type="button"><strong>{group.name}</strong><span>Grade sheets</span></button>)}</nav>}
    <header className="panel-header panel-header--bordered"><div><p className="management-kicker">{semesterName} · {gradeView === "NORMAL" ? "Normal Session" : gradeView === "RATTRAPAGE" ? "Rattrapage" : gradeView === "FINAL_MODULES" ? "Final Modules" : "Semester Results"}</p><h2>{gradeView === "FINAL_MODULES" ? "Final Module Results" : gradeView === "SEMESTER" ? "Semester Results" : "Grade Publication"}</h2><p>{gradeView === "FINAL_MODULES" ? "Resolve each module after the Normal and Rattrapage sessions." : gradeView === "SEMESTER" ? "Apply compensation and determine each student's official semester outcome." : "Review submitted grade sheets and publish approved results to Students."}</p></div>{(gradeView === "FINAL_MODULES" || gradeView === "SEMESTER") && <div className="curriculum-header-actions">{(finalResultsQuery.data?.length ?? 0) > 0 && <button className="danger-ghost-button" disabled={clearResultsMutation.isPending} onClick={() => setConfirmingClearResults(true)} type="button">Clear results</button>}{gradeView === "FINAL_MODULES" ? <button className="management-primary-button" disabled={generateMutation.isPending || !selectedGroupId} onClick={() => generateMutation.mutate()} type="button">{generateMutation.isPending ? "Generating..." : "Generate Final Modules"}</button> : <button className="management-primary-button" disabled={generateSemesterMutation.isPending || !selectedGroupId} onClick={() => generateSemesterMutation.mutate()} type="button">{generateSemesterMutation.isPending ? "Generating..." : "Generate Semester Results"}</button>}</div>}</header>
    {(gradeView === "NORMAL" || gradeView === "RATTRAPAGE") && exams.length > 0 && <div className="final-result-view-tabs grade-result-view-tabs" role="tablist"><button aria-selected={resultView === "MODULE"} onClick={() => setResultView("MODULE")} role="tab" type="button">By Module</button><button aria-selected={resultView === "STUDENT"} onClick={() => setResultView("STUDENT")} role="tab" type="button">By Student</button></div>}
    {workflowMutation.isError && <div className="management-alert management-alert--error">{errorMessage(workflowMutation.error)}</div>}
    {gradeView === "FINAL_MODULES" ? <><>{generateMutation.isError && <div className="management-alert management-alert--error">{errorMessage(generateMutation.error)}</div>}</><FinalResultsTable documentContext={documentContext} isLoading={finalResultsQuery.isPending} results={finalResultsQuery.data ?? []} studentDetailsPath={studentDetailsPath} /></> : gradeView === "SEMESTER" ? <><>{generateSemesterMutation.isError && <div className="management-alert management-alert--error">{errorMessage(generateSemesterMutation.error)}</div>}</><SemesterResultsTable context={documentContext} finalResults={finalResultsQuery.data ?? []} isLoading={semesterResultsQuery.isPending} onOpenOriginalSemester={onOpenOriginalSemester} results={semesterResultsQuery.data ?? []} studentDetailsPath={studentDetailsPath} /></> : !schedule ? <div className="panel-empty"><strong>No examination plan for this context.</strong></div> : examsQuery.isPending || groupsQuery.isPending ? <div className="panel-empty">Loading grade sheets...</div> : exams.length === 0 ? <div className="panel-empty"><strong>No candidate-backed exams are available.</strong><p>Generate each Module Exam candidate list before managing its grades.</p></div> : resultView === "STUDENT" ? <SessionGradesByStudent context={{ ...documentContext, label: `${documentContext.label} · ${gradeView === "NORMAL" ? "Normal Session" : "Rattrapage"}` }} moduleValidationThreshold={moduleValidationThreshold} modules={sessionModules} session={gradeView === "NORMAL" ? "Normal" : "Rattrapage"} studentDetailsPath={studentDetailsPath} /> : <div className="grade-management-list">{exams.map((exam, index) => { const query = sheetQueries[index]; const sheet = query?.data; const module = moduleById.get(exam.subjectModuleId); const completed = sheet?.grades.filter((grade) => grade.gradeValue !== null).length ?? 0; return <article key={exam.id}><div className="grade-management-module"><span>{module?.code}</span><div><strong>{module?.title ?? "Module Exam"}</strong><small>{exam.examDate} · {exam.startTime.slice(0, 5)}–{exam.endTime?.slice(0, 5)}</small></div></div>{query?.isPending ? <span>Loading...</span> : query?.isError ? <span className="status-badge status-badge--inactive">Unavailable</span> : <><div className="grade-management-progress"><strong>{completed}/{sheet?.grades.length ?? 0}</strong><span>grades entered</span></div><span className={`grade-workflow-status grade-workflow-status--${sheet?.workflowStatus.toLowerCase()}`}>{sheet?.workflowStatus}</span><button className="secondary-button" onClick={() => { workflowMutation.reset(); setSelectedExamId(exam.id); }} type="button">Open Sheet</button></>}</article>; })}</div>}
    {selectedExam && selectedSheet && <GradeSheetReviewModal error={workflowMutation.isError ? errorMessage(workflowMutation.error) : undefined} isSubmitting={workflowMutation.isPending} moduleCode={selectedModule?.code} moduleTitle={selectedModule?.title ?? "Module Exam"} onApprove={() => workflowMutation.mutate({ examId: selectedExam.id, action: "approve", status: selectedSheet.workflowStatus })} onClose={() => setSelectedExamId("")} onPublish={() => workflowMutation.mutate({ examId: selectedExam.id, action: "publish", status: selectedSheet.workflowStatus })} sheet={selectedSheet} />}
    {confirmingClearResults && <ConfirmActionModal actionLabel="Clear results" destructive description={`Clear all derived module and semester results for ${semesterName ?? "this semester"} and the selected Class Group? Published source grades will remain available.`} error={clearResultsMutation.isError ? errorMessage(clearResultsMutation.error) : null} isSubmitting={clearResultsMutation.isPending} onCancel={() => setConfirmingClearResults(false)} onConfirm={() => clearResultsMutation.mutate()} title="Clear Derived Results" />}
  </section>;
}
