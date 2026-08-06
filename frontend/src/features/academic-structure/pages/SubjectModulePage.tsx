import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useEstablishmentScope } from "@/features/establishment-management/context/useEstablishmentScope";
import {
  academicStructureKeys,
  getAcademicDomains,
  getAcademicLevels,
  getAcademicYears,
  getModuleTeachingComponents,
  getProgramFiliere,
  getSemesters,
  getSubjectModule,
  replaceModuleTeachingComponents,
  type ModuleTeachingComponent,
} from "../api/academic-structure-api";

type ComponentType = "COURSE" | "TD" | "TP";
type AudienceMode = "WHOLE_COHORT" | "CLASS_GROUP" | "SUBGROUP";
type RoomType = "LECTURE_HALL" | "CLASSROOM" | "COMPUTER_LAB";

interface TeachingComponentForm {
  enabled: boolean;
  componentType: ComponentType;
  sessionsPerWeek: string;
  sessionDurationMinutes: string;
  audienceMode: AudienceMode;
  requiredRoomType: RoomType;
}

const componentOrder: ComponentType[] = ["COURSE", "TD", "TP"];
const componentLabels: Record<ComponentType, { name: string; description: string }> = {
  COURSE: { name: "Course", description: "Theory delivery for the full cohort or a class group." },
  TD: { name: "TD", description: "Guided exercises and problem-solving sessions." },
  TP: { name: "TP", description: "Practical work delivered in smaller groups." },
};
const audienceLabels: Record<AudienceMode, string> = {
  WHOLE_COHORT: "Whole cohort",
  CLASS_GROUP: "Class group",
  SUBGROUP: "Subgroup",
};
const roomLabels: Record<RoomType, string> = {
  LECTURE_HALL: "Lecture hall",
  CLASSROOM: "Classroom",
  COMPUTER_LAB: "Computer lab",
};

function defaultComponent(componentType: ComponentType): TeachingComponentForm {
  if (componentType === "COURSE") {
    return { enabled: false, componentType, sessionsPerWeek: "1", sessionDurationMinutes: "120", audienceMode: "WHOLE_COHORT", requiredRoomType: "LECTURE_HALL" };
  }
  if (componentType === "TD") {
    return { enabled: false, componentType, sessionsPerWeek: "1", sessionDurationMinutes: "120", audienceMode: "CLASS_GROUP", requiredRoomType: "CLASSROOM" };
  }
  return { enabled: false, componentType, sessionsPerWeek: "1", sessionDurationMinutes: "120", audienceMode: "SUBGROUP", requiredRoomType: "COMPUTER_LAB" };
}

