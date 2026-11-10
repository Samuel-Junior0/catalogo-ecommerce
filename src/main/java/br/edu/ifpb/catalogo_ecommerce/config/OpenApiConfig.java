package br.edu.ifpb.catalogo_ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Catálogo E-commerce API")
                        .description("API do mini catálogo de e-commerce com Spring Boot e Cloud Firestore.")
                        .version("0.0.1-SNAPSHOT"));
    }
}
