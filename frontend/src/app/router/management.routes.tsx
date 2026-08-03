import type { RouteObject } from "react-router-dom";
import { ManagementLayout } from "@/workspaces/management/layout/ManagementLayout";
import { ManagementOverviewPage } from "@/workspaces/management/pages/ManagementOverviewPage";

export const managementRoutes: RouteObject = {
  path: "/management",
  element: <ManagementLayout />,
  children: [{ index: true, element: <ManagementOverviewPage /> }],
};
