import { useState } from "react";
import type {
  AcademicDecisionRule,
  AcademicMetric,
  AcademicRuleOutcome,
  AcademicRuleSet,
  ComparisonOperator,
  ProfileVariable,
  RuleExpression,
} from "../api/academic-structure-api";

type RuleCategory = "moduleRules" | "semesterRules" | "academicLevelRules" | "progressionRules";
type SemesterRuleScope = "SHARED" | "AUTUMN" | "SPRING";

const categoryConfig: Record<RuleCategory, {
  label: string;
  description: string;
  metrics: AcademicMetric[];
  visibleOutcomes: AcademicRuleOutcome[];
  fallback: string;
}> = {
  moduleRules: { label: "Module", description: "Module validation", metrics: ["MODULE_FINAL_GRADE", "MODULE_INSCRIPTION_NUMBER"], visibleOutcomes: ["MODULE_VALIDATED"], fallback: "Otherwise, the module is not validated." },
  semesterRules: { label: "Semester", description: "Semester validation and compensation", metrics: ["SEMESTER_AVERAGE", "INDIVIDUALLY_VALIDATED_MODULE_COUNT", "NON_VALIDATED_MODULE_COUNT", "MINIMUM_NON_VALIDATED_MODULE_GRADE"], visibleOutcomes: ["SEMESTER_VALIDATED", "SEMESTER_VALIDATED_BY_COMPENSATION"], fallback: "Otherwise, the semester is not validated." },
  academicLevelRules: { label: "Academic level", description: "Compensation between semesters", metrics: ["ANNUAL_AVERAGE", "INDIVIDUALLY_VALIDATED_MODULE_COUNT", "MINIMUM_NON_VALIDATED_MODULE_GRADE", "NON_VALIDATED_SEMESTER_COUNT"], visibleOutcomes: ["ACADEMIC_LEVEL_VALIDATED", "ACADEMIC_LEVEL_VALIDATED_BY_COMPENSATION"], fallback: "Otherwise, the academic level is not validated." },
  progressionRules: { label: "Progression", description: "Promotion, debt, repetition, or failure", metrics: ["ACADEMIC_LEVEL_VALIDATED", "OUTSTANDING_MODULE_COUNT", "EXHAUSTED_MODULE_INSCRIPTION_COUNT", "NON_VALIDATED_SEMESTER_COUNT", "ANNUAL_AVERAGE"], visibleOutcomes: ["PROMOTED", "PROMOTED_WITH_DEBT", "FAILED"], fallback: "If no progression condition matches, the student repeats the academic level." },
};

const metricLabels: Record<AcademicMetric, string> = {
  MODULE_FINAL_GRADE: "Final module grade",
  MODULE_INSCRIPTION_NUMBER: "Module inscription number",
  SEMESTER_AVERAGE: "Semester average",
  INDIVIDUALLY_VALIDATED_MODULE_COUNT: "Individually validated modules",
  NON_VALIDATED_MODULE_COUNT: "Non-validated modules",
  MINIMUM_NON_VALIDATED_MODULE_GRADE: "Lowest non-validated grade",
  ANNUAL_AVERAGE: "Academic-level average",
  NON_VALIDATED_SEMESTER_COUNT: "Non-validated semesters",
  ACADEMIC_LEVEL_VALIDATED: "Academic level validated",
  OUTSTANDING_MODULE_COUNT: "Outstanding modules",
  EXHAUSTED_MODULE_INSCRIPTION_COUNT: "Modules at inscription limit",
};

const operatorLabels: Record<ComparisonOperator, string> = {
  GREATER_THAN: ">",
  GREATER_THAN_OR_EQUAL: "≥",
  LESS_THAN: "<",
  LESS_THAN_OR_EQUAL: "≤",
  EQUAL: "=",
  NOT_EQUAL: "≠",
};

