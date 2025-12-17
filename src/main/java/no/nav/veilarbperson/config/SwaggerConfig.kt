package no.nav.veilarbperson.config

import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import org.springframework.beans.factory.annotation.Value
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.models.Components
import org.springframework.web.method.HandlerMethod

@Configuration
class SwaggerConfig(
    @Value("\${AUTHORIZATION_URL}")
    val authorizationUrl: String,
    @Value("\${AZUREAD_TOKEN_ENDPOINT_URL}")
    val tokenUrl: String,
    @Value("\${API_SCOPE}")
    val apiScope: String,
) {


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
            .info(Info().title("veilarbperson").version("v1"))
            .components(
                Components()
                    .addSecuritySchemes("oauth2", oauth2SecurityScheme())
                    .addSecuritySchemes("bearer", bearerTokenSecurityScheme()),
            )
            // By adding them as separate items in the list, they become alternatives in Swagger UI
            .addSecurityItem(SecurityRequirement().addList("oauth2"))
            .addSecurityItem(SecurityRequirement().addList("bearer"))
    }

    private fun oauth2SecurityScheme(): SecurityScheme {
        return SecurityScheme().apply {
            type = SecurityScheme.Type.OAUTH2
            flows = OAuthFlows().apply {
                authorizationCode = OAuthFlow().apply {
                    authorizationUrl = this@SwaggerConfig.authorizationUrl
                    tokenUrl = this@SwaggerConfig.tokenUrl
                    scopes = Scopes().addString(apiScope, "Access API")
                }
            }
        }
    }

    private fun bearerTokenSecurityScheme(): SecurityScheme {
        return SecurityScheme().apply {
            type = SecurityScheme.Type.HTTP
            scheme = "bearer"
            bearerFormat = "JWT"
        }
    }
}