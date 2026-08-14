package com.almahir.iti.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${app.server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        Server prodServer = new Server()
                .url("https://almahir-production.up.railway.app")
                .description("Production Server (HTTPS)");
        Server newServer = new Server()
                .url("https://almahir-production-6f98.up.railway.app")
                .description("Production Server (HTTPS) (NEW)");
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Server (HTTP)");

        return new OpenAPI()
                .info(new Info()
                        .title("Al-Mahir ITI API")
                        .version("1.0.0")
                        .description("Backend API documentation for Al-Mahir App"))
                .servers(List.of(prodServer, localServer))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuthentication"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuthentication", new SecurityScheme()
                                .name("BearerAuthentication")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}