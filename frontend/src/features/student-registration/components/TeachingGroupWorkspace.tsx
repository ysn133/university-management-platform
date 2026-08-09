import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { Semester } from "@/features/academic-structure/api/academic-structure-api";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  generateTeachingGroups,
  getTeachingGroups,
  moveTeachingGroupMember,
  teachingGroupKeys,
  type TeachingGroup,
  type TeachingGroupMember,
} from "../api/teaching-group-api";

interface TeachingGroupWorkspaceProps {
  academicLevelName?: string;
  semesters: Semester[];
  studentDetailsPath: (studentId: string) => string;
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function TeachingGroupWorkspace({ academicLevelName, semesters, studentDetailsPath }: TeachingGroupWorkspaceProps) {
  const queryClient = useQueryClient();
  const [semesterId, setSemesterId] = useState(semesters[0]?.id ?? "");
  const [groupType, setGroupType] = useState<"TD" | "TP">("TD");
  const [classGroupId, setClassGroupId] = useState("");
  const [teachingGroupId, setTeachingGroupId] = useState("");
  const [search, setSearch] = useState("");
  const [movingMember, setMovingMember] = useState<{ member: TeachingGroupMember; source: TeachingGroup } | null>(null);
  const [targetGroupId, setTargetGroupId] = useState("");
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());

  useEffect(() => {
    if (!semesters.some((semester) => semester.id === semesterId)) {
      setSemesterId(semesters[0]?.id ?? "");
    }
  }, [semesterId, semesters]);

  const rosterQuery = useQuery({
    queryKey: teachingGroupKeys.roster(semesterId),
    queryFn: () => getTeachingGroups(semesterId),
    enabled: Boolean(semesterId),
  });
  const allGroups = rosterQuery.data?.groups ?? [];
  const availableTypes = new Set(allGroups.map((group) => group.groupType));
  const effectiveGroupType = availableTypes.has(groupType) ? groupType : allGroups[0]?.groupType ?? groupType;
  const typedGroups = allGroups.filter((group) => group.groupType === effectiveGroupType);
  const classGroups = Array.from(new Map(typedGroups.map((group) => [group.sourceClassGroupId, {
    id: group.sourceClassGroupId,
    name: group.sourceClassGroupName,
  }])).values());
  const selectedClassGroupId = classGroups.some((group) => group.id === classGroupId)
    ? classGroupId
    : classGroups[0]?.id ?? "";
  const subgroupGroups = typedGroups.filter((group) => group.sourceClassGroupId === selectedClassGroupId);
  const selectedGroup = subgroupGroups.find((group) => group.id === teachingGroupId) ?? subgroupGroups[0];
  const visibleMembers = (selectedGroup?.members ?? []).filter((member) =>
    !deferredSearch
    || `${member.firstName} ${member.lastName} ${member.apogeeCode}`.toLowerCase().includes(deferredSearch),
  );
  const allClassGroups = new Set(allGroups.map((group) => group.sourceClassGroupId));
  const compactSingleClass = allClassGroups.size === 1
    && [...availableTypes].every((type) => allGroups.filter((group) => group.groupType === type).length <= 1);
  const selectedSemester = semesters.find((semester) => semester.id === semesterId);

  useEffect(() => {
    if (!deferredSearch || visibleMembers.length > 0) return;

    const matchingGroup = typedGroups.find((group) => group.members.some((member) =>
      `${member.firstName} ${member.lastName} ${member.apogeeCode}`.toLowerCase().includes(deferredSearch),
    ));
    if (!matchingGroup) return;

    setClassGroupId(matchingGroup.sourceClassGroupId);
    setTeachingGroupId(matchingGroup.id);
  }, [deferredSearch, typedGroups, visibleMembers.length]);

  const generationMutation = useMutation({
    mutationFn: () => generateTeachingGroups(semesterId),
    onSuccess: (roster) => {
      queryClient.setQueryData(teachingGroupKeys.roster(semesterId), roster);
      setClassGroupId("");
      setTeachingGroupId("");
      setSearch("");
    },
  });
  const moveMutation = useMutation({
    mutationFn: () => moveTeachingGroupMember(targetGroupId, movingMember!.member.semesterRegistrationId),
    onSuccess: (roster) => {
      queryClient.setQueryData(teachingGroupKeys.roster(semesterId), roster);
      closeMove();
    },
  });

