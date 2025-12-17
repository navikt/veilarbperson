package no.nav.veilarbperson.config

import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.models.Components
import org.springframework.web.method.HandlerMethod

@Configuration
class SwaggerConfig {


    /**
     * Lager en gruppering kalt "Eksterne endepunkter (for konsumenter)" i Swagger-doc.
     * Grupperingen inneholder alle endepunkter som er annotert med @Tag(name = "eksternt").
     */
    fun isEksterntEndpoint(handlerMethod: HandlerMethod): Boolean {
        val methodTags = handlerMethod.method.getAnnotationsByType(Tag::class.java).toList()
        val classTags = handlerMethod.beanType.getAnnotationsByType(Tag::class.java).toList()

        return (methodTags + classTags).any { it.name.equals("eksternt", ignoreCase = true) }
    }
    @Bean
    fun externalApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("Eksterne endepunkter (for konsumenter)")
            .addOperationCustomizer { operation, handlerMethod ->
                if (isEksterntEndpoint(handlerMethod)) {
                    operation
                } else {
                    null
                }
            }
            .addOpenApiCustomizer(removeEmptyTags())
            .build()
    }

    /**
     * Lager en gruppering kalt "Interne endepunkter" i Swagger-doc.
     * Grupperingen inneholder alle endepunkter.
     */
    @Bean
    fun internalApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("Interne endepunkter")
            .addOpenApiCustomizer(removeEmptyTags())
            .build()
    }

    /**
     * For å unngå at vi får tomme "seksjoner" i Swagger-doc, fjerner vi tags som ikke er i bruk.
     * Klasser annotert med [io.swagger.v3.oas.annotations.tags.Tag] vil få en egen seksjon i Swagger-doc.
     * Siden vi per dags dato blander eksterne/interne endepunkter i samme controller, må vi eksplisitt gjøre denne filtreringen.
     *
     * Denne trenger vi foreløpig kun å bruke i [externalApi], siden [internalApi] uansett inneholder alle endepunkter.
     */
    @Bean
    fun removeEmptyTags(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi: OpenAPI ->
            val usedTags = openApi.paths.values
                .flatMap { pathItem -> pathItem.readOperations().flatMap { it.tags } }
                .toSet()
            openApi.tags = openApi.tags?.filter { it.name in usedTags }
        }
    }
    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI()
            .security(listOf(SecurityRequirement().addList("bearer-key")))
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer-key", SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
    }
}