package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlugifyTransformation extends AbstractTransformation {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]+");

    public SlugifyTransformation() {
        super(
                "slugify",
                "Slugify",
                List.of(
                        new ParameterDefinition(
                                "separator",
                                "Separator",
                                ParameterType.TEXT,
                                false,
                                "-",
                                "Put between words, e.g. - gives my-blog-post"
                        ),
                        new ParameterDefinition(
                                "lowercase",
                                "Lowercase",
                                ParameterType.BOOLEAN,
                                false,
                                true,
                                "Convert the slug to lowercase"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        String separator = getStringParam(parameters, "separator", "-");
        boolean lowercase = getBooleanParam(parameters, "lowercase", true);

        // Accented letters become plain letters first, so "Çağrı" turns into "Cagri" instead of being dropped
        String ascii = RemoveDiacriticsTransformation.removeDiacritics(input);
        String slug = NON_ALPHANUMERIC.matcher(ascii).replaceAll(" ").strip();
        slug = slug.isEmpty() ? "" : slug.replaceAll(" +", Matcher.quoteReplacement(separator));

        return lowercase ? slug.toLowerCase(Locale.ROOT) : slug;
    }
}
