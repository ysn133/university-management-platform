package com.platform.ai.retrieval.presentation;

import com.platform.ai.retrieval.application.KnowledgeRetrievalService;
import com.platform.ai.retrieval.domain.KnowledgeMatch;
import com.platform.ai.retrieval.presentation.dto.KnowledgeMatchResponse;
import com.platform.ai.retrieval.presentation.dto.KnowledgeRetrievalResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/ai/retrieval")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class KnowledgeRetrievalController {

    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeRetrievalController(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @GetMapping
    public KnowledgeRetrievalResponse retrieve(
        @RequestParam @NotBlank @Size(max = 500) String query,
        @RequestParam(defaultValue = "5") @Min(1) @Max(10) int limit
    ) {
        KnowledgeRetrievalService.RetrievalResult result = retrievalService.retrieve(
            query,
            limit
        );
        return new KnowledgeRetrievalResponse(
            result.query(),
            result.matches().size(),
            result.apiMatches().stream().map(this::toResponse).toList(),
            result.uiMatches().stream().map(this::toResponse).toList(),
            result.context()
        );
    }

    private KnowledgeMatchResponse toResponse(KnowledgeMatch match) {
        return new KnowledgeMatchResponse(
            match.chunk().id(),
            match.chunk().source(),
            match.chunk().title(),
            match.chunk().content(),
            BigDecimal.valueOf(match.score()).setScale(4, RoundingMode.HALF_UP).doubleValue()
        );
    }
}
