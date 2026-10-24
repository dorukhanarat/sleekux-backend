package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CaseConvertTransformationTest {

    private CaseConvertTransformation caseConvertTransformation;

    @BeforeEach
    void setUp() {
        caseConvertTransformation = new CaseConvertTransformation();
    }

    private Map<String, Object> style(Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("style", value);
        return params;
    }

    // Has key "case_convert" and display name "Change Case"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("case_convert", caseConvertTransformation.getKey());
        assertEquals("Change Case", caseConvertTransformation.getDisplayName());
    }

    // Defines an optional style parameter that defaults to "title"
    @Test
    void definesStyleParameter() {
        List<ParameterDefinition> parameters = caseConvertTransformation.getParameters();

        assertEquals(1, parameters.size());
        assertEquals("style", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertFalse(parameters.get(0).isRequired());
        assertEquals("title", parameters.get(0).getDefaultValue());
    }

    // Title case capitalizes every word and keeps the original spacing
    @Test
    void convertsToTitleCase() {
        assertEquals("Hello World  Foo", caseConvertTransformation.apply("hello wORLD  foo", style("title")));
    }

    // Sentence case capitalizes the first letter of each sentence only
    @Test
    void convertsToSentenceCase() {
        assertEquals("Hello world. This is it! Ok?  Yes",
                caseConvertTransformation.apply("hELLO world. this IS it! ok?  yes", style("sentence")));
    }

    // Camel case joins words with the first one lowercase
    @Test
    void convertsToCamelCase() {
        assertEquals("helloWorld", caseConvertTransformation.apply("hello world", style("camel")));
        assertEquals("helloWorldFooBar", caseConvertTransformation.apply("Hello_World-foo bar", style("camel")));
    }

    // Pascal case joins capitalized words
    @Test
    void convertsToPascalCase() {
        assertEquals("UserId", caseConvertTransformation.apply("user_id", style("pascal")));
    }

    // Snake case splits camelCase humps and acronyms
    @Test
    void convertsToSnakeCase() {
        assertEquals("hello_world", caseConvertTransformation.apply("helloWorld", style("snake")));
        assertEquals("parse_html_text", caseConvertTransformation.apply("parseHTMLText", style("snake")));
        assertEquals("version2_update", caseConvertTransformation.apply("version2Update", style("snake")));
    }

    // Kebab case joins lowercase words with hyphens
    @Test
    void convertsToKebabCase() {
        assertEquals("hello-world-foo", caseConvertTransformation.apply("Hello World Foo", style("kebab")));
    }

    // Constant case joins uppercase words with underscores
    @Test
    void convertsToConstantCase() {
        assertEquals("MAX_RETRY_COUNT", caseConvertTransformation.apply("max retry count", style("constant")));
        assertEquals("MAX_RETRY_COUNT", caseConvertTransformation.apply("maxRetryCount", style("constant")));
    }

    // Style is case-insensitive
    @Test
    void styleIsCaseInsensitive() {
        assertEquals("hello_world", caseConvertTransformation.apply("Hello World", style("SNAKE")));
    }

    // Uses title case when the style is missing, unknown or the map is null
    @Test
    void defaultsToTitleCase() {
        assertEquals("Hello World", caseConvertTransformation.apply("hello world", new HashMap<>()));
        assertEquals("Hello World", caseConvertTransformation.apply("hello world", style("weird")));
        assertEquals("Hello World", caseConvertTransformation.apply("hello world", null));
    }

    // Returns an empty string for empty input in every style
    @Test
    void returnsEmptyForEmptyInput() {
        for (String s : List.of("title", "sentence", "camel", "pascal", "snake", "kebab", "constant")) {
            assertEquals("", caseConvertTransformation.apply("", style(s)));
        }
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", caseConvertTransformation.apply(null, style("title")));
    }
}
