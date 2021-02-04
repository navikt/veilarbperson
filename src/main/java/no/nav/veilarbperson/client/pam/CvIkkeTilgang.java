package no.nav.veilarbperson.client.pam;

public enum CvIkkeTilgang {
    IKKE_TILGANG_TIL_BRUKER, // TODO: Skal kode 6 / 7 også sjekkes her?
    BRUKER_ER_DOED,
    IKKE_UNDER_OPPFOLGING,
    BRUKER_IKKE_GODKJENT_SAMTYKKE // if we get status 406
}
