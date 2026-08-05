import { NamedResourceDirectory } from "../components/NamedResourceDirectory";
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
  return <NamedResourceDirectory title="Departments" singular="Department" description="Organize the establishment into its principal academic departments." emptyDescription="Create the first department to begin structuring its programs." queryKey={academicStructureKeys.departments} load={getDepartments} create={createDepartment} update={updateDepartment} remove={deleteDepartment} />;
}

export function ProgramPathsPage() {
  return <NamedResourceDirectory title="Program Paths" singular="Program Path" description="Define the regular, excellence, and other study paths available in this establishment." emptyDescription="Create the first path before classifying programs." queryKey={academicStructureKeys.programPaths} load={getProgramPaths} create={createProgramPath} update={updateProgramPath} remove={deleteProgramPath} />;
}

export function DegreeCyclesPage() {
  return <NamedResourceDirectory title="Degree Cycles" singular="Degree Cycle" description="Maintain the degree cycles used to frame programs, such as Licence, Master, and Engineering." emptyDescription="Create the first degree cycle before defining programs." queryKey={academicStructureKeys.degreeCycles} load={getDegreeCycles} create={createDegreeCycle} update={updateDegreeCycle} remove={deleteDegreeCycle} />;
}
