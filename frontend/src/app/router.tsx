import { createBrowserRouter } from "react-router-dom";
import { AppShell } from "../shared/layout/AppShell";
import { EstablishmentAdminHome } from "../features/establishment-admin/EstablishmentAdminHome";
import { ProfessorHome } from "../features/professor/ProfessorHome";
import { RootAdminHome } from "../features/root-admin/RootAdminHome";
import { StudentHome } from "../features/student/StudentHome";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppShell />,
    children: [
      { index: true, element: <RootAdminHome /> },
      { path: "establishment-admin", element: <EstablishmentAdminHome /> },
      { path: "professor", element: <ProfessorHome /> },
      { path: "student", element: <StudentHome /> },
    ],
  },
]);
