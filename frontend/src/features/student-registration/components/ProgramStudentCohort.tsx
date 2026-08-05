import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { ManagementModal } from "@/features/root-governance/components/ManagementModal";
import { StatusBadge } from "@/features/root-governance/components/StatusBadge";
import type { AcademicLevel, Semester } from "@/features/academic-structure/api/academic-structure-api";
import {
  createAcademicRegistration,
  createStudent,
  getAcademicRegistrations,
  getRegistrationStudyContext,
  getStudents,
  studentRegistrationKeys,
} from "../api/student-registration-api";
import { StudentForm, type StudentFormValues } from "./StudentForm";
import { ClassGroupWorkspace } from "./ClassGroupWorkspace";

interface ProgramStudentCohortProps {
  establishmentId: string;
  programFiliereId: string;
  academicYearId: string;
  academicYearLabel?: string;
  academicLevel?: AcademicLevel;
  academicLevels: AcademicLevel[];
  semesters: Semester[];
  onSelectAcademicLevel: (academicLevelId: string) => void;
  studentDetailsPath: (studentId: string) => string;
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function ProgramStudentCohort({ establishmentId, programFiliereId, academicYearId, academicYearLabel, academicLevel, academicLevels, semesters, onSelectAcademicLevel, studentDetailsPath }: ProgramStudentCohortProps) {
  const queryClient = useQueryClient();
  const [semesterFilter, setSemesterFilter] = useState("");
  const [moduleFilter, setModuleFilter] = useState("");
  const [pendingDebtSemesterId, setPendingDebtSemesterId] = useState("");
  const [search, setSearch] = useState("");
  const [isRegistrationOpen, setRegistrationOpen] = useState(false);
  const [isCreatingStudent, setCreatingStudent] = useState(true);
  const [selectedStudentId, setSelectedStudentId] = useState("");
  const [classGroupRegistrationIds, setClassGroupRegistrationIds] = useState<Set<string> | null>(null);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());

  useEffect(() => {
    if (semesterFilter && !semesters.some((semester) => semester.id === semesterFilter)) setSemesterFilter("");
  }, [semesterFilter, semesters]);
  useEffect(() => {
    setModuleFilter("");
  }, [academicLevel?.id, academicYearId]);
  useEffect(() => {
    setModuleFilter("");
  }, [semesterFilter]);
  useEffect(() => {
    if (pendingDebtSemesterId && semesters.some((semester) => semester.id === pendingDebtSemesterId)) {
      setSemesterFilter(pendingDebtSemesterId);
      setPendingDebtSemesterId("");
    }
  }, [pendingDebtSemesterId, semesters]);

  const studentsQuery = useQuery({
    queryKey: studentRegistrationKeys.students(establishmentId),
    queryFn: () => getStudents(establishmentId),
  });
  const registrationsQuery = useQuery({
    queryKey: studentRegistrationKeys.registrations(establishmentId, academicYearId ? { academicYearId } : {}),
    queryFn: () => getAcademicRegistrations(establishmentId, { academicYearId }),
    enabled: Boolean(academicYearId),
  });
  const programRegistrations = (registrationsQuery.data ?? []).filter((registration) => registration.programFiliereId === programFiliereId);
  const contextQueries = useQueries({
    queries: programRegistrations.map((registration) => ({
      queryKey: studentRegistrationKeys.studyContext(registration.id),
      queryFn: () => getRegistrationStudyContext(registration.id),
    })),
  });
  const contextByRegistration = new Map(programRegistrations.map((registration, index) => [registration.id, contextQueries[index]?.data]));
  const selectedSemesterIds = new Set(semesters.map((semester) => semester.id));
  const cohort = programRegistrations.filter((registration) =>
    registration.academicLevelId === academicLevel?.id
    || contextByRegistration.get(registration.id)?.some((item) =>
      selectedSemesterIds.has(item.semester.semesterId)
      && item.modules.some((module) => module.inscriptionNumber > 1),
    ),
  );
  const cohortModules = Array.from(new Map(
    cohort
      .flatMap((registration) => contextByRegistration.get(registration.id)?.filter((item) =>
        selectedSemesterIds.has(item.semester.semesterId)
        && (!semesterFilter || item.semester.semesterId === semesterFilter),
      ).flatMap((item) => item.modules) ?? [])
      .map((module) => [module.subjectModuleId, module]),
  ).values()).sort((left, right) => left.subjectModuleCode.localeCompare(right.subjectModuleCode));
  const studentById = new Map((studentsQuery.data ?? []).map((student) => [student.studentId, student]));
  const registeredStudentIds = new Set((registrationsQuery.data ?? []).map((registration) => registration.studentId));
  const availableStudents = (studentsQuery.data ?? []).filter((student) => student.accountStatus === "ACTIVE" && !registeredStudentIds.has(student.studentId));
  const visibleCohort = cohort.filter((registration) => {
    const student = studentById.get(registration.studentId);
    const matchesSearch = !deferredSearch || [student?.firstName, student?.lastName, student?.apogeeCode, student?.nationalStudentCode].filter(Boolean).join(" ").toLowerCase().includes(deferredSearch);
    const matchesStudyContext = (!semesterFilter && !moduleFilter) || contextByRegistration.get(registration.id)?.some((item) =>
      (!semesterFilter || item.semester.semesterId === semesterFilter)
      && (!moduleFilter || item.modules.some((module) => module.subjectModuleId === moduleFilter)),
    );
    const matchesClassGroup = classGroupRegistrationIds === null || classGroupRegistrationIds.has(registration.id);
    return matchesSearch && Boolean(matchesStudyContext) && matchesClassGroup;
  });

