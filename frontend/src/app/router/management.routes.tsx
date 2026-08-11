import type { RouteObject } from "react-router-dom";
import { ManagementLayout } from "@/workspaces/management/layout/ManagementLayout";
import { ManagementOverviewPage } from "@/workspaces/management/pages/ManagementOverviewPage";
import { ChangePasswordPage } from "@/features/auth/pages/ChangePasswordPage";
import { RootOnlyRoute } from "@/features/root-governance/components/RootOnlyRoute";
import { EstablishmentsPage } from "@/features/root-governance/pages/EstablishmentsPage";
import { EstablishmentDetailsPage } from "@/features/root-governance/pages/EstablishmentDetailsPage";
import { EstablishmentAuthorityRoute } from "@/features/establishment-management/components/EstablishmentAuthorityRoute";
import { EstablishmentWorkspacePage } from "@/features/establishment-management/pages/EstablishmentWorkspacePage";
import { AdminManagementPage } from "@/features/establishment-management/pages/AdminManagementPage";
import { AdminDetailsPage } from "@/features/establishment-management/pages/AdminDetailsPage";
import { EstablishmentOperationRoute } from "@/features/academic-structure/components/EstablishmentOperationRoute";
import { AcademicYearsPage } from "@/features/academic-structure/pages/AcademicYearsPage";
import { AcademicYearProgramPathsPage } from "@/features/academic-structure/pages/AcademicYearProgramPathsPage";
import { AcademicRuleProfilesPage } from "@/features/academic-structure/pages/AcademicRuleProfilesPage";
import { AcademicDomainsPage } from "@/features/academic-structure/pages/AcademicDomainsPage";
import { ProgramFilieresPage } from "@/features/academic-structure/pages/ProgramFilieresPage";
import { ProgramCurriculumPage } from "@/features/academic-structure/pages/ProgramCurriculumPage";
import { SubjectModulePage } from "@/features/academic-structure/pages/SubjectModulePage";
import { DegreeCyclesPage, DepartmentsPage, ProgramPathsPage } from "@/features/academic-structure/pages/ReferenceCatalogPages";
import { StudentDirectoryPage } from "@/features/student-registration/pages/StudentDirectoryPage";
import { StudentDetailsPage } from "@/features/student-registration/pages/StudentDetailsPage";
import { ProfessorManagementPage } from "@/features/professor-management/pages/ProfessorManagementPage";
import { ProfessorDetailsPage } from "@/features/professor-management/pages/ProfessorDetailsPage";
import { FacilitiesPage } from "@/features/facility-management/pages/FacilitiesPage";

