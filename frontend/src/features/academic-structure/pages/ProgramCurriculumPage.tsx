import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { ProgramStudentCohort } from "@/features/student-registration/components/ProgramStudentCohort";
import {
  academicStructureKeys,
  createAcademicDomain,
  createAcademicLevel,
  createAcademicRuleProfile,
  createSemester,
  createSubjectModule,
  deleteAcademicLevel,
  deleteSemester,
  deleteSubjectModule,
  getAcademicDomains,
  getAcademicLevels,
  getAcademicRuleProfiles,
  getAcademicYears,
  getProgramFiliere,
  getSemesters,
  getSubjectModules,
  updateAcademicLevel,
  updateSemester,
  updateSubjectModule,
  type AcademicLevel,
  type Semester,
  type SubjectModule,
} from "../api/academic-structure-api";

interface LevelForm { name: string; levelOrder: string; initialAcademicYearId: string; academicRuleProfileId: string; }
interface SemesterForm { name: string; semesterOrder: string; }
interface ModuleForm { code: string; title: string; academicDomainIds: string[]; }
interface DomainForm { code: string; name: string; }
interface RuleProfileForm {
  name: string;
  moduleValidationThreshold: string;
  compensationMinimumThreshold: string;
  semesterValidationAverage: string;
  annualValidationAverage: string;
  maximumModuleInscriptions: string;
  sessionGradePolicy: "BEST_GRADE" | "RATTRAPAGE_REPLACES_NORMAL" | "RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD";
  allowProgressionWithDebt: boolean;
  maximumCarriedModules: string;
  maximumUnjustifiedAbsences: string;
  absenceExclusionPolicy: "NORMAL_ONLY" | "NORMAL_AND_RATTRAPAGE";
}
const emptyLevelForm: LevelForm = { name: "", levelOrder: "", initialAcademicYearId: "", academicRuleProfileId: "" };
const emptySemesterForm: SemesterForm = { name: "", semesterOrder: "" };
const emptyModuleForm: ModuleForm = { code: "", title: "", academicDomainIds: [] };
const emptyDomainForm: DomainForm = { code: "", name: "" };
const emptyRuleProfileForm: RuleProfileForm = {
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
};

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function ProgramCurriculumPage() {
  const { programFiliereId } = useParams();
  const [searchParams] = useSearchParams();
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [activeSection, setActiveSection] = useState<"curriculum" | "students">("curriculum");
  const [academicYearId, setAcademicYearId] = useState(() => searchParams.get("academicYearId") ?? "");
  const [academicLevelId, setAcademicLevelId] = useState(() => searchParams.get("academicLevelId") ?? "");
  const [semesterId, setSemesterId] = useState(() => searchParams.get("semesterId") ?? "");
  const [levelForm, setLevelForm] = useState<LevelForm>(emptyLevelForm);
  const [semesterForm, setSemesterForm] = useState<SemesterForm>(emptySemesterForm);
  const [moduleForm, setModuleForm] = useState<ModuleForm>(emptyModuleForm);
  const [domainForm, setDomainForm] = useState<DomainForm>(emptyDomainForm);
  const [creatingAcademicDomain, setCreatingAcademicDomain] = useState(false);
  const [ruleProfileForm, setRuleProfileForm] = useState<RuleProfileForm>(emptyRuleProfileForm);
  const [creatingRuleProfile, setCreatingRuleProfile] = useState(false);
  const [creatingLevel, setCreatingLevel] = useState(false);
  const [editingLevel, setEditingLevel] = useState<AcademicLevel | null>(null);
  const [deletingLevel, setDeletingLevel] = useState<AcademicLevel | null>(null);
  const [creatingSemester, setCreatingSemester] = useState(false);
  const [editingSemester, setEditingSemester] = useState<Semester | null>(null);
  const [deletingSemester, setDeletingSemester] = useState<Semester | null>(null);
  const [creatingModule, setCreatingModule] = useState(false);
  const [editingModule, setEditingModule] = useState<SubjectModule | null>(null);
  const [deletingModule, setDeletingModule] = useState<SubjectModule | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  const programQuery = useQuery({ queryKey: academicStructureKeys.programFiliere(programFiliereId ?? "missing"), queryFn: () => getProgramFiliere(programFiliereId!), enabled: Boolean(programFiliereId) });
  const levelsQuery = useQuery({ queryKey: academicStructureKeys.academicLevels(programFiliereId ?? "missing"), queryFn: () => getAcademicLevels(programFiliereId!), enabled: Boolean(programFiliereId) });
  const yearsQuery = useQuery({ queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"), queryFn: () => getAcademicYears(establishmentId!), enabled: Boolean(establishmentId) });
  const profilesQuery = useQuery({ queryKey: academicStructureKeys.ruleProfiles(establishmentId ?? "missing"), queryFn: () => getAcademicRuleProfiles(establishmentId!), enabled: Boolean(establishmentId) });
  const domainsQuery = useQuery({ queryKey: academicStructureKeys.academicDomains(establishmentId ?? "missing"), queryFn: () => getAcademicDomains(establishmentId!), enabled: Boolean(establishmentId) });
  const semestersQuery = useQuery({ queryKey: academicStructureKeys.semesters(academicLevelId || "missing", academicYearId || "missing"), queryFn: () => getSemesters(academicLevelId, academicYearId), enabled: Boolean(academicLevelId && academicYearId) });
  const modulesQuery = useQuery({ queryKey: academicStructureKeys.subjectModules(semesterId || "missing"), queryFn: () => getSubjectModules(semesterId), enabled: Boolean(semesterId) });

  useEffect(() => {
    if (!academicYearId && yearsQuery.data?.length) {
      setAcademicYearId(yearsQuery.data.find((year) => year.status === "ACTIVE")?.id ?? yearsQuery.data[0].id);
    }
  }, [academicYearId, yearsQuery.data]);
  useEffect(() => {
    if (levelsQuery.data && !levelsQuery.data.some((level) => level.id === academicLevelId)) setAcademicLevelId(levelsQuery.data[0]?.id ?? "");
  }, [academicLevelId, levelsQuery.data]);
  useEffect(() => {
    if (semestersQuery.data && !semestersQuery.data.some((semester) => semester.id === semesterId)) setSemesterId(semestersQuery.data[0]?.id ?? "");
  }, [semesterId, semestersQuery.data]);

  async function refreshLevels() { await queryClient.invalidateQueries({ queryKey: academicStructureKeys.academicLevels(programFiliereId!) }); }
  async function refreshSemesters() { await queryClient.invalidateQueries({ queryKey: academicStructureKeys.semesters(academicLevelId, academicYearId) }); }
  async function refreshModules() { await queryClient.invalidateQueries({ queryKey: academicStructureKeys.subjectModules(semesterId) }); }
  function closeLevelForm() { setCreatingLevel(false); setEditingLevel(null); setCreatingRuleProfile(false); setLevelForm(emptyLevelForm); setRuleProfileForm(emptyRuleProfileForm); setFormError(null); }
  function closeSemesterForm() { setCreatingSemester(false); setEditingSemester(null); setSemesterForm(emptySemesterForm); setFormError(null); }
  function closeModuleForm() { setCreatingModule(false); setEditingModule(null); setCreatingAcademicDomain(false); setModuleForm(emptyModuleForm); setDomainForm(emptyDomainForm); setFormError(null); }

  const levelMutation = useMutation({
    mutationFn: () => editingLevel
      ? updateAcademicLevel(editingLevel.id, { name: levelForm.name.trim(), levelOrder: Number(levelForm.levelOrder) })
      : createAcademicLevel(programFiliereId!, { name: levelForm.name.trim(), levelOrder: Number(levelForm.levelOrder), initialAcademicYearId: levelForm.initialAcademicYearId, academicRuleProfileId: levelForm.academicRuleProfileId }),
    onSuccess: async () => { await refreshLevels(); closeLevelForm(); },
  });
  const ruleProfileMutation = useMutation({
    mutationFn: () => createAcademicRuleProfile(establishmentId!, {
      name: ruleProfileForm.name.trim(),
      moduleValidationThreshold: Number(ruleProfileForm.moduleValidationThreshold),
      compensationMinimumThreshold: Number(ruleProfileForm.compensationMinimumThreshold),
      semesterValidationAverage: Number(ruleProfileForm.semesterValidationAverage),
      annualValidationAverage: ruleProfileForm.annualValidationAverage ? Number(ruleProfileForm.annualValidationAverage) : undefined,
      maximumModuleInscriptions: Number(ruleProfileForm.maximumModuleInscriptions),
      sessionGradePolicy: ruleProfileForm.sessionGradePolicy,
      allowProgressionWithDebt: ruleProfileForm.allowProgressionWithDebt,
      maximumCarriedModules: Number(ruleProfileForm.maximumCarriedModules),
      maximumUnjustifiedAbsences: Number(ruleProfileForm.maximumUnjustifiedAbsences),
      absenceExclusionPolicy: ruleProfileForm.absenceExclusionPolicy,
      status: "ACTIVE",
    }),
    onSuccess: async (profile) => {
      await queryClient.invalidateQueries({ queryKey: academicStructureKeys.ruleProfiles(establishmentId!) });
      setLevelForm((current) => ({ ...current, academicRuleProfileId: profile.id }));
      setCreatingRuleProfile(false);
      setRuleProfileForm(emptyRuleProfileForm);
      setFormError(null);
    },
  });
  const semesterMutation = useMutation({
    mutationFn: () => editingSemester
      ? updateSemester(editingSemester.id, { name: semesterForm.name.trim(), semesterOrder: Number(semesterForm.semesterOrder) })
      : createSemester(academicLevelId, academicYearId, { name: semesterForm.name.trim(), semesterOrder: Number(semesterForm.semesterOrder) }),
    onSuccess: async () => { await refreshSemesters(); closeSemesterForm(); },
  });
  const moduleMutation = useMutation({
    mutationFn: () => editingModule
      ? updateSubjectModule(editingModule.id, { code: moduleForm.code.trim(), title: moduleForm.title.trim(), academicDomainIds: moduleForm.academicDomainIds })
      : createSubjectModule(semesterId, { code: moduleForm.code.trim(), title: moduleForm.title.trim(), academicDomainIds: moduleForm.academicDomainIds }),
    onSuccess: async () => { await refreshModules(); closeModuleForm(); },
  });
  const academicDomainMutation = useMutation({
    mutationFn: () => createAcademicDomain(establishmentId!, {
      code: domainForm.code.trim(),
      name: domainForm.name.trim(),
    }),
    onSuccess: async (domain) => {
      await queryClient.invalidateQueries({ queryKey: academicStructureKeys.academicDomains(establishmentId!) });
      setModuleForm((current) => ({
        ...current,
        academicDomainIds: current.academicDomainIds.includes(domain.id)
          ? current.academicDomainIds
          : [...current.academicDomainIds, domain.id],
      }));
      setCreatingAcademicDomain(false);
      setDomainForm(emptyDomainForm);
      setFormError(null);
    },
  });
  const deleteLevelMutation = useMutation({ mutationFn: (id: string) => deleteAcademicLevel(id), onSuccess: async () => { await refreshLevels(); setDeletingLevel(null); } });
  const deleteSemesterMutation = useMutation({ mutationFn: (id: string) => deleteSemester(id), onSuccess: async () => { await refreshSemesters(); setDeletingSemester(null); } });
  const deleteModuleMutation = useMutation({ mutationFn: (id: string) => deleteSubjectModule(id), onSuccess: async () => { await refreshModules(); setDeletingModule(null); } });

  function submitLevel() {
    if (!levelForm.name.trim() || Number(levelForm.levelOrder) < 1 || (!editingLevel && (!levelForm.initialAcademicYearId || !levelForm.academicRuleProfileId))) { setFormError("Complete all required level fields."); return; }
    levelMutation.mutate();
  }
  function submitRuleProfile() {
    const requiredValues = [
      ruleProfileForm.moduleValidationThreshold,
      ruleProfileForm.compensationMinimumThreshold,
      ruleProfileForm.semesterValidationAverage,
      ruleProfileForm.maximumModuleInscriptions,
      ruleProfileForm.maximumCarriedModules,
      ruleProfileForm.maximumUnjustifiedAbsences,
    ];
    const gradeValues = [
      ruleProfileForm.moduleValidationThreshold,
      ruleProfileForm.compensationMinimumThreshold,
      ruleProfileForm.semesterValidationAverage,
      ruleProfileForm.annualValidationAverage,
    ].filter(Boolean).map(Number);
    if (!ruleProfileForm.name.trim() || requiredValues.some((value) => value === "") || gradeValues.some((value) => Number.isNaN(value) || value < 0 || value > 20)) {
      setFormError("Enter a name and grade thresholds between 0 and 20.");
      return;
    }
    if (Number(ruleProfileForm.maximumModuleInscriptions) < 1 || Number(ruleProfileForm.maximumCarriedModules) < 0 || Number(ruleProfileForm.maximumUnjustifiedAbsences) < 0) {
      setFormError("Enter valid limits for inscriptions, carried modules, and absences.");
      return;
    }
    ruleProfileMutation.mutate();
  }
  function submitSemester() {
    if (!semesterForm.name.trim() || Number(semesterForm.semesterOrder) < 1) { setFormError("Enter a semester name and a positive order."); return; }
    semesterMutation.mutate();
  }
  function submitModule() {
    if (!moduleForm.code.trim() || !moduleForm.title.trim()) { setFormError("Module code and title are required."); return; }
    moduleMutation.mutate();
  }
  function submitAcademicDomain() {
    if (!domainForm.code.trim() || !domainForm.name.trim()) {
      setFormError("Domain code and name are required.");
      return;
    }
    academicDomainMutation.mutate();
  }

  if (!programFiliereId || !establishmentId || !workspacePath) return <div className="management-state management-state--error"><h1>Program context unavailable</h1></div>;
  if (programQuery.isPending) return <div className="management-state">Loading program curriculum...</div>;
  if (programQuery.isError) return <div className="management-state management-state--error"><h1>Program unavailable</h1><p>{errorMessage(programQuery.error)}</p></div>;

  const program = programQuery.data;
  const levels = levelsQuery.data ?? [];
  const semesters = semestersQuery.data ?? [];
  const modules = modulesQuery.data ?? [];
  const selectedLevel = levels.find((level) => level.id === academicLevelId);
  const selectedSemester = semesters.find((semester) => semester.id === semesterId);
  const selectedYear = yearsQuery.data?.find((year) => year.id === academicYearId);
  const activeProfiles = profilesQuery.data?.filter((profile) => profile.status === "ACTIVE") ?? [];
  const domainNames = new Map(domainsQuery.data?.map((domain) => [domain.id, domain.name]));
  const canCreateLevel = Boolean(yearsQuery.data?.length);

  return <div className="management-page curriculum-page">
    <Link className="context-back-link curriculum-back-link" to={`${workspacePath}/programs`}>← Back to programs</Link>
    <header className="curriculum-header"><span className="curriculum-program-code">{program.code}</span><div><p className="management-kicker">Program curriculum</p><h1>{program.name}</h1><p>Manage levels, annual semesters, and the modules delivered in each semester.</p></div><label><span>Academic year</span><select onChange={(event) => { setAcademicYearId(event.target.value); setSemesterId(""); }} value={academicYearId}><option value="">Select academic year</option>{yearsQuery.data?.map((year) => <option key={year.id} value={year.id}>{year.label} · {year.status}</option>)}</select></label></header>

    <nav aria-label="Program workspace" className="curriculum-section-tabs" role="tablist">
      <button aria-selected={activeSection === "curriculum"} onClick={() => setActiveSection("curriculum")} role="tab" type="button">Curriculum</button>
      <button aria-selected={activeSection === "students"} onClick={() => setActiveSection("students")} role="tab" type="button">Students</button>
    </nav>

    <div className="curriculum-layout">
      <aside className="management-panel curriculum-levels">
        <header>
          <div>
            <p className="management-kicker">Program structure</p>
            <h2>Academic Levels</h2>
            <span>{levels.length ? `${levels.length} configured` : "Build the program hierarchy"}</span>
          </div>
          <button className="curriculum-level-add" disabled={!canCreateLevel} onClick={() => { setLevelForm({ name: "", levelOrder: String(levels.length + 1), initialAcademicYearId: academicYearId || yearsQuery.data?.[0]?.id || "", academicRuleProfileId: activeProfiles[0]?.id || "" }); setCreatingRuleProfile(false); setCreatingLevel(true); }} type="button">Add level</button>
        </header>
        {levelsQuery.isPending ? <div className="panel-empty">Loading levels...</div> : levelsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(levelsQuery.error)}</div> : levels.length === 0 ? <div className="panel-empty curriculum-level-empty"><strong>No levels configured</strong><p>{canCreateLevel ? "Add the first level and define its initial academic rules." : "Create an academic year before adding levels."}</p></div> : <div className="curriculum-level-list">{levels.map((level) => {
          const isSelected = level.id === academicLevelId;
          return <article className={isSelected ? "is-active" : ""} key={level.id}>
            <button className="curriculum-level-select" onClick={() => { setAcademicLevelId(level.id); setSemesterId(""); }} type="button">
              <strong>{level.name}</strong>
              <small>{isSelected ? "Currently viewing" : activeSection === "students" ? "View student cohort" : "View semesters and modules"}</small>
            </button>
            <div className="row-actions">
              <button onClick={() => { setEditingLevel(level); setLevelForm({ name: level.name, levelOrder: String(level.levelOrder), initialAcademicYearId: "", academicRuleProfileId: "" }); }} type="button">Edit</button>
              <button className="danger-text" onClick={() => setDeletingLevel(level)} type="button">Delete</button>
            </div>
          </article>;
        })}</div>}
      </aside>

      <main className="curriculum-main">
        {activeSection === "curriculum" ? <>
          <section className="management-panel curriculum-semesters"><header className="panel-header panel-header--bordered"><div><p className="management-kicker">{selectedYear?.label ?? "Academic year required"}</p><h2>{selectedLevel ? `${selectedLevel.name} Semesters` : "Select an Academic Level"}</h2><p>Each academic year has its own semester structure and modules.</p></div><button className="management-primary-button" disabled={!academicLevelId || !academicYearId} onClick={() => { setSemesterForm({ name: "", semesterOrder: String(semesters.length + 1) }); setCreatingSemester(true); }} type="button">New Semester</button></header>{!academicYearId ? <div className="panel-empty"><strong>Select an academic year.</strong><p>The year determines which semesters and modules are displayed.</p></div> : !academicLevelId ? <div className="panel-empty"><strong>Select an academic level.</strong></div> : semestersQuery.isPending ? <div className="panel-empty">Loading semesters...</div> : semestersQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(semestersQuery.error)}</div> : semesters.length === 0 ? <div className="panel-empty"><strong>No semesters configured.</strong><p>Create the first semester for this level and academic year.</p></div> : <div className="semester-card-grid">{semesters.map((semester) => <article className={semester.id === semesterId ? "is-active" : ""} key={semester.id}><button onClick={() => setSemesterId(semester.id)} type="button"><span>Semester {semester.semesterOrder}</span><strong>{semester.name}</strong></button><div className="row-actions"><button onClick={() => { setEditingSemester(semester); setSemesterForm({ name: semester.name, semesterOrder: String(semester.semesterOrder) }); }} type="button">Edit</button><button className="danger-text" onClick={() => setDeletingSemester(semester)} type="button">Delete</button></div></article>)}</div>}</section>

          <section className="management-panel curriculum-modules"><header className="panel-header panel-header--bordered"><div><p className="management-kicker">{selectedSemester?.name ?? "Semester modules"}</p><h2>Subject Modules</h2><p>{selectedSemester ? `Modules delivered in ${selectedSemester.name} for ${selectedYear?.label}.` : "Select a semester to manage its modules."}</p></div><button className="management-primary-button" disabled={!semesterId} onClick={() => { setModuleForm(emptyModuleForm); setCreatingModule(true); }} type="button">New Module</button></header>{!semesterId ? <div className="panel-empty"><strong>Select a semester.</strong></div> : modulesQuery.isPending ? <div className="panel-empty">Loading modules...</div> : modulesQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(modulesQuery.error)}</div> : modules.length === 0 ? <div className="panel-empty"><strong>No modules configured.</strong><p>Create the first subject module for this semester.</p></div> : <div className="resource-table-wrapper"><table className="resource-table"><thead><tr><th>Module</th><th>Academic domains</th><th>Actions</th></tr></thead><tbody>{modules.map((module) => <tr key={module.id}><td><Link className="resource-name resource-name--link module-record-link" to={`${workspacePath}/programs/${programFiliereId}/modules/${module.id}?academicYearId=${academicYearId}&academicLevelId=${academicLevelId}&semesterId=${semesterId}`}><span className="resource-monogram">{module.code.slice(0, 2)}</span><div><strong>{module.title}</strong><small>{module.code}</small></div></Link></td><td>{module.academicDomainIds.length ? module.academicDomainIds.map((id) => domainNames.get(id) ?? "Unknown domain").join(", ") : "No domain assigned"}</td><td><div className="row-actions"><button onClick={() => { setEditingModule(module); setModuleForm({ code: module.code, title: module.title, academicDomainIds: module.academicDomainIds }); }} type="button">Edit</button><button className="danger-text" onClick={() => setDeletingModule(module)} type="button">Delete</button></div></td></tr>)}</tbody></table></div>}</section>
        </> : <ProgramStudentCohort academicLevel={selectedLevel} academicLevels={levels} academicYearId={academicYearId} academicYearLabel={selectedYear?.label} establishmentId={establishmentId} onSelectAcademicLevel={(levelId) => { setAcademicLevelId(levelId); setSemesterId(""); }} programFiliereId={programFiliereId} semesters={semesters} studentDetailsPath={(studentId) => `${workspacePath}/students/${studentId}`} />}
      </main>
    </div>

    {(creatingLevel || editingLevel) && <ManagementModal
      title={creatingRuleProfile ? "Create Academic Rule Profile" : `${editingLevel ? "Edit" : "Create"} Academic Level`}
      description={creatingRuleProfile ? "Define the rules that will govern this level for the selected academic year." : editingLevel ? "Update the level identity and ordering." : "Create the level and its initial academic rule assignment."}
      onClose={closeLevelForm}
    >
      {creatingRuleProfile ? <div className="management-form management-form--two-columns">
        <div className="form-field form-field--wide"><label htmlFor="rule-name">Profile name</label><input autoFocus id="rule-name" maxLength={255} onChange={(event) => { setRuleProfileForm({ ...ruleProfileForm, name: event.target.value }); setFormError(null); }} placeholder="Master standard rules" value={ruleProfileForm.name} /></div>
        <div className="form-field"><label htmlFor="rule-module-threshold">Module validation threshold</label><input id="rule-module-threshold" max="20" min="0" onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, moduleValidationThreshold: event.target.value })} step="0.01" type="number" value={ruleProfileForm.moduleValidationThreshold} /></div>
        <div className="form-field"><label htmlFor="rule-compensation-threshold">Compensation minimum</label><input id="rule-compensation-threshold" max="20" min="0" onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, compensationMinimumThreshold: event.target.value })} step="0.01" type="number" value={ruleProfileForm.compensationMinimumThreshold} /></div>
        <div className="form-field"><label htmlFor="rule-semester-average">Semester validation average</label><input id="rule-semester-average" max="20" min="0" onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, semesterValidationAverage: event.target.value })} step="0.01" type="number" value={ruleProfileForm.semesterValidationAverage} /></div>
        <div className="form-field"><label htmlFor="rule-annual-average">Annual validation average</label><input id="rule-annual-average" max="20" min="0" onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, annualValidationAverage: event.target.value })} step="0.01" type="number" value={ruleProfileForm.annualValidationAverage} /></div>
        <div className="form-field"><label htmlFor="rule-inscriptions">Maximum module inscriptions</label><input id="rule-inscriptions" min="1" onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, maximumModuleInscriptions: event.target.value })} type="number" value={ruleProfileForm.maximumModuleInscriptions} /></div>
        <div className="form-field"><label htmlFor="rule-session-policy">Rattrapage grade policy</label><select id="rule-session-policy" onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, sessionGradePolicy: event.target.value as RuleProfileForm["sessionGradePolicy"] })} value={ruleProfileForm.sessionGradePolicy}><option value="BEST_GRADE">Keep the best grade</option><option value="RATTRAPAGE_REPLACES_NORMAL">Rattrapage replaces normal</option><option value="RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD">Cap at validation threshold</option></select></div>
        <div className="form-field"><label htmlFor="rule-carried-modules">Maximum carried modules</label><input id="rule-carried-modules" min="0" onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, maximumCarriedModules: event.target.value })} type="number" value={ruleProfileForm.maximumCarriedModules} /></div>
        <div className="form-field"><label htmlFor="rule-absence-limit">Maximum unjustified absences</label><input id="rule-absence-limit" min="0" onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, maximumUnjustifiedAbsences: event.target.value })} type="number" value={ruleProfileForm.maximumUnjustifiedAbsences} /></div>
        <div className="form-field"><label htmlFor="rule-absence-policy">Absence exclusion policy</label><select id="rule-absence-policy" onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, absenceExclusionPolicy: event.target.value as RuleProfileForm["absenceExclusionPolicy"] })} value={ruleProfileForm.absenceExclusionPolicy}><option value="NORMAL_ONLY">Normal session only</option><option value="NORMAL_AND_RATTRAPAGE">Normal and rattrapage</option></select></div>
        <label className="form-field form-field--wide curriculum-policy-check"><input checked={ruleProfileForm.allowProgressionWithDebt} onChange={(event) => setRuleProfileForm({ ...ruleProfileForm, allowProgressionWithDebt: event.target.checked })} type="checkbox" /><span><strong>Allow progression with module debt</strong><small>Students may progress while carrying modules within the configured limit.</small></span></label>
        {formError && <div className="management-alert management-alert--error">{formError}</div>}
        {ruleProfileMutation.isError && <div className="management-alert management-alert--error">{errorMessage(ruleProfileMutation.error)}</div>}
        <footer className="form-actions"><button className="secondary-button" onClick={() => { setCreatingRuleProfile(false); setFormError(null); }} type="button">Back</button><button className="management-primary-button" disabled={ruleProfileMutation.isPending} onClick={submitRuleProfile} type="button">{ruleProfileMutation.isPending ? "Creating..." : "Create and select"}</button></footer>
      </div> : <div className="management-form management-form--two-columns">
        <div className="form-field"><label htmlFor="level-name">Name</label><input autoFocus id="level-name" maxLength={100} onChange={(event) => { setLevelForm({ ...levelForm, name: event.target.value }); setFormError(null); }} placeholder="M1" value={levelForm.name} /></div>
        <div className="form-field"><label htmlFor="level-order">Order</label><input id="level-order" min="1" onChange={(event) => { setLevelForm({ ...levelForm, levelOrder: event.target.value }); setFormError(null); }} type="number" value={levelForm.levelOrder} /></div>
        {!editingLevel && <>
          <div className="form-field"><label htmlFor="level-year">Initial academic year</label><select id="level-year" onChange={(event) => setLevelForm({ ...levelForm, initialAcademicYearId: event.target.value })} value={levelForm.initialAcademicYearId}>{yearsQuery.data?.map((year) => <option key={year.id} value={year.id}>{year.label}</option>)}</select></div>
          <div className="form-field"><label htmlFor="level-rule">Academic rule profile</label><select id="level-rule" onChange={(event) => setLevelForm({ ...levelForm, academicRuleProfileId: event.target.value })} value={levelForm.academicRuleProfileId}><option value="">Select a rule profile</option>{activeProfiles.map((profile) => <option key={profile.id} value={profile.id}>{profile.name} · v{profile.version}</option>)}</select></div>
          <div className="form-field form-field--wide curriculum-rule-create"><span>{activeProfiles.length ? "Need a different policy?" : "No active rule profile is available."}</span><button className="secondary-button secondary-button--compact" onClick={() => { setCreatingRuleProfile(true); setRuleProfileForm(emptyRuleProfileForm); setFormError(null); }} type="button">Create rule profile</button></div>
        </>}
        {formError && <div className="management-alert management-alert--error">{formError}</div>}
        {levelMutation.isError && <div className="management-alert management-alert--error">{errorMessage(levelMutation.error)}</div>}
        <footer className="form-actions"><button className="secondary-button" onClick={closeLevelForm} type="button">Cancel</button><button className="management-primary-button" disabled={levelMutation.isPending} onClick={submitLevel} type="button">{levelMutation.isPending ? "Saving..." : "Save"}</button></footer>
      </div>}
    </ManagementModal>}
    {(creatingSemester || editingSemester) && <ManagementModal title={`${editingSemester ? "Edit" : "Create"} Semester`} description={`${selectedLevel?.name ?? "Academic level"} · ${selectedYear?.label ?? "Academic year"}`} onClose={closeSemesterForm}><div className="management-form management-form--two-columns"><div className="form-field"><label htmlFor="semester-name">Name</label><input autoFocus id="semester-name" maxLength={100} onChange={(event) => { setSemesterForm({ ...semesterForm, name: event.target.value }); setFormError(null); }} placeholder="S1" value={semesterForm.name} /></div><div className="form-field"><label htmlFor="semester-order">Order</label><input id="semester-order" min="1" onChange={(event) => { setSemesterForm({ ...semesterForm, semesterOrder: event.target.value }); setFormError(null); }} type="number" value={semesterForm.semesterOrder} /></div>{formError && <div className="management-alert management-alert--error">{formError}</div>}{semesterMutation.isError && <div className="management-alert management-alert--error">{errorMessage(semesterMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={closeSemesterForm} type="button">Cancel</button><button className="management-primary-button" disabled={semesterMutation.isPending} onClick={submitSemester} type="button">{semesterMutation.isPending ? "Saving..." : "Save"}</button></footer></div></ManagementModal>}
    {(creatingModule || editingModule) && <ManagementModal
      title={creatingAcademicDomain ? "Create Academic Domain" : `${editingModule ? "Edit" : "Create"} Subject Module`}
      description={creatingAcademicDomain ? "Add a reusable teaching and expertise domain to this establishment." : `${selectedSemester?.name ?? "Semester"} · ${selectedYear?.label ?? "Academic year"}`}
      onClose={closeModuleForm}
    >
      {creatingAcademicDomain ? <div className="management-form management-form--two-columns">
        <div className="form-field"><label htmlFor="domain-code">Domain code</label><input autoFocus id="domain-code" maxLength={50} onChange={(event) => { setDomainForm({ ...domainForm, code: event.target.value }); setFormError(null); }} placeholder="CS" value={domainForm.code} /></div>
        <div className="form-field"><label htmlFor="domain-name">Domain name</label><input id="domain-name" maxLength={255} onChange={(event) => { setDomainForm({ ...domainForm, name: event.target.value }); setFormError(null); }} placeholder="Computer Science" value={domainForm.name} /></div>
        {formError && <div className="management-alert management-alert--error">{formError}</div>}
        {academicDomainMutation.isError && <div className="management-alert management-alert--error">{errorMessage(academicDomainMutation.error)}</div>}
        <footer className="form-actions"><button className="secondary-button" onClick={() => { setCreatingAcademicDomain(false); setFormError(null); }} type="button">Back</button><button className="management-primary-button" disabled={academicDomainMutation.isPending} onClick={submitAcademicDomain} type="button">{academicDomainMutation.isPending ? "Creating..." : "Create and select"}</button></footer>
      </div> : <div className="management-form management-form--two-columns">
        <div className="form-field"><label htmlFor="module-code">Code</label><input autoFocus id="module-code" maxLength={255} onChange={(event) => { setModuleForm({ ...moduleForm, code: event.target.value }); setFormError(null); }} placeholder="ALG101" value={moduleForm.code} /></div>
        <div className="form-field"><label htmlFor="module-title">Title</label><input id="module-title" maxLength={255} onChange={(event) => { setModuleForm({ ...moduleForm, title: event.target.value }); setFormError(null); }} placeholder="Algorithms" value={moduleForm.title} /></div>
        <fieldset className="form-field form-field--wide curriculum-domain-field">
          <legend>Academic domains</legend>
          {domainsQuery.isPending ? <span>Loading domains...</span> : <>
            {domainsQuery.data?.length ? <div>{domainsQuery.data.map((domain) => <label key={domain.id}><input checked={moduleForm.academicDomainIds.includes(domain.id)} onChange={(event) => setModuleForm({ ...moduleForm, academicDomainIds: event.target.checked ? [...moduleForm.academicDomainIds, domain.id] : moduleForm.academicDomainIds.filter((id) => id !== domain.id) })} type="checkbox" /><span><strong>{domain.name}</strong><small>{domain.code}</small></span></label>)}</div> : null}
            <div className="curriculum-domain-create"><span>{domainsQuery.data?.length ? "Select every domain that applies to this module." : "No academic domains have been configured yet."}</span><button className="secondary-button secondary-button--compact" onClick={() => { setCreatingAcademicDomain(true); setDomainForm(emptyDomainForm); setFormError(null); }} type="button">Create domain</button></div>
          </>}
        </fieldset>
        {formError && <div className="management-alert management-alert--error">{formError}</div>}
        {moduleMutation.isError && <div className="management-alert management-alert--error">{errorMessage(moduleMutation.error)}</div>}
        <footer className="form-actions"><button className="secondary-button" onClick={closeModuleForm} type="button">Cancel</button><button className="management-primary-button" disabled={moduleMutation.isPending} onClick={submitModule} type="button">{moduleMutation.isPending ? "Saving..." : "Save"}</button></footer>
      </div>}
    </ManagementModal>}
    {deletingLevel && <ConfirmActionModal actionLabel="Delete" destructive description={`Delete ${deletingLevel.name}? This requires the level to have no dependent academic records.`} error={deleteLevelMutation.isError ? errorMessage(deleteLevelMutation.error) : null} isSubmitting={deleteLevelMutation.isPending} onCancel={() => setDeletingLevel(null)} onConfirm={() => deleteLevelMutation.mutate(deletingLevel.id)} title="Delete Academic Level" />}
    {deletingSemester && <ConfirmActionModal actionLabel="Delete" destructive description={`Delete ${deletingSemester.name}? This requires the semester to have no dependent records.`} error={deleteSemesterMutation.isError ? errorMessage(deleteSemesterMutation.error) : null} isSubmitting={deleteSemesterMutation.isPending} onCancel={() => setDeletingSemester(null)} onConfirm={() => deleteSemesterMutation.mutate(deletingSemester.id)} title="Delete Semester" />}
    {deletingModule && <ConfirmActionModal actionLabel="Delete" destructive description={`Delete ${deletingModule.title}? This requires the module to have no dependent records.`} error={deleteModuleMutation.isError ? errorMessage(deleteModuleMutation.error) : null} isSubmitting={deleteModuleMutation.isPending} onCancel={() => setDeletingModule(null)} onConfirm={() => deleteModuleMutation.mutate(deletingModule.id)} title="Delete Subject Module" />}
  </div>;
}
