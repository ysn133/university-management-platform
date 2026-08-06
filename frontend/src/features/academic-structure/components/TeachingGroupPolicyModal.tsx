import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import {
  academicStructureKeys,
  getTeachingGroupPolicies,
  replaceTeachingGroupPolicies,
} from "../api/academic-structure-api";

interface TeachingGroupPolicyModalProps {
  academicLevelId: string;
  academicLevelName: string;
  academicYearId: string;
  academicYearLabel: string;
  onClose: () => void;
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function TeachingGroupPolicyModal({ academicLevelId, academicLevelName, academicYearId, academicYearLabel, onClose }: TeachingGroupPolicyModalProps) {
  const queryClient = useQueryClient();
  const [tdEnabled, setTdEnabled] = useState(false);
  const [tdMinimumSize, setTdMinimumSize] = useState("25");
  const [tdMaximumSize, setTdMaximumSize] = useState("30");
  const [tpEnabled, setTpEnabled] = useState(false);
  const [tpMinimumSize, setTpMinimumSize] = useState("25");
  const [tpMaximumSize, setTpMaximumSize] = useState("30");

  const policiesQuery = useQuery({
    queryKey: academicStructureKeys.teachingGroupPolicies(academicLevelId, academicYearId),
    queryFn: () => getTeachingGroupPolicies(academicLevelId, academicYearId),
  });

  useEffect(() => {
    if (!policiesQuery.data) return;
    const td = policiesQuery.data.find((policy) => policy.groupType === "TD");
    const tp = policiesQuery.data.find((policy) => policy.groupType === "TP");
    setTdEnabled(Boolean(td));
    setTpEnabled(Boolean(tp));
    setTdMinimumSize(td ? String(td.minimumGroupSize) : "25");
    setTdMaximumSize(td ? String(td.maximumGroupSize) : "30");
    setTpMinimumSize(tp ? String(tp.minimumGroupSize) : "25");
    setTpMaximumSize(tp ? String(tp.maximumGroupSize) : "30");
  }, [policiesQuery.data]);

  const policyMutation = useMutation({
    mutationFn: () => replaceTeachingGroupPolicies(academicLevelId, academicYearId, [
      ...(tdEnabled ? [{ groupType: "TD" as const, minimumGroupSize: Number(tdMinimumSize), maximumGroupSize: Number(tdMaximumSize) }] : []),
      ...(tpEnabled ? [{ groupType: "TP" as const, minimumGroupSize: Number(tpMinimumSize), maximumGroupSize: Number(tpMaximumSize) }] : []),
    ]),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: academicStructureKeys.teachingGroupPolicies(academicLevelId, academicYearId) });
      onClose();
    },
  });

  const invalidSize = (tdEnabled && (Number(tdMinimumSize) < 1 || Number(tdMinimumSize) > Number(tdMaximumSize)))
    || (tpEnabled && (Number(tpMinimumSize) < 1 || Number(tpMinimumSize) > Number(tpMaximumSize)));

  return <ManagementModal title="Teaching group sizes" description={`${academicLevelName} · ${academicYearLabel} · Applies to both semesters.`} onClose={onClose}>
    {policiesQuery.isPending ? <div className="panel-empty">Loading teaching group policy...</div> : policiesQuery.isError ? <div className="management-alert management-alert--error">{errorMessage(policiesQuery.error)}</div> : <div className="management-form teaching-policy-form">
      <div className="teaching-policy-grid">
        <section className={tdEnabled ? "teaching-policy-card is-enabled" : "teaching-policy-card"}>
          <label className="teaching-policy-toggle"><span><strong>TD groups</strong><small>Guided exercise sessions</small></span><input checked={tdEnabled} onChange={(event) => setTdEnabled(event.target.checked)} type="checkbox" /></label>
          <div className="teaching-policy-size-fields"><div className="form-field"><label htmlFor="td-minimum-size">Minimum</label><input disabled={!tdEnabled} id="td-minimum-size" min="1" onChange={(event) => setTdMinimumSize(event.target.value)} type="number" value={tdMinimumSize} /></div><div className="form-field"><label htmlFor="td-maximum-size">Maximum</label><input disabled={!tdEnabled} id="td-maximum-size" min="1" onChange={(event) => setTdMaximumSize(event.target.value)} type="number" value={tdMaximumSize} /></div></div>
        </section>
        <section className={tpEnabled ? "teaching-policy-card is-enabled" : "teaching-policy-card"}>
          <label className="teaching-policy-toggle"><span><strong>TP groups</strong><small>Practical and laboratory sessions</small></span><input checked={tpEnabled} onChange={(event) => setTpEnabled(event.target.checked)} type="checkbox" /></label>
          <div className="teaching-policy-size-fields"><div className="form-field"><label htmlFor="tp-minimum-size">Minimum</label><input disabled={!tpEnabled} id="tp-minimum-size" min="1" onChange={(event) => setTpMinimumSize(event.target.value)} type="number" value={tpMinimumSize} /></div><div className="form-field"><label htmlFor="tp-maximum-size">Maximum</label><input disabled={!tpEnabled} id="tp-maximum-size" min="1" onChange={(event) => setTpMaximumSize(event.target.value)} type="number" value={tpMaximumSize} /></div></div>
        </section>
      </div>
      {policyMutation.isError && <div className="management-alert management-alert--error">{errorMessage(policyMutation.error)}</div>}
      <footer className="form-actions"><button className="secondary-button" onClick={onClose} type="button">Cancel</button><button className="management-primary-button" disabled={policyMutation.isPending || invalidSize} onClick={() => policyMutation.mutate()} type="button">{policyMutation.isPending ? "Saving..." : "Save group sizes"}</button></footer>
    </div>}
  </ManagementModal>;
}
