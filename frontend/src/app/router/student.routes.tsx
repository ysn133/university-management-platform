import type { RouteObject } from "react-router-dom";
import { StudentLayout } from "@/workspaces/student/layout/StudentLayout";
import { StudentOverviewPage } from "@/workspaces/student/pages/StudentOverviewPage";
import { ChangePasswordPage } from "@/features/auth/pages/ChangePasswordPage";
import { StudentAttendanceCheckInPage } from "@/workspaces/student/pages/StudentAttendanceCheckInPage";

export const studentRoutes: RouteObject = {
  path: "/student",
  element: <StudentLayout />,
  children: [
    { index: true, element: <StudentOverviewPage /> },
    { path: "attendance/check-in", element: <StudentAttendanceCheckInPage /> },
    { path: "account/password", element: <ChangePasswordPage /> },
  ],
};
