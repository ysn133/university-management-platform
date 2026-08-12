import type { RouteObject } from "react-router-dom";
import { ProfessorLayout } from "@/workspaces/professor/layout/ProfessorLayout";
import { ProfessorOverviewPage } from "@/workspaces/professor/pages/ProfessorOverviewPage";
import { ProfessorModulesPage } from "@/workspaces/professor/pages/ProfessorModulesPage";
import { ProfessorModuleDetailsPage } from "@/workspaces/professor/pages/ProfessorModuleDetailsPage";
import { ProfessorClassPage } from "@/workspaces/professor/pages/ProfessorClassPage";
import { ProfessorExamSchedulePage } from "@/workspaces/professor/pages/ProfessorExamSchedulePage";
import { ChangePasswordPage } from "@/features/auth/pages/ChangePasswordPage";

export const professorRoutes: RouteObject = {
  path: "/professor",
  element: <ProfessorLayout />,
  children: [
    { index: true, element: <ProfessorOverviewPage /> },
    { path: "modules", element: <ProfessorModulesPage /> },
    { path: "modules/:subjectModuleId", element: <ProfessorModuleDetailsPage /> },
    { path: "modules/:subjectModuleId/classes/:classGroupId", element: <ProfessorClassPage /> },
    { path: "exams", element: <ProfessorExamSchedulePage /> },
    { path: "account/password", element: <ChangePasswordPage /> },
  ],
};
