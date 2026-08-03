import { createBrowserRouter } from "react-router-dom";
import { HomeRedirect } from "@/features/auth/components/HomeRedirect";
import { ProtectedRoute } from "@/features/auth/components/ProtectedRoute";
import { PublicOnlyRoute } from "@/features/auth/components/PublicOnlyRoute";
import { LoginPage } from "@/features/auth/pages/LoginPage";
import { NotFoundPage } from "@/shared/components/NotFoundPage";
import { managementRoutes } from "./management.routes";
import { professorRoutes } from "./professor.routes";
import { studentRoutes } from "./student.routes";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <HomeRedirect />,
  },
  {
    element: <PublicOnlyRoute />,
    children: [
      { path: "/management/login", element: <LoginPage portal="management" /> },
      { path: "/professor/login", element: <LoginPage portal="professor" /> },
      { path: "/student/login", element: <LoginPage portal="student" /> },
    ],
  },
  {
    element: <ProtectedRoute portal="management" />,
    children: [managementRoutes],
  },
  {
    element: <ProtectedRoute portal="professor" />,
    children: [professorRoutes],
  },
  {
    element: <ProtectedRoute portal="student" />,
    children: [studentRoutes],
  },
  {
    path: "*",
    element: <NotFoundPage />,
  },
]);
