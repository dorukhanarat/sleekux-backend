package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UnicodeNormalizeTransformation extends AbstractTransformation {

    public UnicodeNormalizeTransformation() {
        super(
                "unicode_normalize",
                "Normalize Unicode",
                List.of(
                        new ParameterDefinition(
                                "form",
                                "Normalization Form",
                                ParameterType.TEXT,
                                false,
                                "NFC",
                                "NFC, NFD, NFKC or NFKD. NFKC also turns ﬁ into fi and ① into 1"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        String form = getStringParam(parameters, "form", "NFC");

        Normalizer.Form normalizerForm = switch (form.toUpperCase(Locale.ROOT)) {
            case "NFD" -> Normalizer.Form.NFD;
            case "NFKC" -> Normalizer.Form.NFKC;
            case "NFKD" -> Normalizer.Form.NFKD;
            default -> Normalizer.Form.NFC;
        };

        return Normalizer.normalize(input, normalizerForm);
    }
}
