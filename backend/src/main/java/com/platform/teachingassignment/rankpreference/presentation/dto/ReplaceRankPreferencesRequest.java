package com.platform.teachingassignment.rankpreference.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ReplaceRankPreferencesRequest(@NotNull List<UUID> academicRankIds) {}
