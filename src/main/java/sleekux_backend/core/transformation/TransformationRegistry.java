package sleekux_backend.core.transformation;

import sleekux_backend.core.transformation.impl.*;

import java.util.*;

public class TransformationRegistry {

    private final Map<String, Transformation> transformations = new LinkedHashMap<>();

    public TransformationRegistry() {
        this(List.of(
                new AppendTransformation(),
                new CaseConvertTransformation(),
                new DedupeLinesTransformation(),
                new FilterLinesTransformation(),
                new LowercaseTransformation(),
                new NormalizeWhitespaceTransformation(),
                new NumberLinesTransformation(),
                new PadTransformation(),
                new PrependTransformation(),
                new RegexMatchTransformation(),
                new RegexReplaceTransformation(),
                new RepeatTransformation(),
                new ReplaceTransformation(),
                new ReverseTransformation(),
                new SortLinesTransformation(),
                new SplitJoinTransformation(),
                new SubstringTransformation(),
                new TrimTransformation(),
                new UppercaseTransformation(),
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
