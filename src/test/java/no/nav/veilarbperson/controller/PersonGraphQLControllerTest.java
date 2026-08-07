package no.nav.veilarbperson.controller;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.digdir.DigdirClient;
import no.nav.veilarbperson.client.digdir.DigdirKontaktinfo;
import no.nav.veilarbperson.client.digdir.KRRPostPersonerRequest;
import no.nav.veilarbperson.client.digdir.KRRPostPersonerResponse;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.domain.PdlRequest;
import no.nav.veilarbperson.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(PersonGraphQLController.class)
class PersonGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private PdlClient pdlClient;

    @MockitoBean
    private SkjermetClient skjermetClient;

    @MockitoBean
    private DigdirClient digdirClient;

    @MockitoBean
    private AuthService authService;

    @Test
    void henter_navn_og_fodselsdato() {
        when(pdlClient.hentPerson(any(PdlRequest.class))).thenReturn(lagTestPerson());
        when(skjermetClient.hentSkjermet(any(Fnr.class))).thenReturn(false);
        when(digdirClient.hentKontaktInfo(any(KRRPostPersonerRequest.class))).thenReturn(null);

        graphQlTester.document("""
                query {
                    person(fnr: "%s", behandlingsnummer: "B643") {
                        fornavn
                        etternavn
                        fodselsdato
                        egenAnsatt
                    }
                }
                """.formatted(TEST_FNR))
                .execute()
                .path("person.fornavn").entity(String.class).isEqualTo("Kari")
                .path("person.etternavn").entity(String.class).isEqualTo("Nordmann")
                .path("person.fodselsdato").entity(String.class).isEqualTo("1980-01-15")
                .path("person.egenAnsatt").entity(Boolean.class).isEqualTo(false);
    }

    @Test
    void skjermet_person_returnerer_egenAnsatt_true() {
        when(pdlClient.hentPerson(any(PdlRequest.class))).thenReturn(lagTestPerson());
        when(skjermetClient.hentSkjermet(any(Fnr.class))).thenReturn(true);
        when(digdirClient.hentKontaktInfo(any(KRRPostPersonerRequest.class))).thenReturn(null);

        graphQlTester.document("""
                query {
                    person(fnr: "%s", behandlingsnummer: "B643") {
                        egenAnsatt
                    }
                }
                """.formatted(TEST_FNR))
                .execute()
                .path("person.egenAnsatt").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    void returnerer_null_felt_uten_feil_naar_pdl_mangler_data() {
        HentPerson.Person tomPerson = new HentPerson.Person();
        tomPerson.setNavn(List.of());
        tomPerson.setFoedselsdato(List.of());
        tomPerson.setDoedsfall(List.of());
        tomPerson.setKjoenn(List.of());
        tomPerson.setAdressebeskyttelse(List.of());
        tomPerson.setSikkerhetstiltak(List.of());
        tomPerson.setTelefonnummer(List.of());
        when(pdlClient.hentPerson(any(PdlRequest.class))).thenReturn(tomPerson);
        when(skjermetClient.hentSkjermet(any(Fnr.class))).thenReturn(false);
        when(digdirClient.hentKontaktInfo(any(KRRPostPersonerRequest.class))).thenReturn(null);

        graphQlTester.document("""
                query {
                    person(fnr: "%s", behandlingsnummer: "B643") {
                        fornavn
                        egenAnsatt
                    }
                }
                """.formatted(TEST_FNR))
                .execute()
                .path("person.fornavn").valueIsNull()
                .path("person.egenAnsatt").entity(Boolean.class).isEqualTo(false);
    }

    @Test
    void krr_telefon_faar_prioritet_1_og_pdl_telefon_bumpes() {
        var pdlPersonMedTelefon = lagTestPerson();
        var pdlMetadata = new HentPerson.Metadata();
        pdlMetadata.setMaster("PDL");
        pdlMetadata.setEndringer(List.of());
        var pdlTelefon = new HentPerson.Telefonnummer();
        pdlTelefon.setNummer("11111111");
        pdlTelefon.setPrioritet("1");
        pdlTelefon.setMetadata(pdlMetadata);
        pdlPersonMedTelefon.setTelefonnummer(List.of(pdlTelefon));

        var krrInfo = new DigdirKontaktinfo(
                TEST_FNR.get(), true, true, null, false,
                null, null, null, null, null,
                "99999999", "2024-01-15T12:00:00+01:00", null);
        var krrResponse = new KRRPostPersonerResponse(Map.of(TEST_FNR.get(), krrInfo), null);

        when(pdlClient.hentPerson(any(PdlRequest.class))).thenReturn(pdlPersonMedTelefon);
        when(skjermetClient.hentSkjermet(any(Fnr.class))).thenReturn(false);
        when(digdirClient.hentKontaktInfo(any(KRRPostPersonerRequest.class))).thenReturn(krrResponse);

        graphQlTester.document("""
                query {
                    person(fnr: "%s", behandlingsnummer: "B643") {
                        telefon { telefonNr prioritet master }
                    }
                }
                """.formatted(TEST_FNR))
                .execute()
                .path("person.telefon[0].telefonNr").entity(String.class).isEqualTo("99999999")
                .path("person.telefon[0].prioritet").entity(String.class).isEqualTo("1")
                .path("person.telefon[0].master").entity(String.class).isEqualTo("KRR")
                .path("person.telefon[1].telefonNr").entity(String.class).isEqualTo("11111111")
                .path("person.telefon[1].prioritet").entity(String.class).isEqualTo("2");
    }

    // 🔴 RØD SONE — skriv disse testene selv etter at du har implementert auth-blokkene:
    //
    // @Test
    // void blokkerer_ekstern_bruker() {
    //     // TODO: Verifiser at authService.stoppHvisEksternBruker() kastes og gir GraphQL-feil
    // }
    //
    // @Test
    // void blokkerer_veileder_uten_lesetilgang() {
    //     // TODO: Verifiser at authService.sjekkLesetilgang() ved 403 gir riktig GraphQL-feil
    // }
    //
    // @Test
    // void gir_BAD_USER_INPUT_ved_ugyldig_fnr() {
    //     // TODO: Verifiser at ugyldig fnr gir extensions.code = BAD_USER_INPUT, ikke INTERNAL_ERROR
    // }

    private static HentPerson.Person lagTestPerson() {
        var navn = new HentPerson.Navn();
        navn.setFornavn("Kari");
        navn.setEtternavn("Nordmann");
        var metadata = new HentPerson.MetadataNavn();
        metadata.setMaster(HentPerson.PdlNavnMaster.PDL);
        navn.setMetadata(metadata);

        var foedselsdato = new HentPerson.Foedselsdato();
        foedselsdato.setFoedselsdato(LocalDate.of(1980, 1, 15));

        var person = new HentPerson.Person();
        person.setNavn(List.of(navn));
        person.setFoedselsdato(List.of(foedselsdato));
        person.setDoedsfall(List.of());
        person.setKjoenn(List.of());
        person.setAdressebeskyttelse(List.of());
        person.setSikkerhetstiltak(List.of());
        person.setTelefonnummer(List.of());
        return person;
    }
}
