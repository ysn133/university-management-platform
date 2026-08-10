import { useDeferredValue, useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { academicStructureKeys, getAcademicDomains } from "@/features/academic-structure/api/academic-structure-api";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { getEstablishment, rootGovernanceKeys } from "@/features/root-governance/api/root-governance-api";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import {
  createProfessor,
  getProfessorExpertise,
  getProfessors,
  professorManagementKeys,
  replaceProfessorExpertise,
  type Professor,
  type ProfessorAccountStatus,
  type ProfessorDirectoryFilters,
} from "../api/professor-management-api";
import { ProfessorForm, type ProfessorFormValues } from "../components/ProfessorForm";

interface ExpertiseTarget {
  professorId: string;
  name: string;
  universityEmail: string;
  newlyCreated: boolean;
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function displayDate(value?: string | null): string {
  if (!value) return "Not specified";
  return new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(`${value}T00:00:00`));
}

export function ProfessorManagementPage() {
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<ProfessorAccountStatus | "">("");
  const [joinedFrom, setJoinedFrom] = useState("");
  const [joinedTo, setJoinedTo] = useState("");
  const [academicDomainId, setAcademicDomainId] = useState("");
  const [creating, setCreating] = useState(false);
  const [expertiseTarget, setExpertiseTarget] = useState<ExpertiseTarget | null>(null);
  const [selectedDomainIds, setSelectedDomainIds] = useState<string[]>([]);
  const deferredQuery = useDeferredValue(query.trim());
  const filters: ProfessorDirectoryFilters = {
    ...(deferredQuery ? { query: deferredQuery } : {}),
    ...(status ? { status } : {}),
    ...(joinedFrom ? { joinedFrom } : {}),
    ...(joinedTo ? { joinedTo } : {}),
    ...(academicDomainId ? { academicDomainId } : {}),
  };

  const establishmentQuery = useQuery({ queryKey: rootGovernanceKeys.establishment(establishmentId ?? "missing"), queryFn: () => getEstablishment(establishmentId!), enabled: Boolean(establishmentId) });
  const professorsQuery = useQuery({ queryKey: professorManagementKeys.professors(establishmentId ?? "missing", filters), queryFn: () => getProfessors(establishmentId!, filters), enabled: Boolean(establishmentId) });
  const domainsQuery = useQuery({ queryKey: academicStructureKeys.academicDomains(establishmentId ?? "missing"), queryFn: () => getAcademicDomains(establishmentId!), enabled: Boolean(establishmentId) });
  const expertiseQuery = useQuery({ queryKey: professorManagementKeys.expertise(expertiseTarget?.professorId ?? "missing"), queryFn: () => getProfessorExpertise(expertiseTarget!.professorId), enabled: Boolean(expertiseTarget && !expertiseTarget.newlyCreated) });

  useEffect(() => {
    if (expertiseQuery.data) setSelectedDomainIds(expertiseQuery.data.academicDomains.map((domain) => domain.academicDomainId));
  }, [expertiseQuery.data]);

  async function refreshProfessors() {
    await queryClient.invalidateQueries({ queryKey: ["professor-management", "professors", establishmentId] });
  }

  const createMutation = useMutation({
    mutationFn: (values: ProfessorFormValues) => createProfessor(establishmentId!, {
      employeeNumber: values.employeeNumber.trim(),
      academicRankId: values.academicRankId,
      hireDate: values.hireDate || undefined,
      maximumWeeklyTeachingMinutes: Number(values.maximumWeeklyTeachingMinutes),
      cin: values.cin.trim() || undefined,
      universityEmail: values.universityEmail.trim(),
      password: values.password,
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      birth_date: values.birthDate,
      placeOfBirth: values.placeOfBirth.trim(),
      nationality: values.nationality.trim(),
      sex: values.sex,
      phone_number: values.phoneNumber.trim() || undefined,
    }),
    onSuccess: async (response, values) => {
      await refreshProfessors();
      setCreating(false);
      setSelectedDomainIds([]);
      setExpertiseTarget({ professorId: response.professorId, name: `${values.firstName} ${values.lastName}`, universityEmail: values.universityEmail, newlyCreated: true });
    },
  });
  const expertiseMutation = useMutation({
    mutationFn: () => replaceProfessorExpertise(expertiseTarget!.professorId, selectedDomainIds),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: professorManagementKeys.expertise(expertiseTarget!.professorId) });
      await refreshProfessors();
      setExpertiseTarget(null);
      setSelectedDomainIds([]);
    },
  });

  function openExpertise(professor: Professor) {
    setSelectedDomainIds([]);
    setExpertiseTarget({ professorId: professor.professorId, name: `${professor.firstName} ${professor.lastName}`, universityEmail: professor.universityEmail, newlyCreated: false });
  }

  function toggleDomain(domainId: string) {
    setSelectedDomainIds((current) => current.includes(domainId) ? current.filter((id) => id !== domainId) : [...current, domainId]);
  }

  function clearFilters() {
    setQuery(""); setStatus(""); setJoinedFrom(""); setJoinedTo(""); setAcademicDomainId("");
  }

  if (!establishmentId || !workspacePath) return <div className="management-state management-state--error"><h1>No establishment assigned</h1></div>;
  const professors = professorsQuery.data ?? [];
  const establishmentName = establishmentQuery.data?.name ?? "this establishment";

  return <div className="management-page professor-directory-page">
    <header className="management-page-header management-page-header--compact"><div><p className="management-kicker">Teaching staff</p><h1>Professors</h1><p>Create Professor accounts and manage teaching qualifications for {establishmentName}.</p></div><button className="management-primary-button" onClick={() => { createMutation.reset(); setCreating(true); }} type="button">Add Professor</button></header>

    <section className="professor-filter-panel"><div className="directory-toolbar professor-directory-toolbar"><label className="search-field"><span>Search</span><input onChange={(event) => setQuery(event.target.value)} placeholder="Name, email, employee number, CIN, or rank" value={query} /></label><label><span>Account status</span><select onChange={(event) => setStatus(event.target.value as ProfessorAccountStatus | "")} value={status}><option value="">All statuses</option><option value="ACTIVE">Active</option><option value="LOCKED">Locked</option><option value="DEACTIVATED">Deactivated</option><option value="ARCHIVED">Archived</option></select></label><label><span>Expertise</span><select onChange={(event) => setAcademicDomainId(event.target.value)} value={academicDomainId}><option value="">All domains</option>{domainsQuery.data?.map((domain) => <option key={domain.id} value={domain.id}>{domain.code} · {domain.name}</option>)}</select></label><label><span>Joined from</span><input max={joinedTo || undefined} onChange={(event) => setJoinedFrom(event.target.value)} type="date" value={joinedFrom} /></label><label><span>Joined to</span><input min={joinedFrom || undefined} onChange={(event) => setJoinedTo(event.target.value)} type="date" value={joinedTo} /></label><button className="secondary-button secondary-button--compact" onClick={clearFilters} type="button">Clear filters</button></div></section>

    <section className="management-panel directory-panel"><header className="panel-header panel-header--bordered"><div><h2>Professor Directory</h2><p>{professors.length} {professors.length === 1 ? "Professor" : "Professors"} found</p></div></header>{professorsQuery.isPending ? <div className="panel-empty">Loading Professors...</div> : professorsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(professorsQuery.error)}</div> : professors.length === 0 ? <div className="panel-empty"><strong>No Professor matches this view.</strong><p>Adjust the filters or create a Professor account.</p></div> : <div className="resource-table-wrapper"><table className="resource-table resource-table--accounts professor-directory-table"><thead><tr><th>Professor</th><th>Employment</th><th>Teaching capacity</th><th>Status</th><th>Actions</th></tr></thead><tbody>{professors.map((professor) => <tr key={professor.professorId}><td><div className="resource-name"><span className="person-monogram">{professor.firstName[0]}{professor.lastName[0]}</span><div><Link className="record-name-link" to={`${workspacePath}/professors/${professor.professorId}`}>{professor.firstName} {professor.lastName}</Link><small>{professor.universityEmail}</small></div></div></td><td><div className="table-contact"><span>{professor.employeeNumber}</span><small>{professor.academicRank || "Rank not specified"} · Joined {displayDate(professor.hireDate)}</small></div></td><td><div className="table-contact"><span>{professor.maximumWeeklyTeachingMinutes} minutes / week</span><small>{Math.round(professor.maximumWeeklyTeachingMinutes / 60 * 10) / 10} teaching hours maximum</small></div></td><td><StatusBadge status={professor.accountStatus} /></td><td><div className="row-actions"><Link to={`${workspacePath}/professors/${professor.professorId}`}>View</Link><button onClick={() => openExpertise(professor)} type="button">Expertise</button></div></td></tr>)}</tbody></table></div>}</section>

    {creating && <ManagementModal title="Add Professor" description="Create the Professor's login, personal identity, and employment profile." onClose={() => setCreating(false)}><ProfessorForm establishmentId={establishmentId} error={createMutation.isError ? errorMessage(createMutation.error) : null} isSubmitting={createMutation.isPending} onCancel={() => setCreating(false)} onSubmit={(values) => createMutation.mutate(values)} /></ManagementModal>}

    {expertiseTarget && <ManagementModal title={`${expertiseTarget.newlyCreated ? "Assign expertise to" : "Professor expertise"} ${expertiseTarget.name}`} description={expertiseTarget.newlyCreated ? `The account is active. ${expertiseTarget.universityEmail} can now sign in through the Professor portal.` : "Select the academic domains this Professor is qualified to teach."} onClose={() => { setExpertiseTarget(null); setSelectedDomainIds([]); }}><div className="professor-expertise-form">{expertiseQuery.isPending && !expertiseTarget.newlyCreated ? <div className="panel-empty">Loading expertise...</div> : expertiseQuery.isError ? <div className="management-alert management-alert--error">{errorMessage(expertiseQuery.error)}</div> : domainsQuery.data?.length ? <div className="professor-domain-options">{domainsQuery.data.map((domain) => <label key={domain.id}><input checked={selectedDomainIds.includes(domain.id)} onChange={() => toggleDomain(domain.id)} type="checkbox" /><span><strong>{domain.name}</strong><small>{domain.code}</small></span></label>)}</div> : <div className="panel-empty"><strong>No academic domains are available.</strong><p>Create academic domains before assigning Professor expertise.</p></div>}{expertiseMutation.isError && <div className="management-alert management-alert--error">{errorMessage(expertiseMutation.error)}</div>}<footer className="form-actions"><button className="secondary-button" onClick={() => { setExpertiseTarget(null); setSelectedDomainIds([]); }} type="button">{expertiseTarget.newlyCreated ? "Skip for now" : "Cancel"}</button><button className="management-primary-button" disabled={expertiseMutation.isPending || (!expertiseTarget.newlyCreated && expertiseQuery.isPending)} onClick={() => expertiseMutation.mutate()} type="button">{expertiseMutation.isPending ? "Saving..." : "Save expertise"}</button></footer></div></ManagementModal>}
  </div>;
}
