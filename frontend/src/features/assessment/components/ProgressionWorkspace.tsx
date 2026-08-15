import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  generateProgressionDecisions,
  getProgressionDecisions,
  type ProgressionDecision,
} from "../api/progression-decisions-api";
import { saveAllAcademicYearResultsPdf, saveStudentAcademicYearResultPdf } from "../utils/save-academic-year-results-pdf";

interface ProgressionWorkspaceProps {
  academicLevelId: string;
  academicLevelName?: string;
  academicYearId: string;
  academicYearLabel?: string;
  terminalLevel?: boolean;
  studentDetailsPath: (studentId: string) => string;
}

const statusLabels: Record<ProgressionDecision["decisionStatus"], string> = {
  PROMOTED: "Promoted",
  PROMOTED_BY_COMPENSATION: "Promoted by compensation",
  PROMOTED_WITH_DEBT: "Promoted with debt",
  LEVEL_VALIDATED: "Level validated",
  REPEAT: "Repeat level",
  FAILED: "Failed",
};
const allStatuses = Object.keys(statusLabels) as ProgressionDecision["decisionStatus"][];
const terminalStatuses: ProgressionDecision["decisionStatus"][] = ["LEVEL_VALIDATED", "REPEAT", "FAILED"];

function errorMessage(error: unknown) {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function ProgressionWorkspace({
  academicLevelId,
  academicLevelName,
  academicYearId,
  academicYearLabel,
  terminalLevel = false,
  studentDetailsPath,
}: ProgressionWorkspaceProps) {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<ProgressionDecision["decisionStatus"] | "ALL">("ALL");
  const [exporting, setExporting] = useState<string | null>(null);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const applicableStatuses = terminalLevel ? terminalStatuses : allStatuses;
  useEffect(() => {
    if (terminalLevel && status !== "ALL" && !terminalStatuses.includes(status)) {
      setStatus("ALL");
    }
  }, [status, terminalLevel]);
  const queryKey = ["progression-decisions", academicLevelId, academicYearId];
  const decisionsQuery = useQuery({
    queryKey,
    queryFn: () => getProgressionDecisions(academicLevelId, academicYearId),
    enabled: Boolean(academicLevelId && academicYearId),
    retry: false,
  });
  const generateMutation = useMutation({
    mutationFn: () => generateProgressionDecisions(academicLevelId, academicYearId),
    onSuccess: (decisions) => queryClient.setQueryData(queryKey, decisions),
  });
  const decisions = decisionsQuery.data ?? [];
  const visibleDecisions = decisions.filter((decision) => {
    const matchesStatus = status === "ALL" || decision.decisionStatus === status;
    const identity = `${decision.firstName} ${decision.lastName} ${decision.apogeeCode}`.toLowerCase();
    return matchesStatus && (!deferredSearch || identity.includes(deferredSearch));
  });
  const count = (value: ProgressionDecision["decisionStatus"]) => decisions.filter((item) => item.decisionStatus === value).length;
  const exportPdf = async (key: string, action: () => Promise<void>) => {
    setExporting(key);
    try { await action(); } finally { setExporting(null); }
  };

  return <section className="progression-workspace">
    <header className="progression-header">
      <div>
        <p className="management-kicker">Annual academic decision</p>
        <h2>Progression</h2>
        <p>{academicLevelName ?? "Academic level"} · {academicYearLabel ?? "Academic year"}</p>
      </div>
      <div className="progression-header__actions">
        {decisions.length > 0 && <button className="record-open-link progression-download-link" disabled={Boolean(exporting)} onClick={() => exportPdf("all", () => saveAllAcademicYearResultsPdf(decisions))} type="button">{exporting === "all" ? "Preparing PDF..." : "Download all"}</button>}
        <button className="management-primary-button" disabled={generateMutation.isPending || !academicLevelId || !academicYearId} onClick={() => generateMutation.mutate()} type="button">
          {generateMutation.isPending ? "Calculating..." : decisions.length ? "Recalculate decisions" : "Generate decisions"}
        </button>
      </div>
    </header>

    {(decisionsQuery.isError || generateMutation.isError) && <div className="management-alert management-alert--error">{errorMessage(generateMutation.error ?? decisionsQuery.error)}</div>}

    {decisions.length > 0 && <div className="progression-summary">
      <article><span>Total decisions</span><strong>{decisions.length}</strong></article>
      {!terminalLevel && <article className="is-promoted"><span>Promoted</span><strong>{count("PROMOTED")}</strong></article>}
      {!terminalLevel && <article className="is-compensated"><span>By compensation</span><strong>{count("PROMOTED_BY_COMPENSATION")}</strong></article>}
      {!terminalLevel && <article className="is-debt"><span>With module debt</span><strong>{count("PROMOTED_WITH_DEBT")}</strong></article>}
      <article className="is-promoted"><span>Level validated</span><strong>{count("LEVEL_VALIDATED")}</strong></article>
      <article className="is-repeat"><span>Repeat level</span><strong>{count("REPEAT")}</strong></article>
      <article className="is-failed"><span>Failed</span><strong>{count("FAILED")}</strong></article>
    </div>}

    <div className="management-panel progression-register">
      <div className="progression-register__toolbar">
        <label className="progression-search"><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Student name or Apogee" type="search" value={search} /></label>
        <label className="progression-filter"><span>Decision</span><select onChange={(event) => setStatus(event.target.value as typeof status)} value={status}><option value="ALL">All decisions</option>{applicableStatuses.map((value) => <option key={value} value={value}>{statusLabels[value]}</option>)}</select></label>
        <span className="progression-filter-count">{visibleDecisions.length} of {decisions.length} students</span>
      </div>
      {decisionsQuery.isPending ? <div className="panel-empty">Loading progression decisions...</div>
        : decisions.length === 0 ? <div className="panel-empty"><strong>No progression decisions generated.</strong><p>Complete both semester results, then generate the annual decisions for this level.</p></div>
        : <div className="resource-table-wrapper"><table className="resource-table progression-table"><thead><tr><th>Student</th><th>Apogee</th><th>Annual average</th><th>Outstanding modules</th><th>Decision</th><th aria-label="Download" /></tr></thead><tbody>{visibleDecisions.map((decision) => <tr key={decision.id}><td><Link className="progression-student-link" to={studentDetailsPath(decision.studentId)}><span>{decision.firstName[0]}{decision.lastName[0]}</span><strong>{decision.firstName} {decision.lastName}</strong></Link></td><td>{decision.apogeeCode}</td><td><strong>{decision.annualAverage.toFixed(2)}</strong></td><td>{decision.outstandingModuleCount}</td><td><span className={`progression-status progression-status--${decision.decisionStatus.toLowerCase().replaceAll("_", "-")}`}>{statusLabels[decision.decisionStatus]}</span></td><td><button className="record-open-link progression-download-link" disabled={Boolean(exporting)} onClick={() => exportPdf(decision.id, () => saveStudentAcademicYearResultPdf(decision))} type="button">{exporting === decision.id ? "Preparing..." : "Download"}</button></td></tr>)}{visibleDecisions.length === 0 && <tr><td className="progression-empty-row" colSpan={6}>No decisions match the current filters.</td></tr>}</tbody></table></div>}
    </div>
  </section>;
}
