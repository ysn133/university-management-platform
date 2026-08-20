import { useDeferredValue, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { getMyTeachingAssignments, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getMyExams, professorOverviewKeys, type ProfessorExam } from "../api/professor-overview-api";

interface ClassGradeContext {
  classGroupId: string;
  classGroupName: string;
  academicLevelName: string;
  programFiliereCode: string;
  programFiliereName: string;
  modules: ModuleGradeContext[];
}

interface ModuleGradeContext {
  subjectModuleId: string;
  subjectModuleCode: string;
  subjectModuleTitle: string;
  normalExam?: ProfessorExam;
  rattrapageExam?: ProfessorExam;
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "Your grade records could not be loaded.";
}

function groupByClass(exams: ProfessorExam[], search: string): ClassGradeContext[] {
  const classes = new Map<string, ClassGradeContext>();
  exams.forEach((exam) => {
    const classContext = classes.get(exam.classGroupId) ?? {
      classGroupId: exam.classGroupId,
      classGroupName: exam.classGroupName,
      academicLevelName: exam.academicLevelName,
      programFiliereCode: exam.programFiliereCode,
      programFiliereName: exam.programFiliereName,
      modules: [],
    };
    let module = classContext.modules.find((item) => item.subjectModuleId === exam.subjectModuleId);
    if (!module) {
      module = { subjectModuleId: exam.subjectModuleId, subjectModuleCode: exam.subjectModuleCode, subjectModuleTitle: exam.subjectModuleTitle };
      classContext.modules.push(module);
    }
    if (exam.sessionType === "NORMAL") module.normalExam = exam;
    else module.rattrapageExam = exam;
    classes.set(exam.classGroupId, classContext);
  });
  return Array.from(classes.values()).map((context) => ({
    ...context,
    modules: context.modules.filter((module) => !search || `${context.classGroupName} ${context.programFiliereCode} ${context.programFiliereName} ${context.academicLevelName} ${module.subjectModuleCode} ${module.subjectModuleTitle}`.toLowerCase().includes(search)),
  })).filter((context) => context.modules.length > 0).sort((a, b) => a.classGroupName.localeCompare(b.classGroupName));
}

function gradeHref(module: ModuleGradeContext, classGroupId: string, session: "NORMAL" | "RATTRAPAGE" | "FINAL"): string {
  const exam = session === "RATTRAPAGE" ? module.rattrapageExam : module.normalExam;
  const params = new URLSearchParams({ tab: "grades", session, from: "grades" });
  if (exam) params.set("examId", exam.id);
  return `/professor/grades/modules/${module.subjectModuleId}/classes/${classGroupId}?${params}`;
}

export function ProfessorGradesPage() {
  const [requestedYearId, setRequestedYearId] = useState("");
  const [requestedTerm, setRequestedTerm] = useState<"AUTUMN" | "SPRING" | "">("");
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const examsQuery = useQuery({ queryKey: professorOverviewKeys.exams(), queryFn: getMyExams });
  const assignmentsQuery = useQuery({ queryKey: teachingPlanKeys.myAssignments(), queryFn: getMyTeachingAssignments });
  const exams = examsQuery.data ?? [];
  const academicYears = Array.from(new Map(exams.map((exam) => [exam.academicYearId, exam.academicYearLabel])).entries());
  const academicYearId = academicYears.some(([id]) => id === requestedYearId)
    ? requestedYearId
    : exams.find((exam) => exam.academicYearStatus === "ACTIVE")?.academicYearId ?? academicYears.at(-1)?.[0] ?? "";
  const yearExams = exams.filter((exam) => exam.academicYearId === academicYearId);
  const termBySemester = new Map((assignmentsQuery.data ?? []).map((assignment) => [assignment.semesterId, assignment.semesterTermType]));
  const terms = Array.from(new Set(yearExams.map((exam) => termBySemester.get(exam.semesterId)).filter((term): term is "AUTUMN" | "SPRING" => Boolean(term))));
  const activeTerm = (assignmentsQuery.data ?? []).find((assignment) => assignment.academicYearId === academicYearId && assignment.semesterLifecycleStatus === "ACTIVE")?.semesterTermType;
  const term = requestedTerm && terms.includes(requestedTerm) ? requestedTerm : activeTerm && terms.includes(activeTerm) ? activeTerm : terms[0] ?? "";
  const termExams = yearExams.filter((exam) => termBySemester.get(exam.semesterId) === term);
  const classContexts = groupByClass(termExams, deferredSearch);
  const selectedYearLabel = academicYears.find(([id]) => id === academicYearId)?.[1];
  const selectedTermLabel = term === "AUTUMN" ? "Autumn" : term === "SPRING" ? "Spring" : "Academic term";

  return <div className="management-page professor-grades-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Assessment workspace</p><h1>Grades</h1><p>Open grade sheets through the Class Groups assigned to you.</p></div></header>
    {(examsQuery.error || assignmentsQuery.error) && <div className="management-alert management-alert--error">{errorMessage(examsQuery.error ?? assignmentsQuery.error)}</div>}
    <section className="management-panel professor-grades-panel">
      <header className="professor-grades-panel-header"><div><p className="management-kicker">Current academic context</p><h2>Class Gradebooks</h2><p>{selectedYearLabel} · {selectedTermLabel}</p></div><span><strong>{classContexts.length}</strong> Class Groups</span></header>
      <div className="professor-grades-context professor-grades-context--classes">
        <label><span>Academic year</span><select onChange={(event) => { setRequestedYearId(event.target.value); setRequestedTerm(""); }} value={academicYearId}>{academicYears.map(([id, label]) => <option key={id} value={id}>{label}</option>)}</select></label>
        <label><span>Academic term</span><select onChange={(event) => setRequestedTerm(event.target.value as "AUTUMN" | "SPRING")} value={term}>{terms.map((item) => <option key={item} value={item}>{item === "AUTUMN" ? "Autumn" : "Spring"}</option>)}</select></label>
        <label className="professor-grades-search"><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Module, class, program, or level" value={search} /></label>
      </div>
      {examsQuery.isPending || assignmentsQuery.isPending ? <div className="panel-empty">Loading Class Groups...</div> : termExams.length === 0 ? <div className="panel-empty"><strong>No published examinations for this academic term.</strong></div> : classContexts.length === 0 ? <div className="panel-empty"><strong>No Class Group matches your search.</strong></div> : <div className="professor-grade-class-grid">{classContexts.map((context) => <article className="professor-grade-class-card" key={context.classGroupId}>
        <header><div className="professor-grade-class-mark">{context.classGroupName.replace(/^group\s*/i, "G")}</div><div><span>{context.programFiliereCode} · {context.academicLevelName}</span><h2>{context.classGroupName}</h2><p>{context.programFiliereName}</p></div><strong>{context.modules.length}<small>{context.modules.length === 1 ? "module" : "modules"}</small></strong></header>
        <div className="professor-grade-class-modules">{context.modules.sort((a, b) => a.subjectModuleTitle.localeCompare(b.subjectModuleTitle)).map((module) => <div key={module.subjectModuleId}><span className="professor-grade-directory-code">{module.subjectModuleCode}</span><div><strong>{module.subjectModuleTitle}</strong><small>{module.normalExam ? `Normal exam · ${module.normalExam.examDate}` : "No Normal Session exam"}</small></div><nav aria-label={`${module.subjectModuleTitle} grade sheets`}><Link to={gradeHref(module, context.classGroupId, "NORMAL")}>Normal</Link>{module.rattrapageExam ? <Link to={gradeHref(module, context.classGroupId, "RATTRAPAGE")}>Rattrapage</Link> : <span>Rattrapage</span>}<Link to={gradeHref(module, context.classGroupId, "FINAL")}>Final</Link></nav></div>)}</div>
      </article>)}</div>}
    </section>
  </div>;
}
