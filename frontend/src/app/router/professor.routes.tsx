import type { RouteObject } from "react-router-dom";
import { ProfessorLayout } from "@/workspaces/professor/layout/ProfessorLayout";
import { ProfessorOverviewPage } from "@/workspaces/professor/pages/ProfessorOverviewPage";
import { ProfessorModulesPage } from "@/workspaces/professor/pages/ProfessorModulesPage";
import { ProfessorModuleDetailsPage } from "@/workspaces/professor/pages/ProfessorModuleDetailsPage";
import { ProfessorClassPage } from "@/workspaces/professor/pages/ProfessorClassPage";
import { ProfessorExamSchedulePage } from "@/workspaces/professor/pages/ProfessorExamSchedulePage";
import { ProfessorSchedulePage } from "@/workspaces/professor/pages/ProfessorSchedulePage";
import { ProfessorGradesPage } from "@/workspaces/professor/pages/ProfessorGradesPage";
import { ProfessorGradeDetailsPage } from "@/workspaces/professor/pages/ProfessorGradeDetailsPage";
import { ProfessorAttendancePage } from "@/workspaces/professor/pages/ProfessorAttendancePage";
import { ProfessorTeachingPage } from "@/workspaces/professor/pages/ProfessorTeachingPage";
import { ProfessorTeachingDetailsPage } from "@/workspaces/professor/pages/ProfessorTeachingDetailsPage";
import { ChangePasswordPage } from "@/features/auth/pages/ChangePasswordPage";

export const professorRoutes: RouteObject = {
  path: "/professor",
  element: <ProfessorLayout />,
  children: [
    { index: true, element: <ProfessorOverviewPage /> },
    { path: "modules", element: <ProfessorModulesPage /> },
    { path: "modules/:subjectModuleId", element: <ProfessorModuleDetailsPage /> },
    { path: "modules/:subjectModuleId/classes/:classGroupId", element: <ProfessorClassPage /> },
    { path: "teaching", element: <ProfessorTeachingPage /> },
    { path: "teaching/:teachingAssignmentId", element: <ProfessorTeachingDetailsPage /> },
    { path: "schedule", element: <ProfessorSchedulePage /> },
    { path: "exams", element: <ProfessorExamSchedulePage /> },
    { path: "grades", element: <ProfessorGradesPage /> },
    { path: "grades/modules/:subjectModuleId/classes/:classGroupId", element: <ProfessorGradeDetailsPage /> },
    { path: "attendance", element: <ProfessorAttendancePage /> },
    { path: "account", element: <ChangePasswordPage /> },
    { path: "account/password", element: <ChangePasswordPage /> },
  ],
};
