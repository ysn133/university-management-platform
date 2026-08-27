import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import { useUrlSelection } from "@/shared/hooks/useUrlSelection";
import { createBlock, createRoom, deactivateBlock, deactivateRoom, facilityKeys, getBlocks, getRooms, updateBlock, updateRoom, type Block, type BlockInput, type Room, type RoomInput, type RoomType } from "../api/facility-api";

const facilitySections = ["blocks", "rooms"] as const;
type DeactivationTarget = { kind: "block"; value: Block } | { kind: "room"; value: Room };
const emptyBlock: BlockInput = { code: "", name: "" };
const emptyRoom: RoomInput = { code: "", name: "", roomType: "CLASSROOM", capacity: 30 };

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function roomTypeLabel(type: RoomType): string {
  return type === "LECTURE_HALL" ? "Amphitheatre" : type === "COMPUTER_LAB" ? "Computer lab" : "Classroom";
}

export function FacilitiesPage() {
  const { establishmentId } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [section, setSection] = useUrlSelection("section", facilitySections, "blocks");
  const [query, setQuery] = useState("");
  const [blockFilter, setBlockFilter] = useState("all");
  const [editingBlock, setEditingBlock] = useState<Block | null>(null);
  const [blockForm, setBlockForm] = useState<BlockInput | null>(null);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);
  const [roomForm, setRoomForm] = useState<RoomInput | null>(null);
  const [deactivationTarget, setDeactivationTarget] = useState<DeactivationTarget | null>(null);
  const blocksQuery = useQuery({ queryKey: facilityKeys.blocks(establishmentId ?? "missing"), queryFn: () => getBlocks(establishmentId!), enabled: Boolean(establishmentId) });
  const roomsQuery = useQuery({ queryKey: facilityKeys.rooms(establishmentId ?? "missing"), queryFn: () => getRooms(establishmentId!), enabled: Boolean(establishmentId) });

  async function refreshFacilities() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: facilityKeys.blocks(establishmentId!) }),
      queryClient.invalidateQueries({ queryKey: facilityKeys.rooms(establishmentId!) }),
    ]);
  }

  const blockMutation = useMutation({
    mutationFn: (input: BlockInput) => editingBlock ? updateBlock(editingBlock.id, { ...input, status: editingBlock.status }) : createBlock(establishmentId!, input),
    onSuccess: async () => { await refreshFacilities(); setBlockForm(null); setEditingBlock(null); },
  });
  const roomMutation = useMutation({
    mutationFn: (input: RoomInput) => editingRoom ? updateRoom(editingRoom.id, { ...input, status: editingRoom.status }) : createRoom(establishmentId!, input),
    onSuccess: async () => { await refreshFacilities(); setRoomForm(null); setEditingRoom(null); },
  });
  const deactivateMutation = useMutation({
    mutationFn: () => deactivationTarget?.kind === "block" ? deactivateBlock(deactivationTarget.value.id) : deactivateRoom(deactivationTarget!.value.id),
    onSuccess: async () => { await refreshFacilities(); setDeactivationTarget(null); },
  });

  if (!establishmentId) return <div className="management-state management-state--error"><h1>Establishment context unavailable</h1></div>;

  const blocks = blocksQuery.data ?? [];
  const rooms = roomsQuery.data ?? [];
  const activeBlocks = blocks.filter((block) => block.status === "ACTIVE");
  const normalizedQuery = query.trim().toLowerCase();
  const visibleRooms = rooms.filter((room) => (blockFilter === "all" || blockFilter === "standalone" ? blockFilter === "all" || !room.blockId : room.blockId === blockFilter) && `${room.code} ${room.name}`.toLowerCase().includes(normalizedQuery));
  const visibleBlocks = blocks.filter((block) => `${block.code} ${block.name}`.toLowerCase().includes(normalizedQuery));

  function openRoom(blockId?: string) {
    setEditingRoom(null);
    setRoomForm({ ...emptyRoom, blockId });
  }

  return <div className="management-page facilities-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Establishment resources</p><h1>Facilities</h1><p>Manage teaching spaces used when preparing semester schedules.</p></div><button className="management-primary-button" onClick={() => section === "rooms" ? openRoom() : (setEditingBlock(null), setBlockForm(emptyBlock))} type="button">{section === "rooms" ? "New Room" : "New Block"}</button></header>
    <nav aria-label="Facility sections" className="curriculum-section-tabs" role="tablist"><button aria-selected={section === "blocks"} onClick={() => { setSection("blocks"); setQuery(""); }} role="tab" type="button">Blocks <span>{blocks.length}</span></button><button aria-selected={section === "rooms"} onClick={() => { setSection("rooms"); setQuery(""); setBlockFilter("all"); }} role="tab" type="button">Rooms <span>{rooms.length}</span></button></nav>
    <section className="management-panel facilities-directory"><div className="directory-toolbar directory-toolbar--inside"><label><span>Search</span><input onChange={(event) => setQuery(event.target.value)} placeholder={section === "rooms" ? "Room code or name" : "Block code or name"} value={query} /></label>{section === "rooms" && <label><span>Location</span><select onChange={(event) => setBlockFilter(event.target.value)} value={blockFilter}><option value="all">All locations</option><option value="standalone">Standalone spaces</option>{blocks.map((block) => <option key={block.id} value={block.id}>{block.code} · {block.name}</option>)}</select></label>}</div>
      {section === "rooms" ? roomsQuery.isPending ? <div className="panel-empty">Loading rooms...</div> : roomsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(roomsQuery.error)}</div> : visibleRooms.length === 0 ? <div className="panel-empty"><strong>No rooms match this view.</strong></div> : <div className="resource-table-wrapper"><table className="resource-table"><thead><tr><th>Room</th><th>Type</th><th>Location</th><th>Capacity</th><th>Status</th><th>Actions</th></tr></thead><tbody>{visibleRooms.map((room) => <tr key={room.id}><td><div className="resource-name"><span className="resource-monogram">{room.code.slice(0, 2)}</span><div><strong>{room.name}</strong><small>{room.code}</small></div></div></td><td>{roomTypeLabel(room.roomType)}</td><td>{room.blockId ? `${room.blockCode} · ${blocks.find((block) => block.id === room.blockId)?.name ?? "Block"}` : <span className="standalone-room-label">Standalone</span>}</td><td>{room.capacity} seats</td><td><StatusBadge status={room.status} /></td><td><div className="row-actions"><button onClick={() => { setEditingRoom(room); setRoomForm({ blockId: room.blockId ?? undefined, code: room.code, name: room.name, roomType: room.roomType, capacity: room.capacity }); }} type="button">Edit</button>{room.status === "ACTIVE" && <button className="danger-text" onClick={() => setDeactivationTarget({ kind: "room", value: room })} type="button">Deactivate</button>}</div></td></tr>)}</tbody></table></div>
      : blocksQuery.isPending || roomsQuery.isPending ? <div className="panel-empty">Loading blocks...</div> : blocksQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(blocksQuery.error)}</div> : visibleBlocks.length === 0 ? <div className="panel-empty"><strong>No blocks match this view.</strong></div> : <div className="facility-block-grid">{visibleBlocks.map((block) => { const blockRooms = rooms.filter((room) => room.blockId === block.id); return <article key={block.id}><button className="facility-block-open" onClick={() => { setBlockFilter(block.id); setQuery(""); setSection("rooms"); }} type="button"><header><span className="resource-monogram">{block.code.slice(0, 2)}</span><div><strong>{block.name}</strong><small>{block.code}</small></div><StatusBadge status={block.status} /></header><div><strong>{blockRooms.length}</strong><span>{blockRooms.length === 1 ? "room" : "rooms"}</span><strong>{blockRooms.reduce((total, room) => total + room.capacity, 0)}</strong><span>total seats</span></div><small className="facility-block-open-label">View rooms →</small></button><footer><button disabled={block.status !== "ACTIVE"} onClick={() => { setBlockFilter(block.id); openRoom(block.id); setSection("rooms"); }} type="button">Add room</button><button onClick={() => { setEditingBlock(block); setBlockForm({ code: block.code, name: block.name }); }} type="button">Edit</button>{block.status === "ACTIVE" && <button className="danger-text" onClick={() => setDeactivationTarget({ kind: "block", value: block })} type="button">Deactivate</button>}</footer></article>; })}</div>}
    </section>
    {blockForm && <ManagementModal description="Blocks organize rooms that belong to the same establishment area." onClose={() => { setBlockForm(null); setEditingBlock(null); blockMutation.reset(); }} title={editingBlock ? "Edit Block" : "Create Block"}><form className="management-form management-form--two-columns facility-form" onSubmit={(event) => { event.preventDefault(); blockMutation.mutate(blockForm); }}><div className="form-field"><label htmlFor="block-code">Block code</label><input autoFocus id="block-code" maxLength={50} onChange={(event) => setBlockForm({ ...blockForm, code: event.target.value })} placeholder="Example: B" required value={blockForm.code} /></div><div className="form-field"><label htmlFor="block-name">Block name</label><input id="block-name" maxLength={255} onChange={(event) => setBlockForm({ ...blockForm, name: event.target.value })} placeholder="Example: Science Block" required value={blockForm.name} /></div>{blockMutation.isError && <div className="management-alert management-alert--error form-field--wide">{errorMessage(blockMutation.error)}</div>}<footer className="form-actions form-field--wide"><button className="secondary-button" onClick={() => { setBlockForm(null); setEditingBlock(null); }} type="button">Cancel</button><button className="management-primary-button" disabled={blockMutation.isPending} type="submit">{blockMutation.isPending ? "Saving..." : editingBlock ? "Save changes" : "Create Block"}</button></footer></form></ManagementModal>}
    {roomForm && <ManagementModal description="Configure the teaching space and choose whether it belongs to a Block." onClose={() => { setRoomForm(null); setEditingRoom(null); roomMutation.reset(); }} title={editingRoom ? "Edit Room" : "Create Room"}><form className="management-form management-form--two-columns facility-form" onSubmit={(event) => { event.preventDefault(); roomMutation.mutate(roomForm); }}><div className="form-field"><label htmlFor="room-code">Room code</label><input autoFocus id="room-code" maxLength={50} onChange={(event) => setRoomForm({ ...roomForm, code: event.target.value })} placeholder="Example: AMP-1" required value={roomForm.code} /></div><div className="form-field"><label htmlFor="room-name">Room name</label><input id="room-name" maxLength={255} onChange={(event) => setRoomForm({ ...roomForm, name: event.target.value })} placeholder="Example: Main Amphitheatre" required value={roomForm.name} /></div><div className="form-field"><label htmlFor="room-type">Space type</label><select id="room-type" onChange={(event) => setRoomForm({ ...roomForm, roomType: event.target.value as RoomType })} value={roomForm.roomType}><option value="LECTURE_HALL">Amphitheatre</option><option value="CLASSROOM">Classroom</option><option value="COMPUTER_LAB">Computer lab</option></select></div><div className="form-field"><label htmlFor="room-capacity">Student capacity</label><input id="room-capacity" min="1" onChange={(event) => setRoomForm({ ...roomForm, capacity: Number(event.target.value) })} required type="number" value={roomForm.capacity} /></div><div className="form-field form-field--wide facility-location-field"><label htmlFor="room-block">Location</label><select id="room-block" onChange={(event) => setRoomForm({ ...roomForm, blockId: event.target.value || undefined })} value={roomForm.blockId ?? ""}><option value="">Standalone space · no Block</option>{activeBlocks.map((block) => <option key={block.id} value={block.id}>{block.code} · {block.name}</option>)}</select><small>{roomForm.blockId ? "This room will appear inside the selected Block." : "Use this for independent amphitheatres and other spaces outside Blocks."}</small></div>{roomMutation.isError && <div className="management-alert management-alert--error form-field--wide">{errorMessage(roomMutation.error)}</div>}<footer className="form-actions form-field--wide"><button className="secondary-button" onClick={() => { setRoomForm(null); setEditingRoom(null); }} type="button">Cancel</button><button className="management-primary-button" disabled={roomMutation.isPending} type="submit">{roomMutation.isPending ? "Saving..." : editingRoom ? "Save changes" : "Create Room"}</button></footer></form></ManagementModal>}
    {deactivationTarget && <ConfirmActionModal actionLabel="Deactivate" destructive description={`Deactivate ${deactivationTarget.value.name}? It will no longer be available for new scheduling.`} error={deactivateMutation.isError ? errorMessage(deactivateMutation.error) : null} isSubmitting={deactivateMutation.isPending} onCancel={() => { setDeactivationTarget(null); deactivateMutation.reset(); }} onConfirm={() => deactivateMutation.mutate()} title={`Deactivate ${deactivationTarget.kind === "block" ? "Block" : "Room"}`} />}
  </div>;
}
