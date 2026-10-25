package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Base64Transformation extends AbstractTransformation {

    public Base64Transformation() {
        super(
                "base64",
                "Base64 Encode/Decode",
                List.of(
                        new ParameterDefinition(
                                "mode",
                                "Mode",
                                ParameterType.TEXT,
                                false,
                                "encode",
                                "encode turns text into Base64, decode turns it back"
                        ),
                        new ParameterDefinition(
                                "urlSafe",
                                "URL-safe Alphabet",
                                ParameterType.BOOLEAN,
                                false,
                                false,
                                "Use - and _ instead of + and /, for URLs and file names"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        String mode = getStringParam(parameters, "mode", "encode");
        boolean urlSafe = getBooleanParam(parameters, "urlSafe", false);

        if(mode.toLowerCase(Locale.ROOT).equals("decode")) {
            return decode(input, urlSafe);
        }

        Base64.Encoder encoder = urlSafe ? Base64.getUrlEncoder() : Base64.getEncoder();
        return encoder.encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String input, boolean urlSafe) {
        // Line breaks and spaces are common in copied Base64, so they are ignored
        String compact = input.replaceAll("\\s+", "");
        Base64.Decoder decoder = urlSafe ? Base64.getUrlDecoder() : Base64.getDecoder();

        byte[] bytes;
        try {
            bytes = decoder.decode(compact);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 input: " + e.getMessage());
        }

        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("Decoded Base64 data is not valid UTF-8 text");
        }
    }
}
