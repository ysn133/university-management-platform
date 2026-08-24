import type { RouteObject } from "react-router-dom";
import { StudentLayout } from "@/workspaces/student/layout/StudentLayout";
import { StudentOverviewPage } from "@/workspaces/student/pages/StudentOverviewPage";
import { ChangePasswordPage } from "@/features/auth/pages/ChangePasswordPage";
import { StudentAttendanceCheckInPage } from "@/workspaces/student/pages/StudentAttendanceCheckInPage";
import { StudentSchedulePage } from "@/workspaces/student/pages/StudentSchedulePage";
import { StudentGradesPage } from "@/workspaces/student/pages/StudentGradesPage";
import { StudentAttendancePage } from "@/workspaces/student/pages/StudentAttendancePage";
import { StudentStudiesPage } from "@/workspaces/student/pages/StudentStudiesPage";

export const studentRoutes: RouteObject = {
  path: "/student",
  element: <StudentLayout />,
  children: [
    { index: true, element: <StudentOverviewPage /> },
    { path: "studies", element: <StudentStudiesPage /> },
    { path: "schedule", element: <StudentSchedulePage /> },
    { path: "grades", element: <StudentGradesPage /> },
    { path: "attendance", element: <StudentAttendancePage /> },
    { path: "attendance/check-in", element: <StudentAttendanceCheckInPage /> },
    { path: "account/password", element: <ChangePasswordPage /> },
  ],
};
