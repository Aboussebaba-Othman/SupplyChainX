package com.othman.exemple.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SupplyChainX API")
                        .description("Complete Supply Chain Management System - From procurement to delivery")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SupplyChainX Team")
                                .email("support@supplychainx.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    @Bean
    public OperationCustomizer globalHeaders() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new Parameter()
                    .in("header")
                    .required(false)
                    .schema(new StringSchema())
                    .name("X-User-Email")
                    .description("User email for authentication"));

            operation.addParametersItem(new Parameter()
                    .in("header")
                    .required(false)
                    .schema(new StringSchema())
                    .name("X-User-Password")
                    .description("User password for authentication"));

            return operation;
        };
    }
}
