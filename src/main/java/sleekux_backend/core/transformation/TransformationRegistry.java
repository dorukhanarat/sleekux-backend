package sleekux_backend.core.transformation;

import sleekux_backend.core.transformation.impl.*;

import java.util.*;

public class TransformationRegistry {

    private final Map<String, Transformation> transformations = new LinkedHashMap<>();

    public TransformationRegistry() {
        this(List.of(
                new ReplaceTransformation(),
                new AppendTransformation(),
                new PrependTransformation(),
                new SubstringTransformation(),
                new RegexMatchTransformation(),
                new UppercaseTransformation(),
                new LowercaseTransformation(),
                new TrimTransformation()
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
