import type { WorkspaceNavigationItem } from "@/shared/layouts/WorkspaceLayout";

export const studentNavigation: WorkspaceNavigationItem[] = [
  { label: "Overview", to: "/student", end: true, icon: "overview", group: "Student workspace" },
  { label: "My Schedule", to: "/student/schedule", icon: "years", group: "Academic services" },
  { label: "Grades", to: "/student/grades", icon: "rules", group: "Academic services" },
  { label: "Attendance", to: "/student/attendance", icon: "students", group: "Academic services" },
];