  function selectType(type: "TD" | "TP") {
    setGroupType(type);
    setClassGroupId("");
    setTeachingGroupId("");
    setSearch("");
  }

  function selectClass(id: string) {
    setClassGroupId(id);
    setTeachingGroupId("");
    setSearch("");
  }

  function beginMove(group: TeachingGroup, member: TeachingGroupMember) {
    const firstTarget = allGroups.find((candidate) =>
      candidate.groupType === group.groupType
      && candidate.sourceClassGroupId === group.sourceClassGroupId
      && candidate.id !== group.id,
    );
    setMovingMember({ source: group, member });
    setTargetGroupId(firstTarget?.id ?? "");
    moveMutation.reset();
  }

  function closeMove() {
    setMovingMember(null);
    setTargetGroupId("");
    moveMutation.reset();
  }

  if (!academicLevelName) return <section className="management-panel"><div className="panel-empty"><strong>Select an academic level.</strong><p>Teaching groups are configured inside a semester.</p></div></section>;

  return <section className="management-panel teaching-groups-workspace">
    <header className="panel-header panel-header--bordered">
      <div><p className="management-kicker">Teaching audiences</p><h2>{academicLevelName} Teaching Groups</h2><p>Generate and manage semester TD and TP groups derived from the class-group roster.</p></div>
      <button className="management-primary-button" disabled={!semesterId || generationMutation.isPending} onClick={() => generationMutation.mutate()} type="button">{allGroups.length ? "Regenerate groups" : "Generate groups"}</button>
    </header>

