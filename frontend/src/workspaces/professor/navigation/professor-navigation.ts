import type { WorkspaceNavigationItem } from "@/shared/layouts/WorkspaceLayout";

export const professorNavigation: WorkspaceNavigationItem[] = [
  { label: "Overview", to: "/professor", end: true, icon: "overview", group: "Professor workspace" },
  { label: "My Modules", to: "/professor/modules", icon: "programs", group: "Teaching" },
  { label: "Exam Schedule", to: "/professor/exams", icon: "years", group: "Teaching" },
];
