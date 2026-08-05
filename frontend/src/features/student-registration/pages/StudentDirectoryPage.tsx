import { useQueries, useQuery } from "@tanstack/react-query";
import { useDeferredValue, useState } from "react";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  academicStructureKeys,
  getAcademicLevels,
  getAcademicYears,
  getDepartments,
  getProgramFilieres,
  getProgramPaths,
  getSemesters,
} from "@/features/academic-structure/api/academic-structure-api";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import { getEstablishment, rootGovernanceKeys } from "@/features/root-governance/api/root-governance-api";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import {
  getAcademicRegistrations,
  getStudents,
  studentRegistrationKeys,
  type AcademicRegistrationFilters,
  type StudentAccountStatus,
  type StudentDirectoryFilters,
} from "../api/student-registration-api";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

function displayDate(value: string): string {
  return new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(`${value}T00:00:00`));
}

export function StudentDirectoryPage() {
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<StudentAccountStatus | "">("");
  const [enrolledFrom, setEnrolledFrom] = useState("");
  const [enrolledTo, setEnrolledTo] = useState("");
  const [academicYearId, setAcademicYearId] = useState("");
  const [programPathId, setProgramPathId] = useState("");
  const [programFiliereId, setProgramFiliereId] = useState("");
  const [academicLevelId, setAcademicLevelId] = useState("");
  const [semesterId, setSemesterId] = useState("");
  const [registrationStatus, setRegistrationStatus] = useState<AcademicRegistrationFilters["status"] | "">("");
  const deferredQuery = useDeferredValue(query.trim());
  const identityFilters: StudentDirectoryFilters = {
    ...(deferredQuery ? { query: deferredQuery } : {}),
    ...(status ? { status } : {}),
    ...(enrolledFrom ? { enrolledFrom } : {}),
    ...(enrolledTo ? { enrolledTo } : {}),
  };
  const academicFilters: AcademicRegistrationFilters = {
    ...(academicYearId ? { academicYearId } : {}),
    ...(programFiliereId ? { programFiliereId } : {}),
    ...(academicLevelId && !semesterId ? { academicLevelId } : {}),
    ...(semesterId ? { semesterId } : {}),
    ...(registrationStatus ? { status: registrationStatus } : {}),
  };
  const hasAcademicFilters = Boolean(academicYearId || programPathId || programFiliereId || academicLevelId || semesterId || registrationStatus);

  const establishmentQuery = useQuery({ queryKey: rootGovernanceKeys.establishment(establishmentId ?? "missing"), queryFn: () => getEstablishment(establishmentId!), enabled: Boolean(establishmentId) });
  const studentsQuery = useQuery({ queryKey: studentRegistrationKeys.students(establishmentId ?? "missing", identityFilters), queryFn: () => getStudents(establishmentId!, identityFilters), enabled: Boolean(establishmentId) });
  const departmentsQuery = useQuery({ queryKey: academicStructureKeys.departments(establishmentId ?? "missing"), queryFn: () => getDepartments(establishmentId!), enabled: Boolean(establishmentId) });
  const pathsQuery = useQuery({ queryKey: academicStructureKeys.programPaths(establishmentId ?? "missing"), queryFn: () => getProgramPaths(establishmentId!), enabled: Boolean(establishmentId) });
  const yearsQuery = useQuery({ queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"), queryFn: () => getAcademicYears(establishmentId!), enabled: Boolean(establishmentId) });
  const programQueries = useQueries({ queries: (departmentsQuery.data ?? []).map((department) => ({ queryKey: academicStructureKeys.programFilieres(department.id), queryFn: () => getProgramFilieres(department.id) })) });
  const allPrograms = programQueries.flatMap((programQuery) => programQuery.data ?? []).sort((left, right) => left.name.localeCompare(right.name));
  const availablePrograms = programPathId ? allPrograms.filter((program) => program.programPathId === programPathId) : allPrograms;
  const levelsQuery = useQuery({ queryKey: academicStructureKeys.academicLevels(programFiliereId || "missing"), queryFn: () => getAcademicLevels(programFiliereId), enabled: Boolean(programFiliereId) });
  const semestersQuery = useQuery({ queryKey: academicStructureKeys.semesters(academicLevelId || "missing", academicYearId || "missing"), queryFn: () => getSemesters(academicLevelId, academicYearId), enabled: Boolean(academicLevelId && academicYearId) });
  const registrationsQuery = useQuery({ queryKey: studentRegistrationKeys.registrations(establishmentId ?? "missing", academicFilters), queryFn: () => getAcademicRegistrations(establishmentId!, academicFilters), enabled: Boolean(establishmentId && hasAcademicFilters) });

  if (!establishmentId || !workspacePath) return <div className="management-state management-state--error"><h1>No establishment assigned</h1></div>;

  const pathProgramIds = programPathId ? new Set(availablePrograms.map((program) => program.id)) : null;
  const academicStudentIds = hasAcademicFilters ? new Set((registrationsQuery.data ?? [])
    .filter((registration) => !pathProgramIds || pathProgramIds.has(registration.programFiliereId))
    .map((registration) => registration.studentId)) : null;
  const students = (studentsQuery.data ?? []).filter((student) => !academicStudentIds || academicStudentIds.has(student.studentId));
  const academicStructurePending = Boolean(programPathId && (departmentsQuery.isPending || programQueries.some((queryResult) => queryResult.isPending)));
  const isPending = studentsQuery.isPending || (hasAcademicFilters && (registrationsQuery.isPending || academicStructurePending));
  const requestError = studentsQuery.error ?? registrationsQuery.error;
  const establishmentName = establishmentQuery.data?.name ?? "this establishment";

  function clearAcademicDependants(scope: "path" | "program" | "level" | "year") {
    if (scope === "path") setProgramFiliereId("");
    if (scope === "path" || scope === "program") setAcademicLevelId("");
    if (scope === "path" || scope === "program" || scope === "level" || scope === "year") setSemesterId("");
  }

  function clearFilters() {
    setQuery(""); setStatus(""); setEnrolledFrom(""); setEnrolledTo(""); setAcademicYearId(""); setProgramPathId(""); setProgramFiliereId(""); setAcademicLevelId(""); setSemesterId(""); setRegistrationStatus("");
  }

  return <div className="management-page student-directory-page">
    <header className="management-page-header management-page-header--compact">
      <div><p className="management-kicker">Student administration</p><h1>Students</h1><p>Find Student identities and academic registrations across {establishmentName}.</p></div>
    </header>

    <section aria-label="Student filters" className="student-filter-panel">
      <div className="directory-toolbar student-directory-toolbar">
        <label className="search-field"><span>Search</span><input onChange={(event) => setQuery(event.target.value)} placeholder="Name, email, Apogee, national code, or CIN" value={query} /></label>
        <label><span>Account status</span><select onChange={(event) => setStatus(event.target.value as StudentAccountStatus | "")} value={status}><option value="">All statuses</option><option value="ACTIVE">Active</option><option value="LOCKED">Locked</option><option value="DEACTIVATED">Deactivated</option><option value="ARCHIVED">Archived</option></select></label>
        <label><span>Enrolled from</span><input max={enrolledTo || undefined} onChange={(event) => setEnrolledFrom(event.target.value)} type="date" value={enrolledFrom} /></label>
        <label><span>Enrolled to</span><input min={enrolledFrom || undefined} onChange={(event) => setEnrolledTo(event.target.value)} type="date" value={enrolledTo} /></label>
      </div>
      <div className="student-academic-filters">
        <label><span>Academic year</span><select onChange={(event) => { setAcademicYearId(event.target.value); clearAcademicDependants("year"); }} value={academicYearId}><option value="">All years</option>{yearsQuery.data?.map((year) => <option key={year.id} value={year.id}>{year.label}</option>)}</select></label>
        <label><span>Program path</span><select onChange={(event) => { setProgramPathId(event.target.value); clearAcademicDependants("path"); }} value={programPathId}><option value="">All paths</option>{pathsQuery.data?.map((path) => <option key={path.id} value={path.id}>{path.name}</option>)}</select></label>
        <label><span>Program / Filière</span><select onChange={(event) => { setProgramFiliereId(event.target.value); clearAcademicDependants("program"); }} value={programFiliereId}><option value="">All programs</option>{availablePrograms.map((program) => <option key={program.id} value={program.id}>{program.code} · {program.name}</option>)}</select></label>
        <label><span>Academic level</span><select disabled={!programFiliereId} onChange={(event) => { setAcademicLevelId(event.target.value); clearAcademicDependants("level"); }} value={academicLevelId}><option value="">All levels</option>{levelsQuery.data?.map((level) => <option key={level.id} value={level.id}>{level.name}</option>)}</select></label>
        <label><span>Semester</span><select disabled={!academicLevelId || !academicYearId} onChange={(event) => setSemesterId(event.target.value)} value={semesterId}><option value="">All semesters</option>{semestersQuery.data?.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label>
        <label><span>Registration status</span><select onChange={(event) => setRegistrationStatus(event.target.value as AcademicRegistrationFilters["status"] | "")} value={registrationStatus}><option value="">All registrations</option><option value="ACTIVE">Active</option><option value="COMPLETED">Completed</option><option value="SUSPENDED">Suspended</option><option value="CANCELLED">Cancelled</option></select></label>
        <button className="secondary-button secondary-button--compact" onClick={clearFilters} type="button">Clear filters</button>
      </div>
    </section>

    <section className="management-panel directory-panel">
      <header className="panel-header panel-header--bordered"><div><h2>Student Directory</h2><p>{students.length} {students.length === 1 ? "Student" : "Students"} found</p></div></header>
      {isPending ? <div className="panel-empty">Loading Students...</div>
        : requestError ? <div className="panel-empty panel-empty--error">{errorMessage(requestError)}</div>
        : students.length === 0 ? <div className="panel-empty"><strong>No Student matches this view.</strong><p>Adjust the identity or academic filters.</p></div>
        : <div className="resource-table-wrapper"><table className="resource-table resource-table--accounts student-directory-table"><thead><tr><th>Student</th><th>Institutional identity</th><th>Initial enrollment</th><th>Status</th><th>Actions</th></tr></thead><tbody>{students.map((student) => <tr className="resource-row--linked" key={student.studentId}>
          <td><Link className="resource-name resource-name--link" to={`${workspacePath}/students/${student.studentId}`}><span className="person-monogram">{student.firstName[0]}{student.lastName[0]}</span><div><strong>{student.firstName} {student.lastName}</strong><small>{student.apogeeCode}</small></div></Link></td>
          <td><div className="table-contact"><span>{student.universityEmail}</span><small>{[student.nationalStudentCode, student.cin].filter(Boolean).join(" · ") || "No secondary identifier"}</small></div></td>
          <td><div className="table-contact"><span>{displayDate(student.initialEnrollmentDate)}</span><small>{student.phoneNumber || "No phone number"}</small></div></td>
          <td><StatusBadge status={student.accountStatus} /></td>
          <td><div className="row-actions"><Link className="record-open-link" to={`${workspacePath}/students/${student.studentId}`}>View</Link></div></td>
        </tr>)}</tbody></table></div>}
    </section>
  </div>;
}
