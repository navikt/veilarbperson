package no.nav.veilarbperson.client.pdl.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum VergemaalEllerFullmaktTjenesteoppgaveType {
    @JsonAlias("familie")
    FAMILIE,
    @JsonAlias("arbeid")
    ARBEID,
    @JsonAlias("hjelpemidler")
    HJELPEMIDLER,
    @JsonAlias("pensjon")
    PENSJON,
    @JsonAlias("sosialeTjenester")
    SOSIALE_TJENESTER,
}

