import type { RouteObject } from "react-router-dom";
import { ManagementLayout } from "@/workspaces/management/layout/ManagementLayout";
import { ManagementOverviewPage } from "@/workspaces/management/pages/ManagementOverviewPage";
import { ChangePasswordPage } from "@/features/auth/pages/ChangePasswordPage";
import { RootOnlyRoute } from "@/features/root-governance/components/RootOnlyRoute";
import { EstablishmentsPage } from "@/features/root-governance/pages/EstablishmentsPage";
import { EstablishmentDetailsPage } from "@/features/root-governance/pages/EstablishmentDetailsPage";

export const managementRoutes: RouteObject = {
  path: "/management",
  element: <ManagementLayout />,
  children: [
    { index: true, element: <ManagementOverviewPage /> },
    {
      element: <RootOnlyRoute />,
      children: [
        { path: "establishments", element: <EstablishmentsPage /> },
        { path: "establishments/:establishmentId", element: <EstablishmentDetailsPage /> },
      ],
    },
    { path: "account/password", element: <ChangePasswordPage /> },
  ],
};
