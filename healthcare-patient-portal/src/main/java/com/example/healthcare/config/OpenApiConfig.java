package com.example.healthcare.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "sessionCookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "JSESSIONID",
        description = "Session cookie returned after a successful login")
public class OpenApiConfig {
    @Bean
    OpenAPI healthcareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Healthcare Patient Portal API")
                        .version("v1")
                        .description("API for patient accounts, profiles, and medical reports."));
    }
}
