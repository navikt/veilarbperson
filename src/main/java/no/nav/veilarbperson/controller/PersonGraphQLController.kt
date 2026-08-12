package no.nav.veilarbperson.controller

import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.domain.PersonVisittkortData
import no.nav.veilarbperson.service.AuthService
import no.nav.veilarbperson.service.PersonVisittkortService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class PersonGraphQLController(
    private val personVisittkortService: PersonVisittkortService,
    private val authService: AuthService
) {
    @QueryMapping
    fun person(
        @Argument fnr: String,
        @Argument behandlingsnummer: String?
    ): PersonVisittkortData {
        val validertFnr = Fnr.of(fnr)
        authService.stoppHvisEksternBruker()
        authService.sjekkLesetilgang(validertFnr)

        return personVisittkortService.hentVisittkortData(validertFnr, behandlingsnummer)
    }
}

