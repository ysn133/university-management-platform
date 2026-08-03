import type { RouteObject } from "react-router-dom";
import { StudentLayout } from "@/workspaces/student/layout/StudentLayout";
import { StudentOverviewPage } from "@/workspaces/student/pages/StudentOverviewPage";

export const studentRoutes: RouteObject = {
  path: "/student",
  element: <StudentLayout />,
  children: [{ index: true, element: <StudentOverviewPage /> }],
};
