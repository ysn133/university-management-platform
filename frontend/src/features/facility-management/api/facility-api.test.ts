import { afterEach, describe, expect, it, vi } from "vitest";
import { createBlock, createRoom, deactivateRoom, getBlocks, getRooms } from "./facility-api";

const establishmentId = "00000000-0000-4000-8000-000000000001";
const blockId = "00000000-0000-4000-8000-000000000002";
const roomId = "00000000-0000-4000-8000-000000000003";

afterEach(() => vi.unstubAllGlobals());

describe("facility API", () => {
  it("lists and creates blocks", async () => {
    const block = { id: blockId, establishmentId, code: "B", name: "Block B", status: "ACTIVE" };
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify([block]), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify(block), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);
    await expect(getBlocks(establishmentId)).resolves.toEqual([block]);
    await expect(createBlock(establishmentId, { code: "B", name: "Block B" })).resolves.toEqual(block);
    expect((fetchMock.mock.calls[1][0] as Request).method).toBe("POST");
  });

  it("supports standalone rooms", async () => {
    const room = { id: roomId, establishmentId, code: "AMP-1", name: "Amphitheatre 1", roomType: "LECTURE_HALL", capacity: 300, status: "ACTIVE" };
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify([room]), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify(room), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);
    await expect(getRooms(establishmentId)).resolves.toEqual([room]);
    await expect(createRoom(establishmentId, { code: "AMP-1", name: "Amphitheatre 1", roomType: "LECTURE_HALL", capacity: 300 })).resolves.toEqual(room);
    expect(await (fetchMock.mock.calls[1][0] as Request).json()).toEqual({ code: "AMP-1", name: "Amphitheatre 1", roomType: "LECTURE_HALL", capacity: 300 });
  });

  it("deactivates a room", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: true, message: "Room deactivated" }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);
    await expect(deactivateRoom(roomId)).resolves.toBeUndefined();
    expect((fetchMock.mock.calls[0][0] as Request).method).toBe("DELETE");
  });
});
