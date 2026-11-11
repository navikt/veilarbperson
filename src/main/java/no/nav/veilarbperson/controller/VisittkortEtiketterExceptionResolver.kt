package no.nav.veilarbperson.controller

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import no.nav.veilarbperson.client.VisittkortKildeIkkeKonfigurertException
import no.nav.veilarbperson.client.VisittkortKildeHttpException
import no.nav.veilarbperson.client.VisittkortKildeKontraktException
import org.slf4j.LoggerFactory
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.graphql.execution.ErrorType
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class VisittkortEtiketterExceptionResolver : DataFetcherExceptionResolverAdapter() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        if (env.field.name != "visittkortEtiketter") return null
        val status = when (ex) {
            is ResponseStatusException -> ex.statusCode.value()
            is VisittkortKildeHttpException -> ex.statusCode.value()
            else -> null
        }
        val (type, code) = when {
            status == 400 ->
                ErrorType.BAD_REQUEST to "UGYLDIG_FORESPORSEL"
            status == 401 ->
                ErrorType.UNAUTHORIZED to "IKKE_AUTENTISERT"
            status == 403 ->
                ErrorType.FORBIDDEN to "INGEN_TILGANG"
            status == 404 ->
                ErrorType.NOT_FOUND to "PERSON_IKKE_FUNNET"
            ex is VisittkortKildeKontraktException ->
                ErrorType.INTERNAL_ERROR to "UGYLDIG_KILDESVAR"
            ex is VisittkortKildeIkkeKonfigurertException ->
                ErrorType.INTERNAL_ERROR to "KILDE_IKKE_KONFIGURERT"
            else -> ErrorType.INTERNAL_ERROR to "INTERN_FEIL"
        }
        if (type == ErrorType.INTERNAL_ERROR) {
            log.error("Visittkort source processing failed: {}", ex.javaClass.simpleName)
        }
        return GraphqlErrorBuilder.newError(env)
            .errorType(type)
            .message(code)
            .extensions(mapOf("code" to code))
            .build()
    }
}
