package sleekux_backend.core.transformation;

import sleekux_backend.core.model.ParameterDefinition;

import java.util.List;
import java.util.Map;

public interface Transformation {

    String getKey();

    String getDisplayName();

    List<ParameterDefinition> getParameters();

    String apply(String input, Map<String, Object> parameters);

}
