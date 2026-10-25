package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExtractTransformationTest {

    private ExtractTransformation extractTransformation;

    @BeforeEach
    void setUp() {
        extractTransformation = new ExtractTransformation();
    }

    private Map<String, Object> params(Object type, Object unique) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("unique", unique);
        return params;
    }

    // Has key "extract" and display name "Extract"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("extract", extractTransformation.getKey());
        assertEquals("Extract", extractTransformation.getDisplayName());
    }

    // Defines an optional type (default "emails") and an optional unique flag (default false)
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = extractTransformation.getParameters();

        assertEquals(2, parameters.size());
        assertEquals("type", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertEquals("emails", parameters.get(0).getDefaultValue());
        assertEquals("unique", parameters.get(1).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(1).getType());
        assertEquals(false, parameters.get(1).getDefaultValue());
    }

    // Extracts email addresses, one per line, in the order they appear
    @Test
    void extractsEmails() {
        String text = "Contact ali.veli@ornek.com.tr or support+help@example.io, not x@y or @handle.";

        assertEquals("ali.veli@ornek.com.tr\nsupport+help@example.io", extractTransformation.apply(text, params("emails", false)));
    }

    // Extracts http and https URLs
    @Test
    void extractsUrls() {
        String text = "Docs: https://example.com/docs?page=2#top and http://test.org, but not ftp://x.org";

        assertEquals("https://example.com/docs?page=2#top\nhttp://test.org", extractTransformation.apply(text, params("urls", false)));
    }

    // Removes sentence punctuation after a URL
    @Test
    void trimsPunctuationAfterUrls() {
        assertEquals("https://a.com/x", extractTransformation.apply("See https://a.com/x.", params("urls", false)));
        assertEquals("https://a.com", extractTransformation.apply("(see https://a.com)", params("urls", false)));
        assertEquals("https://a.com", extractTransformation.apply("Really? https://a.com!?", params("urls", false)));
    }

    // Keeps a closing bracket that belongs to the URL
    @Test
    void keepsBalancedParenthesesInUrls() {
        assertEquals("https://en.wikipedia.org/wiki/Java_(language)",
                extractTransformation.apply("Read https://en.wikipedia.org/wiki/Java_(language).", params("urls", false)));
    }

    // Extracts whole numbers, decimals and negative numbers
    @Test
    void extractsNumbers() {
        assertEquals("42\n-5\n3.14\n0", extractTransformation.apply("Got 42 items, -5 degrees, pi is 3.14 and 0 left", params("numbers", false)));
    }

    // Treats a dash between numbers as a separator, not a minus sign
    @Test
    void dashBetweenNumbersIsNotMinus() {
        assertEquals("2024\n10\n25", extractTransformation.apply("2024-10-25", params("numbers", false)));
        assertEquals("7", extractTransformation.apply("a-7", params("numbers", false)));
    }

    // Finds numbers that follow letters and does not split a number into parts
    @Test
    void numbersInsideText() {
        assertEquals("42", extractTransformation.apply("item42", params("numbers", false)));
        assertEquals("1.2", extractTransformation.apply("version 1.2.3", params("numbers", false)));
    }

    // Extracts hashtags, including non-ASCII letters, but not # inside words
    @Test
    void extractsHashtags() {
        assertEquals("#merhaba\n#çay_saati\n#2024", extractTransformation.apply("#merhaba #çay_saati #2024 a#b ##x #", params("hashtags", false)));
    }

    // Keeps repeated values unless unique is true, which keeps only the first of each
    @Test
    void uniqueRemovesRepeats() {
        String text = "1 2 1 3 2";

        assertEquals("1\n2\n1\n3\n2", extractTransformation.apply(text, params("numbers", false)));
        assertEquals("1\n2\n3", extractTransformation.apply(text, params("numbers", true)));
    }

    // Type is case-insensitive
    @Test
    void typeIsCaseInsensitive() {
        assertEquals("42", extractTransformation.apply("x 42", params("NUMBERS", false)));
    }

    // Extracts emails when the type is missing, null, unknown or the map is null
    @Test
    void defaultsToEmails() {
        String text = "mail a@b.co and 42";

        assertEquals("a@b.co", extractTransformation.apply(text, new HashMap<>()));
        assertEquals("a@b.co", extractTransformation.apply(text, params(null, null)));
        assertEquals("a@b.co", extractTransformation.apply(text, params("phones", false)));
        assertEquals("a@b.co", extractTransformation.apply(text, null));
    }

    // Returns an empty string when nothing is found
    @Test
    void returnsEmptyWhenNothingFound() {
        assertEquals("", extractTransformation.apply("no emails here", params("emails", false)));
        assertEquals("", extractTransformation.apply("no numbers", params("numbers", false)));
    }

    // Returns an empty string for null or empty input
    @Test
    void returnsEmptyStringForNullOrEmptyInput() {
        assertEquals("", extractTransformation.apply(null, params("emails", false)));
        assertEquals("", extractTransformation.apply("", params("emails", false)));
    }
}
