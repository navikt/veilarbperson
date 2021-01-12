package no.nav.veilarbperson.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.PersonV2Data;
import no.nav.veilarbperson.client.pdl.domain.Familiemedlem;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.Personinfo;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.domain.Enhet;
import no.nav.veilarbperson.domain.PersonData;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.PersonDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.veilarbperson.utils.Mappers.fraNorg2Enhet;

@Slf4j
@Service
public class PersonV2Service {

    private final PdlClient pdlClient;
    private final AuthService authService;
    private final DkifClient dkifClient;
    private final Norg2Client norg2Client;
    private final PersonClient personClient;
    private final PamClient pamClient;
    private final EgenAnsattClient egenAnsattClient;
    private final VeilarbportefoljeClient veilarbportefoljeClient;
    private final KodeverkService kodeverkService;

    @Autowired
    public PersonV2Service(PdlClient pdlClient, AuthService authService, DkifClient dkifClient, Norg2Client norg2Client, PersonClient personClient,
                           PamClient pamClient, EgenAnsattClient egenAnsattClient, VeilarbportefoljeClient veilarbportefoljeClient, KodeverkService kodeverkService) {
        this.pdlClient = pdlClient;
        this.authService = authService;
        this.dkifClient = dkifClient;
        this.norg2Client = norg2Client;
        this.personClient = personClient;
        this.pamClient = pamClient;
        this.egenAnsattClient = egenAnsattClient;
        this.veilarbportefoljeClient = veilarbportefoljeClient;
        this.kodeverkService = kodeverkService;
    }

    public HentPdlPerson.PdlPerson hentPerson(String personIdent) {
        return pdlClient.hentPerson(personIdent, authService.getInnloggetBrukerToken());
    }

    public PersonData hentPersonDataFraTps(String personIdent) {
        return PersonDataMapper.tilPersonData(personClient.hentPerson(Fnr.of(personIdent)));
    }

    public PersonV2Data hentFlettetPerson(String fodselsnummer, String userToken) throws Exception {
        PersonData personDataFraTps = hentPersonDataFraTps(fodselsnummer);
        HentPdlPerson.PdlPerson personDataFraPdl = Optional.of(pdlClient.hentPerson(fodselsnummer, userToken)).orElseThrow(() -> new Exception("Fant ikke person i hentPerson operasjonen i PDL"));
        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(personDataFraPdl, personDataFraTps);

        try {
            flettPersoninfoFraPortefolje(personV2Data, fodselsnummer);
        } catch (Exception e) {
            log.warn("Bruker fallbackløsning for egenAnsatt-sjekk", e);
            flettEgenAnsatt(fodselsnummer, personV2Data);
            flettSikkerhetstiltak(fodselsnummer, personV2Data);
        }

        flettBarnInformasjon(personDataFraPdl.getFamilierelasjoner(), personV2Data);
        flettPartnerInformasjon(personDataFraPdl.getSivilstand(), personV2Data, userToken);
        flettDigitalKontaktinformasjon(fodselsnummer, personV2Data);
        flettGeografiskEnhet(fodselsnummer, userToken, personV2Data);
        flettKodeverk(personV2Data);

        return personV2Data;
    }

    public List<Familiemedlem> hentOpplysningerTilBarna(String[] barnasFnrs) {
        List<HentPdlPerson.PdlPersonBolk> barnasaInformasjon = pdlClient.hentPersonBolk(barnasFnrs);

        return Optional.ofNullable(barnasaInformasjon)
                .stream()
                .flatMap(Collection::stream)
                .filter(barn -> barn.getCode().equals("ok"))
                .map(HentPdlPerson.PdlPersonBolk::getPerson)
                .map(PersonV2DataMapper::familiemedlemMapper)
                .collect(Collectors.toList());
    }

