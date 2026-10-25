package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HashTransformation extends AbstractTransformation {

    private static final Map<String, String> ALGORITHMS = Map.of(
            "md5", "MD5",
            "sha1", "SHA-1",
            "sha256", "SHA-256",
            "sha512", "SHA-512"
    );

    public HashTransformation() {
        super(
                "hash",
                "Hash",
                List.of(
                        new ParameterDefinition(
                                "algorithm",
                                "Algorithm",
                                ParameterType.TEXT,
                                false,
                                "sha256",
                                "md5, sha1, sha256 or sha512"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        String text = input != null ? input : "";
        String algorithm = getStringParam(parameters, "algorithm", "sha256");

        // Dashes are ignored, so "SHA-256" works as well as "sha256"
        String key = algorithm.toLowerCase(Locale.ROOT).replace("-", "");
        String javaName = ALGORITHMS.getOrDefault(key, "SHA-256");

        try {
            byte[] digest = MessageDigest.getInstance(javaName).digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // Every Java runtime is required to support these algorithms
            throw new IllegalStateException("Hash algorithm not available: " + javaName, e);
        }
    }
}
