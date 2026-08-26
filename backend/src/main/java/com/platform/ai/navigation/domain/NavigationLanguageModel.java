package com.platform.ai.navigation.domain;

public interface NavigationLanguageModel {

    NavigationModelResult createPlan(NavigationContext context);

    NavigationModelResult repairPlan(
        NavigationContext context,
        NavigationPlan failedPlan,
        NavigationPlanExecutionException failure
    );

    NavigationAnswerResult createAnswer(String question, String verifiedData);
}
