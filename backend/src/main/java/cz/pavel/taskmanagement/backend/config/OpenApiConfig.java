package cz.pavel.taskmanagement.backend.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tasker API")
                        .version("1.0.0.")
                        .description("REST API for task management system - Portfolio project demonstrating Spring Boot, JPA and React integration")
                        .contact(new Contact()
                                .name("Pavel Kupcik")
                                .email("kupcik46@gmail.com")
                                .url("https://github.com//pk46")
                        )
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }
}
