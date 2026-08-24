import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  getMyAcademicContexts,
  getMyModuleRegistrations,
  studentOverviewKeys,
} from "../api/student-overview-api";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "Your module registrations could not be loaded.";
}

export function StudentStudiesPage() {
  const [academicYearId, setAcademicYearId] = useState("");
  const [semesterId, setSemesterId] = useState("");
  const contextsQuery = useQuery({ queryKey: studentOverviewKeys.academicContexts(), queryFn: getMyAcademicContexts });
  const modulesQuery = useQuery({ queryKey: studentOverviewKeys.moduleRegistrations(), queryFn: getMyModuleRegistrations });
  const contexts = contextsQuery.data ?? [];
  const modules = modulesQuery.data ?? [];
  const years = Array.from(new Map(contexts.map((context) => [context.academicYearId, {
    id: context.academicYearId,
    label: context.academicYearLabel,
    status: context.academicYearStatus,
  }])).values());

  useEffect(() => {
    if (academicYearId || !years.length) return;
    setAcademicYearId(years.find((year) => year.status === "ACTIVE")?.id ?? years[0].id);
  }, [academicYearId, years]);

  const yearContexts = contexts
    .filter((context) => context.academicYearId === academicYearId)
    .sort((left, right) => left.semesterStartDate.localeCompare(right.semesterStartDate));

  useEffect(() => {
    if (!academicYearId || !yearContexts.length) return;
    const today = new Date().toISOString().slice(0, 10);
    const current = yearContexts.find((context) => context.semesterStartDate <= today && context.semesterEndDate >= today);
    setSemesterId((selected) => yearContexts.some((context) => context.semesterId === selected)
      ? selected
      : current?.semesterId ?? yearContexts[0].semesterId);
  }, [academicYearId, yearContexts.map((context) => context.semesterId).join(",")]);

  const selectedContext = yearContexts.find((context) => context.semesterId === semesterId);
  const visibleModules = modules
    .filter((module) => module.academicYearId === academicYearId && module.semesterId === semesterId)
    .sort((left, right) => left.subjectModuleCode.localeCompare(right.subjectModuleCode));
  const repeatedModules = visibleModules.filter((module) => module.inscriptionNumber > 1);
  const error = contextsQuery.error ?? modulesQuery.error;

  return <div className="management-page student-studies-page">
    <header className="management-page-header">
      <div><p className="management-kicker">Academic registration</p><h1>My Studies</h1><p>Modules included in your current and previous registrations.</p></div>
      {selectedContext && <div className="student-grades-current"><span>Selected context</span><strong>{selectedContext.academicYearLabel}</strong><small>{selectedContext.academicLevelName} · {selectedContext.semesterName}</small></div>}
    </header>

    {error && <div className="management-alert management-alert--error">{errorMessage(error)}</div>}

    <section className="management-panel student-studies-panel">
      <div className="student-studies-toolbar">
        <div className="student-workspace-tabs" role="tablist">
          {yearContexts.map((context) => <button aria-selected={semesterId === context.semesterId} key={context.semesterRegistrationId} onClick={() => setSemesterId(context.semesterId)} role="tab" type="button">{context.semesterName}</button>)}
        </div>
        <label className="student-studies-year"><span>Academic year</span><select disabled={contextsQuery.isPending || !years.length} onChange={(event) => { setAcademicYearId(event.target.value); setSemesterId(""); }} value={academicYearId}>{years.map((year) => <option key={year.id} value={year.id}>{year.label}</option>)}</select></label>
      </div>

      {selectedContext && <header className="student-studies-context-header">
        <div><span>{selectedContext.programPathName}</span><h2>{selectedContext.programFiliereName}</h2><p>{selectedContext.programFiliereCode} · {selectedContext.academicLevelName} · {selectedContext.semesterName}</p></div>
        <dl><div><dt>Class</dt><dd>{selectedContext.classGroupName ?? "Not assigned"}</dd></div><div><dt>Modules</dt><dd>{visibleModules.length}</dd></div>{repeatedModules.length > 0 && <div className="is-warning"><dt>Second inscription</dt><dd>{repeatedModules.length}</dd></div>}</dl>
      </header>}

      {contextsQuery.isPending || modulesQuery.isPending ? <div className="panel-empty">Loading your studies...</div>
        : !selectedContext ? <div className="panel-empty"><strong>No academic registration is available.</strong></div>
        : visibleModules.length === 0 ? <div className="panel-empty"><strong>No module registration is available for this semester.</strong></div>
        : <div className="student-studies-table-wrap"><table className="student-studies-table"><thead><tr><th>Module</th><th>Registration</th><th>Status</th></tr></thead><tbody>{visibleModules.map((module) => <tr className={module.inscriptionNumber > 1 ? "is-repeated" : ""} key={module.moduleRegistrationId}><td><span>{module.subjectModuleCode}</span><strong>{module.subjectModuleTitle}</strong></td><td>{module.inscriptionNumber > 1 ? <div><strong>Second inscription</strong><small>{module.originAcademicLevelName ? `From ${module.originAcademicLevelName}` : "Carried module"}</small></div> : <span className="student-inscription-label">First inscription</span>}</td><td><span className={`student-study-status student-study-status--${module.status.toLowerCase()}`}>{module.status.toLowerCase()}</span></td></tr>)}</tbody></table></div>}
    </section>
  </div>;
}