    public String[] hentFnrTilBarna(List<HentPdlPerson.Familierelasjoner> familierelasjoner) {

        return Optional.ofNullable(familierelasjoner)
                .stream()
                .flatMap(Collection::stream)
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getRelatertPersonsRolle()))
                .map(HentPdlPerson.Familierelasjoner::getRelatertPersonsIdent)
                .toArray(String[]::new);
    }

    private void flettBarnInformasjon(List<HentPdlPerson.Familierelasjoner> familierelasjoner, PersonV2Data personV2Data) {
        String[] barnasFnrListe = hentFnrTilBarna(familierelasjoner);
        List<Familiemedlem> barnasInformasjon = hentOpplysningerTilBarna(barnasFnrListe);
        personV2Data.setBarn(barnasInformasjon);
    }

    public String hentFnrTilPartner(List<HentPdlPerson.Sivilstand> personsSivilstand){
        return Optional.ofNullable(PersonV2DataMapper.getFirstElement(personsSivilstand))
                .map(HentPdlPerson.Sivilstand::getRelatertVedSivilstand)
                .orElse(null);
    }

    public void flettPartnerInformasjon(List<HentPdlPerson.Sivilstand> personsSivilstand, PersonV2Data personV2Data, String userToken) {
        String fnrTilPartner = hentFnrTilPartner(personsSivilstand);
        HentPdlPerson.PersonsFamiliemedlem partnerInformasjon =  pdlClient.hentPartnerOpplysninger(fnrTilPartner, userToken);
        personV2Data.setPartner(PersonV2DataMapper.familiemedlemMapper(partnerInformasjon));
    }

    private void flettPersoninfoFraPortefolje(PersonV2Data personData, String fodselsnummer) {
        Personinfo personinfo = veilarbportefoljeClient.hentPersonInfo(Fnr.of(fodselsnummer));
        personData.setSikkerhetstiltak(personinfo.sikkerhetstiltak);
        personData.setEgenAnsatt(personinfo.egenAnsatt);
    }

    private void flettEgenAnsatt(String fnr, PersonV2Data personV2Data) {
        personV2Data.setEgenAnsatt(egenAnsattClient.erEgenAnsatt(Fnr.of(fnr)));
    }

    private void flettSikkerhetstiltak(String fnr, PersonV2Data personV2Data) {
        try {
            String sikkerhetstiltak = personClient.hentSikkerhetstiltak(Fnr.of(fnr));
            personV2Data.setSikkerhetstiltak(sikkerhetstiltak);
        } catch (Exception e) {
            log.warn("Kunne ikke flette Sikkerhetstiltak", e);
        }
    }

    private void flettGeografiskEnhet(String fodselsnummer,  String userToken, PersonV2Data personV2Data) {
        String geografiskTilknytning = Optional.ofNullable(pdlClient.hentGeografiskTilknytning(fodselsnummer, userToken))
                                               .map(HentPdlPerson.GeografiskTilknytning::getGtKommune).orElse(null);

        personV2Data.setGeografiskTilknytning(geografiskTilknytning);

        if (geografiskTilknytning != null && geografiskTilknytning.matches("\\d+")) {
            try {
                Enhet enhet = fraNorg2Enhet(norg2Client.hentTilhorendeEnhet(geografiskTilknytning));
                personV2Data.setGeografiskEnhet(enhet);
            } catch (Exception e) {
                log.error("Klarte ikke å flette inn geografisk enhet", e);
            }
        }
    }

    private void flettKodeverk(PersonV2Data personV2Data) {
        personV2Data.setStatsborgerskap(kodeverkService.getBeskrivelseForLandkode(personV2Data.getStatsborgerskap()));
        personV2Data.setPoststedUnderBostedsAdresse(kodeverkService.getPoststedForPostnummer(personV2Data.getPostnummerFraBostedsadresse()));
        personV2Data.setBeskrivelseForLandkodeIKontaktadresse(kodeverkService.getBeskrivelseForLandkode(personV2Data.getLandKodeFraKontaktadresse()));
    }

    private void flettDigitalKontaktinformasjon(String fnr, PersonV2Data personV2Data) {
        try {
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(Fnr.of(fnr));
            personV2Data.setEpost(kontaktinfo.getEpostadresse());
            personV2Data.setMalform(kontaktinfo.getSpraak());
        } catch (Exception e) {
            log.warn("Kunne ikke flette digitalkontaktinfo fra KRR", e);
        }
    }

    public String hentMalform(Fnr fnr) {
        try{
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(fnr);
            return kontaktinfo.getSpraak();
        } catch(Exception e) {
            log.warn("Kunne ikke hente malform fra KRR", e);
        }
        return null;
    }
}
