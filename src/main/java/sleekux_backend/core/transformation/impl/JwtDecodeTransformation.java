package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JwtDecodeTransformation extends AbstractTransformation {

    public JwtDecodeTransformation() {
        super(
                "jwt_decode",
                "Decode JWT",
                List.of(
                        new ParameterDefinition(
                                "part",
                                "Part",
                                ParameterType.TEXT,
                                false,
                                "both",
                                "header, payload or both. The signature is not checked"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isBlank()) {
            return "";
        }

        String part = getStringParam(parameters, "part", "both");

        String token = input.strip();
        // Tokens are often copied together with their Authorization header prefix
        if(token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).strip();
        }

        String[] parts = token.split("\\.", -1);
        if(parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT: expected 3 parts separated by dots, found " + parts.length);
        }

        JsonNode header = decodePart(parts[0], "header");
        JsonNode payload = decodePart(parts[1], "payload");

        return switch (part.toLowerCase(Locale.ROOT)) {
            case "header" -> JsonFormatTransformation.writePretty(header, 2);
            case "payload" -> JsonFormatTransformation.writePretty(payload, 2);
            default -> {
                ObjectNode both = JsonFormatTransformation.emptyObject();
                both.set("header", header);
                both.set("payload", payload);
                yield JsonFormatTransformation.writePretty(both, 2);
            }
        };
    }

    private static JsonNode decodePart(String encoded, String name) {
        String json;
        try {
            json = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JWT: " + name + " is not valid Base64URL");
        }

        JsonNode node = JsonFormatTransformation.readJson(json, "Invalid JWT: " + name + " is not valid JSON");
        if(!node.isObject()) {
            throw new IllegalArgumentException("Invalid JWT: " + name + " is not a JSON object");
        }
        return node;
    }
}