export const managementRoutes: RouteObject = {
  path: "/management",
  element: <ManagementLayout />,
  children: [
    { index: true, element: <ManagementOverviewPage /> },
    {
      element: <RootOnlyRoute />,
      children: [
        { path: "establishments", element: <EstablishmentsPage /> },
        { path: "establishments/:establishmentId", element: <EstablishmentWorkspacePage /> },
        { path: "establishments/:establishmentId/super-admins", element: <EstablishmentDetailsPage /> },
        { path: "establishments/:establishmentId/admins", element: <AdminManagementPage /> },
        { path: "establishments/:establishmentId/admins/:adminId", element: <AdminDetailsPage /> },
        { path: "establishments/:establishmentId/students", element: <StudentDirectoryPage /> },
        { path: "establishments/:establishmentId/students/:studentId", element: <StudentDetailsPage /> },
        { path: "establishments/:establishmentId/professors", element: <ProfessorManagementPage /> },
        { path: "establishments/:establishmentId/professors/:professorId", element: <ProfessorDetailsPage /> },
        { path: "establishments/:establishmentId/departments", element: <DepartmentsPage /> },
        { path: "establishments/:establishmentId/program-paths", element: <ProgramPathsPage /> },
        { path: "establishments/:establishmentId/program-paths/:programPathId/programs", element: <ProgramFilieresPage /> },
        { path: "establishments/:establishmentId/program-paths/:programPathId/programs/:programFiliereId", element: <ProgramCurriculumPage /> },
        { path: "establishments/:establishmentId/program-paths/:programPathId/programs/:programFiliereId/modules/:subjectModuleId", element: <SubjectModulePage /> },
        { path: "establishments/:establishmentId/degree-cycles", element: <DegreeCyclesPage /> },
        { path: "establishments/:establishmentId/programs", element: <ProgramFilieresPage /> },
        { path: "establishments/:establishmentId/programs/:programFiliereId", element: <ProgramCurriculumPage /> },
        { path: "establishments/:establishmentId/programs/:programFiliereId/modules/:subjectModuleId", element: <SubjectModulePage /> },
        { path: "establishments/:establishmentId/academic-years", element: <AcademicYearsPage /> },
        { path: "establishments/:establishmentId/academic-rule-profiles", element: <AcademicRuleProfilesPage /> },
        { path: "establishments/:establishmentId/academic-domains", element: <AcademicDomainsPage /> },
        { path: "establishments/:establishmentId/facilities", element: <FacilitiesPage /> },
        { path: "establishments/:establishmentId/academic-years/:academicYearId/program-paths", element: <AcademicYearProgramPathsPage /> },
        { path: "establishments/:establishmentId/academic-years/:academicYearId/program-paths/:programPathId/programs", element: <ProgramFilieresPage /> },
        { path: "establishments/:establishmentId/academic-years/:academicYearId/program-paths/:programPathId/programs/:programFiliereId", element: <ProgramCurriculumPage /> },
        { path: "establishments/:establishmentId/academic-years/:academicYearId/program-paths/:programPathId/programs/:programFiliereId/modules/:subjectModuleId", element: <SubjectModulePage /> },
      ],
    },
    {
      element: <EstablishmentAuthorityRoute />,
      children: [
        { path: "admins", element: <AdminManagementPage /> },
        { path: "admins/:adminId", element: <AdminDetailsPage /> },
      ],
    },
    {
      element: <EstablishmentOperationRoute />,
      children: [
        { path: "departments", element: <DepartmentsPage /> },
        { path: "program-paths", element: <ProgramPathsPage /> },
        { path: "program-paths/:programPathId/programs", element: <ProgramFilieresPage /> },
        { path: "program-paths/:programPathId/programs/:programFiliereId", element: <ProgramCurriculumPage /> },
        { path: "program-paths/:programPathId/programs/:programFiliereId/modules/:subjectModuleId", element: <SubjectModulePage /> },
        { path: "degree-cycles", element: <DegreeCyclesPage /> },
        { path: "programs", element: <ProgramFilieresPage /> },
        { path: "programs/:programFiliereId", element: <ProgramCurriculumPage /> },
        { path: "programs/:programFiliereId/modules/:subjectModuleId", element: <SubjectModulePage /> },
        { path: "academic-years", element: <AcademicYearsPage /> },
        { path: "academic-rule-profiles", element: <AcademicRuleProfilesPage /> },
        { path: "academic-domains", element: <AcademicDomainsPage /> },
        { path: "facilities", element: <FacilitiesPage /> },
        { path: "academic-years/:academicYearId/program-paths", element: <AcademicYearProgramPathsPage /> },
        { path: "academic-years/:academicYearId/program-paths/:programPathId/programs", element: <ProgramFilieresPage /> },
        { path: "academic-years/:academicYearId/program-paths/:programPathId/programs/:programFiliereId", element: <ProgramCurriculumPage /> },
        { path: "academic-years/:academicYearId/program-paths/:programPathId/programs/:programFiliereId/modules/:subjectModuleId", element: <SubjectModulePage /> },
        { path: "students", element: <StudentDirectoryPage /> },
        { path: "students/:studentId", element: <StudentDetailsPage /> },
        { path: "professors", element: <ProfessorManagementPage /> },
        { path: "professors/:professorId", element: <ProfessorDetailsPage /> },
      ],
    },
    { path: "account/password", element: <ChangePasswordPage /> },
  ],
};
