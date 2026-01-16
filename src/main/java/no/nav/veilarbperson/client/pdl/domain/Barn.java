package no.nav.veilarbperson.client.pdl.domain;

public interface Barn {
    String getFornavn();
    String getGradering();
    Boolean getErEgenAnsatt();
    RelasjonsBosted getRelasjonsBosted();
}
