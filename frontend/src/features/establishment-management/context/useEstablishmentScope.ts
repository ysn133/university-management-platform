import { useParams } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";

export interface EstablishmentScope {
  establishmentId: string | null;
  isRootContext: boolean;
  workspacePath: string | null;
}

export function useEstablishmentScope(): EstablishmentScope {
  const { establishmentId: routeEstablishmentId } = useParams();
  const { user } = useAuth();
  const isRootContext = user?.role === "ROOT_SUPER_ADMIN";
  const establishmentId = routeEstablishmentId ?? user?.establishmentId ?? null;

  return {
    establishmentId,
    isRootContext,
    workspacePath: establishmentId
      ? isRootContext
        ? `/management/establishments/${establishmentId}`
        : "/management"
      : null,
  };
}
