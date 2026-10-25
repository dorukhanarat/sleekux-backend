package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SlugifyTransformationTest {

    private SlugifyTransformation slugifyTransformation;

    @BeforeEach
    void setUp() {
        slugifyTransformation = new SlugifyTransformation();
    }

    private Map<String, Object> params(Object separator, Object lowercase) {
        Map<String, Object> params = new HashMap<>();
        params.put("separator", separator);
        params.put("lowercase", lowercase);
        return params;
    }

    // Has key "slugify" and display name "Slugify"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("slugify", slugifyTransformation.getKey());
        assertEquals("Slugify", slugifyTransformation.getDisplayName());
    }

    // Defines an optional separator (default "-") and an optional lowercase flag (default true)
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = slugifyTransformation.getParameters();

        assertEquals(2, parameters.size());
        assertEquals("separator", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertEquals("-", parameters.get(0).getDefaultValue());
        assertEquals("lowercase", parameters.get(1).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(1).getType());
        assertEquals(true, parameters.get(1).getDefaultValue());
    }

    // Turns a title into a lowercase, dash-separated slug
    @Test
    void slugifiesTitle() {
        assertEquals("hello-world-2024", slugifyTransformation.apply("Hello, World! 2024", params("-", true)));
    }

    // Converts accented letters instead of dropping them
    @Test
    void convertsAccentedLetters() {
        assertEquals("cok-guzel-bir-baslik", slugifyTransformation.apply("Çok Güzel Bir Başlık!", params("-", true)));
        assertEquals("creme-brulee-in-lodz", slugifyTransformation.apply("Crème Brûlée in Łódź", params("-", true)));
    }

    // Collapses runs of spaces and symbols into one separator and trims them from the ends
    @Test
    void collapsesAndTrimsSeparators() {
        assertEquals("a-b-c", slugifyTransformation.apply("  --a___b !!! c--  ", params("-", true)));
    }

    // Uses a custom separator, including an empty one
    @Test
    void usesCustomSeparator() {
        assertEquals("my_blog_post", slugifyTransformation.apply("My Blog Post", params("_", true)));
        assertEquals("myblogpost", slugifyTransformation.apply("My Blog Post", params("", true)));
    }

    // Treats special characters in the separator literally
    @Test
    void separatorIsLiteral() {
        assertEquals("a$1b", slugifyTransformation.apply("a b", params("$1", true)));
        assertEquals("a\\b", slugifyTransformation.apply("a b", params("\\", true)));
    }

    // Keeps the original letter case when lowercase is false
    @Test
    void keepsCaseWhenLowercaseIsFalse() {
        assertEquals("Hello-World", slugifyTransformation.apply("Hello World", params("-", false)));
    }

    // Removes characters that cannot be converted to ASCII letters, such as emoji and other scripts
    @Test
    void removesNonLatinCharacters() {
        assertEquals("tea-time", slugifyTransformation.apply("😀 tea time 茶", params("-", true)));
    }

    // Returns an empty string when the text has no letters or digits
    @Test
    void returnsEmptyWhenNothingRemains() {
        assertEquals("", slugifyTransformation.apply(" !!! ", params("-", true)));
    }

    // Uses "-" and lowercase when parameters are missing, null or the map is null
    @Test
    void usesDefaults() {
        assertEquals("hello-world", slugifyTransformation.apply("Hello World", new HashMap<>()));
        assertEquals("hello-world", slugifyTransformation.apply("Hello World", params(null, null)));
        assertEquals("hello-world", slugifyTransformation.apply("Hello World", null));
    }

    // Returns an empty string for null or empty input
    @Test
    void returnsEmptyStringForNullOrEmptyInput() {
        assertEquals("", slugifyTransformation.apply(null, params("-", true)));
        assertEquals("", slugifyTransformation.apply("", params("-", true)));
    }
}