const variableLabels: Record<ProfileVariable, string> = {
  MODULE_VALIDATION_THRESHOLD: "Module validation threshold",
  COMPENSATION_MINIMUM_THRESHOLD: "Compensation minimum",
  SEMESTER_VALIDATION_AVERAGE: "Semester validation average",
  ANNUAL_VALIDATION_AVERAGE: "Academic-level validation average",
  MINIMUM_INDIVIDUALLY_VALIDATED_MODULES_PER_SEMESTER: "Minimum validated modules per semester",
  MAXIMUM_NON_VALIDATED_MODULES_PER_SEMESTER: "Maximum non-validated modules per semester",
  MINIMUM_INDIVIDUALLY_VALIDATED_MODULES_PER_ACADEMIC_LEVEL: "Minimum validated modules per academic level",
  MAXIMUM_MODULE_INSCRIPTIONS: "Maximum module inscriptions",
  MAXIMUM_CARRIED_MODULES: "Maximum carried modules",
};

const outcomeLabels: Record<AcademicRuleOutcome, string> = {
  MODULE_VALIDATED: "Module validated",
  MODULE_NON_VALIDATED: "Module not validated",
  SEMESTER_VALIDATED: "Semester validated",
  SEMESTER_VALIDATED_BY_COMPENSATION: "Semester validated by compensation",
  SEMESTER_NON_VALIDATED: "Semester not validated",
  ACADEMIC_LEVEL_VALIDATED: "Academic level validated",
  ACADEMIC_LEVEL_VALIDATED_BY_COMPENSATION: "Academic level validated by compensation",
  ACADEMIC_LEVEL_NON_VALIDATED: "Academic level not validated",
  PROMOTED: "Promoted",
  PROMOTED_WITH_DEBT: "Promoted with carried modules",
  REPEAT: "Repeat academic level",
  FAILED: "Failed",
};

const profileVariables = Object.keys(variableLabels) as ProfileVariable[];
const comparisonOperators = Object.keys(operatorLabels) as ComparisonOperator[];

function defaultComparison(metric: AcademicMetric): RuleExpression {
  return { type: "COMPARISON", left: metric, operator: "GREATER_THAN_OR_EQUAL", rightProfileVariable: "MODULE_VALIDATION_THRESHOLD" };
}

function changeConnector(
  expression: Extract<RuleExpression, { type: "LOGICAL" }>,
  connectorIndex: number,
  operator: "AND" | "OR",
): RuleExpression {
  if (operator === expression.operator) return expression;
  if (expression.children.length === 2) return { ...expression, operator };

  const group = (children: RuleExpression[]): RuleExpression => children.length === 1
    ? children[0]
    : { type: "LOGICAL", operator: expression.operator, children };

  return {
    type: "LOGICAL",
    operator,
    children: [
      group(expression.children.slice(0, connectorIndex)),
      group(expression.children.slice(connectorIndex)),
    ],
  };
}

function ConditionEditor({ expression, metrics, onChange, onRemove }: {
  expression: Extract<RuleExpression, { type: "COMPARISON" }>;
  metrics: AcademicMetric[];
  onChange: (value: RuleExpression) => void;
  onRemove?: () => void;
}) {
  const usesLiteral = expression.rightProfileVariable == null;
  return <div className="academic-rule-condition">
    <select aria-label="Academic value" onChange={(event) => onChange({ ...expression, left: event.target.value as AcademicMetric })} value={expression.left}>{metrics.map((metric) => <option key={metric} value={metric}>{metricLabels[metric]}</option>)}</select>
    <select aria-label="Operator" className="academic-rule-condition__operator" onChange={(event) => onChange({ ...expression, operator: event.target.value as ComparisonOperator })} value={expression.operator}>{comparisonOperators.map((operator) => <option key={operator} value={operator}>{operatorLabels[operator]}</option>)}</select>
    <select aria-label="Value type" onChange={(event) => onChange(event.target.value === "LITERAL" ? { ...expression, rightProfileVariable: null, literalValue: 0 } : { ...expression, rightProfileVariable: profileVariables[0], literalValue: null })} value={usesLiteral ? "LITERAL" : "PROFILE"}><option value="PROFILE">Profile value</option><option value="LITERAL">Fixed value</option></select>
    {usesLiteral ? <input aria-label="Fixed value" onChange={(event) => onChange({ ...expression, literalValue: Number(event.target.value) })} step="0.01" type="number" value={expression.literalValue ?? 0} /> : <select aria-label="Profile value" onChange={(event) => onChange({ ...expression, rightProfileVariable: event.target.value as ProfileVariable })} value={expression.rightProfileVariable ?? profileVariables[0]}>{profileVariables.map((variable) => <option key={variable} value={variable}>{variableLabels[variable]}</option>)}</select>}
    {onRemove && <button className="academic-rule-remove" onClick={onRemove} type="button">Remove</button>}
  </div>;
}

