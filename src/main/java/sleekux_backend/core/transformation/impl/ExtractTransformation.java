package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractTransformation extends AbstractTransformation {

    private static final Pattern EMAILS = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}");
    private static final Pattern URLS = Pattern.compile("https?://[^\\s<>\"']+");
    // A minus sign only counts when it does not follow a word or number, so "2024-10-25" gives 2024, 10, 25.
    // A number never starts in the middle of another number, so "1.2.3" gives 1.2 and not 1.2 and 3
    private static final Pattern NUMBERS = Pattern.compile("(?:(?<![\\w.])-)?(?<![\\d.])\\d+(?:\\.\\d+)?");
    private static final Pattern HASHTAGS = Pattern.compile("(?<![\\w#])#[\\p{L}\\p{N}_]+");

    public ExtractTransformation() {
        super(
                "extract",
                "Extract",
                List.of(
                        new ParameterDefinition(
                                "type",
                                "What to Extract",
                                ParameterType.TEXT,
                                false,
                                "emails",
                                "emails, urls, numbers or hashtags"
                        ),
                        new ParameterDefinition(
                                "unique",
                                "Unique Only",
                                ParameterType.BOOLEAN,
                                false,
                                false,
                                "Keep only the first of each repeated value"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        String type = getStringParam(parameters, "type", "emails");
        boolean unique = getBooleanParam(parameters, "unique", false);

        Pattern pattern = switch (type.toLowerCase(Locale.ROOT)) {
            case "urls" -> URLS;
            case "numbers" -> NUMBERS;
            case "hashtags" -> HASHTAGS;
            default -> EMAILS;
        };

        List<String> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()) {
            String match = matcher.group();
            matches.add(pattern == URLS ? trimUrl(match) : match);
        }

        return String.join("\n", unique ? new LinkedHashSet<>(matches) : matches);
    }

    // Punctuation at the end of a URL usually belongs to the sentence, e.g. "see https://a.com."
    // A closing bracket is kept only when the URL also contains the opening one, e.g. Wikipedia links
    private static String trimUrl(String url) {
        String result = url;
        while(!result.isEmpty()) {
            char last = result.charAt(result.length() - 1);
            boolean unbalancedParen = last == ')' && count(result, '(') < count(result, ')');
            if(".,;:!?".indexOf(last) >= 0 || unbalancedParen) {
                result = result.substring(0, result.length() - 1);
            } else {
                break;
            }
        }
        return result;
    }

    private static long count(String text, char c) {
        return text.chars().filter(ch -> ch == c).count();
    }
}
