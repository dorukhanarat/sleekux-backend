package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveDiacriticsTransformationTest {

    private RemoveDiacriticsTransformation removeDiacriticsTransformation;

    @BeforeEach
    void setUp() {
        removeDiacriticsTransformation = new RemoveDiacriticsTransformation();
    }

    // Has key "remove_diacritics" and display name "Remove Accents"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("remove_diacritics", removeDiacriticsTransformation.getKey());
        assertEquals("Remove Accents", removeDiacriticsTransformation.getDisplayName());
    }

    // Has no parameters
    @Test
    void hasNoParameters() {
        assertTrue(removeDiacriticsTransformation.getParameters().isEmpty());
    }

    // Removes Turkish accents, including dotless ı and dotted İ
    @Test
    void removesTurkishAccents() {
        assertEquals("Cagri Sukru Istanbul Isik igne", removeDiacriticsTransformation.apply("Çağrı Şükrü İstanbul Işık iğne", Map.of()));
        assertEquals("CGIOSU cgiosu", removeDiacriticsTransformation.apply("ÇĞİÖŞÜ çğıöşü", Map.of()));
    }

    // Removes accents used in other European languages
    @Test
    void removesEuropeanAccents() {
        assertEquals("Creme brulee, naive, Aangstrom, nino, Ceske", removeDiacriticsTransformation.apply("Crème brûlée, naïve, Åångström, niño, České", Map.of()));
    }

    // Replaces letters that Unicode does not split into letter and accent
    @Test
    void replacesSpecialLetters() {
        assertEquals("Lodz Oslo Strasse AEsir OEuvre Dord",removeDiacriticsTransformation.apply("Łódź Øslo Straße Æsir Œuvre Đorđ", Map.of()));
    }

    // Works the same on text that is already decomposed into letter and combining accent
    @Test
    void handlesDecomposedInput() {
        String decomposed = Normalizer.normalize("Çağrı", Normalizer.Form.NFD);

        assertEquals("Cagri", removeDiacriticsTransformation.apply(decomposed, Map.of()));
    }

    // Leaves plain ASCII, numbers, symbols and emoji unchanged
    @Test
    void keepsOtherCharacters() {
        assertEquals("Hello 123 !?#$ 😀", removeDiacriticsTransformation.apply("Hello 123 !?#$ 😀", Map.of()));
    }

    // Leaves scripts that are not Latin-based unchanged, such as Korean, Japanese and Hindi
    @Test
    void keepsNonLatinScripts() {
        assertEquals("한국어 日本語 हिन्दी", removeDiacriticsTransformation.apply("한국어 日本語 हिन्दी", Map.of()));
    }

    // Ignores the parameter map, even when it is null
    @Test
    void ignoresParameters() {
        assertEquals("cay", removeDiacriticsTransformation.apply("çay", null));
    }

    // Returns an empty string for null or empty input
    @Test
    void returnsEmptyStringForNullOrEmptyInput() {
        assertEquals("", removeDiacriticsTransformation.apply(null, Map.of()));
        assertEquals("", removeDiacriticsTransformation.apply("", Map.of()));
    }
}
