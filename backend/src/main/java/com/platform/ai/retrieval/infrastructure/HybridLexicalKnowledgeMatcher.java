package com.platform.ai.retrieval.infrastructure;

import com.platform.ai.retrieval.domain.KnowledgeChunk;
import com.platform.ai.retrieval.domain.KnowledgeCorpus;
import com.platform.ai.retrieval.domain.KnowledgeMatch;
import com.platform.ai.retrieval.domain.KnowledgeMatcher;
import com.platform.ai.retrieval.domain.KnowledgeSource;
import com.platform.ai.retrieval.domain.SemanticKnowledgeMatcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HybridLexicalKnowledgeMatcher implements KnowledgeMatcher {

    private static final double BM25_SATURATION = 1.2;
    private static final Logger log = LoggerFactory.getLogger(HybridLexicalKnowledgeMatcher.class);

    private final RetrievalTextAnalyzer analyzer;
    private final List<IndexedChunk> index;
    private final Map<String, Integer> documentFrequency;
    private final Optional<SemanticKnowledgeMatcher> semanticMatcher;
    private final double lexicalWeight;
    private final double semanticWeight;

    public HybridLexicalKnowledgeMatcher(
        KnowledgeCorpus corpus,
        RetrievalTextAnalyzer analyzer
    ) {
        this(corpus, analyzer, Optional.empty(), 1.0, 0.0);
    }

    @Autowired
    public HybridLexicalKnowledgeMatcher(
        KnowledgeCorpus corpus,
        RetrievalTextAnalyzer analyzer,
        Optional<SemanticKnowledgeMatcher> semanticMatcher,
        @Value("${app.ai.retrieval.lexical-weight:0.35}") double lexicalWeight,
        @Value("${app.ai.retrieval.semantic-weight:0.65}") double semanticWeight
    ) {
        this.analyzer = analyzer;
        this.index = corpus.chunks().stream().map(this::index).toList();
        this.documentFrequency = documentFrequency(index);
        this.semanticMatcher = semanticMatcher;
        this.lexicalWeight = lexicalWeight;
        this.semanticWeight = semanticWeight;
    }

    @Override
    public List<KnowledgeMatch> match(String query, KnowledgeSource source, int limit) {
        List<KnowledgeMatch> lexicalMatches = lexicalMatches(query, source, Math.max(limit * 2, limit));
        if (semanticMatcher.isEmpty()) {
            return lexicalMatches.stream().limit(limit).toList();
        }

        try {
            List<KnowledgeMatch> semanticMatches = semanticMatcher.get().match(
                query,
                source,
                Math.max(limit * 2, limit)
            );
            return combine(lexicalMatches, semanticMatches, limit);
        } catch (RuntimeException exception) {
            log.warn("Semantic retrieval failed; using lexical matches", exception);
            return lexicalMatches.stream().limit(limit).toList();
        }
    }

    private List<KnowledgeMatch> lexicalMatches(String query, KnowledgeSource source, int limit) {
        List<String> originalTerms = analyzer.tokens(query);
        Map<String, Double> queryTerms = analyzer.expandedQueryTerms(query);
        String normalizedQuery = analyzer.normalize(query);

        return index.stream()
            .filter(indexed -> source == null || indexed.chunk().source() == source)
            .map(indexed -> new KnowledgeMatch(
                indexed.chunk(),
                score(indexed, originalTerms, queryTerms, normalizedQuery)
            ))
            .filter(match -> match.score() > 0)
            .sorted(Comparator.comparingDouble(KnowledgeMatch::score).reversed()
                .thenComparing(match -> match.chunk().title())
                .thenComparing(match -> match.chunk().id()))
            .limit(limit)
            .toList();
    }

    private List<KnowledgeMatch> combine(
        List<KnowledgeMatch> lexicalMatches,
        List<KnowledgeMatch> semanticMatches,
        int limit
    ) {
        Map<String, CombinedMatch> combined = new HashMap<>();
        double maximumLexicalScore = lexicalMatches.stream()
            .mapToDouble(KnowledgeMatch::score)
            .max()
            .orElse(1.0);

        lexicalMatches.forEach(match -> combined.compute(
            match.chunk().id(),
            (id, existing) -> new CombinedMatch(
                match.chunk(),
                (existing == null ? 0 : existing.score())
                    + lexicalWeight * (match.score() / maximumLexicalScore)
            )
        ));
        semanticMatches.forEach(match -> combined.compute(
            match.chunk().id(),
            (id, existing) -> new CombinedMatch(
                match.chunk(),
                (existing == null ? 0 : existing.score())
                    + semanticWeight * Math.max(0, match.score())
            )
        ));

        return combined.values().stream()
            .map(match -> new KnowledgeMatch(match.chunk(), match.score()))
            .sorted(Comparator.comparingDouble(KnowledgeMatch::score).reversed()
                .thenComparing(match -> match.chunk().title())
                .thenComparing(match -> match.chunk().id()))
            .limit(limit)
            .toList();
    }

    private double score(
        IndexedChunk indexed,
        List<String> originalTerms,
        Map<String, Double> queryTerms,
        String normalizedQuery
    ) {
        double score = 0;
        int exactOriginalMatches = 0;

        for (Map.Entry<String, Double> queryTerm : queryTerms.entrySet()) {
            String term = queryTerm.getKey();
            double weight = queryTerm.getValue();
            int contentFrequency = indexed.contentTerms().getOrDefault(term, 0);
            int titleFrequency = indexed.titleTerms().getOrDefault(term, 0);
            if (contentFrequency > 0 || titleFrequency > 0) {
                double idf = inverseDocumentFrequency(term);
                score += weight * idf * saturated(contentFrequency);
                score += weight * idf * saturated(titleFrequency) * 2.2;
            }
        }

        for (String term : new HashSet<>(originalTerms)) {
            if (indexed.allTerms().contains(term)) {
                exactOriginalMatches++;
            } else if (term.length() >= 5 && hasCloseTerm(term, indexed.allTerms())) {
                score += inverseDocumentFrequency(term) * 0.35;
            }
        }

        if (!originalTerms.isEmpty()) {
            double coverage = (double) exactOriginalMatches / new HashSet<>(originalTerms).size();
            score += coverage * coverage * 4.0;
        }
        if (normalizedQuery.length() >= 5 && indexed.normalizedText().contains(normalizedQuery)) {
            score += 8.0;
        }
        return score;
    }

    private double saturated(int frequency) {
        if (frequency == 0) {
            return 0;
        }
        return (frequency * (BM25_SATURATION + 1)) / (frequency + BM25_SATURATION);
    }

    private double inverseDocumentFrequency(String term) {
        int frequency = documentFrequency.getOrDefault(term, 0);
        return Math.log(1 + ((index.size() - frequency + 0.5) / (frequency + 0.5)));
    }

    private boolean hasCloseTerm(String term, Set<String> candidates) {
        for (String candidate : candidates) {
            if (Math.abs(term.length() - candidate.length()) <= 1
                && editDistanceAtMostOne(term, candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean editDistanceAtMostOne(String left, String right) {
        if (left.equals(right)) {
            return true;
        }
        if (Math.abs(left.length() - right.length()) > 1) {
            return false;
        }
        if (isAdjacentTransposition(left, right)) {
            return true;
        }

        int leftIndex = 0;
        int rightIndex = 0;
        int edits = 0;
        while (leftIndex < left.length() && rightIndex < right.length()) {
            if (left.charAt(leftIndex) == right.charAt(rightIndex)) {
                leftIndex++;
                rightIndex++;
                continue;
            }
            if (++edits > 1) {
                return false;
            }
            if (left.length() > right.length()) {
                leftIndex++;
            } else if (right.length() > left.length()) {
                rightIndex++;
            } else {
                leftIndex++;
                rightIndex++;
            }
        }
        return edits + (left.length() - leftIndex) + (right.length() - rightIndex) <= 1;
    }

    private boolean isAdjacentTransposition(String left, String right) {
        if (left.length() != right.length()) {
            return false;
        }
        List<Integer> mismatches = new ArrayList<>(2);
        for (int index = 0; index < left.length(); index++) {
            if (left.charAt(index) != right.charAt(index)) {
                mismatches.add(index);
                if (mismatches.size() > 2) {
                    return false;
                }
            }
        }
        return mismatches.size() == 2
            && mismatches.get(1) == mismatches.get(0) + 1
            && left.charAt(mismatches.get(0)) == right.charAt(mismatches.get(1))
            && left.charAt(mismatches.get(1)) == right.charAt(mismatches.get(0));
    }

    private IndexedChunk index(KnowledgeChunk chunk) {
        Map<String, Integer> titleTerms = frequencies(analyzer.tokens(chunk.title()));
        Map<String, Integer> contentTerms = frequencies(analyzer.tokens(chunk.content()));
        Set<String> allTerms = new HashSet<>(titleTerms.keySet());
        allTerms.addAll(contentTerms.keySet());
        return new IndexedChunk(
            chunk,
            titleTerms,
            contentTerms,
            Set.copyOf(allTerms),
            analyzer.normalize(chunk.title() + " " + chunk.content())
        );
    }

    private Map<String, Integer> frequencies(List<String> terms) {
        Map<String, Integer> frequencies = new HashMap<>();
        terms.forEach(term -> frequencies.merge(term, 1, Integer::sum));
        return Map.copyOf(frequencies);
    }

    private Map<String, Integer> documentFrequency(List<IndexedChunk> indexedChunks) {
        Map<String, Integer> frequencies = new HashMap<>();
        indexedChunks.forEach(chunk -> chunk.allTerms().forEach(term ->
            frequencies.merge(term, 1, Integer::sum)
        ));
        return Map.copyOf(frequencies);
    }

    private record IndexedChunk(
        KnowledgeChunk chunk,
        Map<String, Integer> titleTerms,
        Map<String, Integer> contentTerms,
        Set<String> allTerms,
        String normalizedText
    ) {
    }

    private record CombinedMatch(KnowledgeChunk chunk, double score) {
    }
}
