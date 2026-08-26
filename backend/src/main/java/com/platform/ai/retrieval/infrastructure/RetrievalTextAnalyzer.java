package com.platform.ai.retrieval.infrastructure;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RetrievalTextAnalyzer {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has",
        "have", "how", "i", "in", "is", "it", "of", "on", "or", "that", "the",
        "this", "to", "was", "what", "when", "where", "which", "who", "with",
        "can", "could", "do", "does", "show", "get", "give", "open", "find",
        "un", "une", "des", "du", "de", "la", "le", "les", "dans", "pour"
    );

    private final Map<String, Set<String>> synonyms = synonymMap();

    public List<String> tokens(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = normalize(text);
        List<String> tokens = new ArrayList<>();
        for (String token : normalized.split(" ")) {
            String canonical = stem(token);
            if (canonical.length() > 1 && !STOP_WORDS.contains(canonical)) {
                tokens.add(canonical);
            }
        }
        return List.copyOf(tokens);
    }

    public Map<String, Double> expandedQueryTerms(String query) {
        Map<String, Double> weightedTerms = new LinkedHashMap<>();
        for (String token : tokens(query)) {
            weightedTerms.merge(token, 1.0, Math::max);
            for (String synonym : synonyms.getOrDefault(token, Set.of())) {
                weightedTerms.merge(synonym, 0.55, Math::max);
            }
        }
        return Map.copyOf(weightedTerms);
    }

    public String normalize(String text) {
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS.matcher(decomposed).replaceAll("");
        return NON_ALPHANUMERIC.matcher(withoutDiacritics.toLowerCase(Locale.ROOT))
            .replaceAll(" ")
            .trim();
    }

    private String stem(String token) {
        if (token.length() > 4 && token.endsWith("ies")) {
            return token.substring(0, token.length() - 3) + "y";
        }
        if (token.length() > 3 && token.endsWith("s") && !token.endsWith("ss")) {
            return token.substring(0, token.length() - 1);
        }
        return token;
    }

    private Map<String, Set<String>> synonymMap() {
        Map<String, Set<String>> result = new HashMap<>();
        addGroup(result, "grade", "note", "mark", "result");
        addGroup(result, "schedule", "timetable", "planning", "calendar");
        addGroup(result, "professor", "teacher", "lecturer");
        addGroup(result, "student", "learner");
        addGroup(result, "establishment", "faculty", "school");
        addGroup(result, "program", "filiere");
        addGroup(result, "module", "subject", "course");
        addGroup(result, "absence", "absent", "attendance");
        addGroup(result, "permission", "privilege", "grant");
        addGroup(result, "progression", "promotion", "decision", "pass");
        addGroup(result, "room", "classroom", "amphitheatre", "lab");
        addGroup(result, "rattrapage", "resit", "retake");
        return Map.copyOf(result);
    }

    private void addGroup(Map<String, Set<String>> target, String... words) {
        Set<String> group = new HashSet<>();
        for (String word : words) {
            group.add(stem(word));
        }
        for (String word : group) {
            Set<String> alternatives = new HashSet<>(group);
            alternatives.remove(word);
            target.computeIfAbsent(word, ignored -> new HashSet<>()).addAll(alternatives);
        }
    }
}
