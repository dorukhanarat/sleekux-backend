package sleekux_backend.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sleekux_backend.core.model.PipelineStep;
import sleekux_backend.core.transformation.AbstractTransformation;
import sleekux_backend.core.transformation.TransformationRegistry;
import sleekux_backend.core.transformation.pipeline.TransformationPipeline;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CoreConfigTest {

    // The registry bean contains all built-in transformations
    @Test
    void providesDefaultRegistry() {
        TransformationRegistry registry = new CoreConfig().transformationRegistry();

        assertEquals(20, registry.getAll().size());
    }

    // The pipeline bean uses the registry bean from the same context
    @Test
    void pipelineUsesRegistryBean() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CoreConfig.class)) {
            TransformationRegistry registry = context.getBean(TransformationRegistry.class);
            TransformationPipeline pipeline = context.getBean(TransformationPipeline.class);

            registry.register(new AbstractTransformation("shout", "Shout", List.of()) {
                @Override
                public String apply(String input, Map<String, Object> parameters) {
                    return input + "!!!";
                }
            });

            assertEquals("abc!!!", pipeline.execute("abc", List.of(new PipelineStep("shout", null))).getFinalResult());
        }
    }

    // A CORS configurer bean is provided
    @Test
    void providesCorsConfigurer() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CoreConfig.class)) {
            assertNotNull(context.getBean(WebMvcConfigurer.class));
        }
    }
}
