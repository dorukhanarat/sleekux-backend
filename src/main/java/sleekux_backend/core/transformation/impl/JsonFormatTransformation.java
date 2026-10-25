package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JsonFormatTransformation extends AbstractTransformation {

    static final int MAX_INDENT = 8;

    // Decimals are read as BigDecimal so values like 0.1, 1.50 or 1e400 keep their exact value.
    // Exponents are written in a standard form, e.g. 1e400 becomes 1E+400
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    public JsonFormatTransformation() {
        super(
                "json_format",
                "Format JSON",
                List.of(
                        new ParameterDefinition(
                                "mode",
                                "Mode",
                                ParameterType.TEXT,
                                false,
                                "pretty",
                                "pretty adds line breaks and indentation, minify removes them"
                        ),
                        new ParameterDefinition(
                                "indent",
                                "Indent Size",
                                ParameterType.NUMBER,
                                false,
                                2,
                                "Spaces per level when pretty printing, 1 to 8"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isBlank()) {
            return "";
        }

        String mode = getStringParam(parameters, "mode", "pretty");
        int indent = Math.max(1, Math.min(getIntParam(parameters, "indent", 2), MAX_INDENT));

        JsonNode node = readJson(input, "Invalid JSON");

        if(mode.toLowerCase(Locale.ROOT).equals("minify")) {
            return MAPPER.writeValueAsString(node);
        }

        return writePretty(node, indent);
    }

    static JsonNode readJson(String json, String errorPrefix) {
        try {
            return MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new IllegalArgumentException(errorPrefix + ": " + describe(e));
        }
    }

    static ObjectNode emptyObject() {
        return MAPPER.createObjectNode();
    }

    static String writePretty(JsonNode node, int indent) {
        DefaultIndenter indenter = new DefaultIndenter(" ".repeat(indent), "\n");
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter()
                .withSeparators(Separators.createDefaultInstance()
                        .withObjectNameValueSpacing(Separators.Spacing.AFTER)
                        .withObjectEmptySeparator("")
                        .withArrayEmptySeparator(""))
                .withObjectIndenter(indenter)
                .withArrayIndenter(indenter);

        return MAPPER.writer().with(printer).writeValueAsString(node);
    }

    private static String describe(JacksonException e) {
        String message = e.getOriginalMessage();
        // Jackson adds internal source details to some messages, which mean nothing to the user
        int sourceDetails = message.indexOf(" (start marker at");
        if(sourceDetails >= 0) {
            message = message.substring(0, sourceDetails);
        }
        TokenStreamLocation location = e.getLocation();
        if(location != null && location.getLineNr() > 0) {
            message += " (line " + location.getLineNr() + ", column " + location.getColumnNr() + ")";
        }
        return message;
    }
}
