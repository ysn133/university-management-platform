import { createBrowserRouter, Navigate } from "react-router-dom";
import { NotFoundPage } from "@/shared/components/NotFoundPage";
import { managementRoutes } from "./management.routes";
import { professorRoutes } from "./professor.routes";
import { studentRoutes } from "./student.routes";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <Navigate to="/management" replace />,
  },
  managementRoutes,
  professorRoutes,
  studentRoutes,
  {
    path: "*",
    element: <NotFoundPage />,
  },
]);
