package sleekux_backend.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sleekux_backend.core.transformation.TransformationRegistry;
import sleekux_backend.core.transformation.pipeline.TransformationPipeline;

@Configuration
public class CoreConfig {

    @Bean
    public TransformationRegistry transformationRegistry() {
        return new TransformationRegistry();
    }

    @Bean
    public TransformationPipeline transformationPipeline(TransformationRegistry transformationRegistry) {
        return new TransformationPipeline(transformationRegistry);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry corsRegistry) {
                corsRegistry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173" , "http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
