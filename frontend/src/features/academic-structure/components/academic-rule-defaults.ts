import type { AcademicRuleSet, RuleExpression } from "../api/academic-structure-api";

const comparison = (left: Extract<RuleExpression, { type: "COMPARISON" }>["left"], operator: Extract<RuleExpression, { type: "COMPARISON" }>["operator"], rightProfileVariable: Extract<RuleExpression, { type: "COMPARISON" }>["rightProfileVariable"]): RuleExpression => ({ type: "COMPARISON", left, operator, rightProfileVariable });
const literal = (left: Extract<RuleExpression, { type: "COMPARISON" }>["left"], operator: Extract<RuleExpression, { type: "COMPARISON" }>["operator"], literalValue: number): RuleExpression => ({ type: "COMPARISON", left, operator, literalValue });
export function defaultAcademicRuleSet(): AcademicRuleSet {
  const semesterRules: AcademicRuleSet["semesterRules"] = [
    { name: "All modules validated", priority: 10, outcome: "SEMESTER_VALIDATED", enabled: true, expression: literal("NON_VALIDATED_MODULE_COUNT", "EQUAL", 0) },
    { name: "Semester compensation", priority: 20, outcome: "SEMESTER_VALIDATED_BY_COMPENSATION", enabled: true, expression: comparison("SEMESTER_AVERAGE", "GREATER_THAN_OR_EQUAL", "SEMESTER_VALIDATION_AVERAGE") },
    { name: "Semester not validated", priority: 100, outcome: "SEMESTER_NON_VALIDATED", enabled: true, expression: literal("NON_VALIDATED_MODULE_COUNT", "GREATER_THAN", 0) },
  ];
  return {
    moduleRules: [
      { name: "Module validated", priority: 10, outcome: "MODULE_VALIDATED", enabled: true, expression: comparison("MODULE_FINAL_GRADE", "GREATER_THAN_OR_EQUAL", "MODULE_VALIDATION_THRESHOLD") },
      { name: "Module not validated", priority: 100, outcome: "MODULE_NON_VALIDATED", enabled: true, expression: comparison("MODULE_FINAL_GRADE", "LESS_THAN", "MODULE_VALIDATION_THRESHOLD") },
    ],
    semesterRules,
    academicLevelRules: [
      { name: "Both semesters validated", priority: 10, outcome: "ACADEMIC_LEVEL_VALIDATED", enabled: true, expression: literal("NON_VALIDATED_SEMESTER_COUNT", "EQUAL", 0) },
      { name: "Inter-semester compensation", priority: 20, outcome: "ACADEMIC_LEVEL_VALIDATED_BY_COMPENSATION", enabled: true, expression: comparison("ANNUAL_AVERAGE", "GREATER_THAN_OR_EQUAL", "ANNUAL_VALIDATION_AVERAGE") },
      { name: "Academic level not validated", priority: 100, outcome: "ACADEMIC_LEVEL_NON_VALIDATED", enabled: true, expression: literal("NON_VALIDATED_SEMESTER_COUNT", "GREATER_THAN", 0) },
    ],
    progressionRules: [
      { name: "Module inscription limit reached", priority: 10, outcome: "FAILED", enabled: true, expression: literal("EXHAUSTED_MODULE_INSCRIPTION_COUNT", "GREATER_THAN", 0) },
      { name: "Academic level validated", priority: 20, outcome: "PROMOTED", enabled: true, expression: literal("ACADEMIC_LEVEL_VALIDATED", "EQUAL", 1) },
      { name: "Progression with debt", priority: 40, outcome: "PROMOTED_WITH_DEBT", enabled: true, expression: comparison("OUTSTANDING_MODULE_COUNT", "LESS_THAN_OR_EQUAL", "MAXIMUM_CARRIED_MODULES") },
      { name: "Repeat level", priority: 100, outcome: "REPEAT", enabled: true, expression: literal("NON_VALIDATED_SEMESTER_COUNT", "GREATER_THAN", 0) },
    ],
    useSharedSemesterRules: true,
    autumnSemesterRules: semesterRules,
    springSemesterRules: semesterRules,
  };
}
