package no.nav.veilarbperson.controller

import graphql.language.BooleanValue
import graphql.language.Directive
import graphql.language.Field
import graphql.language.FragmentSpread
import graphql.language.InlineFragment
import graphql.language.SelectionSet
import graphql.language.VariableReference
import graphql.schema.DataFetchingEnvironment
import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.domain.visittkort.VisittkortEtiketter
import no.nav.veilarbperson.service.AuthService
import no.nav.veilarbperson.service.VisittkortEtiketterService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.server.ResponseStatusException

@Controller
class VisittkortEtiketterGraphQLController(
    private val service: VisittkortEtiketterService,
    private val authService: AuthService
) {
    @QueryMapping
    fun visittkortEtiketter(
        @Argument fnr: String,
        @Argument behandlingsnummer: String?,
        environment: DataFetchingEnvironment
    ): VisittkortEtiketter {
        if (!authService.erInternBruker()) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        if (!fnr.matches(Regex("[0-9]{11}"))) throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        if (behandlingsnummer != null && !behandlingsnummer.matches(Regex("B[0-9]{1,10}"))) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        val oppslag = aktiveRotfelter(environment.operationDefinition.selectionSet, environment)
            .filter { it.name == "visittkortEtiketter" }
            .map { it.alias ?: it.name }
            .toSet()
        if (oppslag.size > 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        val felter = environment.selectionSet.immediateFields.map { it.name }.toSet()
        if (felter.any { it in VisittkortEtiketterService.PDL_FELTER } && behandlingsnummer == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        val person = try {
            Fnr.of(fnr)
        } catch (_: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        authService.sjekkLesetilgang(person)
        return service.hent(person, behandlingsnummer, felter)
    }

    private fun aktiveRotfelter(
        selectionSet: SelectionSet,
        environment: DataFetchingEnvironment,
        besokteFragmenter: MutableSet<String> = mutableSetOf()
    ): List<Field> = selectionSet.selections.flatMap { selection ->
        when (selection) {
            is Field -> if (inkludert(selection.directives, environment)) listOf(selection) else emptyList()
            is InlineFragment -> if (inkludert(selection.directives, environment)) {
                aktiveRotfelter(selection.selectionSet, environment, besokteFragmenter)
            } else emptyList()
            is FragmentSpread -> {
                if (!inkludert(selection.directives, environment) || !besokteFragmenter.add(selection.name)) emptyList()
                else {
                    val fragment = environment.fragmentsByName[selection.name]
                        ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                    aktiveRotfelter(fragment.selectionSet, environment, besokteFragmenter)
                }
            }
            else -> emptyList()
        }
    }

    private fun inkludert(directives: List<Directive>, environment: DataFetchingEnvironment): Boolean =
        directives.all { directive ->
            if (directive.name !in setOf("skip", "include")) true
            else {
                val value = when (val argument = directive.getArgument("if")?.value) {
                    is BooleanValue -> argument.isValue
                    is VariableReference -> environment.variables[argument.name] as? Boolean
                        ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                    else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                }
                if (directive.name == "skip") !value else value
            }
        }
}
