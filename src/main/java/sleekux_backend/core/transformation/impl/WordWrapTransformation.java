package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WordWrapTransformation extends AbstractTransformation {

    static final int MAX_WIDTH = 10_000;

    public WordWrapTransformation() {
        super(
                "word_wrap",
                "Word Wrap",
                List.of(
                        new ParameterDefinition(
                                "width",
                                "Line Width",
                                ParameterType.NUMBER,
                                false,
                                80,
                                "Maximum characters per line, e.g. 72 for commit messages"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        int width = Math.max(1, Math.min(getIntParam(parameters, "width", 80), MAX_WIDTH));

        // Existing line breaks are kept, so paragraphs and blank lines stay where they are
        List<String> result = new ArrayList<>();
        for(String line : input.split("\\R", -1)) {
            wrapLine(line, width, result);
        }
        return String.join("\n", result);
    }

    // Packs as many words as fit on each line. A word longer than the width gets a line of its own
    private static void wrapLine(String line, int width, List<String> result) {
        String[] words = line.strip().split("\\s+");
        if(words.length == 1 && words[0].isEmpty()) {
            result.add("");
            return;
        }

        StringBuilder current = new StringBuilder();
        int currentLength = 0;
        for(String word : words) {
            int wordLength = word.codePointCount(0, word.length());
            if(currentLength > 0 && currentLength + 1 + wordLength > width) {
                result.add(current.toString());
                current.setLength(0);
                currentLength = 0;
            }
            if(currentLength > 0) {
                current.append(' ');
                currentLength++;
            }
            current.append(word);
            currentLength += wordLength;
        }
        result.add(current.toString());
    }
}
