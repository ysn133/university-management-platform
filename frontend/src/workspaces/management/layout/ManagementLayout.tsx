import { WorkspaceLayout } from "@/shared/layouts/WorkspaceLayout";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getEstablishment, rootGovernanceKeys } from "@/features/root-governance/api/root-governance-api";
import { useQuery } from "@tanstack/react-query";
import { useLocation } from "react-router-dom";
import { academicStructureKeys, getAcademicYears, getDegreeCycles, getDepartments, getProgramPaths } from "@/features/academic-structure/api/academic-structure-api";
import { getManagementNavigation } from "../navigation/management-navigation";

export function ManagementLayout() {
  const { user } = useAuth();
  const location = useLocation();
  const routeEstablishmentId = location.pathname.match(
    /^\/management\/establishments\/([^/]+)/,
  )?.[1];
  const establishmentId = routeEstablishmentId ?? (
    user?.role === "SUPER_ADMIN" || user?.role === "ADMIN"
      ? user.establishmentId
      : null
  );
  const establishmentQuery = useQuery({
    queryKey: rootGovernanceKeys.establishment(establishmentId ?? "missing"),
    queryFn: () => getEstablishment(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const academicYearRouteMatch = location.pathname.match(/\/academic-years\/([^/]+)/);
  const searchParams = new URLSearchParams(location.search);
  const academicYearId = academicYearRouteMatch?.[1] ?? searchParams.get("academicYearId");
  const isAcademicYearContext = Boolean(academicYearRouteMatch);
  const academicYearProgramPathMatch = location.pathname.match(/\/academic-years\/[^/]+\/program-paths\/([^/]+)\/programs/);
  const directProgramPathMatch = isAcademicYearContext ? null : location.pathname.match(/\/program-paths\/([^/]+)\/programs/);
  const programPathId = academicYearProgramPathMatch?.[1] ?? directProgramPathMatch?.[1] ?? searchParams.get("programPathId");
  const isDirectProgramPathContext = Boolean(directProgramPathMatch);
  const departmentContextMatch = location.pathname.match(/\/departments\/([^/]+)\/programs/);
  const degreeCycleContextMatch = location.pathname.match(/\/degree-cycles\/([^/]+)\/programs/);
  const departmentId = departmentContextMatch?.[1];
  const degreeCycleId = degreeCycleContextMatch?.[1];
  const academicYearsQuery = useQuery({
    queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"),
    queryFn: () => getAcademicYears(establishmentId!),
    enabled: Boolean(establishmentId && academicYearId),
  });
  const programPathsQuery = useQuery({
    queryKey: academicStructureKeys.programPaths(establishmentId ?? "missing"),
    queryFn: () => getProgramPaths(establishmentId!),
    enabled: Boolean(establishmentId && programPathId && (isAcademicYearContext || isDirectProgramPathContext)),
  });
  const departmentsQuery = useQuery({
    queryKey: academicStructureKeys.departments(establishmentId ?? "missing"),
    queryFn: () => getDepartments(establishmentId!),
    enabled: Boolean(establishmentId && departmentId),
  });
  const degreeCyclesQuery = useQuery({
    queryKey: academicStructureKeys.degreeCycles(establishmentId ?? "missing"),
    queryFn: () => getDegreeCycles(establishmentId!),
    enabled: Boolean(establishmentId && degreeCycleId),
  });
  const establishment = establishmentQuery.data;
  const academicYear = academicYearsQuery.data?.find((item) => item.id === academicYearId);
  const programPath = programPathsQuery.data?.find((item) => item.id === programPathId);
  const department = departmentsQuery.data?.find((item) => item.id === departmentId);
  const degreeCycle = degreeCyclesQuery.data?.find((item) => item.id === degreeCycleId);
  const contextPath = user?.role === "ROOT_SUPER_ADMIN" && establishmentId
    ? `/management/establishments/${establishmentId}`
    : "/management";
  const adminDetailMatch = location.pathname.match(/\/admins\/([^/]+)$/);
  const studentDetailMatch = location.pathname.match(/\/students\/([^/]+)$/);
  const programDetailMatch = location.pathname.match(/\/programs\/([^/]+)$/);
  const moduleDetailMatch = location.pathname.match(/\/programs\/([^/]+)\/modules\/([^/]+)$/);
  const sectionLabels: Record<string, string> = {
    "super-admins": "Super Admins",
    admins: "Admins",
    students: "Students",
    professors: "Professors",
    departments: "Departments",
    "program-paths": "Program Paths",
    "degree-cycles": "Degree Cycles",
    programs: "Programs / Filières",
    "academic-years": "Academic Years",
    "academic-rule-profiles": "Academic Settings",
    "academic-domains": "Academic Domains",
    facilities: "Facilities",
  };
  const lastPathSegment = location.pathname.split("/").filter(Boolean).at(-1) ?? "";
  const isCatalogContext = isAcademicYearContext || isDirectProgramPathContext || Boolean(departmentId) || Boolean(degreeCycleId);
  const section = isCatalogContext ? null : sectionLabels[lastPathSegment] ?? null;
  const adminsPath = user?.role === "ROOT_SUPER_ADMIN"
    ? `${contextPath}/admins`
    : "/management/admins";
  const programsPath = user?.role === "ROOT_SUPER_ADMIN"
    ? `${contextPath}/programs`
    : "/management/programs";
  const breadcrumbs = establishmentId
    ? [
        ...(user?.role === "ROOT_SUPER_ADMIN"
          ? [
              { label: "Université Ibn Zohr", to: "/management" },
              { label: "Establishments", to: "/management/establishments" },
            ]
          : []),
        { label: establishment?.name ?? "Establishment", to: contextPath },
        ...(section ? [{ label: section }] : []),
        ...(isAcademicYearContext && academicYearId
          ? [
              { label: "Academic Years", to: `${contextPath}/academic-years` },
              { label: academicYear?.label ?? "Academic year", to: `${contextPath}/academic-years/${academicYearId}/program-paths` },
              ...(programPathId
                ? [
                    { label: programPath?.name ?? "Program path", to: `${contextPath}/academic-years/${academicYearId}/program-paths/${programPathId}/programs` },
                    { label: programDetailMatch || moduleDetailMatch ? "Program curriculum" : "Programs / Filières" },
                  ]
                : [{ label: "Program Paths" }]),
            ]
          : []),
        ...(isDirectProgramPathContext && programPathId
          ? [
              { label: "Program Paths", to: `${contextPath}/program-paths` },
              { label: programPath?.name ?? "Program path", to: `${contextPath}/program-paths/${programPathId}/programs` },
              ...(programDetailMatch
                ? [{ label: "Program curriculum" }]
                : moduleDetailMatch
                ? [
                    { label: "Program curriculum", to: `${contextPath}/program-paths/${programPathId}/programs/${moduleDetailMatch[1]}` },
                    { label: "Module delivery" },
                  ]
                : [{ label: "Programs / Filières" }]),
            ]
          : []),
        ...(departmentId
          ? [
              { label: "Departments", to: `${contextPath}/departments` },
              { label: department?.name ?? "Department", to: `${contextPath}/departments/${departmentId}/programs` },
              ...(programDetailMatch
                ? [{ label: "Program curriculum" }]
                : moduleDetailMatch
                ? [
                    { label: "Program curriculum", to: `${contextPath}/departments/${departmentId}/programs/${moduleDetailMatch[1]}` },
                    { label: "Module delivery" },
                  ]
                : [{ label: "Programs / Filières" }]),
            ]
          : []),
        ...(degreeCycleId
          ? [
              { label: "Degree Cycles", to: `${contextPath}/degree-cycles` },
              { label: degreeCycle?.name ?? "Degree cycle", to: `${contextPath}/degree-cycles/${degreeCycleId}/programs` },
              ...(programDetailMatch
                ? [{ label: "Program curriculum" }]
                : moduleDetailMatch
                ? [
                    { label: "Program curriculum", to: `${contextPath}/degree-cycles/${degreeCycleId}/programs/${moduleDetailMatch[1]}` },
                    { label: "Module delivery" },
                  ]
                : [{ label: "Programs / Filières" }]),
            ]
          : []),
        ...(adminDetailMatch
          ? [
              { label: "Admins", to: adminsPath },
              { label: "Admin details" },
            ]
          : []),
        ...(studentDetailMatch
          ? [
              { label: "Students", to: `${contextPath}/students` },
              { label: "Student record" },
            ]
          : []),
        ...(!isCatalogContext && programDetailMatch
          ? [
              { label: "Programs / Filières", to: programsPath },
              { label: "Program curriculum" },
            ]
          : []),
        ...(!isCatalogContext && moduleDetailMatch
          ? [
              { label: "Programs / Filières", to: programsPath },
              { label: "Program curriculum", to: `${programsPath}/${moduleDetailMatch[1]}` },
              { label: "Module delivery" },
            ]
          : []),
      ]
    : [];

  return (
    <WorkspaceLayout
      breadcrumbs={breadcrumbs}
      context={establishmentId ? {
        name: establishment?.name ?? "Loading establishment...",
        meta: establishment
          ? `${establishment.type[0] + establishment.type.slice(1).toLowerCase()} · ${establishment.status}`
          : "Establishment context",
        monogram: establishment?.name.slice(0, 2).toUpperCase() ?? "--",
        backLabel: user?.role === "ROOT_SUPER_ADMIN" ? "Back to establishments" : undefined,
        backTo: user?.role === "ROOT_SUPER_ADMIN" ? "/management/establishments" : undefined,
      } : {
        name: "Université Ibn Zohr",
        meta: "University governance",
        monogram: "UIZ",
      }}
      navigation={getManagementNavigation(user?.role, establishmentId)}
      scopeLabel={establishmentId ? "Establishment operations" : "University governance"}
      variant="management"
      workspaceName="Management"
    />
  );
}
