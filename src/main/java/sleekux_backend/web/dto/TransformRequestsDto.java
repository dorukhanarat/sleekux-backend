package sleekux_backend.web.dto;

import java.util.List;

public class TransformRequestsDto {

    private String text;
    private List<StepDto> steps;

    public TransformRequestsDto() {
    }

    public TransformRequestsDto(String text, List<StepDto> steps) {
        this.text = text;
        this.steps = steps;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<StepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<StepDto> steps) {
        this.steps = steps;
    }
}
