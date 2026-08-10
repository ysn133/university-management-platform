import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  academicStructureKeys,
  getAcademicRanks,
  getTeachingAssignmentRankPreferences,
  replaceTeachingAssignmentRankPreferences,
  type TeachingComponentType,
} from "../api/academic-structure-api";

interface TeachingPreferencesSettingsProps {
  establishmentId: string;
}

const componentTypes: TeachingComponentType[] = ["COURSE", "TD", "TP"];
const componentLabels: Record<TeachingComponentType, string> = { COURSE: "Course", TD: "TD", TP: "TP" };

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function TeachingPreferencesSettings({ establishmentId }: TeachingPreferencesSettingsProps) {
  const queryClient = useQueryClient();
  const [selectedType, setSelectedType] = useState<TeachingComponentType>("COURSE");
  const [orderedIds, setOrderedIds] = useState<string[]>([]);
  const [rankToAdd, setRankToAdd] = useState("");
  const ranksQuery = useQuery({ queryKey: academicStructureKeys.academicRanks(establishmentId), queryFn: () => getAcademicRanks(establishmentId) });
  const preferencesQuery = useQuery({ queryKey: academicStructureKeys.teachingAssignmentRankPreferences(establishmentId), queryFn: () => getTeachingAssignmentRankPreferences(establishmentId) });
  const currentPreferences = (preferencesQuery.data ?? []).filter((item) => item.componentType === selectedType).sort((left, right) => left.priority - right.priority);

  useEffect(() => {
    setOrderedIds(currentPreferences.map((item) => item.academicRankId));
    setRankToAdd("");
  }, [preferencesQuery.data, selectedType]);

  const saveMutation = useMutation({
    mutationFn: () => replaceTeachingAssignmentRankPreferences(establishmentId, selectedType, { academicRankIds: orderedIds }),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: academicStructureKeys.teachingAssignmentRankPreferences(establishmentId) }); },
  });

  const ranks = (ranksQuery.data ?? []).filter((rank) => rank.status === "ACTIVE");
  const availableRanks = ranks.filter((rank) => !orderedIds.includes(rank.id) && (selectedType !== "COURSE" || rank.canHoldModuleResponsibility));
  const hasChanges = orderedIds.join("|") !== currentPreferences.map((item) => item.academicRankId).join("|");

  function move(index: number, offset: number) {
    const target = index + offset;
    if (target < 0 || target >= orderedIds.length) return;
    setOrderedIds((current) => {
      const next = [...current];
      [next[index], next[target]] = [next[target], next[index]];
      return next;
    });
  }

  return <>
    <section className="settings-section-heading"><div><h2>Teaching preferences</h2><p>Set which academic ranks are considered first when teaching assignments are generated.</p></div></section>
    <nav aria-label="Teaching component" className="settings-subtabs" role="tablist">{componentTypes.map((type) => <button aria-selected={selectedType === type} key={type} onClick={() => setSelectedType(type)} role="tab" type="button">{componentLabels[type]}</button>)}</nav>
    <section className="management-panel preference-editor">
      {(ranksQuery.isPending || preferencesQuery.isPending) ? <div className="panel-empty">Loading teaching preferences...</div> : (ranksQuery.isError || preferencesQuery.isError) ? <div className="panel-empty panel-empty--error">{errorMessage(ranksQuery.error ?? preferencesQuery.error)}</div> : <>
        <div className="preference-editor-header"><div><strong>{componentLabels[selectedType]} priority</strong><p>Higher ranks in the list are considered first after expertise and workload checks.</p></div><span>{orderedIds.length} configured</span></div>
        <div className="preference-rank-list">{orderedIds.length === 0 ? <div className="preference-empty">No rank preference configured. Eligible professors will not receive rank-based priority.</div> : orderedIds.map((rankId, index) => { const rank = ranks.find((item) => item.id === rankId); return <div className="preference-rank-row" key={rankId}><span className="preference-position">{index + 1}</span><div><strong>{rank?.name ?? "Unavailable rank"}</strong><small>{rank?.code}</small></div><div className="preference-row-actions"><button aria-label="Move up" disabled={index === 0} onClick={() => move(index, -1)} type="button">Up</button><button aria-label="Move down" disabled={index === orderedIds.length - 1} onClick={() => move(index, 1)} type="button">Down</button><button className="danger-link" onClick={() => setOrderedIds((current) => current.filter((id) => id !== rankId))} type="button">Remove</button></div></div>; })}</div>
        <div className="preference-add-row"><label><span>Add rank</span><select onChange={(event) => setRankToAdd(event.target.value)} value={rankToAdd}><option value="">Select an academic rank</option>{availableRanks.map((rank) => <option key={rank.id} value={rank.id}>{rank.name}</option>)}</select></label><button className="secondary-button" disabled={!rankToAdd} onClick={() => { setOrderedIds((current) => [...current, rankToAdd]); setRankToAdd(""); }} type="button">Add to order</button></div>
        {selectedType === "COURSE" && <p className="settings-note">Course preferences only include ranks eligible for module responsibility.</p>}
        {saveMutation.isError && <div className="management-alert management-alert--error">{errorMessage(saveMutation.error)}</div>}
        <footer className="settings-save-row"><button className="secondary-button" disabled={!hasChanges || saveMutation.isPending} onClick={() => setOrderedIds(currentPreferences.map((item) => item.academicRankId))} type="button">Discard changes</button><button className="management-primary-button" disabled={!hasChanges || saveMutation.isPending} onClick={() => saveMutation.mutate()} type="button">{saveMutation.isPending ? "Saving..." : "Save preferences"}</button></footer>
      </>}
    </section>
  </>;
}
