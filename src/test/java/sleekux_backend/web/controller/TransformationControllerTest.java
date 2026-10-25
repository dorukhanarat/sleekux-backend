package sleekux_backend.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import sleekux_backend.web.config.CoreConfig;
import sleekux_backend.web.config.GlobalExceptionHandler;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransformationController.class)
@Import({CoreConfig.class, GlobalExceptionHandler.class})
public class TransformationControllerTest {

    private static final String TRANSFORMATIONS_URL = "/api/transformations";
    private static final String TRANSFORM_URL = "/api/transform";

    @Autowired
    private MockMvc mockMvc;

    // JSONPath that selects a transformation by key, so tests do not depend on its position in the list
    private static String byKey(String key) {
        return "$[?(@.key == '" + key + "')]";
    }

    private ResultActions postTransform(String json) throws Exception {
        return mockMvc.perform(post(TRANSFORM_URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // Lists every registered transformation in registry order
    @Test
    void listsAllTransformations() throws Exception {
        mockMvc.perform(get(TRANSFORMATIONS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(41)))
                .andExpect(jsonPath("$[*].key", contains("append", "base64", "case_convert", "count", "csv_column", "dedupe_lines", "extract",
                        "filter_lines", "hash", "html_escape", "json_escape", "json_format", "jwt_decode", "lowercase",
                        "mask", "normalize_whitespace", "number_lines", "pad", "prepend", "regex_match", "regex_replace",
                        "remove_chars", "remove_diacritics", "repeat", "replace", "reverse", "shuffle_lines", "slugify",
                        "sort_lines", "split_join", "strip_html", "substring", "swap_case", "take_lines", "trim",
                        "truncate", "unicode_normalize", "uppercase", "url_encode", "word_wrap", "wrap_lines")));
    }

    // Each transformation includes its display name and parameter definitions
    @Test
    void transformationsIncludeParameterDefinitions() throws Exception {
        mockMvc.perform(get(TRANSFORMATIONS_URL))
                .andExpect(jsonPath(byKey("replace") + ".displayName", contains("Replace Text")))
                .andExpect(jsonPath(byKey("replace") + ".parameters[*]", hasSize(3)))
                .andExpect(jsonPath(byKey("replace") + ".parameters[0].name", contains("oldValue")))
                .andExpect(jsonPath(byKey("replace") + ".parameters[0].label", contains("Target String")))
                .andExpect(jsonPath(byKey("replace") + ".parameters[0].required", contains(true)))
                .andExpect(jsonPath(byKey("replace") + ".parameters[0].placeholder", contains("Exact text to find, e.g. world")))
                .andExpect(jsonPath(byKey("lowercase") + ".parameters[*]", empty()));
    }

    // Parameter types are written as their lowercase JSON value
    @Test
    void parameterTypesUseJsonValue() throws Exception {
        mockMvc.perform(get(TRANSFORMATIONS_URL))
                .andExpect(jsonPath(byKey("replace") + ".parameters[0].type", contains("text")))
                .andExpect(jsonPath(byKey("substring") + ".parameters[0].type", contains("number")))
                .andExpect(jsonPath(byKey("regex_match") + ".parameters[0].type", contains("regex")))
                .andExpect(jsonPath(byKey("regex_match") + ".parameters[1].type", contains("boolean")));
    }

    // Runs the steps in order and returns the original text, final result and history
    @Test
    void transformRunsSteps() throws Exception {
        postTransform("""
                {
                  "text": "  hello world  ",
                  "steps": [
                    { "transformationKey": "trim" },
                    { "transformationKey": "uppercase" },
                    { "transformationKey": "append", "parameters": { "suffix": "!" } }
                  ]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalText").value("  hello world  "))
                .andExpect(jsonPath("$.finalResult").value("HELLO WORLD!"))
                .andExpect(jsonPath("$.history", hasSize(3)));
    }

    // Each history entry contains the step details and the text before and after the step
    @Test
    void transformReturnsStepHistory() throws Exception {
        postTransform("""
                {
                  "text": "abc",
                  "steps": [
                    { "transformationKey": "replace", "parameters": { "oldValue": "b", "newValue": "X" } }
                  ]
                }
                """)
                .andExpect(jsonPath("$.history[0].stepIndex").value(1))
                .andExpect(jsonPath("$.history[0].transformationKey").value("replace"))
                .andExpect(jsonPath("$.history[0].transformationName").value("Replace Text"))
                .andExpect(jsonPath("$.history[0].parameters.oldValue").value("b"))
                .andExpect(jsonPath("$.history[0].parameters.newValue").value("X"))
                .andExpect(jsonPath("$.history[0].inputSnapshot").value("abc"))
                .andExpect(jsonPath("$.history[0].outputSnapshot").value("aXc"))
                .andExpect(jsonPath("$.history[0].executionTime").isNumber());
    }

    // Numeric and boolean parameters sent as JSON values are understood
    @Test
    void transformAcceptsNonStringParameters() throws Exception {
        postTransform("""
                {
                  "text": "a1b22c333",
                  "steps": [
                    { "transformationKey": "regex_match", "parameters": { "pattern": "\\\\d+", "matchAll": false } },
                    { "transformationKey": "substring", "parameters": { "startIndex": 0, "endIndex": 1 } }
                  ]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalResult").value("1"));
    }

    // Transformation keys in the request are matched regardless of case
    @Test
    void transformKeysAreCaseInsensitive() throws Exception {
        postTransform("""
                { "text": "abc", "steps": [ { "transformationKey": "UPPERCASE" } ] }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalResult").value("ABC"));
    }

    // Without steps the text is returned unchanged with an empty history
    @Test
    void transformWithoutSteps() throws Exception {
        postTransform("""
                { "text": "abc" }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalResult").value("abc"))
                .andExpect(jsonPath("$.history", empty()));

        postTransform("""
                { "text": "abc", "steps": [] }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalResult").value("abc"));
    }

    // Missing text is treated as an empty string
    @Test
    void transformWithoutText() throws Exception {
        postTransform("""
                { "steps": [ { "transformationKey": "append", "parameters": { "suffix": "x" } } ] }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalText").value(""))
                .andExpect(jsonPath("$.finalResult").value("x"));
    }

    // An unknown transformation key returns 400 with an error body
    @Test
    void unknownKeyReturnsBadRequest() throws Exception {
        postTransform("""
                { "text": "abc", "steps": [ { "transformationKey": "does_not_exist" } ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Unknown transformation key: does_not_exist"))
                .andExpect(jsonPath("$.path").value(TRANSFORM_URL))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // A step with a missing or blank transformation key returns 400
    @Test
    void missingKeyReturnsBadRequest() throws Exception {
        postTransform("""
                { "text": "abc", "steps": [ { "parameters": {} } ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("transformationKey cannot be null or blank"));

        postTransform("""
                { "text": "abc", "steps": [ { "transformationKey": "  " } ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("transformationKey cannot be null or blank"));
    }

    // A null entry in the steps list returns 400
    @Test
    void nullStepReturnsBadRequest() throws Exception {
        postTransform("""
                { "text": "abc", "steps": [ null ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("steps cannot contain null entries"));
    }

    // An invalid regex pattern returns 400 explaining the problem
    @Test
    void invalidRegexReturnsBadRequest() throws Exception {
        postTransform("""
                { "text": "abc", "steps": [ { "transformationKey": "regex_match", "parameters": { "pattern": "(" } } ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("Invalid regular expression: ")));
    }

    // Malformed JSON returns 400
    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        postTransform("{ not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON."));
    }

    // A missing request body returns 400
    @Test
    void missingBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post(TRANSFORM_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON."));
    }

    // Using the wrong HTTP method returns 405
    @Test
    void wrongMethodReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(get(TRANSFORM_URL))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    // Sending a body that is not JSON returns 415
    @Test
    void wrongContentTypeReturnsUnsupportedMediaType() throws Exception {
        mockMvc.perform(post(TRANSFORM_URL).contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    // An unknown URL returns 404
    @Test
    void unknownUrlReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/does-not-exist"));
    }

    // CORS preflight requests from the allowed frontend origins are accepted
    @Test
    void corsAllowsFrontendOrigins() throws Exception {
        for (String origin : new String[]{"http://localhost:5173", "http://localhost:3000"}) {
            mockMvc.perform(options(TRANSFORM_URL)
                            .header("Origin", origin)
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", origin));
        }
    }

    // Actual requests from an allowed origin include the CORS header
    @Test
    void corsHeaderOnActualRequest() throws Exception {
        mockMvc.perform(get(TRANSFORMATIONS_URL).header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    // CORS requests from other origins are rejected
    @Test
    void corsRejectsOtherOrigins() throws Exception {
        mockMvc.perform(options(TRANSFORM_URL)
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    // New transformations can be chained like the others
    @Test
    void chainsNewTransformations() throws Exception {
        postTransform("""
                {
                  "text": "Çok Güzel Bir Başlık!",
                  "steps": [
                    { "transformationKey": "slugify" },
                    { "transformationKey": "base64", "parameters": { "mode": "encode" } },
                    { "transformationKey": "base64", "parameters": { "mode": "decode" } },
                    { "transformationKey": "truncate", "parameters": { "length": 8, "ellipsis": "", "wordBoundary": false } }
                  ]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalResult").value("cok-guze"))
                .andExpect(jsonPath("$.history[0].outputSnapshot").value("cok-guzel-bir-baslik"));
    }

    // Invalid input for json_format, base64 and url_encode returns 400 with an explanation
    @Test
    void invalidInputForNewTransformationsReturnsBadRequest() throws Exception {
        postTransform("""
                { "text": "{\\"a\\": }", "steps": [ { "transformationKey": "json_format" } ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("Invalid JSON: ")));

        postTransform("""
                { "text": "not base64!", "steps": [ { "transformationKey": "base64", "parameters": { "mode": "decode" } } ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("Invalid Base64 input")));

        postTransform("""
                { "text": "100%zz", "steps": [ { "transformationKey": "url_encode", "parameters": { "mode": "decode" } } ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("Invalid percent-encoding")));
    }

    // Invalid input for jwt_decode and json_escape returns 400 with an explanation
    @Test
    void invalidInputForDecodersReturnsBadRequest() throws Exception {
        postTransform("""
                { "text": "not-a-token", "steps": [ { "transformationKey": "jwt_decode" } ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid JWT: expected 3 parts separated by dots, found 1"));

        postTransform("""
                { "text": "bad \\\\x escape", "steps": [ { "transformationKey": "json_escape", "parameters": { "mode": "unescape" } } ] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("Invalid escape sequence")));
    }

    // Text copied from a web page can be cleaned up and summarised in one pipeline
    @Test
    void chainsCleanupTransformations() throws Exception {
        postTransform("""
                {
                  "text": "<p>Hello <b>World</b> &amp; friends!</p><p>Second   paragraph</p>",
                  "steps": [
                    { "transformationKey": "strip_html" },
                    { "transformationKey": "remove_chars", "parameters": { "types": "punctuation" } },
                    { "transformationKey": "count", "parameters": { "what": "words" } }
                  ]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history[0].outputSnapshot").value("Hello World & friends!\n\nSecond paragraph"))
                .andExpect(jsonPath("$.history[1].outputSnapshot").value("Hello World  friends\n\nSecond paragraph"))
                .andExpect(jsonPath("$.finalResult").value("5"));
    }
}
