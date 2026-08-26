package com.platform.ai.retrieval.infrastructure;

import com.platform.ai.retrieval.domain.KnowledgeChunk;
import com.platform.ai.retrieval.domain.KnowledgeSource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class MarkdownKnowledgeChunker {

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern API_ENTRY = Pattern.compile("^- `GET /api/v1/.+");

    public List<KnowledgeChunk> chunk(
        String documentName,
        String markdown,
        int maximumChunkCharacters
    ) {
        KnowledgeSource source = sourceOf(documentName);
        String[] headings = new String[6];
        List<KnowledgeChunk> chunks = new ArrayList<>();
        StringBuilder body = new StringBuilder();

        for (String line : markdown.split("\\R")) {
            Matcher heading = HEADING.matcher(line);
            if (heading.matches()) {
                flush(chunks, source, headings, body);
                int level = heading.group(1).length();
                headings[level - 1] = heading.group(2).trim();
                Arrays.fill(headings, level, headings.length, null);
                continue;
            }

            if (API_ENTRY.matcher(line).matches()) {
                flush(chunks, source, headings, body);
                body.append(line.trim());
                flush(chunks, source, headings, body);
                continue;
            }

            if (line.isBlank()) {
                append(body, "");
                continue;
            }

            if (body.length() > 0
                && body.length() + line.length() + 1 > maximumChunkCharacters) {
                flush(chunks, source, headings, body);
            }
            append(body, line);
        }

        flush(chunks, source, headings, body);
        return List.copyOf(chunks);
    }

    private void append(StringBuilder body, String line) {
        if (body.length() > 0) {
            body.append('\n');
        }
        body.append(line);
    }

    private void flush(
        List<KnowledgeChunk> chunks,
        KnowledgeSource source,
        String[] headings,
        StringBuilder body
    ) {
        String content = body.toString().trim();
        body.setLength(0);
        if (content.isEmpty()) {
            return;
        }

        String title = Arrays.stream(headings)
            .filter(value -> value != null && !value.isBlank())
            .reduce((left, right) -> left + " > " + right)
            .orElse(source.name());
        String idSeed = source + "\n" + title + "\n" + content;
        String id = UUID.nameUUIDFromBytes(idSeed.getBytes(StandardCharsets.UTF_8)).toString();
        chunks.add(new KnowledgeChunk(id, source, title, content));
    }

    private KnowledgeSource sourceOf(String documentName) {
        return documentName.toLowerCase().contains("ui-navigation")
            ? KnowledgeSource.UI
            : KnowledgeSource.API;
    }
}
