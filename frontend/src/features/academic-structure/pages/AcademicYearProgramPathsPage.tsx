import { useDeferredValue, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { academicStructureKeys, getAcademicYears, getProgramPaths } from "../api/academic-structure-api";
import { AcademicYearWorkspaceHeader } from "../components/AcademicYearWorkspaceHeader";

function errorMessage(error: unknown): string { return error instanceof ApiRequestError ? error.message : "The request could not be completed."; }

export function AcademicYearProgramPathsPage() {
  const { academicYearId } = useParams<{ academicYearId: string }>();
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const yearsQuery = useQuery({ queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"), queryFn: () => getAcademicYears(establishmentId!), enabled: Boolean(establishmentId) });
  const pathsQuery = useQuery({ queryKey: academicStructureKeys.programPaths(establishmentId ?? "missing"), queryFn: () => getProgramPaths(establishmentId!), enabled: Boolean(establishmentId) });
  const year = yearsQuery.data?.find((item) => item.id === academicYearId);
  const paths = (pathsQuery.data ?? []).filter((path) => path.name.toLowerCase().includes(deferredSearch));

  if (!establishmentId || !workspacePath || !academicYearId) return <div className="management-state management-state--error"><h1>Academic year context is unavailable</h1></div>;

  return <div className="management-page academic-directory-page">
    <AcademicYearWorkspaceHeader academicYear={year} academicYearId={academicYearId} workspacePath={workspacePath} />
    <header className="academic-year-section-header"><div><p className="management-kicker">Academic structure</p><h2>Program Paths</h2><p>Choose a path to browse its Programs/Filières for {year?.label ?? "this academic year"}.</p></div></header>
    <section className="directory-toolbar academic-directory-toolbar"><label className="search-field"><span>Search paths</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Regular, Excellence..." value={search} /></label><span className="directory-result-count">{paths.length} {paths.length === 1 ? "path" : "paths"}</span></section>
    <section className="management-panel directory-panel">{yearsQuery.isPending || pathsQuery.isPending ? <div className="panel-empty">Loading academic paths...</div> : yearsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(yearsQuery.error)}</div> : pathsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(pathsQuery.error)}</div> : paths.length === 0 ? <div className="panel-empty"><strong>No program paths found.</strong><p>{search ? "Try another search term." : "Create a program path before browsing its programs."}</p></div> : <div className="resource-table-wrapper"><table className="resource-table academic-resource-table"><thead><tr><th>Program path</th><th>Academic year</th><th>Action</th></tr></thead><tbody>{paths.map((path) => <tr key={path.id}><td><Link className="resource-name resource-name--link" to={`${workspacePath}/academic-years/${academicYearId}/program-paths/${path.id}/programs`}><span className="resource-monogram">{path.name.slice(0, 2).toUpperCase()}</span><strong>{path.name}</strong></Link></td><td>{year?.label ?? "Selected year"}</td><td><Link className="record-open-link" to={`${workspacePath}/academic-years/${academicYearId}/program-paths/${path.id}/programs`}>Open programs</Link></td></tr>)}</tbody></table></div>}</section>
  </div>;
}
