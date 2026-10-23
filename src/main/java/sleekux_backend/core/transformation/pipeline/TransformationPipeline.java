package sleekux_backend.core.transformation.pipeline;

import sleekux_backend.core.model.PipelineResult;
import sleekux_backend.core.model.PipelineStep;
import sleekux_backend.core.model.StepExecutionResult;
import sleekux_backend.core.transformation.Transformation;
import sleekux_backend.core.transformation.TransformationRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransformationPipeline {

    private final TransformationRegistry registry;

    public TransformationPipeline(TransformationRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "TransformationRegistry cannot be null");
    }

    public PipelineResult execute(String initialText, List<PipelineStep> steps) {
        String currentText = initialText != null ? initialText : "";
        List<StepExecutionResult> history = new ArrayList<>();

        if(steps == null || steps.isEmpty()) {
            return new PipelineResult(initialText, currentText, history);
        }

        for(int i = 0; i < steps.size(); i++) {
            PipelineStep step = steps.get(i);
            if(step == null) {
                throw new IllegalArgumentException("Step " + (i + 1) + " is null");
            }
            Transformation transformation = registry.get(step.getTransformationKey());

            if(transformation == null) {
                throw new IllegalArgumentException("Unknown transformation key: " + step.getTransformationKey());
            }

            long startTime = System.currentTimeMillis();
            String stepInput = currentText;
            String stepOutput = transformation.apply(stepInput, step.getParameters());
            long duration = System.currentTimeMillis() - startTime;

            history.add(new StepExecutionResult(
                    i + 1,
                    transformation.getKey(),
                    transformation.getDisplayName(),
                    step.getParameters(),
                    stepInput,
                    stepOutput,
                    duration
            ));

            currentText = stepOutput;
        }
        return new PipelineResult(initialText, currentText, history);
    }
}