function componentForm(componentType: ComponentType, components: ModuleTeachingComponent[]): TeachingComponentForm {
  const component = components.find((item) => item.componentType === componentType);
  if (!component) return defaultComponent(componentType);
  return {
    enabled: true,
    componentType,
    sessionsPerWeek: String(component.sessionsPerWeek),
    sessionDurationMinutes: String(component.sessionDurationMinutes),
    audienceMode: component.audienceMode,
    requiredRoomType: component.requiredRoomType,
  };
}

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function SubjectModulePage() {
  const { programFiliereId, subjectModuleId } = useParams();
  const [searchParams] = useSearchParams();
  const { establishmentId, workspacePath } = useEstablishmentScope();
  const queryClient = useQueryClient();
  const academicYearId = searchParams.get("academicYearId") ?? "";
  const academicLevelId = searchParams.get("academicLevelId") ?? "";
  const [components, setComponents] = useState<TeachingComponentForm[]>(() => componentOrder.map(defaultComponent));
  const [formError, setFormError] = useState<string | null>(null);

  const moduleQuery = useQuery({ queryKey: academicStructureKeys.subjectModule(subjectModuleId ?? "missing"), queryFn: () => getSubjectModule(subjectModuleId!), enabled: Boolean(subjectModuleId) });
  const componentsQuery = useQuery({ queryKey: academicStructureKeys.moduleTeachingComponents(subjectModuleId ?? "missing"), queryFn: () => getModuleTeachingComponents(subjectModuleId!), enabled: Boolean(subjectModuleId) });
  const programQuery = useQuery({ queryKey: academicStructureKeys.programFiliere(programFiliereId ?? "missing"), queryFn: () => getProgramFiliere(programFiliereId!), enabled: Boolean(programFiliereId) });
  const levelsQuery = useQuery({ queryKey: academicStructureKeys.academicLevels(programFiliereId ?? "missing"), queryFn: () => getAcademicLevels(programFiliereId!), enabled: Boolean(programFiliereId) });
  const yearsQuery = useQuery({ queryKey: academicStructureKeys.academicYears(establishmentId ?? "missing"), queryFn: () => getAcademicYears(establishmentId!), enabled: Boolean(establishmentId) });
  const semestersQuery = useQuery({ queryKey: academicStructureKeys.semesters(academicLevelId || "missing", academicYearId || "missing"), queryFn: () => getSemesters(academicLevelId, academicYearId), enabled: Boolean(academicLevelId && academicYearId) });
  const domainsQuery = useQuery({ queryKey: academicStructureKeys.academicDomains(establishmentId ?? "missing"), queryFn: () => getAcademicDomains(establishmentId!), enabled: Boolean(establishmentId) });

  useEffect(() => {
    if (componentsQuery.data) setComponents(componentOrder.map((type) => componentForm(type, componentsQuery.data)));
  }, [componentsQuery.data]);

  const saveMutation = useMutation({
    mutationFn: () => replaceModuleTeachingComponents(subjectModuleId!, {
      components: components.filter((component) => component.enabled).map((component) => ({
        componentType: component.componentType,
        sessionsPerWeek: Number(component.sessionsPerWeek),
        sessionDurationMinutes: Number(component.sessionDurationMinutes),
        audienceMode: component.audienceMode,
        requiredRoomType: component.requiredRoomType,
      })),
    }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: academicStructureKeys.moduleTeachingComponents(subjectModuleId!) });
      setFormError(null);
    },
  });

  function updateComponent(componentType: ComponentType, values: Partial<TeachingComponentForm>) {
    setComponents((current) => current.map((component) => component.componentType === componentType ? { ...component, ...values } : component));
    setFormError(null);
  }

  function submitComponents() {
    const enabledComponents = components.filter((component) => component.enabled);
    const invalidDuration = enabledComponents.some((component) => Number(component.sessionsPerWeek) <= 0 || Number(component.sessionDurationMinutes) <= 0);
    if (invalidDuration) {
      setFormError("Sessions per week and session duration must be greater than zero.");
      return;
    }
    saveMutation.mutate();
  }

  if (moduleQuery.isPending || programQuery.isPending) return <div className="management-state"><p>Loading module workspace...</p></div>;
  if (moduleQuery.isError || programQuery.isError || !moduleQuery.data || !programQuery.data) {
    return <div className="management-state"><h1>Module unavailable</h1><p>{errorMessage(moduleQuery.error ?? programQuery.error)}</p></div>;
  }

  const module = moduleQuery.data;
  const program = programQuery.data;
  const selectedLevel = levelsQuery.data?.find((level) => level.id === academicLevelId);
  const selectedYear = yearsQuery.data?.find((year) => year.id === academicYearId);
  const selectedSemester = semestersQuery.data?.find((semester) => semester.id === module.semesterId);
  const moduleDomains = module.academicDomainIds.map((id) => domainsQuery.data?.find((domain) => domain.id === id)?.name).filter(Boolean);
  const backParams = new URLSearchParams();
  if (academicYearId) backParams.set("academicYearId", academicYearId);
  if (academicLevelId) backParams.set("academicLevelId", academicLevelId);
  if (module.semesterId) backParams.set("semesterId", module.semesterId);
  const backPath = `${workspacePath}/programs/${programFiliereId}${backParams.size ? `?${backParams}` : ""}`;

  return <div className="management-page module-workspace-page">
    <Link className="context-back-link curriculum-back-link" to={backPath}>← Back to {program.name}</Link>

    <header className="module-workspace-header">
      <span className="module-workspace-code">{module.code}</span>
      <div>
        <p className="management-kicker">Subject module</p>
        <h1>{module.title}</h1>
        <p>{[program.name, selectedLevel?.name, selectedSemester?.name, selectedYear?.label].filter(Boolean).join(" · ")}</p>
      </div>
      <div className="module-workspace-domains">
        <span>Academic domains</span>
        <strong>{moduleDomains.length ? moduleDomains.join(", ") : "Not assigned"}</strong>
      </div>
    </header>

    <section className="management-panel teaching-components-panel">
      <header className="panel-header panel-header--bordered">
        <div>
          <p className="management-kicker">Teaching configuration</p>
          <h2>Delivery components</h2>
          <p>Define how this module is delivered during the selected semester.</p>
        </div>
        <span className="teaching-component-count">{components.filter((component) => component.enabled).length} configured</span>
      </header>

      {componentsQuery.isPending ? <div className="panel-empty">Loading teaching components...</div> : componentsQuery.isError ? <div className="panel-empty panel-empty--error">{errorMessage(componentsQuery.error)}</div> : <div className="teaching-component-form">
        <div className="teaching-component-grid">
          {components.map((component) => {
            const label = componentLabels[component.componentType];
            return <article className={component.enabled ? "is-enabled" : ""} key={component.componentType}>
              <header>
                <div>
                  <span>{label.name}</span>
                  <p>{label.description}</p>
                </div>
                <label className="component-enable-control">
                  <input checked={component.enabled} onChange={(event) => updateComponent(component.componentType, { enabled: event.target.checked })} type="checkbox" />
                  <span>{component.enabled ? "Included" : "Not included"}</span>
                </label>
              </header>

              <fieldset disabled={!component.enabled}>
                <label><span>Sessions per week</span><input min="1" onChange={(event) => updateComponent(component.componentType, { sessionsPerWeek: event.target.value })} type="number" value={component.sessionsPerWeek} /></label>
                <label><span>Duration</span><div className="component-input-unit"><input min="1" onChange={(event) => updateComponent(component.componentType, { sessionDurationMinutes: event.target.value })} type="number" value={component.sessionDurationMinutes} /><small>minutes</small></div></label>
                <label><span>Audience</span><select onChange={(event) => updateComponent(component.componentType, { audienceMode: event.target.value as AudienceMode })} value={component.audienceMode}>{Object.entries(audienceLabels).map(([value, text]) => <option key={value} value={value}>{text}</option>)}</select></label>
                <label><span>Room type</span><select onChange={(event) => updateComponent(component.componentType, { requiredRoomType: event.target.value as RoomType })} value={component.requiredRoomType}>{Object.entries(roomLabels).map(([value, text]) => <option key={value} value={value}>{text}</option>)}</select></label>
              </fieldset>
            </article>;
          })}
        </div>

        {(formError || saveMutation.isError) && <div className="management-alert management-alert--error">{formError ?? errorMessage(saveMutation.error)}</div>}
        {saveMutation.isSuccess && !formError && <div className="management-alert management-alert--success">Teaching components saved.</div>}
        <footer className="teaching-component-actions">
          <p>Saving replaces the current Course, TD, and TP configuration for this module.</p>
          <button className="management-primary-button" disabled={saveMutation.isPending} onClick={submitComponents} type="button">{saveMutation.isPending ? "Saving..." : "Save configuration"}</button>
        </footer>
      </div>}
    </section>
  </div>;
}
