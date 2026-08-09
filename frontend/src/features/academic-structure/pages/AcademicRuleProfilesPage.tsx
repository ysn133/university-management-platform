import { useDeferredValue, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import {
  academicStructureKeys,
  createAcademicRuleProfile,
  getAcademicRuleProfiles,
  updateAcademicRuleProfile,
  type AcademicRuleProfile,
  type CreateAcademicRuleProfileRequest,
} from "../api/academic-structure-api";

type ProfileStatusFilter = "ALL" | AcademicRuleProfile["status"];

interface RuleProfileForm {
  name: string;
  moduleValidationThreshold: string;
  compensationMinimumThreshold: string;
  semesterValidationAverage: string;
  annualValidationAverage: string;
  maximumModuleInscriptions: string;
  sessionGradePolicy: AcademicRuleProfile["sessionGradePolicy"];
  allowProgressionWithDebt: boolean;
  maximumCarriedModules: string;
  maximumUnjustifiedAbsences: string;
  absenceExclusionPolicy: AcademicRuleProfile["absenceExclusionPolicy"];
  status: AcademicRuleProfile["status"];
}

const emptyForm: RuleProfileForm = {
  name: "",
  moduleValidationThreshold: "10",
  compensationMinimumThreshold: "7",
  semesterValidationAverage: "10",
  annualValidationAverage: "10",
  maximumModuleInscriptions: "2",
  sessionGradePolicy: "RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD",
  allowProgressionWithDebt: true,
  maximumCarriedModules: "2",
  maximumUnjustifiedAbsences: "3",
  absenceExclusionPolicy: "NORMAL_AND_RATTRAPAGE",
  status: "ACTIVE",
};

const sessionPolicyLabels: Record<AcademicRuleProfile["sessionGradePolicy"], string> = {
  BEST_GRADE: "Best grade",
  RATTRAPAGE_REPLACES_NORMAL: "Rattrapage replaces normal",
  RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD: "Capped at validation threshold",
};

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function formFromProfile(profile: AcademicRuleProfile): RuleProfileForm {
  return {
    name: profile.name,
    moduleValidationThreshold: String(profile.moduleValidationThreshold),
    compensationMinimumThreshold: String(profile.compensationMinimumThreshold),
    semesterValidationAverage: String(profile.semesterValidationAverage),
    annualValidationAverage: profile.annualValidationAverage == null ? "" : String(profile.annualValidationAverage),
    maximumModuleInscriptions: String(profile.maximumModuleInscriptions),
    sessionGradePolicy: profile.sessionGradePolicy,
    allowProgressionWithDebt: profile.allowProgressionWithDebt,
    maximumCarriedModules: String(profile.maximumCarriedModules),
    maximumUnjustifiedAbsences: String(profile.maximumUnjustifiedAbsences),
    absenceExclusionPolicy: profile.absenceExclusionPolicy,
    status: profile.status,
  };
}

function requestFromForm(form: RuleProfileForm): CreateAcademicRuleProfileRequest {
  return {
    name: form.name.trim(),
    moduleValidationThreshold: Number(form.moduleValidationThreshold),
    compensationMinimumThreshold: Number(form.compensationMinimumThreshold),
    semesterValidationAverage: Number(form.semesterValidationAverage),
    annualValidationAverage: form.annualValidationAverage ? Number(form.annualValidationAverage) : undefined,
    maximumModuleInscriptions: Number(form.maximumModuleInscriptions),
    sessionGradePolicy: form.sessionGradePolicy,
    allowProgressionWithDebt: form.allowProgressionWithDebt,
    maximumCarriedModules: Number(form.maximumCarriedModules),
    maximumUnjustifiedAbsences: Number(form.maximumUnjustifiedAbsences),
    absenceExclusionPolicy: form.absenceExclusionPolicy,
    status: form.status,
  };
}

export function AcademicRuleProfilesPage() {
  const { establishmentId } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<ProfileStatusFilter>("ALL");
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<AcademicRuleProfile | null>(null);
  const [form, setForm] = useState<RuleProfileForm>(emptyForm);
  const [validationError, setValidationError] = useState<string | null>(null);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const profilesQuery = useQuery({
    queryKey: academicStructureKeys.ruleProfiles(establishmentId ?? "missing"),
    queryFn: () => getAcademicRuleProfiles(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const profiles = (profilesQuery.data ?? []).filter((profile) => {
    const matchesSearch = `${profile.name} ${profile.version}`.toLowerCase().includes(deferredSearch);
    const matchesStatus = statusFilter === "ALL" || profile.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: academicStructureKeys.ruleProfiles(establishmentId!) });
  }

  function closeForm() {
    setCreating(false);
    setEditing(null);
    setForm(emptyForm);
    setValidationError(null);
    createMutation.reset();
    updateMutation.reset();
  }

  const createMutation = useMutation({
    mutationFn: () => createAcademicRuleProfile(establishmentId!, requestFromForm(form)),
    onSuccess: async () => { await refresh(); closeForm(); },
  });
  const updateMutation = useMutation({
    mutationFn: () => updateAcademicRuleProfile(editing!.id, requestFromForm(form)),
    onSuccess: async () => { await refresh(); closeForm(); },
  });

  function updateField<K extends keyof RuleProfileForm>(field: K, value: RuleProfileForm[K]) {
    setForm((current) => ({ ...current, [field]: value }));
    setValidationError(null);
  }

  function openCreate() {
    setForm(emptyForm);
    setCreating(true);
  }

  function openEdit(profile: AcademicRuleProfile) {
    setForm(formFromProfile(profile));
    setEditing(profile);
  }

  function submit() {
    const scoreValues = [
      form.moduleValidationThreshold,
      form.compensationMinimumThreshold,
      form.semesterValidationAverage,
      ...(form.annualValidationAverage ? [form.annualValidationAverage] : []),
    ].map(Number);
    const countValues = [form.maximumModuleInscriptions, form.maximumCarriedModules, form.maximumUnjustifiedAbsences].map(Number);
    if (!form.name.trim() || scoreValues.some((value) => !Number.isFinite(value) || value < 0 || value > 20)) {
      setValidationError("Enter a profile name and score thresholds between 0 and 20.");
      return;
    }
    if (!Number.isInteger(countValues[0]) || countValues[0] < 1 || countValues.slice(1).some((value) => !Number.isInteger(value) || value < 0)) {
      setValidationError("Module inscriptions must be at least 1, and the remaining limits cannot be negative.");
      return;
    }
    if (editing) updateMutation.mutate(); else createMutation.mutate();
  }

  if (!establishmentId) return <div className="management-state management-state--error"><h1>No establishment assigned</h1></div>;
  const mutation = editing ? updateMutation : createMutation;

  return <div className="management-page academic-directory-page rule-profile-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Academic structure</p><h1>Academic Rule Profiles</h1><p>Manage the versioned rules used for validation, compensation, progression, rattrapage, and absence eligibility.</p></div><button className="management-primary-button" onClick={openCreate} type="button">New Rule Profile</button></header>

    <section className="directory-toolbar academic-directory-toolbar rule-profile-toolbar"><label className="search-field"><span>Search</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Profile name or version" value={search} /></label><label><span>Status</span><select onChange={(event) => setStatusFilter(event.target.value as ProfileStatusFilter)} value={statusFilter}><option value="ALL">All statuses</option><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></label><span className="directory-result-count">{profiles.length} {profiles.length === 1 ? "profile" : "profiles"}</span></section>

    <section className="management-panel directory-panel">{profilesQuery.isPending ? <div className="panel-empty">Loading rule profiles...</div> : profilesQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(profilesQuery.error)}</div> : profiles.length === 0 ? <div className="panel-empty"><strong>No rule profile found.</strong><p>{search || statusFilter !== "ALL" ? "Adjust the current filters." : "Create the first academic rule profile."}</p></div> : <div className="resource-table-wrapper"><table className="resource-table rule-profile-table"><thead><tr><th>Profile</th><th>Validation</th><th>Progression</th><th>Rattrapage and absence</th><th>Status</th><th>Action</th></tr></thead><tbody>{profiles.map((profile) => <tr key={profile.id}><td><div className="table-contact"><strong>{profile.name}</strong><small>Version {profile.version}</small></div></td><td><div className="table-contact"><span>Module {profile.moduleValidationThreshold} / 20</span><small>Compensation from {profile.compensationMinimumThreshold} · Semester {profile.semesterValidationAverage}</small></div></td><td><div className="table-contact"><span>{profile.allowProgressionWithDebt ? `Up to ${profile.maximumCarriedModules} carried modules` : "No progression with debt"}</span><small>Maximum {profile.maximumModuleInscriptions} module inscriptions</small></div></td><td><div className="table-contact"><span>{sessionPolicyLabels[profile.sessionGradePolicy]}</span><small>{profile.maximumUnjustifiedAbsences} unjustified absences · {profile.absenceExclusionPolicy === "NORMAL_ONLY" ? "Normal only" : "Normal and rattrapage"}</small></div></td><td><StatusBadge status={profile.status} /></td><td><button className="record-open-link" onClick={() => openEdit(profile)} type="button">View and edit</button></td></tr>)}</tbody></table></div>}</section>

    {(creating || editing) && <ManagementModal title={editing ? `${editing.name} · Version ${editing.version}` : "Create Academic Rule Profile"} description={editing ? "Assigned rule definitions are historical and cannot be changed. Create a new version when the policy changes." : "Creating a profile with an existing name creates its next version."} onClose={closeForm}><div className="management-form management-form--two-columns rule-profile-form"><div className="form-field form-field--wide"><label htmlFor="profile-name">Profile name</label><input autoFocus id="profile-name" onChange={(event) => updateField("name", event.target.value)} value={form.name} /></div><div className="form-field"><label htmlFor="module-threshold">Module validation threshold</label><input id="module-threshold" max="20" min="0" onChange={(event) => updateField("moduleValidationThreshold", event.target.value)} step="0.01" type="number" value={form.moduleValidationThreshold} /></div><div className="form-field"><label htmlFor="compensation-threshold">Compensation minimum</label><input id="compensation-threshold" max="20" min="0" onChange={(event) => updateField("compensationMinimumThreshold", event.target.value)} step="0.01" type="number" value={form.compensationMinimumThreshold} /></div><div className="form-field"><label htmlFor="semester-average">Semester validation average</label><input id="semester-average" max="20" min="0" onChange={(event) => updateField("semesterValidationAverage", event.target.value)} step="0.01" type="number" value={form.semesterValidationAverage} /></div><div className="form-field"><label htmlFor="annual-average">Annual validation average</label><input id="annual-average" max="20" min="0" onChange={(event) => updateField("annualValidationAverage", event.target.value)} step="0.01" type="number" value={form.annualValidationAverage} /></div><div className="form-field"><label htmlFor="maximum-inscriptions">Maximum module inscriptions</label><input id="maximum-inscriptions" min="1" onChange={(event) => updateField("maximumModuleInscriptions", event.target.value)} type="number" value={form.maximumModuleInscriptions} /></div><div className="form-field"><label htmlFor="session-policy">Rattrapage grade policy</label><select id="session-policy" onChange={(event) => updateField("sessionGradePolicy", event.target.value as RuleProfileForm["sessionGradePolicy"])} value={form.sessionGradePolicy}><option value="BEST_GRADE">Keep the best grade</option><option value="RATTRAPAGE_REPLACES_NORMAL">Rattrapage replaces normal</option><option value="RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD">Cap at validation threshold</option></select></div><div className="form-field"><label htmlFor="maximum-carried">Maximum carried modules</label><input disabled={!form.allowProgressionWithDebt} id="maximum-carried" min="0" onChange={(event) => updateField("maximumCarriedModules", event.target.value)} type="number" value={form.maximumCarriedModules} /></div><div className="form-field"><label htmlFor="maximum-absences">Maximum unjustified absences</label><input id="maximum-absences" min="0" onChange={(event) => updateField("maximumUnjustifiedAbsences", event.target.value)} type="number" value={form.maximumUnjustifiedAbsences} /></div><div className="form-field"><label htmlFor="absence-policy">Absence exclusion policy</label><select id="absence-policy" onChange={(event) => updateField("absenceExclusionPolicy", event.target.value as RuleProfileForm["absenceExclusionPolicy"])} value={form.absenceExclusionPolicy}><option value="NORMAL_ONLY">Normal session only</option><option value="NORMAL_AND_RATTRAPAGE">Normal and rattrapage</option></select></div><div className="form-field"><label htmlFor="profile-status">Status</label><select id="profile-status" onChange={(event) => updateField("status", event.target.value as AcademicRuleProfile["status"])} value={form.status}><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></div><label className="form-field form-field--wide curriculum-policy-check"><input checked={form.allowProgressionWithDebt} onChange={(event) => updateField("allowProgressionWithDebt", event.target.checked)} type="checkbox" /><span><strong>Allow progression with module debt</strong><small>Students may progress while carrying modules within the configured limit.</small></span></label>{validationError && <div className="management-alert management-alert--error">{validationError}</div>}{mutation.isError && <div className="management-alert management-alert--error">{errorMessage(mutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={closeForm} type="button">Cancel</button><button className="management-primary-button" disabled={mutation.isPending} onClick={submit} type="button">{mutation.isPending ? "Saving..." : editing ? "Save changes" : "Create profile"}</button></footer></div></ManagementModal>}
  </div>;
}
