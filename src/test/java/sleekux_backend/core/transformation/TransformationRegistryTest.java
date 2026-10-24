package sleekux_backend.core.transformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.transformation.impl.RegexMatchTransformation;
import sleekux_backend.core.transformation.impl.ReplaceTransformation;
import sleekux_backend.core.transformation.impl.TrimTransformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TransformationRegistryTest {

    private static class StubTransformation extends AbstractTransformation {
        StubTransformation(String key) {
            super(key, "Stub " + key, List.of());
        }

        @Override
        public String apply(String input, Map<String, Object> parameters) {
            return input;
        }
    }

    private TransformationRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TransformationRegistry();
    }

    // Default registry contains all built-in transformations in order
    @Test
    void defaultRegistryContainsBuiltIns() {
        List<String> keys = registry.getAllAsList().stream().map(Transformation::getKey).toList();

        assertEquals(List.of("replace", "append", "prepend", "substring", "regex_match", "uppercase", "lowercase", "trim",
                "regex_replace", "case_convert", "reverse", "pad", "repeat", "normalize_whitespace",
                "sort_lines", "dedupe_lines", "filter_lines", "number_lines", "split_join", "wrap_lines"), keys);
    }

    // get returns the transformation registered under the key
    @Test
    void getReturnsTransformationByKey() {
        assertInstanceOf(ReplaceTransformation.class, registry.get("replace"));
        assertInstanceOf(RegexMatchTransformation.class, registry.get("regex_match"));
        assertInstanceOf(TrimTransformation.class, registry.get("trim"));
    }

    // get ignores the case of the key
    @Test
    void getIsCaseInsensitive() {
        assertInstanceOf(ReplaceTransformation.class, registry.get("REPLACE"));
        assertInstanceOf(RegexMatchTransformation.class, registry.get("Regex_Match"));
    }

    // get returns null for an unknown or null key
    @Test
    void getReturnsNullForUnknownKey() {
        assertNull(registry.get("does_not_exist"));
        assertNull(registry.get(null));
    }

    // Registering a key with uppercase letters stores it in lowercase
    @Test
    void registerStoresKeyInLowercase() {
        registry.register(new StubTransformation("Custom"));

        assertNotNull(registry.get("custom"));
        assertNotNull(registry.get("Custom"));
        assertEquals("Custom", registry.get("custom").getKey());
    }

    // Registering a transformation with an existing key replaces the old one
    @Test
    void registerReplacesExistingKey() {
        StubTransformation replacement = new StubTransformation("replace");

        registry.register(replacement);

        assertSame(replacement, registry.get("replace"));
        assertEquals(20, registry.getAll().size());
    }

    // Registering a null transformation or one with a null key is ignored
    @Test
    void registerIgnoresNulls() {
        registry.register(null);
        registry.register(new StubTransformation(null));

        assertEquals(20, registry.getAll().size());
    }

    // A registry built from a list contains only those transformations
    @Test
    void customListConstructor() {
        TransformationRegistry custom = new TransformationRegistry(List.of(new StubTransformation("a"), new StubTransformation("b")));

        assertEquals(2, custom.getAll().size());
        assertNotNull(custom.get("a"));
        assertNotNull(custom.get("b"));
        assertNull(custom.get("replace"));
    }

    // A registry built from a null or empty list is empty
    @Test
    void nullOrEmptyListConstructor() {
        assertTrue(new TransformationRegistry(null).getAll().isEmpty());
        assertTrue(new TransformationRegistry(new ArrayList<>()).getAll().isEmpty());
    }

    // getAll returns a collection that cannot be modified
    @Test
    void getAllIsUnmodifiable() {
        Collection<Transformation> all = registry.getAll();

        assertThrows(UnsupportedOperationException.class, () -> all.add(new StubTransformation("x")));
        assertThrows(UnsupportedOperationException.class, all::clear);
    }

    // getAll reflects transformations registered later
    @Test
    void getAllIsLiveView() {
        Collection<Transformation> all = registry.getAll();

        registry.register(new StubTransformation("later"));

        assertEquals(21, all.size());
    }

    // getAllAsList returns an immutable snapshot
    @Test
    void getAllAsListIsSnapshot() {
        List<Transformation> list = registry.getAllAsList();

        registry.register(new StubTransformation("later"));

        assertEquals(20, list.size());
        assertThrows(UnsupportedOperationException.class, () -> list.add(new StubTransformation("x")));
    }
}