  async function refresh() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: studentRegistrationKeys.students(establishmentId) }),
      queryClient.invalidateQueries({ queryKey: ["student-registration", "registrations", establishmentId] }),
    ]);
  }

  function closeRegistration() {
    setRegistrationOpen(false);
    setCreatingStudent(true);
    setSelectedStudentId("");
    createStudentMutation.reset();
    registrationMutation.reset();
  }

  const registrationMutation = useMutation({
    mutationFn: (studentId: string) => createAcademicRegistration(establishmentId, {
      studentId,
      programFiliereId,
      academicLevelId: academicLevel!.id,
      academicYearId,
    }),
    onSuccess: async () => { await refresh(); closeRegistration(); },
  });
  const createStudentMutation = useMutation({
    mutationFn: (values: StudentFormValues) => createStudent(establishmentId, {
      apogeeCode: values.apogeeCode,
      nationalStudentCode: values.nationalStudentCode || undefined,
      cin: values.cin || undefined,
      initialEnrollmentDate: values.initialEnrollmentDate,
      universityEmail: values.universityEmail,
      password: values.password,
      firstName: values.firstName,
      lastName: values.lastName,
      birth_date: values.birthDate,
      placeOfBirth: values.placeOfBirth,
      nationality: values.nationality,
      sex: values.sex,
      phone_number: values.phoneNumber || undefined,
    }),
    onSuccess: async (student) => {
      setSelectedStudentId(student.studentId);
      await queryClient.invalidateQueries({ queryKey: studentRegistrationKeys.students(establishmentId) });
      try { await registrationMutation.mutateAsync(student.studentId); } catch { setCreatingStudent(false); }
    },
  });

  const canRegister = Boolean(academicLevel && academicYearId && semesters.length === 2);
  return <section className="management-panel curriculum-students">
    <header className="panel-header panel-header--bordered">
      <div><p className="management-kicker">Academic cohort</p><h2>{academicLevel ? `${academicLevel.name} Students` : "Students"}</h2><p>{academicLevel ? `${academicYearLabel ?? "Selected year"} registrations, including carried modules from earlier levels.` : "Select an academic level to view its students."}</p></div>
      <button className="management-primary-button" disabled={!canRegister} onClick={() => { setCreatingStudent(true); setSelectedStudentId(availableStudents[0]?.studentId ?? ""); setRegistrationOpen(true); }} type="button">Add Student</button>
    </header>
    {academicLevel && <div className="cohort-toolbar">
      <label><span>Search cohort</span><input onChange={(event) => setSearch(event.target.value)} placeholder="Name, Apogee, or national code" value={search} /></label>
      <label><span>Study period</span><select onChange={(event) => setSemesterFilter(event.target.value)} value={semesterFilter}><option value="">Entire academic level</option>{semesters.map((semester) => <option key={semester.id} value={semester.id}>{semester.name}</option>)}</select></label>
      <label><span>Module</span><select onChange={(event) => setModuleFilter(event.target.value)} value={moduleFilter}><option value="">All modules</option>{cohortModules.map((module) => <option key={module.subjectModuleId} value={module.subjectModuleId}>{module.subjectModuleCode} · {module.subjectModuleTitle}</option>)}</select></label>
    </div>}
    {academicLevel && academicYearId && <ClassGroupWorkspace
      academicLevelId={academicLevel.id}
      academicYearId={academicYearId}
      semesterId={semesterFilter || semesters[0]?.id || ""}
      registrations={programRegistrations.filter((registration) => registration.academicLevelId === academicLevel.id && registration.status === "ACTIVE")}
      students={studentsQuery.data ?? []}
      onFilterChange={setClassGroupRegistrationIds}
    />}
    {!academicLevel ? <div className="panel-empty"><strong>Select an academic level.</strong></div>
      : studentsQuery.isPending || registrationsQuery.isPending ? <div className="panel-empty">Loading student cohort...</div>
      : studentsQuery.isError || registrationsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(studentsQuery.error ?? registrationsQuery.error)}</div>
      : visibleCohort.length === 0 ? <div className="panel-empty"><strong>{classGroupRegistrationIds === null ? "No students found in this context." : "No students in this class-group view."}</strong><p>{classGroupRegistrationIds === null ? `Add the first Student registration for ${academicLevel.name}.` : "Choose another group or return to All Students."}</p></div>
      : <div className="resource-table-wrapper"><table className="resource-table"><thead><tr><th>Student</th><th>Registration</th><th>Module history</th><th>Status</th></tr></thead><tbody>{visibleCohort.map((registration) => {
        const student = studentById.get(registration.studentId);
        const context = contextByRegistration.get(registration.id);
        const primaryLevel = academicLevels.find((level) => level.id === registration.academicLevelId);
        const isCarriedStudent = registration.academicLevelId !== academicLevel.id;
        const selectedSemester = semesters.find((semester) => semester.id === semesterFilter);
        const repeatedModules = context?.filter((item) => !selectedSemester || item.semester.semesterOrder === selectedSemester.semesterOrder).flatMap((item) => item.modules
          .filter((module) => module.inscriptionNumber > 1)
          .map((module) => ({ module, semester: item.semester }))) ?? [];
        return <tr key={registration.id}><td><Link className="resource-name resource-name--link" to={studentDetailsPath(registration.studentId)}><span className="person-monogram">{student?.firstName[0]}{student?.lastName[0]}</span><div><strong>{student ? `${student.firstName} ${student.lastName}` : "Student"}</strong><small>{student?.apogeeCode ?? registration.studentId}</small></div></Link></td><td><div className="table-contact"><span>{primaryLevel?.name ?? academicLevel.name}</span><small>{isCarriedStudent ? `Attending ${academicLevel.name} by second inscription` : semesterFilter ? semesters.find((semester) => semester.id === semesterFilter)?.name : academicYearLabel}</small></div></td><td>{context === undefined ? <span className="cohort-context-loading">Checking module history...</span> : repeatedModules.length ? <div className="second-inscription"><strong>Second inscription</strong><span>{repeatedModules.map(({ module, semester }) => module.originAcademicLevelId ? <button className="second-inscription-link" key={module.id} onClick={() => { setPendingDebtSemesterId(semester.semesterId); onSelectAcademicLevel(module.originAcademicLevelId!); }} type="button">{module.subjectModuleCode} · {semester.semesterName}</button> : `${module.subjectModuleCode} · ${semester.semesterName}`)}</span></div> : <span className="cohort-standard-label">Standard registration</span>}</td><td><StatusBadge status={registration.status} /></td></tr>;
      })}</tbody></table></div>}

    {isRegistrationOpen && academicLevel && <ManagementModal size={isCreatingStudent ? "wide" : "default"} title={isCreatingStudent ? "Add Student" : "Register Existing Student"} description={`${academicLevel.name} · ${academicYearLabel ?? "Academic year"}`} onClose={closeRegistration}>
      {isCreatingStudent ? <StudentForm isSubmitting={createStudentMutation.isPending || registrationMutation.isPending} requestError={createStudentMutation.isError ? errorMessage(createStudentMutation.error) : registrationMutation.isError ? errorMessage(registrationMutation.error) : null} onUseExistingStudent={() => setCreatingStudent(false)} onSubmit={async (values) => { try { await createStudentMutation.mutateAsync(values); } catch { /* mutation state renders the error */ } }} /> : <div className="management-form">
        <div className="registration-context-summary"><span>Academic registration</span><strong>{academicLevel.name} · {academicYearLabel}</strong><small>Semester and module registrations will be created automatically.</small></div>
        <div className="form-field form-field--wide"><label htmlFor="registration-student">Existing Student</label><select id="registration-student" onChange={(event) => setSelectedStudentId(event.target.value)} value={selectedStudentId}><option value="">Select a Student</option>{availableStudents.map((student) => <option key={student.studentId} value={student.studentId}>{student.firstName} {student.lastName} · {student.apogeeCode}</option>)}</select></div>
        {!availableStudents.length && <div className="management-alert">Every active Student is already registered for {academicYearLabel}, or no Student account exists.</div>}
        {registrationMutation.isError && <div className="management-alert management-alert--error">{errorMessage(registrationMutation.error)}</div>}
        <button className="registration-create-student" onClick={() => { setCreatingStudent(true); registrationMutation.reset(); }} type="button"><strong>Create a new Student instead</strong><span>Create the identity and academic registration together.</span></button>
        <footer className="form-actions"><button className="secondary-button" onClick={closeRegistration} type="button">Cancel</button><button className="management-primary-button" disabled={!selectedStudentId || registrationMutation.isPending} onClick={() => registrationMutation.mutate(selectedStudentId)} type="button">{registrationMutation.isPending ? "Registering..." : "Register Student"}</button></footer>
      </div>}
    </ManagementModal>}
  </section>;
}
