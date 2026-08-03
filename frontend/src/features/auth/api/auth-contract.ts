import { z } from "zod";
import { accountRoleSchema, type AuthenticatedUser } from "../model/auth-types";

const nullableUuid = z.string().uuid().nullable().optional().transform((value) => value ?? null);

const userFieldsSchema = z.object({
  userAccountId: z.string().uuid(),
  role: accountRoleSchema,
  roleEntityId: z.string().uuid(),
  establishmentId: nullableUuid,
  universityEmail: z.string().email(),
  firstName: z.string(),
  lastName: z.string(),
  accountStatus: z.string(),
});

export const authResponseSchema = userFieldsSchema.extend({
  accessToken: z.string().min(1),
  refreshToken: z.string().min(1),
});

export const currentUserResponseSchema = userFieldsSchema;

export function toAuthenticatedUser(value: z.infer<typeof userFieldsSchema>): AuthenticatedUser {
  return value;
}
