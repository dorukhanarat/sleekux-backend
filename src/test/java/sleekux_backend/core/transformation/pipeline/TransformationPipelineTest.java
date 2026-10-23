package sleekux_backend.core.transformation.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.PipelineResult;
import sleekux_backend.core.model.PipelineStep;
import sleekux_backend.core.model.StepExecutionResult;
import sleekux_backend.core.transformation.AbstractTransformation;
import sleekux_backend.core.transformation.TransformationRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TransformationPipelineTest {

    private static class RecordingTransformation extends AbstractTransformation {
        private final List<String> inputs = new ArrayList<>();
        private final List<Map<String, Object>> parameters = new ArrayList<>();

        RecordingTransformation() {
            super("record", "Record", List.of());
        }

        @Override
        public String apply(String input, Map<String, Object> parameters) {
            this.inputs.add(input);
            this.parameters.add(parameters);
            return input + "#";
        }
    }

    private TransformationPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new TransformationPipeline(new TransformationRegistry());
    }

    private TransformationPipeline pipelineWith(RecordingTransformation recorder) {
        return new TransformationPipeline(new TransformationRegistry(List.of(recorder)));
    }

    // Creating a pipeline without a registry throws an exception
    @Test
    void rejectsNullRegistry() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> new TransformationPipeline(null));

        assertEquals("TransformationRegistry cannot be null", e.getMessage());
    }

    // With no steps, the final result is the input and the history is empty
    @Test
    void noStepsReturnsInputUnchanged() {
        for (List<PipelineStep> steps : Arrays.asList(List.<PipelineStep>of(), null)) {
            PipelineResult result = pipeline.execute("hello", steps);

            assertEquals("hello", result.getOriginalText());
            assertEquals("hello", result.getFinalResult());
            assertTrue(result.getHistory().isEmpty());
        }
    }

    // Null input is kept as the original text but becomes an empty string for the final result
    @Test
    void nullInputWithNoSteps() {
        PipelineResult result = pipeline.execute(null, List.of());

        assertNull(result.getOriginalText());
        assertEquals("", result.getFinalResult());
    }

    // A single step applies its transformation and records one history entry
    @Test
    void runsSingleStep() {
        PipelineResult result = pipeline.execute("hello", List.of(new PipelineStep("uppercase", null)));

        assertEquals("hello", result.getOriginalText());
        assertEquals("HELLO", result.getFinalResult());
        assertEquals(1, result.getHistory().size());

        StepExecutionResult step = result.getHistory().get(0);
        assertEquals(1, step.getStepIndex());
        assertEquals("uppercase", step.getTransformationKey());
        assertEquals("To Uppercase", step.getTransformationName());
        assertEquals(Map.of(), step.getParameters());
        assertEquals("hello", step.getInputSnapshot());
        assertEquals("HELLO", step.getOutputSnapshot());
    }

    // Each step receives the previous step's output, and the history records every step in order
    @Test
    void chainsStepsInOrder() {
        List<PipelineStep> steps = List.of(
                new PipelineStep("trim", null),
                new PipelineStep("uppercase", null),
                new PipelineStep("append", Map.of("suffix", "!"))
        );

        PipelineResult result = pipeline.execute("  hello  ", steps);

        assertEquals("HELLO!", result.getFinalResult());
        assertEquals(List.of(
                new StepExecutionResult(1, "trim", "Trim Whitespace", Map.of(), "  hello  ", "hello", 0),
                new StepExecutionResult(2, "uppercase", "To Uppercase", Map.of(), "hello", "HELLO", 0),
                new StepExecutionResult(3, "append", "Append Text", Map.of("suffix", "!"), "HELLO", "HELLO!", 0)
        ), result.getHistory());
    }

    // Step numbers start at 1 and increase by one for each step
    @Test
    void stepIndexesStartAtOne() {
        PipelineStep upper = new PipelineStep("uppercase", null);

        PipelineResult result = pipeline.execute("x", List.of(upper, upper, upper));

        assertEquals(List.of(1, 2, 3), result.getHistory().stream().map(StepExecutionResult::getStepIndex).toList());
    }

    // Execution time is recorded for every step and is never negative
    @Test
    void recordsNonNegativeExecutionTime() {
        PipelineResult result = pipeline.execute("hello", List.of(
                new PipelineStep("uppercase", null),
                new PipelineStep("lowercase", null)
        ));

        result.getHistory().forEach(step -> assertTrue(step.getExecutionTime() >= 0));
    }

    // The order of the steps changes the result
    @Test
    void stepOrderMatters() {
        PipelineStep replace = new PipelineStep("replace", Map.of("oldValue", "a", "newValue", "b"));
        PipelineStep uppercase = new PipelineStep("uppercase", null);

        assertEquals("BA", pipeline.execute("aA", List.of(replace, uppercase)).getFinalResult());
        assertEquals("AA", pipeline.execute("aA", List.of(uppercase, replace)).getFinalResult());
    }

    // The same transformation can be used in more than one step
    @Test
    void sameTransformationCanRepeat() {
        PipelineStep exclaim = new PipelineStep("append", Map.of("suffix", "!"));

        PipelineResult result = pipeline.execute("hi", List.of(exclaim, exclaim, exclaim));

        assertEquals("hi!!!", result.getFinalResult());
        assertEquals(List.of("hi", "hi!", "hi!!"), result.getHistory().stream().map(StepExecutionResult::getInputSnapshot).toList());
    }

    // Transformation keys in steps are matched regardless of case, and the history uses the registered key
    @Test
    void stepKeysAreCaseInsensitive() {
        PipelineResult result = pipeline.execute("hello", List.of(new PipelineStep("UpperCase", null)));

        assertEquals("HELLO", result.getFinalResult());
        assertEquals("uppercase", result.getHistory().get(0).getTransformationKey());
    }

    // An unknown transformation key throws an exception naming the key
    @Test
    void unknownKeyThrows() {
        List<PipelineStep> steps = List.of(new PipelineStep("does_not_exist", null));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> pipeline.execute("hello", steps));

        assertEquals("Unknown transformation key: does_not_exist", e.getMessage());
    }

    // An unknown key later in the pipeline stops execution before later steps run
    @Test
    void unknownKeyStopsExecution() {
        RecordingTransformation recorder = new RecordingTransformation();
        List<PipelineStep> steps = List.of(
                new PipelineStep("record", null),
                new PipelineStep("missing", null),
                new PipelineStep("record", null)
        );

        assertThrows(IllegalArgumentException.class, () -> pipelineWith(recorder).execute("x", steps));
        assertEquals(List.of("x"), recorder.inputs);
    }

    // A null step throws an exception naming its position, and later steps do not run
    @Test
    void nullStepThrows() {
        RecordingTransformation recorder = new RecordingTransformation();
        List<PipelineStep> steps = Arrays.asList(
                new PipelineStep("record", null),
                null,
                new PipelineStep("record", null)
        );

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> pipelineWith(recorder).execute("x", steps));

        assertEquals("Step 2 is null", e.getMessage());
        assertEquals(List.of("x"), recorder.inputs);
    }

    // Null input is passed to the first step as an empty string
    @Test
    void nullInputIsPassedAsEmptyString() {
        RecordingTransformation recorder = new RecordingTransformation();

        PipelineResult result = pipelineWith(recorder).execute(null, List.of(new PipelineStep("record", null)));

        assertEquals(List.of(""), recorder.inputs);
        assertNull(result.getOriginalText());
        assertEquals("", result.getHistory().get(0).getInputSnapshot());
        assertEquals("#", result.getFinalResult());
    }

    // Each step's parameters are passed to its transformation, and a step without parameters gets an empty map
    @Test
    void passesStepParametersToTransformation() {
        RecordingTransformation recorder = new RecordingTransformation();

        pipelineWith(recorder).execute("x", List.of(
                new PipelineStep("record", Map.of("a", 1)),
                new PipelineStep("record", null)
        ));

        assertEquals(List.of(Map.of("a", 1), Map.of()), recorder.parameters);
    }

    // Uses only the transformations in the registry it was given
    @Test
    void usesProvidedRegistry() {
        TransformationPipeline custom = pipelineWith(new RecordingTransformation());

        assertThrows(IllegalArgumentException.class, () -> custom.execute("x", List.of(new PipelineStep("uppercase", null))));
    }

    // The returned history cannot be modified
    @Test
    void historyIsUnmodifiable() {
        PipelineResult result = pipeline.execute("x", List.of(new PipelineStep("uppercase", null)));

        assertThrows(UnsupportedOperationException.class, () -> result.getHistory().clear());
    }
}
