import { z } from "zod";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";

const blockSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  code: z.string(),
  name: z.string(),
  status: z.enum(["ACTIVE", "INACTIVE"]),
});

const roomSchema = z.object({
  id: z.string().uuid(),
  establishmentId: z.string().uuid(),
  blockId: z.string().uuid().nullable().optional(),
  blockCode: z.string().nullable().optional(),
  code: z.string(),
  name: z.string(),
  roomType: z.enum(["LECTURE_HALL", "CLASSROOM", "COMPUTER_LAB"]),
  capacity: z.number().int().positive(),
  status: z.enum(["ACTIVE", "INACTIVE"]),
});

export type Block = z.infer<typeof blockSchema>;
export type Room = z.infer<typeof roomSchema>;
export type RoomType = Room["roomType"];

export interface BlockInput {
  code: string;
  name: string;
  status?: Block["status"];
}

export interface RoomInput {
  blockId?: string;
  code: string;
  name: string;
  roomType: RoomType;
  capacity: number;
  status?: Room["status"];
}

export const facilityKeys = {
  blocks: (establishmentId: string) => ["facilities", "blocks", establishmentId] as const,
  rooms: (establishmentId: string) => ["facilities", "rooms", establishmentId] as const,
};

async function parse<T>(result: { response: Response; data?: unknown; error?: unknown }, schema: z.ZodType<T>): Promise<T> {
  if (!result.response.ok || !result.data) throw apiRequestError(result.response, result.error);
  return schema.parse(result.data);
}

export async function getBlocks(establishmentId: string): Promise<Block[]> {
  return parse(await apiClient.GET("/api/v1/establishments/{establishmentId}/blocks", { params: { path: { establishmentId } } }), z.array(blockSchema));
}

export async function createBlock(establishmentId: string, input: BlockInput): Promise<Block> {
  return parse(await apiClient.POST("/api/v1/establishments/{establishmentId}/blocks", { params: { path: { establishmentId } }, body: { code: input.code, name: input.name } }), blockSchema);
}

export async function updateBlock(blockId: string, input: BlockInput): Promise<Block> {
  return parse(await apiClient.PUT("/api/v1/blocks/{blockId}", { params: { path: { blockId } }, body: { code: input.code, name: input.name, status: input.status ?? "ACTIVE" } }), blockSchema);
}

export async function deactivateBlock(blockId: string): Promise<void> {
  const result = await apiClient.DELETE("/api/v1/blocks/{blockId}", { params: { path: { blockId } } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}

export async function getRooms(establishmentId: string): Promise<Room[]> {
  return parse(await apiClient.GET("/api/v1/establishments/{establishmentId}/rooms", { params: { path: { establishmentId } } }), z.array(roomSchema));
}

export async function createRoom(establishmentId: string, input: RoomInput): Promise<Room> {
  return parse(await apiClient.POST("/api/v1/establishments/{establishmentId}/rooms", { params: { path: { establishmentId } }, body: { blockId: input.blockId, code: input.code, name: input.name, roomType: input.roomType, capacity: input.capacity } }), roomSchema);
}

export async function updateRoom(roomId: string, input: RoomInput): Promise<Room> {
  return parse(await apiClient.PUT("/api/v1/rooms/{roomId}", { params: { path: { roomId } }, body: { blockId: input.blockId, code: input.code, name: input.name, roomType: input.roomType, capacity: input.capacity, status: input.status ?? "ACTIVE" } }), roomSchema);
}

export async function deactivateRoom(roomId: string): Promise<void> {
  const result = await apiClient.DELETE("/api/v1/rooms/{roomId}", { params: { path: { roomId } } });
  if (!result.response.ok) throw apiRequestError(result.response, result.error);
}
