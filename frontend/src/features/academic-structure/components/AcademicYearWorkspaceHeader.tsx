import { Link } from "react-router-dom";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import type { AcademicYear } from "../api/academic-structure-api";

interface AcademicYearWorkspaceHeaderProps {
  academicYear?: AcademicYear;
  academicYearId: string;
  workspacePath: string;
}

export function AcademicYearWorkspaceHeader({
  academicYear,
  academicYearId,
  workspacePath,
}: AcademicYearWorkspaceHeaderProps) {
  const programPathsPath = `${workspacePath}/academic-years/${academicYearId}/program-paths`;

  return (
    <section className="academic-year-workspace-header">
      <Link className="context-back-link" to={`${workspacePath}/academic-years`}>
        ← All academic years
      </Link>
      <header>
        <div>
          <p className="management-kicker">Academic year workspace</p>
          <h1>{academicYear?.label ?? "Selected academic year"}</h1>
          <p>Browse and manage the academic structure delivered during this year.</p>
        </div>
        {academicYear && <StatusBadge status={academicYear.status} />}
      </header>
      <nav aria-label="Academic year sections">
        <Link className="is-active" to={programPathsPath}>Program Paths</Link>
      </nav>
    </section>
  );
}
