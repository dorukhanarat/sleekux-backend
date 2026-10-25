package sleekux_backend.core.transformation;

import sleekux_backend.core.transformation.impl.*;

import java.util.*;

public class TransformationRegistry {

    private final Map<String, Transformation> transformations = new LinkedHashMap<>();

    public TransformationRegistry() {
        this(List.of(
                new AppendTransformation(),
                new Base64Transformation(),
                new CaseConvertTransformation(),
                new CountTransformation(),
                new CsvColumnTransformation(),
                new DedupeLinesTransformation(),
                new ExtractTransformation(),
                new FilterLinesTransformation(),
                new HashTransformation(),
                new HtmlEscapeTransformation(),
                new JsonEscapeTransformation(),
                new JsonFormatTransformation(),
                new JwtDecodeTransformation(),
                new LowercaseTransformation(),
                new MaskTransformation(),
                new NormalizeWhitespaceTransformation(),
                new NumberLinesTransformation(),
                new PadTransformation(),
                new PrependTransformation(),
                new RegexMatchTransformation(),
                new RegexReplaceTransformation(),
                new RemoveCharsTransformation(),
                new RemoveDiacriticsTransformation(),
                new RepeatTransformation(),
                new ReplaceTransformation(),
                new ReverseTransformation(),
                new ShuffleLinesTransformation(),
                new SlugifyTransformation(),
                new SortLinesTransformation(),
                new SplitJoinTransformation(),
                new StripHtmlTransformation(),
                new SubstringTransformation(),
                new SwapCaseTransformation(),
                new TakeLinesTransformation(),
                new TrimTransformation(),
                new TruncateTransformation(),
                new UnicodeNormalizeTransformation(),
                new UppercaseTransformation(),
                new UrlEncodeTransformation(),
                new WordWrapTransformation(),
                new WrapLinesTransformation()
        ));
    }

    public TransformationRegistry(List<Transformation> initialTransformations) {
        if (initialTransformations != null) {
            initialTransformations.forEach(this::register);
        }
    }

    public void register(Transformation transformation) {
        if(transformation != null && transformation.getKey() != null) {
            transformations.put(transformation.getKey().toLowerCase(Locale.ROOT), transformation);
        }
    }

    public Transformation get(String key) {
        if(key == null) {
            return null;
        }
        return transformations.get(key.toLowerCase(Locale.ROOT));
    }

    public Collection<Transformation> getAll() {
        return Collections.unmodifiableCollection(transformations.values());
    }

    public List<Transformation> getAllAsList() {
        return List.copyOf(transformations.values());
    }
}
