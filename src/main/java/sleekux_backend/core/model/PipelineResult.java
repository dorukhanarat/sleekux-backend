package sleekux_backend.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PipelineResult {

    private final String originalText;
    private final String finalResult;
    private final List<StepExecutionResult> history;

    public PipelineResult(String originalText, String finalResult, List<StepExecutionResult> history) {
        this.originalText = originalText;
        this.finalResult = finalResult;
        this.history = history != null ? Collections.unmodifiableList(history) : Collections.emptyList();
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public List<StepExecutionResult> getHistory() {
        return history;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PipelineResult that = (PipelineResult) o;
        return Objects.equals(originalText, that.originalText) && Objects.equals(finalResult, that.finalResult) && Objects.equals(history, that.history);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalText, finalResult, history);
    }

    @Override
    public String toString() {
        return "PipelineResult{" +
                "originalText='" + originalText + '\'' +
                ", finalResult='" + finalResult + '\'' +
                ", history=" + history +
                '}';
    }
}
