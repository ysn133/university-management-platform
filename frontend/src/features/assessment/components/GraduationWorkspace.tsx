import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useState } from "react";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { generateGraduationDecisions, getGraduationDecisions } from "../api/graduation-decisions-api";
import { saveGraduationDecisionPdf } from "../utils/save-graduation-decision-pdf";

interface GraduationWorkspaceProps {
  academicLevelId: string;
  academicLevelName?: string;
  academicYearId: string;
  academicYearLabel?: string;
  studentDetailsPath: (studentId: string) => string;
}

function errorMessage(error: unknown) {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function GraduationWorkspace({ academicLevelId, academicLevelName, academicYearId, academicYearLabel, studentDetailsPath }: GraduationWorkspaceProps) {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [exporting, setExporting] = useState<string | null>(null);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const queryKey = ["graduation-decisions", academicLevelId, academicYearId];
  const decisionsQuery = useQuery({
    queryKey,
    queryFn: () => getGraduationDecisions(academicLevelId, academicYearId),
    enabled: Boolean(academicLevelId && academicYearId),
    retry: false,
  });
  const generateMutation = useMutation({
    mutationFn: () => generateGraduationDecisions(academicLevelId, academicYearId),
    onSuccess: (decisions) => queryClient.setQueryData(queryKey, decisions),
  });
  const decisions = decisionsQuery.data ?? [];
  const visibleDecisions = decisions.filter((decision) => !deferredSearch || `${decision.firstName} ${decision.lastName} ${decision.apogeeCode}`.toLowerCase().includes(deferredSearch));

  async function download(id: string, action: () => Promise<void>) {
    setExporting(id);
    try { await action(); } finally { setExporting(null); }
  }

  return <section className="progression-workspace graduation-workspace">
    <header className="progression-header">
      <div><p className="management-kicker">Program completion</p><h2>Graduation</h2><p>{academicLevelName} · {academicYearLabel}</p></div>
      <button className="management-primary-button" disabled={generateMutation.isPending} onClick={() => generateMutation.mutate()} type="button">{generateMutation.isPending ? "Calculating..." : decisions.length ? "Recalculate graduation" : "Generate graduation"}</button>
    </header>
    {(decisionsQuery.isError || generateMutation.isError) && <div className="management-alert management-alert--error">{errorMessage(generateMutation.error ?? decisionsQuery.error)}</div>}
    {decisions.length > 0 && <div className="graduation-summary"><span>Graduated students</span><strong>{decisions.length}</strong><small>Students who completed every configured level in this program.</small></div>}
    <div className="management-panel progression-register">
      {decisions.length > 0 && <div className="progression-register__toolbar"><label className="progression-search"><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Student name or Apogee" type="search" value={search} /></label><span className="progression-filter-count">{visibleDecisions.length} graduates</span></div>}
      {decisionsQuery.isPending ? <div className="panel-empty">Loading graduation decisions...</div> : decisions.length === 0 ? <div className="panel-empty"><strong>No graduation decisions generated.</strong><p>Generate after the terminal-level progression decisions are complete.</p></div> : <div className="resource-table-wrapper"><table className="resource-table progression-table"><thead><tr><th>Student</th><th>Apogee</th><th>Degree cycle</th><th>Graduation average</th><th>Status</th><th aria-label="Download" /></tr></thead><tbody>{visibleDecisions.map((decision) => <tr key={decision.id}><td><Link className="progression-student-link" to={studentDetailsPath(decision.studentId)}><span>{decision.firstName[0]}{decision.lastName[0]}</span><strong>{decision.firstName} {decision.lastName}</strong></Link></td><td>{decision.apogeeCode}</td><td>{decision.degreeCycleName}</td><td><strong>{decision.graduationAverage.toFixed(2)}</strong></td><td><span className="progression-status progression-status--level-validated">Graduated</span></td><td><button className="record-open-link progression-download-link" disabled={Boolean(exporting)} onClick={() => download(decision.id, () => saveGraduationDecisionPdf(decision))} type="button">{exporting === decision.id ? "Preparing..." : "Download"}</button></td></tr>)}</tbody></table></div>}
    </div>
  </section>;
}
