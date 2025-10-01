package com.capacidad.identityservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(

        info = @Info(
                title = "identity service api",
                description = "provides access and validates our app's users",
                termsOfService = "https://rionegro.gov.ar/terminos-y-condiciones",
                version = "2.0",
                contact = @Contact(
                        name = "ipross rio negro",
                        url = "https://ipross.rionegro.gov.ar/",
                        email = "aantonelli@ipross.rionegro.gov.ar\n" +
                                "awalter@ipross.rionegro.gov.ar"
                )
        ),
        servers = {
                @Server(
                        description = "DEV SERVER",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "PROD SERVER",
                        url = "ingresar url de produccion aws"


                )
        }
)
@SecurityScheme(type = SecuritySchemeType.DEFAULT)
public class SwaggerConfig {
}
