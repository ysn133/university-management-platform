import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useAuth } from "@/features/auth/hooks/useAuth";
import {
  activateEstablishment,
  deactivateEstablishment,
  getEstablishment,
  getSuperAdmins,
  rootGovernanceKeys,
  updateEstablishment,
} from "@/features/root-governance/api/root-governance-api";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { EstablishmentForm, type EstablishmentFormValues } from "@/features/root-governance/components/EstablishmentForm";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import { establishmentAdminKeys, getAdmins } from "../api/establishment-admin-api";
import { useEstablishmentScope } from "../context/useEstablishmentScope";
import { academicStructureKeys, getAcademicYears, getDepartments, getProgramPaths } from "@/features/academic-structure/api/academic-structure-api";
import { getStudentDirectory, studentRegistrationKeys } from "@/features/student-registration/api/student-registration-api";
import { getProfessors, professorManagementKeys } from "@/features/professor-management/api/professor-management-api";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The establishment workspace is unavailable.";
}

export function EstablishmentWorkspacePage() {
  const { establishmentId, isRootContext, workspacePath } = useEstablishmentScope();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [isEditOpen, setEditOpen] = useState(false);
  const [statusAction, setStatusAction] = useState<"activate" | "deactivate" | null>(null);
  const establishmentQuery = useQuery({
    queryKey: rootGovernanceKeys.establishment(establishmentId ?? "missing"),
    queryFn: () => getEstablishment(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const adminsQuery = useQuery({
    queryKey: establishmentAdminKeys.admins(establishmentId ?? "missing"),
    queryFn: () => getAdmins(establishmentId!),
    enabled: Boolean(establishmentId) && user?.role !== "ADMIN",
  });
  const superAdminsQuery = useQuery({
    queryKey: rootGovernanceKeys.superAdmins(establishmentId ?? "missing"),
    queryFn: () => getSuperAdmins(establishmentId!),
    enabled: Boolean(establishmentId) && isRootContext,
  });
  const academicYearsQuery = useQuery({
    queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"),
    queryFn: () => getAcademicYears(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const studentsQuery = useQuery({
    queryKey: studentRegistrationKeys.students(establishmentId ?? "missing", { page: 0, size: 5 }),
    queryFn: () => getStudentDirectory(establishmentId!, { page: 0, size: 5 }),
    enabled: Boolean(establishmentId),
  });
  const professorsQuery = useQuery({
    queryKey: professorManagementKeys.professors(establishmentId ?? "missing"),
    queryFn: () => getProfessors(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const departmentsQuery = useQuery({
    queryKey: academicStructureKeys.departments(establishmentId ?? "missing"),
    queryFn: () => getDepartments(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const pathsQuery = useQuery({
    queryKey: academicStructureKeys.programPaths(establishmentId ?? "missing"),
    queryFn: () => getProgramPaths(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const updateMutation = useMutation({
    mutationFn: (values: EstablishmentFormValues) => updateEstablishment(establishmentId!, values),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: rootGovernanceKeys.establishment(establishmentId!) }),
        queryClient.invalidateQueries({ queryKey: ["root-governance", "establishments"] }),
      ]);
      setEditOpen(false);
    },
  });
  const statusMutation = useMutation({
    mutationFn: (action: "activate" | "deactivate") => action === "activate"
      ? activateEstablishment(establishmentId!)
      : deactivateEstablishment(establishmentId!),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: rootGovernanceKeys.establishment(establishmentId!) }),
        queryClient.invalidateQueries({ queryKey: ["root-governance", "establishments"] }),
      ]);
      setStatusAction(null);
    },
  });

  if (!establishmentId || !workspacePath) {
    return <div className="management-state management-state--error"><h1>No establishment assigned</h1><p>This account has no establishment context.</p></div>;
  }

  if (establishmentQuery.isPending) return <div className="management-state">Loading establishment workspace...</div>;
  if (establishmentQuery.isError) {
    return <div className="management-state management-state--error"><h1>Workspace unavailable</h1><p>{errorMessage(establishmentQuery.error)}</p></div>;
  }

  const establishment = establishmentQuery.data;
  const admins = adminsQuery.data ?? [];
  const superAdmins = superAdminsQuery.data ?? [];
  const academicYears = academicYearsQuery.data ?? [];
  const currentYear = academicYears.find((year) => year.status === "ACTIVE");
  const professors = professorsQuery.data ?? [];
  const activeProfessors = professors.filter((professor) => professor.accountStatus === "ACTIVE");
  const contextPath = isRootContext ? workspacePath : "/management";
  const loadingSummary = academicYearsQuery.isPending || studentsQuery.isPending
    || professorsQuery.isPending || departmentsQuery.isPending || pathsQuery.isPending;

  return (
    <div className="management-page admin-overview-page">
      <header className="admin-overview-header">
        <div>
          <p>{establishment.name}</p>
          <h1>Overview</h1>
          <span>{establishment.type[0] + establishment.type.slice(1).toLowerCase()}</span>
        </div>
        <div className="admin-overview-header-actions">
          {currentYear && <Link to={`${contextPath}/academic-years/${currentYear.id}/program-paths`}><span>Academic year</span><strong>{currentYear.label}</strong></Link>}
          {isRootContext && <button className="secondary-button secondary-button--compact" onClick={() => setEditOpen(true)} type="button">Edit establishment</button>}
        </div>
      </header>

      <section className="admin-overview-stats" aria-label="Establishment summary">
        <Link to={`${contextPath}/students`}><span>Students</span><strong>{studentsQuery.isPending ? "—" : studentsQuery.data?.totalElements ?? 0}</strong><small>View directory</small></Link>
        <Link to={`${contextPath}/professors`}><span>Professors</span><strong>{professorsQuery.isPending ? "—" : activeProfessors.length}</strong><small>{professors.length === activeProfessors.length ? "Active accounts" : `${professors.length} total accounts`}</small></Link>
        <Link to={`${contextPath}/departments`}><span>Departments</span><strong>{departmentsQuery.isPending ? "—" : departmentsQuery.data?.length ?? 0}</strong><small>Academic structure</small></Link>
        <Link to={`${contextPath}/program-paths`}><span>Program paths</span><strong>{pathsQuery.isPending ? "—" : pathsQuery.data?.length ?? 0}</strong><small>View programs</small></Link>
      </section>

      {loadingSummary && <div className="admin-overview-loading">Loading overview...</div>}

      <section className="admin-overview-content">
        <article className="management-panel admin-year-panel">
          <header><div><h2>Academic year</h2><p>{currentYear ? "Currently active" : "No active academic year"}</p></div><Link to={`${contextPath}/academic-years`}>All years</Link></header>
          {academicYearsQuery.isError ? <div className="panel-empty panel-empty--error">Academic year data is unavailable.</div> : currentYear ? <div className="admin-year-current"><div><strong>{currentYear.label}</strong><span>Active</span></div><dl><div><dt>Starts</dt><dd>{currentYear.startYear}</dd></div><div><dt>Ends</dt><dd>{currentYear.endYear}</dd></div><div><dt>Previous years</dt><dd>{academicYears.filter((year) => year.status === "CLOSED").length}</dd></div></dl><Link to={`${contextPath}/academic-years/${currentYear.id}/program-paths`}>Open academic year <span aria-hidden="true">→</span></Link></div> : <div className="panel-empty"><strong>No active year.</strong><p>Select an academic year to continue.</p></div>}
        </article>

        <article className="management-panel admin-structure-panel">
          <header><div><h2>Academic structure</h2><p>Configured for this establishment</p></div><Link to={`${contextPath}/programs`}>Programs</Link></header>
          <div className="admin-structure-summary">
            <Link to={`${contextPath}/departments`}><div><strong>{departmentsQuery.data?.length ?? 0}</strong><span>Departments</span></div><small>{departmentsQuery.data?.slice(0, 3).map((department) => department.name).join(" · ") || "Not configured"}</small><i>→</i></Link>
            <Link to={`${contextPath}/program-paths`}><div><strong>{pathsQuery.data?.length ?? 0}</strong><span>Program paths</span></div><small>{pathsQuery.data?.map((path) => path.name).join(" · ") || "Not configured"}</small><i>→</i></Link>
            <Link to={`${contextPath}/degree-cycles`}><div><span>Degree cycles</span></div><small>Licence, Master, and other cycles</small><i>→</i></Link>
          </div>
        </article>

        <article className="management-panel admin-overview-links">
          <header><h2>Management</h2></header>
          <nav>
            <Link to={`${contextPath}/students`}><span>Students</span><small>Accounts and registrations</small><i>→</i></Link>
            <Link to={`${contextPath}/professors`}><span>Professors</span><small>Teaching staff and expertise</small><i>→</i></Link>
            <Link to={`${contextPath}/academic-rule-profiles`}><span>Academic settings</span><small>Rules and teaching preferences</small><i>→</i></Link>
            <Link to={`${contextPath}/facilities`}><span>Facilities</span><small>Blocks and rooms</small><i>→</i></Link>
          </nav>
        </article>
      </section>

      {isRootContext && <div className="admin-overview-root-actions"><StatusBadge status={establishment.status} />{establishment.status === "ACTIVE" ? <button className="danger-ghost-button" onClick={() => setStatusAction("deactivate")} type="button">Deactivate establishment</button> : <button className="management-primary-button" onClick={() => setStatusAction("activate")} type="button">Activate establishment</button>}<span>{superAdmins.length} super admins · {admins.length} admins</span></div>}

      {isEditOpen && <ManagementModal title="Edit establishment" description="Update the official establishment identity." onClose={() => setEditOpen(false)}>
        <EstablishmentForm establishment={establishment} isSubmitting={updateMutation.isPending} requestError={updateMutation.isError ? errorMessage(updateMutation.error) : null} onCancel={() => setEditOpen(false)} onSubmit={async (values) => { try { await updateMutation.mutateAsync(values); } catch { /* mutation state renders the error */ } }} />
      </ManagementModal>}

      {statusAction && <ConfirmActionModal actionLabel={statusAction === "activate" ? "Activate" : "Deactivate"} destructive={statusAction === "deactivate"} description={`${statusAction === "activate" ? "Activate" : "Deactivate"} ${establishment.name}? Existing records remain preserved.`} error={statusMutation.isError ? errorMessage(statusMutation.error) : null} isSubmitting={statusMutation.isPending} title={`${statusAction === "activate" ? "Activate" : "Deactivate"} establishment`} onCancel={() => setStatusAction(null)} onConfirm={() => statusMutation.mutate(statusAction)} />}
    </div>
  );
}
