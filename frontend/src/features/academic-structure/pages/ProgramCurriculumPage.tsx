import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { ConfirmActionModal } from "@/features/root-governance/components/ConfirmActionModal";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { ProgramStudentCohort } from "@/features/student-registration/components/ProgramStudentCohort";
import { TeachingGroupWorkspace } from "@/features/student-registration/components/TeachingGroupWorkspace";
import { TeachingPlanWorkspace } from "@/features/teaching-planning/components/TeachingPlanWorkspace";
import { SemesterProfessorsWorkspace } from "@/features/teaching-planning/components/SemesterProfessorsWorkspace";
import { SemesterTimetableWorkspace } from "@/features/scheduling/components/SemesterTimetableWorkspace";
import { ExamPlanningWorkspace } from "@/features/scheduling/components/ExamPlanningWorkspace";
import { GradeManagementWorkspace } from "@/features/assessment/components/GradeManagementWorkspace";
import { ProgressionWorkspace } from "@/features/assessment/components/ProgressionWorkspace";
import { GraduationWorkspace } from "@/features/assessment/components/GraduationWorkspace";
import { TeachingGroupPolicyModal } from "../components/TeachingGroupPolicyModal";
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
  getAcademicLevelRuleAssignments,
  getAcademicRuleProfiles,
  getAcademicYears,
  getProgramFiliere,
  getProgramPaths,
  getSemesters,
  getSubjectModules,
  updateAcademicLevel,
  updateAcademicLevelRuleAssignment,
  updateSemester,
  updateSubjectModule,
  type AcademicLevel,
  type Semester,
  type SubjectModule,
} from "../api/academic-structure-api";

