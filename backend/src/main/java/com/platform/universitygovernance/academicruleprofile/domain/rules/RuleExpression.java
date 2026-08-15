package com.platform.universitygovernance.academicruleprofile.domain.rules;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ComparisonExpression.class, name = "COMPARISON"),
    @JsonSubTypes.Type(value = LogicalExpression.class, name = "LOGICAL")
})
public sealed interface RuleExpression permits ComparisonExpression, LogicalExpression {
}
