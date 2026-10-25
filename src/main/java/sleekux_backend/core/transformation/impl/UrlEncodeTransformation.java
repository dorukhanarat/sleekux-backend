package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UrlEncodeTransformation extends AbstractTransformation {

    public UrlEncodeTransformation() {
        super(
                "url_encode",
                "URL Encode/Decode",
                List.of(
                        new ParameterDefinition(
                                "mode",
                                "Mode",
                                ParameterType.TEXT,
                                false,
                                "encode",
                                "encode turns a b&c into a%20b%26c, decode turns it back"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        String mode = getStringParam(parameters, "mode", "encode");

        if(mode.toLowerCase(Locale.ROOT).equals("decode")) {
            try {
                return URLDecoder.decode(input, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid percent-encoding: " + e.getMessage());
            }
        }

        // URLEncoder uses HTML form rules. These replacements give standard percent-encoding (RFC 3986),
        // where a space is %20 and only A-Z a-z 0-9 - _ . ~ stay unencoded
        return URLEncoder.encode(input, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }
}