function ExpressionEditor({ expression, metrics, onChange, nested = false, onRemove }: {
  expression: RuleExpression;
  metrics: AcademicMetric[];
  onChange: (value: RuleExpression) => void;
  nested?: boolean;
  onRemove?: () => void;
}) {
  if (expression.type === "COMPARISON") {
    return <div className="academic-rule-expression"><ConditionEditor expression={expression} metrics={metrics} onChange={onChange} /><button className="academic-rule-add-condition" onClick={() => onChange({ type: "LOGICAL", operator: "AND", children: [expression, defaultComparison(metrics[0])] })} type="button">Add condition</button></div>;
  }

  return <div className={nested ? "academic-rule-expression-group academic-rule-expression-group--nested" : "academic-rule-expression-group"}>
    {nested && <header className="academic-rule-expression-group__header"><span>Condition group</span>{onRemove && <button onClick={onRemove} type="button">Remove group</button>}</header>}
    {expression.children.map((child, index) => <div className="academic-rule-expression-row" key={`${child.type}-${index}`}>
      {index > 0 && <select aria-label="Condition relationship" className="academic-rule-connector" onChange={(event) => onChange(changeConnector(expression, index, event.target.value as "AND" | "OR"))} value={expression.operator}><option value="AND">AND</option><option value="OR">OR</option></select>}
      {child.type === "COMPARISON" ? <ConditionEditor expression={child} metrics={metrics} onChange={(updated) => onChange({ ...expression, children: expression.children.map((item, itemIndex) => itemIndex === index ? updated : item) })} onRemove={expression.children.length > 1 ? () => onChange({ ...expression, children: expression.children.filter((_, itemIndex) => itemIndex !== index) }) : undefined} /> : <ExpressionEditor expression={child} metrics={metrics} nested onChange={(updated) => onChange({ ...expression, children: expression.children.map((item, itemIndex) => itemIndex === index ? updated : item) })} onRemove={() => onChange({ ...expression, children: expression.children.filter((_, itemIndex) => itemIndex !== index) })} />}
    </div>)}
    <div className="academic-rule-expression-actions"><button onClick={() => onChange({ ...expression, children: [...expression.children, defaultComparison(metrics[0])] })} type="button">Add condition</button><button onClick={() => onChange({ ...expression, children: [...expression.children, { type: "LOGICAL", operator: "AND", children: [defaultComparison(metrics[0]), defaultComparison(metrics[0])] }] })} type="button">Add condition group</button></div>
  </div>;
}

function DecisionCard({ rule, metrics, onChange }: {
  rule: AcademicDecisionRule;
  metrics: AcademicMetric[];
  onChange: (rule: AcademicDecisionRule) => void;
}) {
  return <article className="academic-rule-card academic-rule-card--simple">
    <header><strong>{outcomeLabels[rule.outcome]}</strong><span>when</span></header>
    <ExpressionEditor expression={rule.expression} metrics={metrics} onChange={(expression) => onChange({ ...rule, expression })} />
  </article>;
}

