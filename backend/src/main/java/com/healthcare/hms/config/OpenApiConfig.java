package com.healthcare.hms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hmsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Healthcare Management System API")
                        .description("Enterprise-grade HMS backend APIs")
                        .version("v1")
                        .contact(new Contact()
                                .name("HMS Engineering")
                                .email("engineering@hms.local"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://hms.local/license")));
    }
}
