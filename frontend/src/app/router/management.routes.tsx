import type { RouteObject } from "react-router-dom";
import { ManagementLayout } from "@/workspaces/management/layout/ManagementLayout";
import { ManagementOverviewPage } from "@/workspaces/management/pages/ManagementOverviewPage";
import { ChangePasswordPage } from "@/features/auth/pages/ChangePasswordPage";
import { RootOnlyRoute } from "@/features/root-governance/components/RootOnlyRoute";
import { EstablishmentsPage } from "@/features/root-governance/pages/EstablishmentsPage";
import { EstablishmentDetailsPage } from "@/features/root-governance/pages/EstablishmentDetailsPage";
import { EstablishmentAuthorityRoute } from "@/features/establishment-management/components/EstablishmentAuthorityRoute";
import { EstablishmentWorkspacePage } from "@/features/establishment-management/pages/EstablishmentWorkspacePage";
import { AdminManagementPage } from "@/features/establishment-management/pages/AdminManagementPage";
import { AdminDetailsPage } from "@/features/establishment-management/pages/AdminDetailsPage";

export const managementRoutes: RouteObject = {
  path: "/management",
  element: <ManagementLayout />,
  children: [
    { index: true, element: <ManagementOverviewPage /> },
    {
      element: <RootOnlyRoute />,
      children: [
        { path: "establishments", element: <EstablishmentsPage /> },
        { path: "establishments/:establishmentId", element: <EstablishmentWorkspacePage /> },
        { path: "establishments/:establishmentId/super-admins", element: <EstablishmentDetailsPage /> },
        { path: "establishments/:establishmentId/admins", element: <AdminManagementPage /> },
        { path: "establishments/:establishmentId/admins/:adminId", element: <AdminDetailsPage /> },
      ],
    },
    {
      element: <EstablishmentAuthorityRoute />,
      children: [
        { path: "admins", element: <AdminManagementPage /> },
        { path: "admins/:adminId", element: <AdminDetailsPage /> },
      ],
    },
    { path: "account/password", element: <ChangePasswordPage /> },
  ],
};
