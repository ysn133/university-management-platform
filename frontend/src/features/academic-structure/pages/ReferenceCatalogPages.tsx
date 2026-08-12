import { NamedResourceDirectory } from "../components/NamedResourceDirectory";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import {
  academicStructureKeys,
  createDegreeCycle,
  createDepartment,
  createProgramPath,
  deleteDegreeCycle,
  deleteDepartment,
  deleteProgramPath,
  getDegreeCycles,
  getDepartments,
  getProgramPaths,
  updateDegreeCycle,
  updateDepartment,
  updateProgramPath,
} from "../api/academic-structure-api";

export function DepartmentsPage() {
  const { workspacePath } = useEstablishmentScope();
  return <NamedResourceDirectory title="Departments" singular="Department" description="Organize the establishment into its principal academic departments." emptyDescription="Create the first department to begin structuring its programs." queryKey={academicStructureKeys.departments} load={getDepartments} create={createDepartment} update={updateDepartment} remove={deleteDepartment} resourcePath={workspacePath ? (department) => `${workspacePath}/departments/${department.id}/programs` : undefined} />;
}

export function ProgramPathsPage() {
  const { workspacePath } = useEstablishmentScope();
  return <NamedResourceDirectory title="Program Paths" singular="Program Path" description="Define the regular, excellence, and other study paths available in this establishment." emptyDescription="Create the first path before classifying programs." queryKey={academicStructureKeys.programPaths} load={getProgramPaths} create={createProgramPath} update={updateProgramPath} remove={deleteProgramPath} resourcePath={workspacePath ? (path) => `${workspacePath}/program-paths/${path.id}/programs` : undefined} />;
}

export function DegreeCyclesPage() {
  const { workspacePath } = useEstablishmentScope();
  return <NamedResourceDirectory title="Degree Cycles" singular="Degree Cycle" description="Maintain the degree cycles used to frame programs, such as Licence, Master, and Engineering." emptyDescription="Create the first degree cycle before defining programs." queryKey={academicStructureKeys.degreeCycles} load={getDegreeCycles} create={createDegreeCycle} update={updateDegreeCycle} remove={deleteDegreeCycle} resourcePath={workspacePath ? (cycle) => `${workspacePath}/degree-cycles/${cycle.id}/programs` : undefined} />;
}