interface LevelForm { name: string; levelOrder: string; terminalLevel: boolean; initialAcademicYearId: string; academicRuleProfileId: string; }
interface SemesterForm { name: string; semesterOrder: string; termType: "AUTUMN" | "SPRING"; startDate: string; endDate: string; }
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
const emptyLevelForm: LevelForm = { name: "", levelOrder: "", terminalLevel: false, initialAcademicYearId: "", academicRuleProfileId: "" };
const emptySemesterForm: SemesterForm = { name: "", semesterOrder: "", termType: "AUTUMN", startDate: "", endDate: "" };
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
const curriculumSections = ["curriculum", "students", "teaching-groups", "teaching-plan", "professors", "schedule", "exam-planning", "grades", "progression", "graduation"] as const;
type CurriculumSection = typeof curriculumSections[number];

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function ProgramCurriculumPage() {
  const {
    programFiliereId,
    academicYearId: routeAcademicYearId,
    programPathId: routeProgramPathId,
    departmentId: routeDepartmentId,
    degreeCycleId: routeDegreeCycleId,
  } = useParams<{ programFiliereId: string; academicYearId?: string; programPathId?: string; departmentId?: string; degreeCycleId?: string }>();
  const [searchParams, setSearchParams] = useSearchParams();
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const requestedSection = searchParams.get("section");
  const activeSection: CurriculumSection = curriculumSections.includes(requestedSection as CurriculumSection)
    ? requestedSection as CurriculumSection
    : "curriculum";
  const academicYearId = routeAcademicYearId ?? searchParams.get("academicYearId") ?? "";
  const academicLevelId = searchParams.get("academicLevelId") ?? "";
  const semesterId = searchParams.get("semesterId") ?? "";
  const [pendingSemesterId, setPendingSemesterId] = useState("");
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
  const [configuringGroupPolicy, setConfiguringGroupPolicy] = useState<AcademicLevel | null>(null);
  const [creatingSemester, setCreatingSemester] = useState(false);
  const [editingSemester, setEditingSemester] = useState<Semester | null>(null);
  const [deletingSemester, setDeletingSemester] = useState<Semester | null>(null);
  const [creatingModule, setCreatingModule] = useState(false);
  const [editingModule, setEditingModule] = useState<SubjectModule | null>(null);
  const [deletingModule, setDeletingModule] = useState<SubjectModule | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  function updateNavigationParameter(parameter: string, value: string, replace = false) {
    setSearchParams((current) => {
      const next = new URLSearchParams(current);
      if (value) next.set(parameter, value);
      else next.delete(parameter);
      return next;
    }, { replace });
  }

  function setActiveSection(section: CurriculumSection) {
    updateNavigationParameter("section", section === "curriculum" ? "" : section);
  }

  function setAcademicYearId(value: string, replace = false) {
    if (!routeAcademicYearId) updateNavigationParameter("academicYearId", value, replace);
  }

  function setAcademicLevelId(value: string, replace = false) {
    updateNavigationParameter("academicLevelId", value, replace);
  }

  function selectAcademicLevel(value: string) {
    setSearchParams((current) => {
      const next = new URLSearchParams(current);
      next.set("academicLevelId", value);
      next.delete("semesterId");
      return next;
    });
  }

  function setSemesterId(value: string, replace = false) {
    updateNavigationParameter("semesterId", value, replace);
  }

  function selectAcademicYear(value: string) {
    setSearchParams((current) => {
      const next = new URLSearchParams(current);
      if (value) next.set("academicYearId", value);
      else next.delete("academicYearId");
      next.delete("semesterId");
      return next;
    });
  }

  function openOriginalSemester(yearId: string, levelId: string, originalSemesterId: string) {
    setPendingSemesterId(originalSemesterId);
    setSearchParams((current) => {
      const next = new URLSearchParams(current);
      if (!routeAcademicYearId) next.set("academicYearId", yearId);
      next.set("academicLevelId", levelId);
      next.delete("semesterId");
      return next;
    });
  }

  const programQuery = useQuery({ queryKey: academicStructureKeys.programFiliere(programFiliereId ?? "missing"), queryFn: () => getProgramFiliere(programFiliereId!), enabled: Boolean(programFiliereId) });
  const programPathsQuery = useQuery({ queryKey: academicStructureKeys.programPaths(establishmentId ?? "missing"), queryFn: () => getProgramPaths(establishmentId!), enabled: Boolean(establishmentId) });
  const levelsQuery = useQuery({ queryKey: academicStructureKeys.academicLevels(programFiliereId ?? "missing"), queryFn: () => getAcademicLevels(programFiliereId!), enabled: Boolean(programFiliereId) });
  const yearsQuery = useQuery({ queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"), queryFn: () => getAcademicYears(establishmentId!), enabled: Boolean(establishmentId) });
  const profilesQuery = useQuery({ queryKey: academicStructureKeys.ruleProfiles(establishmentId ?? "missing"), queryFn: () => getAcademicRuleProfiles(establishmentId!), enabled: Boolean(establishmentId) });
  const ruleAssignmentsQuery = useQuery({ queryKey: academicStructureKeys.levelRuleAssignments(academicLevelId || "missing"), queryFn: () => getAcademicLevelRuleAssignments(academicLevelId), enabled: Boolean(academicLevelId) });
  const domainsQuery = useQuery({ queryKey: academicStructureKeys.academicDomains(establishmentId ?? "missing"), queryFn: () => getAcademicDomains(establishmentId!), enabled: Boolean(establishmentId) });
  const semestersQuery = useQuery({ queryKey: academicStructureKeys.semesters(academicLevelId || "missing", academicYearId || "missing"), queryFn: () => getSemesters(academicLevelId, academicYearId), enabled: Boolean(academicLevelId && academicYearId) });
  const modulesQuery = useQuery({ queryKey: academicStructureKeys.subjectModules(semesterId || "missing"), queryFn: () => getSubjectModules(semesterId), enabled: Boolean(semesterId) });
  const currentRuleAssignment = ruleAssignmentsQuery.data?.find((assignment) => assignment.academicYearId === academicYearId && assignment.status === "ACTIVE");
  const selectedLevelIsTerminal = levelsQuery.data?.find((level) => level.id === academicLevelId)?.terminalLevel;

  useEffect(() => {
    if (!academicYearId && yearsQuery.data?.length) {
      setAcademicYearId(yearsQuery.data.find((year) => year.status === "ACTIVE")?.id ?? yearsQuery.data[0].id, true);
    }
  }, [academicYearId, yearsQuery.data]);
  useEffect(() => {
    if (!levelsQuery.data) return;
    if (levelsQuery.data.some((level) => level.id === academicLevelId)) return;
    const fallbackLevelId = levelsQuery.data[0]?.id ?? "";
    updateNavigationParameter("academicLevelId", fallbackLevelId, true);
  }, [academicLevelId, levelsQuery.data]);
  useEffect(() => {
    if (!semestersQuery.data) return;
    if (pendingSemesterId) {
      if (semestersQuery.data.some((semester) => semester.id === pendingSemesterId)) {
        setSemesterId(pendingSemesterId, true);
        setPendingSemesterId("");
      }
      return;
    }
    if (!semestersQuery.data.some((semester) => semester.id === semesterId)) setSemesterId(semestersQuery.data[0]?.id ?? "", true);
  }, [pendingSemesterId, semesterId, semestersQuery.data]);
  useEffect(() => {
    if (!editingLevel || !currentRuleAssignment) return;
    setLevelForm((current) => ({
      ...current,
      academicRuleProfileId: currentRuleAssignment.academicRuleProfileId,
    }));
  }, [currentRuleAssignment, editingLevel]);
  useEffect(() => {
    if (activeSection === "graduation" && selectedLevelIsTerminal === false) {
      setActiveSection("progression");
    }
  }, [activeSection, selectedLevelIsTerminal]);

  async function refreshLevels() { await queryClient.invalidateQueries({ queryKey: academicStructureKeys.academicLevels(programFiliereId!) }); }
  async function refreshSemesters() { await queryClient.invalidateQueries({ queryKey: academicStructureKeys.semesters(academicLevelId, academicYearId) }); }
  async function refreshModules() { await queryClient.invalidateQueries({ queryKey: academicStructureKeys.subjectModules(semesterId) }); }
  function closeLevelForm() { setCreatingLevel(false); setEditingLevel(null); setCreatingRuleProfile(false); setLevelForm(emptyLevelForm); setRuleProfileForm(emptyRuleProfileForm); setFormError(null); }
  function closeSemesterForm() { setCreatingSemester(false); setEditingSemester(null); setSemesterForm(emptySemesterForm); setFormError(null); }
  function closeModuleForm() { setCreatingModule(false); setEditingModule(null); setCreatingAcademicDomain(false); setModuleForm(emptyModuleForm); setDomainForm(emptyDomainForm); setFormError(null); }

  const levelMutation = useMutation({
    mutationFn: async () => {
      if (!editingLevel) {
        return createAcademicLevel(programFiliereId!, { name: levelForm.name.trim(), levelOrder: Number(levelForm.levelOrder), terminalLevel: levelForm.terminalLevel, initialAcademicYearId: levelForm.initialAcademicYearId, academicRuleProfileId: levelForm.academicRuleProfileId });
      }
      const updatedLevel = await updateAcademicLevel(editingLevel.id, { name: levelForm.name.trim(), levelOrder: Number(levelForm.levelOrder), terminalLevel: levelForm.terminalLevel });
      if (currentRuleAssignment && levelForm.academicRuleProfileId !== currentRuleAssignment.academicRuleProfileId) {
        await updateAcademicLevelRuleAssignment(currentRuleAssignment.id, levelForm.academicRuleProfileId);
      }
      return updatedLevel;
    },
    onSuccess: async () => {
      await Promise.all([
        refreshLevels(),
        queryClient.invalidateQueries({ queryKey: academicStructureKeys.levelRuleAssignments(academicLevelId) }),
      ]);
      closeLevelForm();
    },
  });
  const ruleProfileMutation = useMutation({
    mutationFn: () => createAcademicRuleProfile(establishmentId!, {
      name: ruleProfileForm.name.trim(),
      moduleValidationThreshold: Number(ruleProfileForm.moduleValidationThreshold),
      compensationMinimumThreshold: Number(ruleProfileForm.compensationMinimumThreshold),
      semesterValidationAverage: Number(ruleProfileForm.semesterValidationAverage),
      annualValidationAverage: ruleProfileForm.annualValidationAverage ? Number(ruleProfileForm.annualValidationAverage) : undefined,
      minimumIndividuallyValidatedModulesPerSemester: 5,
      maximumNonValidatedModulesPerSemester: 2,
      allowInterSemesterCompensation: true,
      minimumIndividuallyValidatedModulesPerAcademicLevel: 10,
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
      ? updateSemester(editingSemester.id, { name: semesterForm.name.trim(), semesterOrder: Number(semesterForm.semesterOrder), termType: semesterForm.termType, startDate: semesterForm.startDate, endDate: semesterForm.endDate })
      : createSemester(academicLevelId, academicYearId, { name: semesterForm.name.trim(), semesterOrder: Number(semesterForm.semesterOrder), termType: semesterForm.termType, startDate: semesterForm.startDate, endDate: semesterForm.endDate }),
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
    if (!levelForm.name.trim() || Number(levelForm.levelOrder) < 1 || !levelForm.academicRuleProfileId || (!editingLevel && !levelForm.initialAcademicYearId)) { setFormError("Complete all required level fields."); return; }
    const profile = profilesQuery.data?.find((item) => item.id === levelForm.academicRuleProfileId);
    if (levelForm.terminalLevel && profile?.allowProgressionWithDebt) {
      setFormError("A final academic level cannot use progression with debt because students must fully validate the level before graduation.");
      return;
    }
    levelMutation.mutate();
  }
  function selectRuleProfile(academicRuleProfileId: string) {
    const profile = profilesQuery.data?.find((item) => item.id === academicRuleProfileId);
    setLevelForm({ ...levelForm, academicRuleProfileId });
    if (levelForm.terminalLevel && profile?.allowProgressionWithDebt) {
      setFormError("This profile allows progression with debt. Select a profile without debt progression for a final academic level.");
      return;
    }
    setFormError(null);
  }
  function setTerminalLevel(terminalLevel: boolean) {
    const profile = profilesQuery.data?.find((item) => item.id === levelForm.academicRuleProfileId);
    setLevelForm({ ...levelForm, terminalLevel });
    if (terminalLevel && profile?.allowProgressionWithDebt) {
      setFormError("This level cannot be marked as final while its rule profile allows progression with debt. Choose a compatible profile first.");
      return;
    }
    setFormError(null);
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
  const selectedProgramPath = programPathsQuery.data?.find((path) => path.id === program?.programPathId);
  const selectedRuleAssignment = currentRuleAssignment;
  const selectedRuleProfile = profilesQuery.data?.find((profile) => profile.id === selectedRuleAssignment?.academicRuleProfileId);
  const activeProfiles = profilesQuery.data?.filter((profile) => profile.status === "ACTIVE") ?? [];
  const hasIncompatibleTerminalRule = levelForm.terminalLevel && activeProfiles.some((profile) =>
    profile.id === levelForm.academicRuleProfileId && profile.allowProgressionWithDebt
  );
  const domainNames = new Map(domainsQuery.data?.map((domain) => [domain.id, domain.name]));
  const canCreateLevel = Boolean(yearsQuery.data?.length);
  const programPathId = routeProgramPathId ?? searchParams.get("programPathId");
  const programsBackPath = programPathId && routeAcademicYearId
    ? `${workspacePath}/academic-years/${routeAcademicYearId}/program-paths/${programPathId}/programs`
    : programPathId
    ? `${workspacePath}/program-paths/${programPathId}/programs`
    : routeDepartmentId
    ? `${workspacePath}/departments/${routeDepartmentId}/programs`
    : routeDegreeCycleId
    ? `${workspacePath}/degree-cycles/${routeDegreeCycleId}/programs`
    : `${workspacePath}/programs`;
  const programPathQuery = programPathId ? `&programPathId=${programPathId}` : "";
  const modulePathFor = (moduleId: string) => routeAcademicYearId && programPathId
    ? `${workspacePath}/academic-years/${routeAcademicYearId}/program-paths/${programPathId}/programs/${programFiliereId}/modules/${moduleId}?academicLevelId=${academicLevelId}&semesterId=${semesterId}`
    : routeProgramPathId
    ? `${workspacePath}/program-paths/${routeProgramPathId}/programs/${programFiliereId}/modules/${moduleId}?academicYearId=${academicYearId}&academicLevelId=${academicLevelId}&semesterId=${semesterId}`
    : routeDepartmentId
    ? `${workspacePath}/departments/${routeDepartmentId}/programs/${programFiliereId}/modules/${moduleId}?academicYearId=${academicYearId}&academicLevelId=${academicLevelId}&semesterId=${semesterId}`
    : routeDegreeCycleId
    ? `${workspacePath}/degree-cycles/${routeDegreeCycleId}/programs/${programFiliereId}/modules/${moduleId}?academicYearId=${academicYearId}&academicLevelId=${academicLevelId}&semesterId=${semesterId}`
    : `${workspacePath}/programs/${programFiliereId}/modules/${moduleId}?academicYearId=${academicYearId}&academicLevelId=${academicLevelId}&semesterId=${semesterId}${programPathQuery}`;

  return <div className="management-page curriculum-page">
    <Link className="context-back-link curriculum-back-link" to={programsBackPath}>← Back to programs</Link>
    <header className="curriculum-header"><span className="curriculum-program-code">{program.code}</span><div><p className="management-kicker">Program curriculum</p><h1>{program.name}</h1><p>Manage levels, annual semesters, and the modules delivered in each semester.</p></div><label><span>Academic year</span><select disabled={Boolean(routeAcademicYearId)} onChange={(event) => selectAcademicYear(event.target.value)} value={academicYearId}><option value="">Select academic year</option>{yearsQuery.data?.map((year) => <option key={year.id} value={year.id}>{year.label} · {year.status}</option>)}</select></label></header>

    <nav aria-label="Program workspace" className="curriculum-section-tabs" role="tablist">
      <button aria-selected={activeSection === "curriculum"} onClick={() => setActiveSection("curriculum")} role="tab" type="button">Curriculum</button>
      <button aria-selected={activeSection === "students"} onClick={() => setActiveSection("students")} role="tab" type="button">Students</button>
      <button aria-selected={activeSection === "teaching-groups"} onClick={() => setActiveSection("teaching-groups")} role="tab" type="button">Teaching Groups</button>
      <button aria-selected={activeSection === "teaching-plan"} onClick={() => setActiveSection("teaching-plan")} role="tab" type="button">Teaching Plan</button>
      <button aria-selected={activeSection === "professors"} onClick={() => setActiveSection("professors")} role="tab" type="button">Professors</button>
      <button aria-selected={activeSection === "schedule"} onClick={() => setActiveSection("schedule")} role="tab" type="button">Schedule</button>
      <button aria-selected={activeSection === "exam-planning"} onClick={() => setActiveSection("exam-planning")} role="tab" type="button">Exam Planning</button>
      <button aria-selected={activeSection === "grades"} onClick={() => setActiveSection("grades")} role="tab" type="button">Grades</button>
      <button aria-selected={activeSection === "progression"} onClick={() => setActiveSection("progression")} role="tab" type="button">Progression</button>
      {selectedLevel?.terminalLevel && <button aria-selected={activeSection === "graduation"} onClick={() => setActiveSection("graduation")} role="tab" type="button">Graduation</button>}
    </nav>

    <div className="curriculum-layout">
      <aside className="management-panel curriculum-levels">
        <header>
          <div>
            <p className="management-kicker">Program structure</p>
            <h2>Academic Levels</h2>
            <span>{levels.length ? `${levels.length} configured` : "Build the program hierarchy"}</span>
          </div>
          {activeSection === "curriculum" && <button className="curriculum-level-add" disabled={!canCreateLevel} onClick={() => { setLevelForm({ name: "", levelOrder: String(levels.length + 1), terminalLevel: false, initialAcademicYearId: academicYearId || yearsQuery.data?.[0]?.id || "", academicRuleProfileId: activeProfiles[0]?.id || "" }); setCreatingRuleProfile(false); setCreatingLevel(true); }} type="button">Add level</button>}
        </header>
        {levelsQuery.isPending ? <div className="panel-empty">Loading levels...</div> : levelsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(levelsQuery.error)}</div> : levels.length === 0 ? <div className="panel-empty curriculum-level-empty"><strong>No levels configured</strong><p>{canCreateLevel ? "Add the first level and define its initial academic rules." : "Create an academic year before adding levels."}</p></div> : <div className="curriculum-level-list">{levels.map((level) => {
          const isSelected = level.id === academicLevelId;
          return <article className={isSelected ? "is-active" : ""} key={level.id}>
            <button className="curriculum-level-select" onClick={() => selectAcademicLevel(level.id)} type="button">
              <strong>{level.name}</strong>
              <small>{isSelected ? "Currently viewing" : activeSection === "students" ? "View student cohort" : activeSection === "teaching-groups" ? "View TD and TP groups" : activeSection === "teaching-plan" ? "View required teaching delivery" : activeSection === "professors" ? "View assigned Professors" : activeSection === "schedule" ? "View weekly timetable" : activeSection === "exam-planning" ? "Plan examination periods" : activeSection === "grades" ? "Review and publish grades" : activeSection === "progression" ? "Review annual decisions" : activeSection === "graduation" ? "Review graduated students" : "View semesters and modules"}</small>
            </button>
            {activeSection === "curriculum" && <div className="row-actions">
              <button disabled={!academicYearId} onClick={() => { setAcademicLevelId(level.id); setConfiguringGroupPolicy(level); }} type="button">Group sizes</button>
              <button onClick={() => { setAcademicLevelId(level.id); setEditingLevel(level); setLevelForm({ name: level.name, levelOrder: String(level.levelOrder), terminalLevel: level.terminalLevel, initialAcademicYearId: "", academicRuleProfileId: level.id === academicLevelId ? currentRuleAssignment?.academicRuleProfileId ?? "" : "" }); }} type="button">Edit</button>
              <button className="danger-text" onClick={() => setDeletingLevel(level)} type="button">Delete</button>
            </div>}
          </article>;
        })}</div>}
      </aside>

      <main className="curriculum-main">
        {activeSection === "curriculum" ? <>
          <section className="management-panel curriculum-semesters"><header className="panel-header panel-header--bordered"><div><p className="management-kicker">{selectedYear?.label ?? "Academic year required"}</p><h2>{selectedLevel ? `${selectedLevel.name} Semesters` : "Select an Academic Level"}</h2><p>{selectedRuleProfile ? `Academic rules: ${selectedRuleProfile.name} · v${selectedRuleProfile.version}` : "Each academic year has its own semester structure and modules."}</p></div><div className="curriculum-header-actions"><button className="management-primary-button" disabled={!academicLevelId || !academicYearId} onClick={() => { const order = semesters.length + 1; const autumn = order % 2 === 1; setSemesterForm({ name: "", semesterOrder: String(order), termType: autumn ? "AUTUMN" : "SPRING", startDate: selectedYear ? `${autumn ? selectedYear.startYear : selectedYear.endYear}-${autumn ? "09-01" : "02-01"}` : "", endDate: selectedYear ? `${selectedYear.endYear}-${autumn ? "01-31" : "06-30"}` : "" }); setCreatingSemester(true); }} type="button">New Semester</button></div></header>{!academicYearId ? <div className="panel-empty"><strong>Select an academic year.</strong><p>The year determines which semesters and modules are displayed.</p></div> : !academicLevelId ? <div className="panel-empty"><strong>Select an academic level.</strong></div> : semestersQuery.isPending ? <div className="panel-empty">Loading semesters...</div> : semestersQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(semestersQuery.error)}</div> : semesters.length === 0 ? <div className="panel-empty"><strong>No semesters configured.</strong><p>Create the first semester for this level and academic year.</p></div> : <div className="semester-card-grid">{semesters.map((semester) => <article className={semester.id === semesterId ? "is-active" : ""} key={semester.id}><button onClick={() => setSemesterId(semester.id)} type="button"><span>{semester.termType === "AUTUMN" ? "Autumn term" : "Spring term"} · {semester.lifecycleStatus}</span><strong>{semester.name}</strong><small>{semester.startDate} – {semester.endDate}</small></button><div className="row-actions"><button onClick={() => { setEditingSemester(semester); setSemesterForm({ name: semester.name, semesterOrder: String(semester.semesterOrder), termType: semester.termType, startDate: semester.startDate, endDate: semester.endDate }); }} type="button">Edit</button><button className="danger-text" onClick={() => setDeletingSemester(semester)} type="button">Delete</button></div></article>)}</div>}</section>

          <section className="management-panel curriculum-modules"><header className="panel-header panel-header--bordered"><div><p className="management-kicker">{selectedSemester?.name ?? "Semester modules"}</p><h2>Subject Modules</h2><p>{selectedSemester ? `Modules delivered in ${selectedSemester.name} for ${selectedYear?.label}.` : "Select a semester to manage its modules."}</p></div><button className="management-primary-button" disabled={!semesterId} onClick={() => { setModuleForm(emptyModuleForm); setCreatingModule(true); }} type="button">New Module</button></header>{!semesterId ? <div className="panel-empty"><strong>Select a semester.</strong></div> : modulesQuery.isPending ? <div className="panel-empty">Loading modules...</div> : modulesQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(modulesQuery.error)}</div> : modules.length === 0 ? <div className="panel-empty"><strong>No modules configured.</strong><p>Create the first subject module for this semester.</p></div> : <div className="resource-table-wrapper"><table className="resource-table"><thead><tr><th>Module</th><th>Academic domains</th><th>Actions</th></tr></thead><tbody>{modules.map((module) => <tr key={module.id}><td><Link className="resource-name resource-name--link module-record-link" to={modulePathFor(module.id)}><span className="resource-monogram">{module.code.slice(0, 2)}</span><div><strong>{module.title}</strong><small>{module.code}</small></div></Link></td><td>{module.academicDomainIds.length ? module.academicDomainIds.map((id) => domainNames.get(id) ?? "Unknown domain").join(", ") : "No domain assigned"}</td><td><div className="row-actions"><button onClick={() => { setEditingModule(module); setModuleForm({ code: module.code, title: module.title, academicDomainIds: module.academicDomainIds }); }} type="button">Edit</button><button className="danger-text" onClick={() => setDeletingModule(module)} type="button">Delete</button></div></td></tr>)}</tbody></table></div>}</section>
        </> : activeSection === "students" ? <ProgramStudentCohort academicLevel={selectedLevel} academicLevels={levels} academicYearId={academicYearId} academicYearLabel={selectedYear?.label} establishmentId={establishmentId} onSelectAcademicLevel={selectAcademicLevel} programFiliereId={programFiliereId} semesters={semesters} studentDetailsPath={(studentId) => `${workspacePath}/students/${studentId}`} />
          : activeSection === "teaching-groups" ? <TeachingGroupWorkspace academicLevelName={selectedLevel?.name} semesters={semesters} studentDetailsPath={(studentId) => `${workspacePath}/students/${studentId}`} />
          : activeSection === "professors" ? <SemesterProfessorsWorkspace academicLevelName={selectedLevel?.name} academicYearLabel={selectedYear?.label} establishmentId={establishmentId} modules={modules} onSelectSemester={setSemesterId} professorDetailsPath={(professorId) => `${workspacePath}/professors/${professorId}`} semesterId={semesterId} semesterName={selectedSemester?.name} semesters={semesters} />
          : activeSection === "schedule" ? <SemesterTimetableWorkspace academicLevelId={academicLevelId} academicLevelName={selectedLevel?.name} academicYearId={academicYearId} academicYearLabel={selectedYear?.label} establishmentId={establishmentId} modules={modules} onSelectSemester={setSemesterId} semesterId={semesterId} semesterName={selectedSemester?.name} semesters={semesters} />
          : activeSection === "exam-planning" ? <ExamPlanningWorkspace academicLevelId={academicLevelId} academicYearId={academicYearId} academicYearLabel={selectedYear?.label} establishmentId={establishmentId} modules={modules} programName={program.name} onSelectSemester={setSemesterId} semesterId={semesterId} semesterName={selectedSemester?.name} semesters={semesters} />
          : activeSection === "grades" ? <GradeManagementWorkspace academicLevelId={academicLevelId} academicLevelName={selectedLevel?.name} academicYearId={academicYearId} academicYearLabel={selectedYear?.label} establishmentId={establishmentId} moduleValidationThreshold={selectedRuleProfile?.moduleValidationThreshold} modules={modules} onOpenOriginalSemester={openOriginalSemester} onSelectSemester={setSemesterId} programName={program.name} programPathName={selectedProgramPath?.name} semesterId={semesterId} semesterName={selectedSemester?.name} semesters={semesters} studentDetailsPath={(studentId) => `${workspacePath}/students/${studentId}`} />
          : activeSection === "progression" ? <ProgressionWorkspace academicLevelId={academicLevelId} academicLevelName={selectedLevel?.name} academicYearId={academicYearId} academicYearLabel={selectedYear?.label} terminalLevel={selectedLevel?.terminalLevel} studentDetailsPath={(studentId) => `${workspacePath}/students/${studentId}`} />
          : activeSection === "graduation" && selectedLevel?.terminalLevel ? <GraduationWorkspace academicLevelId={academicLevelId} academicLevelName={selectedLevel.name} academicYearId={academicYearId} academicYearLabel={selectedYear?.label} studentDetailsPath={(studentId) => `${workspacePath}/students/${studentId}`} />
          : <TeachingPlanWorkspace academicLevelName={selectedLevel?.name} academicYearLabel={selectedYear?.label} establishmentId={establishmentId} modules={modules} onSelectSemester={setSemesterId} semesterId={semesterId} semesterName={selectedSemester?.name} semesters={semesters} />}
      </main>
    </div>

    {(creatingLevel || editingLevel) && <ManagementModal
      title={creatingRuleProfile ? "Create Academic Rule Profile" : `${editingLevel ? "Edit" : "Create"} Academic Level`}
      description={creatingRuleProfile ? "Define the rules that will govern this level for the selected academic year." : editingLevel ? `Update the level and its rules for ${selectedYear?.label ?? "the selected academic year"}.` : "Create the level and its initial academic rule assignment."}
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
        <label className="form-field form-field--wide curriculum-policy-check"><input checked={ruleProfileForm.allowProgressionWithDebt} onChange={(event) => { if (event.target.checked && levelForm.terminalLevel) { setFormError("Progression with debt is not available for a final academic level because there is no next level to promote the student into."); return; } setRuleProfileForm({ ...ruleProfileForm, allowProgressionWithDebt: event.target.checked }); setFormError(null); }} type="checkbox" /><span><strong>Allow progression with module debt</strong><small>Students may progress while carrying modules within the configured limit.</small></span></label>
        {formError && <div className="management-alert management-alert--error">{formError}</div>}
        {ruleProfileMutation.isError && <div className="management-alert management-alert--error">{errorMessage(ruleProfileMutation.error)}</div>}
        <footer className="form-actions"><button className="secondary-button" onClick={() => { setCreatingRuleProfile(false); setFormError(null); }} type="button">Back</button><button className="management-primary-button" disabled={ruleProfileMutation.isPending} onClick={submitRuleProfile} type="button">{ruleProfileMutation.isPending ? "Creating..." : "Create and select"}</button></footer>
      </div> : <div className="management-form management-form--two-columns">
        <div className="form-field"><label htmlFor="level-name">Name</label><input autoFocus id="level-name" maxLength={100} onChange={(event) => { setLevelForm({ ...levelForm, name: event.target.value }); setFormError(null); }} placeholder="M1" value={levelForm.name} /></div>
        <div className="form-field"><label htmlFor="level-order">Order</label><input id="level-order" min="1" onChange={(event) => { setLevelForm({ ...levelForm, levelOrder: event.target.value }); setFormError(null); }} type="number" value={levelForm.levelOrder} /></div>
        <label className="form-field form-field--wide curriculum-policy-check"><input checked={levelForm.terminalLevel} onChange={(event) => setTerminalLevel(event.target.checked)} type="checkbox" /><span><strong>Final level of the program</strong><small>Successful annual decisions validate this level instead of promoting the student.</small></span></label>
        {!editingLevel && <>
          <div className="form-field"><label htmlFor="level-year">Initial academic year</label><select id="level-year" onChange={(event) => setLevelForm({ ...levelForm, initialAcademicYearId: event.target.value })} value={levelForm.initialAcademicYearId}>{yearsQuery.data?.map((year) => <option key={year.id} value={year.id}>{year.label}</option>)}</select></div>
        </>}
        <div className="form-field"><label htmlFor="level-rule">{editingLevel ? `Rule profile · ${selectedYear?.label ?? "selected year"}` : "Academic rule profile"}</label><select id="level-rule" onChange={(event) => selectRuleProfile(event.target.value)} value={levelForm.academicRuleProfileId}><option value="">Select a rule profile</option>{activeProfiles.map((profile) => <option key={profile.id} value={profile.id}>{profile.name} · v{profile.version}</option>)}</select></div>
        {!editingLevel && <>
          <div className="form-field form-field--wide curriculum-rule-create"><span>{activeProfiles.length ? "Need a different policy?" : "No active rule profile is available."}</span><button className="secondary-button secondary-button--compact" onClick={() => { setCreatingRuleProfile(true); setRuleProfileForm({ ...emptyRuleProfileForm, allowProgressionWithDebt: levelForm.terminalLevel ? false : emptyRuleProfileForm.allowProgressionWithDebt, maximumCarriedModules: levelForm.terminalLevel ? "0" : emptyRuleProfileForm.maximumCarriedModules }); setFormError(null); }} type="button">Create rule profile</button></div>
        </>}
        {formError && <div className="management-alert management-alert--error">{formError}</div>}
        {levelMutation.isError && <div className="management-alert management-alert--error">{errorMessage(levelMutation.error)}</div>}
        <footer className="form-actions"><button className="secondary-button" onClick={closeLevelForm} type="button">Cancel</button><button className="management-primary-button" disabled={levelMutation.isPending || hasIncompatibleTerminalRule} onClick={submitLevel} type="button">{levelMutation.isPending ? "Saving..." : "Save"}</button></footer>
      </div>}
    </ManagementModal>}
    {configuringGroupPolicy && academicYearId && <TeachingGroupPolicyModal academicLevelId={configuringGroupPolicy.id} academicLevelName={configuringGroupPolicy.name} academicYearId={academicYearId} academicYearLabel={selectedYear?.label ?? "Selected academic year"} onClose={() => setConfiguringGroupPolicy(null)} />}
    {(creatingSemester || editingSemester) && <ManagementModal title={`${editingSemester ? "Edit" : "Create"} Semester`} description={`${selectedLevel?.name ?? "Academic level"} · ${selectedYear?.label ?? "Academic year"}`} onClose={closeSemesterForm}><div className="management-form management-form--two-columns"><div className="form-field"><label htmlFor="semester-name">Name</label><input autoFocus id="semester-name" maxLength={100} onChange={(event) => { setSemesterForm({ ...semesterForm, name: event.target.value }); setFormError(null); }} placeholder="S1" value={semesterForm.name} /></div><div className="form-field"><label htmlFor="semester-order">Order</label><input id="semester-order" min="1" onChange={(event) => { setSemesterForm({ ...semesterForm, semesterOrder: event.target.value }); setFormError(null); }} type="number" value={semesterForm.semesterOrder} /></div><div className="form-field form-field--wide"><label htmlFor="semester-term">Teaching period</label><select id="semester-term" onChange={(event) => setSemesterForm({ ...semesterForm, termType: event.target.value as SemesterForm["termType"] })} value={semesterForm.termType}><option value="AUTUMN">Autumn term</option><option value="SPRING">Spring term</option></select></div><div className="form-field"><label htmlFor="semester-start">Teaching start</label><input id="semester-start" onChange={(event) => setSemesterForm({ ...semesterForm, startDate: event.target.value })} required type="date" value={semesterForm.startDate} /></div><div className="form-field"><label htmlFor="semester-end">Teaching end</label><input id="semester-end" min={semesterForm.startDate} onChange={(event) => setSemesterForm({ ...semesterForm, endDate: event.target.value })} required type="date" value={semesterForm.endDate} /></div>{formError && <div className="management-alert management-alert--error">{formError}</div>}{semesterMutation.isError && <div className="management-alert management-alert--error">{errorMessage(semesterMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={closeSemesterForm} type="button">Cancel</button><button className="management-primary-button" disabled={semesterMutation.isPending || !semesterForm.startDate || !semesterForm.endDate} onClick={submitSemester} type="button">{semesterMutation.isPending ? "Saving..." : "Save"}</button></footer></div></ManagementModal>}
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
