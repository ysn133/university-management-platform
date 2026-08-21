import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getMyTeachingAssignments, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { getMyModuleResponsibilities, professorOverviewKeys } from "../api/professor-overview-api";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "Your modules could not be loaded.";
}

export function ProfessorModulesPage() {
  const [academicYearId, setAcademicYearId] = useState("");
  const [academicLevelId, setAcademicLevelId] = useState("");
  const [semesterId, setSemesterId] = useState("CURRENT");
  const responsibilitiesQuery = useQuery({
    queryKey: professorOverviewKeys.responsibilities(),
    queryFn: getMyModuleResponsibilities,
  });
  const assignmentsQuery = useQuery({
    queryKey: teachingPlanKeys.myAssignments(),
    queryFn: getMyTeachingAssignments,
  });
  const activeAssignments = (assignmentsQuery.data ?? []).filter((item) => item.status === "ACTIVE");
  const assignmentByContext = new Map(activeAssignments.map((assignment) => [`${assignment.subjectModuleId}:${assignment.academicYearId}:${assignment.semesterId}`, assignment]));
  const activeResponsibilities = (responsibilitiesQuery.data ?? []).filter((item) => item.status === "ACTIVE");
  const modules = Array.from(
    activeResponsibilities.reduce((items, responsibility) => {
      const key = `${responsibility.subjectModuleId}:${responsibility.academicYearId}:${responsibility.semesterId}`;
      const existing = items.get(key);
      if (existing) {
        existing.classGroups.add(responsibility.classGroupName);
      } else {
        items.set(key, {
          ...responsibility,
          classGroups: new Set([responsibility.classGroupName]),
          academicContext: assignmentByContext.get(`${responsibility.subjectModuleId}:${responsibility.academicYearId}:${responsibility.semesterId}`),
        });
      }
      return items;
    }, new Map<string, (typeof activeResponsibilities)[number] & { classGroups: Set<string>; academicContext: (typeof activeAssignments)[number] | undefined }>()).values(),
  );
  const academicYears = Array.from(new Map(modules.map((module) => [module.academicYearId, module.academicYearLabel])).entries());
  const activeYearId = activeAssignments.find((assignment) => assignment.academicYearStatus === "ACTIVE")?.academicYearId;
  const selectedAcademicYearId = academicYearId === "ALL" ? "ALL" : academicYearId || activeYearId || academicYears[0]?.[0] || "ALL";
  const selectedYearAssignments = activeAssignments.filter((assignment) => selectedAcademicYearId === "ALL" || assignment.academicYearId === selectedAcademicYearId);
  const availableTerms = Array.from(new Set(selectedYearAssignments.map((assignment) => assignment.semesterTermType)));
  const currentTerm = selectedYearAssignments.find((assignment) => assignment.semesterLifecycleStatus === "ACTIVE")?.semesterTermType ?? availableTerms[0];
  const currentSemesterIds = new Set(selectedYearAssignments.filter((assignment) => assignment.semesterTermType === currentTerm).map((assignment) => assignment.semesterId));
  const yearModules = modules.filter((module) => selectedAcademicYearId === "ALL" || module.academicYearId === selectedAcademicYearId);
  const academicLevels = Array.from(new Map(yearModules.flatMap((module) => module.academicContext ? [[module.academicContext.academicLevelId, module.academicContext.academicLevelName] as const] : [])).entries());
  const semesters = Array.from(new Map(yearModules.filter((module) => !academicLevelId || module.academicContext?.academicLevelId === academicLevelId).map((module) => [module.semesterId, module.semesterName])).entries());
  const filteredModules = modules.filter((module) =>
    (selectedAcademicYearId === "ALL" || module.academicYearId === selectedAcademicYearId)
    && (!academicLevelId || module.academicContext?.academicLevelId === academicLevelId)
    && (semesterId === "ALL" || (semesterId === "CURRENT" ? currentSemesterIds.has(module.semesterId) : module.semesterId === semesterId)),
  );
  const loading = responsibilitiesQuery.isPending || assignmentsQuery.isPending;
  const loadError = responsibilitiesQuery.error ?? assignmentsQuery.error;

  return (
    <div className="management-page professor-modules-page">
      <header className="management-page-header management-page-header--compact">
        <div>
          <p className="management-kicker">Academic work</p>
          <h1>Module Responsibilities</h1>
          <p>Modules where you manage assessment and academic follow-up for assigned classes.</p>
        </div>
        <div className="professor-modules-total"><strong>{filteredModules.length}</strong><span>{filteredModules.length === modules.length ? "active modules" : `of ${modules.length} modules`}</span></div>
      </header>

      {loadError && <div className="management-alert management-alert--error">{errorMessage(loadError)}</div>}

      <section className="management-panel professor-modules-panel">
        <header><div><p className="management-kicker">Current responsibility</p><h2>Responsibility Directory</h2></div><span>Assessment ownership</span></header>
        <div className="professor-module-filters">
          <label><span>Academic Year</span><select onChange={(event) => { setAcademicYearId(event.target.value); setAcademicLevelId(""); setSemesterId("CURRENT"); }} value={selectedAcademicYearId}><option value="ALL">All academic years</option>{academicYears.map(([id, label]) => <option key={id} value={id}>{label}</option>)}</select></label>
          <label><span>Academic Level</span><select onChange={(event) => { setAcademicLevelId(event.target.value); setSemesterId("CURRENT"); }} value={academicLevelId}><option value="">All academic levels</option>{academicLevels.map(([id, name]) => <option key={id} value={id}>{name}</option>)}</select></label>
          <label><span>Semester</span><select onChange={(event) => setSemesterId(event.target.value)} value={semesterId}><option value="CURRENT">Current period ({currentTerm === "SPRING" ? "Spring" : "Autumn"})</option><option value="ALL">All semesters</option>{semesters.map(([id, name]) => <option key={id} value={id}>{name}</option>)}</select></label>
          {(academicYearId || academicLevelId || semesterId !== "CURRENT") && <button onClick={() => { setAcademicYearId(""); setAcademicLevelId(""); setSemesterId("CURRENT"); }} type="button">Current context</button>}
        </div>
        {loading ? <div className="panel-empty">Loading your modules...</div> : modules.length === 0 ? <div className="panel-empty"><strong>No active modules assigned.</strong><p>Your responsible modules will appear here after Course assignments are confirmed.</p></div> : filteredModules.length === 0 ? <div className="panel-empty"><strong>No modules match these filters.</strong><p>Change or clear the academic filters.</p></div> : <div className="professor-module-directory">{filteredModules.map((module) => <Link key={`${module.subjectModuleId}:${module.academicYearId}:${module.semesterId}`} to={`/professor/modules/${module.subjectModuleId}`}><span className="professor-module-directory-code">{module.subjectModuleCode}</span><div className="professor-module-directory-main"><strong>{module.subjectModuleTitle}</strong><span>{module.academicContext?.programFiliereCode} · {module.academicContext?.academicLevelName} · {module.semesterName} · {module.academicYearLabel}</span></div><div><small>Assigned classes</small><strong>{Array.from(module.classGroups).join(", ")}</strong></div><span className="professor-module-directory-state">Active</span></Link>)}</div>}
      </section>
    </div>
  );
}
