package sleekux_backend.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sleekux_backend.core.model.PipelineResult;
import sleekux_backend.core.model.PipelineStep;
import sleekux_backend.core.transformation.Transformation;
import sleekux_backend.core.transformation.TransformationRegistry;
import sleekux_backend.core.transformation.pipeline.TransformationPipeline;
import sleekux_backend.web.dto.TransformRequestsDto;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TransformationController {

    private final TransformationRegistry transformationRegistry;
    private final TransformationPipeline transformationPipeline;

    public TransformationController(TransformationRegistry transformationRegistry, TransformationPipeline transformationPipeline) {
        this.transformationRegistry = transformationRegistry;
        this.transformationPipeline = transformationPipeline;
    }

    @GetMapping("/transformations")
    public ResponseEntity<Collection<Transformation>> getTransformations() {
        return ResponseEntity.ok(transformationRegistry.getAll());
    }

    @PostMapping("/transform")
    public ResponseEntity<PipelineResult> transform(@RequestBody TransformRequestsDto request) {
        String inputText = request.getText() != null ? request.getText() : "";

        List<PipelineStep> pipelineSteps = List.of();
        if(request.getSteps() != null) {
            pipelineSteps = request.getSteps().stream()
                    .map(step -> {
                        if(step == null) {
                            throw new IllegalArgumentException("steps cannot contain null entries");
                        }
                        return new PipelineStep(step.getTransformationKey(), step.getParameters());
                    })
                    .toList();
        }

        PipelineResult result = transformationPipeline.execute(inputText, pipelineSteps);
        return ResponseEntity.ok(result);

    }
}
