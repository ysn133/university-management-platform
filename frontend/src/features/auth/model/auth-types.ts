import { z } from "zod";

export const accountRoleSchema = z.enum([
  "ROOT_SUPER_ADMIN",
  "SUPER_ADMIN",
  "ADMIN",
  "PROFESSOR",
  "STUDENT",
]);

export type AccountRole = z.infer<typeof accountRoleSchema>;

export interface AuthenticatedUser {
  userAccountId: string;
  role: AccountRole;
  roleEntityId: string;
  establishmentId: string | null;
  universityEmail: string;
  firstName: string;
  lastName: string;
  accountStatus: string;
}

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
}

export type PortalType = "management" | "professor" | "student";

export const portalRoles: Record<PortalType, AccountRole[]> = {
  management: ["ROOT_SUPER_ADMIN", "SUPER_ADMIN", "ADMIN"],
  professor: ["PROFESSOR"],
  student: ["STUDENT"],
};

export function getWorkspacePath(role: AccountRole): string {
  if (role === "PROFESSOR") {
    return "/professor";
  }

  if (role === "STUDENT") {
    return "/student";
  }

  return "/management";
}

export function getLoginPath(portal: PortalType): string {
  return `/${portal}/login`;
}
