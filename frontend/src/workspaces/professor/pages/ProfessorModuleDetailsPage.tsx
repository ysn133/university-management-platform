import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { getMyTeachingAssignments, teachingPlanKeys } from "@/features/teaching-planning/api/teaching-plan-api";
import { getMyModuleResponsibilities, professorOverviewKeys } from "../api/professor-overview-api";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The module could not be loaded.";
}

export function ProfessorModuleDetailsPage() {
  const { subjectModuleId = "" } = useParams();
  const responsibilitiesQuery = useQuery({
    queryKey: professorOverviewKeys.responsibilities(),
    queryFn: getMyModuleResponsibilities,
  });
  const assignmentsQuery = useQuery({
    queryKey: teachingPlanKeys.myAssignments(),
    queryFn: getMyTeachingAssignments,
  });
  const responsibilities = (responsibilitiesQuery.data ?? []).filter(
    (item) => item.status === "ACTIVE" && item.subjectModuleId === subjectModuleId,
  );
  const module = responsibilities[0];
  const moduleAssignments = (assignmentsQuery.data ?? []).filter(
    (item) => item.status === "ACTIVE" && item.subjectModuleId === subjectModuleId,
  );
  const academicContext = moduleAssignments[0];
  const loading = responsibilitiesQuery.isPending || assignmentsQuery.isPending;
  const loadError = responsibilitiesQuery.error ?? assignmentsQuery.error;

  return (
    <div className="management-page professor-module-details-page">
      <Link className="management-back-link" to="/professor/modules">← Back to My Modules</Link>

      {loadError && <div className="management-alert management-alert--error">{errorMessage(loadError)}</div>}
      {loading ? <div className="management-panel panel-empty">Loading module...</div> : !module ? <div className="management-panel panel-empty"><strong>Module not found.</strong><p>This module is not assigned to your account.</p></div> : <>
        <header className="management-page-header professor-module-details-header">
          <span className="professor-module-details-code">{module.subjectModuleCode}</span>
          <div><p className="management-kicker">My Modules</p><h1>{module.subjectModuleTitle}</h1><p>{academicContext?.programFiliereName ?? "Assigned module"}</p></div>
          <div className="professor-module-class-count"><strong>{responsibilities.length}</strong><span>{responsibilities.length === 1 ? "assigned class" : "assigned classes"}</span></div>
        </header>

        <section className="management-panel professor-module-classes-panel">
          <header><div><p className="management-kicker">Teaching scope</p><h2>Assigned Classes</h2><p>Classes for which you manage assessment and academic follow-up in this module.</p></div></header>
          <div className="professor-module-class-list">{responsibilities.map((responsibility) => <Link key={responsibility.id} to={`/professor/modules/${subjectModuleId}/classes/${responsibility.classGroupId}`}><span className="professor-module-class-monogram">{responsibility.classGroupName.slice(0, 2).toUpperCase()}</span><div className="professor-module-class-identity"><strong>{responsibility.classGroupName}</strong><span>{academicContext?.programFiliereName ?? "Program not available"}</span></div><div><small>Academic Level</small><strong>{academicContext?.academicLevelName ?? "Not available"}</strong></div><div><small>Semester</small><strong>{responsibility.semesterName}</strong></div><div><small>Academic Year</small><strong>{responsibility.academicYearLabel}</strong></div><span className="professor-module-directory-state">Open</span></Link>)}</div>
        </section>
      </>}
    </div>
  );
}