export function AcademicRuleBuilder({ value, onChange }: { value: AcademicRuleSet; onChange: (value: AcademicRuleSet) => void }) {
  const [category, setCategory] = useState<RuleCategory>("moduleRules");
  const [semesterScope, setSemesterScope] = useState<SemesterRuleScope>("SHARED");
  const [decisionIndex, setDecisionIndex] = useState(0);
  const config = categoryConfig[category];
  const semesterRuleKey = value.useSharedSemesterRules || semesterScope === "SHARED"
    ? "semesterRules"
    : semesterScope === "AUTUMN" ? "autumnSemesterRules" : "springSemesterRules";
  const activeRuleKey: RuleCategory | "autumnSemesterRules" | "springSemesterRules" = category === "semesterRules"
    ? semesterRuleKey
    : category;
  const rules = config.visibleOutcomes
    .map((outcome) => value[activeRuleKey].find((rule) => rule.enabled && rule.outcome === outcome))
    .filter((rule): rule is AcademicDecisionRule => rule != null);
  const selectedRule = rules[Math.min(decisionIndex, rules.length - 1)];

  function selectCategory(nextCategory: RuleCategory) {
    setCategory(nextCategory);
    if (nextCategory === "semesterRules") {
      setSemesterScope(value.useSharedSemesterRules ? "SHARED" : "AUTUMN");
    }
    setDecisionIndex(0);
  }

  function setSharedSemesterRules(shared: boolean) {
    setSemesterScope(shared ? "SHARED" : "AUTUMN");
    setDecisionIndex(0);
    onChange({
      ...value,
      useSharedSemesterRules: shared,
    });
  }

  return <div className="academic-rule-builder academic-rule-builder--simple">
    <aside className="academic-rule-builder__navigation"><div><span>Decision rules</span><p>Choose an academic result to configure.</p></div><nav aria-label="Decision rule areas" className="academic-rule-category-tabs" role="tablist">{(Object.keys(categoryConfig) as RuleCategory[]).map((item) => <button aria-selected={category === item} key={item} onClick={() => selectCategory(item)} role="tab" type="button"><strong>{categoryConfig[item].label}</strong><span>{categoryConfig[item].description}</span></button>)}</nav></aside>
    <section className="academic-rule-category academic-rule-category--simple"><header><div><span className="management-kicker">{config.label}</span><strong>{config.description}</strong><p>Define the condition that produces each result.</p></div></header>
      {category === "semesterRules" && <div className="semester-rule-mode">
        <label><input checked={value.useSharedSemesterRules} onChange={(event) => setSharedSemesterRules(event.target.checked)} type="checkbox" /><span><strong>Use the same rules for both semesters</strong><small>Disable this to configure Autumn and Spring separately.</small></span></label>
        {!value.useSharedSemesterRules && <nav aria-label="Semester rule period" role="tablist"><button aria-selected={semesterScope === "AUTUMN"} onClick={() => { setSemesterScope("AUTUMN"); setDecisionIndex(0); }} role="tab" type="button">Autumn</button><button aria-selected={semesterScope === "SPRING"} onClick={() => { setSemesterScope("SPRING"); setDecisionIndex(0); }} role="tab" type="button">Spring</button></nav>}
      </div>}
      {rules.length > 1 && <nav aria-label={`${config.label} results`} className="academic-rule-outcome-tabs" role="tablist">{rules.map((rule, index) => <button aria-selected={decisionIndex === index} key={`${rule.outcome}-${index}`} onClick={() => setDecisionIndex(index)} role="tab" type="button">{outcomeLabels[rule.outcome]}</button>)}</nav>}{selectedRule && <DecisionCard metrics={config.metrics} onChange={(updated) => onChange({ ...value, [activeRuleKey]: value[activeRuleKey].map((item) => item === selectedRule ? updated : item) })} rule={selectedRule} />}<p className="academic-rule-fallback"><strong>Fallback</strong>{config.fallback}</p></section>
  </div>;
}