    <div className="teaching-group-toolbar">
      <label><span>Semester</span><select onChange={(event) => { setSemesterId(event.target.value); setClassGroupId(""); setTeachingGroupId(""); setSearch(""); }} value={semesterId}>{semesters.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label>
      {availableTypes.size > 1 && <div aria-label="Teaching group type" className="teaching-group-type-tabs" role="tablist">
        <button aria-selected={effectiveGroupType === "TD"} onClick={() => selectType("TD")} role="tab" type="button">TD Groups</button>
        <button aria-selected={effectiveGroupType === "TP"} onClick={() => selectType("TP")} role="tab" type="button">TP Groups</button>
      </div>}
    </div>

    {generationMutation.isError && <div className="management-alert management-alert--error">{errorMessage(generationMutation.error)}</div>}
    {rosterQuery.isPending ? <div className="panel-empty">Loading teaching groups...</div>
      : rosterQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(rosterQuery.error)}</div>
      : typedGroups.length === 0 ? <div className="panel-empty"><strong>{availableTypes.size ? `No ${effectiveGroupType} groups are required.` : "No teaching groups generated."}</strong><p>{availableTypes.size ? `No module in ${selectedSemester?.name ?? "this semester"} uses ${effectiveGroupType} subgroups.` : "Generate groups after class groups and teaching components are ready."}</p></div>
      : <div className="teaching-group-browser">
        {!compactSingleClass && <div className="teaching-class-navigation"><span>Class groups</span><nav aria-label="Class groups" className="teaching-class-tabs" role="tablist">{classGroups.map((group) => <button aria-selected={selectedClassGroupId === group.id} key={group.id} onClick={() => selectClass(group.id)} role="tab" type="button"><strong>{group.name}</strong><span>{typedGroups.filter((item) => item.sourceClassGroupId === group.id).reduce((total, item) => total + item.members.length, 0)} placements</span></button>)}</nav></div>}
        <div className={`teaching-subgroup-bar${compactSingleClass ? " teaching-subgroup-bar--compact" : ""}`}>
          {!compactSingleClass && <div className="teaching-subgroup-navigation"><span>{effectiveGroupType} groups</span><div aria-label={`${effectiveGroupType} subgroups`} className="teaching-subgroup-tabs" role="tablist">{subgroupGroups.map((group) => <button aria-selected={selectedGroup?.id === group.id} key={group.id} onClick={() => { setTeachingGroupId(group.id); setSearch(""); }} role="tab" type="button"><strong>{group.name}</strong><span>{group.members.length}</span></button>)}</div></div>}
          <label className="teaching-group-search"><span>Search students</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Name or Apogee code" value={search} /></label>
        </div>

        {selectedGroup && <div className="teaching-group-roster">
          <header><div><p className="management-kicker">{compactSingleClass ? "Class roster" : selectedGroup.sourceClassGroupName}</p><h3>{compactSingleClass ? `${academicLevelName} Class` : selectedGroup.name}</h3></div><span>{deferredSearch ? `${visibleMembers.length} of ${selectedGroup.members.length} students` : `${selectedGroup.members.length} students`}</span></header>
          {visibleMembers.length === 0 ? <div className="panel-empty"><strong>No matching students.</strong><p>Try another name or Apogee code.</p></div> : <div className="resource-table-wrapper"><table className="resource-table teaching-group-table"><thead><tr><th>Student</th><th>Apogee code</th>{!compactSingleClass && <th><span className="sr-only">Actions</span></th>}</tr></thead><tbody>{visibleMembers.map((member) => <tr key={member.semesterRegistrationId}><td><Link className="resource-name resource-name--link teaching-group-student-link" to={studentDetailsPath(member.studentId)}><span className="person-monogram">{member.firstName[0]}{member.lastName[0]}</span><div><span className="teaching-group-student-name"><strong>{member.firstName} {member.lastName}</strong>{member.secondInscription && <span className="second-inscription-badge">Second inscription</span>}</span>{!compactSingleClass && <small>{effectiveGroupType} · {selectedGroup.name}</small>}</div></Link></td><td><span className="teaching-group-apogee">{member.apogeeCode}</span></td>{!compactSingleClass && <td><button aria-label={`Transfer ${member.firstName} ${member.lastName} to another ${effectiveGroupType} group`} className="teaching-group-transfer" onClick={() => beginMove(selectedGroup, member)} type="button"><span>Transfer</span><svg aria-hidden="true" fill="none" height="16" viewBox="0 0 16 16" width="16"><path d="M3 5h8.5M9 2.5 11.5 5 9 7.5M13 11H4.5M7 8.5 4.5 11 7 13.5" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.4" /></svg></button></td>}</tr>)}</tbody></table></div>}
        </div>}
      </div>}

    {movingMember && <ManagementModal title="Transfer student" description={`Move between ${movingMember.source.groupType} groups inside ${movingMember.source.sourceClassGroupName}.`} onClose={closeMove}>
      <div className="management-form teaching-group-move-form">
        <div className="teaching-group-move-summary"><span className="person-monogram">{movingMember.member.firstName[0]}{movingMember.member.lastName[0]}</span><div><strong>{movingMember.member.firstName} {movingMember.member.lastName}</strong><small>{movingMember.member.apogeeCode}</small></div></div>
        <div className="form-field"><label htmlFor="teaching-group-target">Target {movingMember.source.groupType} group</label><select id="teaching-group-target" onChange={(event) => setTargetGroupId(event.target.value)} value={targetGroupId}><option value="">No compatible group available</option>{allGroups.filter((group) => group.groupType === movingMember.source.groupType && group.sourceClassGroupId === movingMember.source.sourceClassGroupId && group.id !== movingMember.source.id).map((group) => <option key={group.id} value={group.id}>{group.name} · {group.members.length} students</option>)}</select><small>The Student remains inside class {movingMember.source.sourceClassGroupName}.</small></div>
        {moveMutation.isError && <div className="management-alert management-alert--error">{errorMessage(moveMutation.error)}</div>}
        <footer className="form-actions"><button className="secondary-button" onClick={closeMove} type="button">Cancel</button><button className="management-primary-button" disabled={!targetGroupId || moveMutation.isPending} onClick={() => moveMutation.mutate()} type="button">{moveMutation.isPending ? "Moving..." : "Confirm move"}</button></footer>
      </div>
    </ManagementModal>}
  </section>;
}
