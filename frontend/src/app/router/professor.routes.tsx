import type { RouteObject } from "react-router-dom";
import { ProfessorLayout } from "@/workspaces/professor/layout/ProfessorLayout";
import { ProfessorOverviewPage } from "@/workspaces/professor/pages/ProfessorOverviewPage";

export const professorRoutes: RouteObject = {
  path: "/professor",
  element: <ProfessorLayout />,
  children: [{ index: true, element: <ProfessorOverviewPage /> }],
};
