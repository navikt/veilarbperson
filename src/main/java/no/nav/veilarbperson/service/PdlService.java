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
import no.nav.veilarbperson.client.pdl.PdlPersonData;
import no.nav.veilarbperson.client.pdl.domain.Familiemedlem;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.Personinfo;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.domain.Enhet;
import no.nav.veilarbperson.domain.PersonData;
import no.nav.veilarbperson.utils.PdlPersonDataMappper;
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
public class PdlService {

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
    public PdlService(PdlClient pdlClient, AuthService authService, DkifClient dkifClient, Norg2Client norg2Client, PersonClient personClient,
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

    public PdlPersonData hentFlettetPerson(String fodselsnummer, String userToken) throws Exception {
        PersonData personDataFraTps = hentPersonDataFraTps(fodselsnummer);
        HentPdlPerson.PdlPerson personDataFraPdl = Optional.of(pdlClient.hentPerson(fodselsnummer, userToken)).orElseThrow(() -> new Exception("Fant ikke person i hentPerson operasjonen i PDL"));
        PdlPersonData pdlPersonData = PdlPersonDataMappper.toPdlPersonData(personDataFraPdl, personDataFraTps);

        try {
            flettPersoninfoFraPortefolje(pdlPersonData, fodselsnummer);
        } catch (Exception e) {
            log.warn("Bruker fallbackløsning for egenAnsatt-sjekk", e);
            flettEgenAnsatt(fodselsnummer, pdlPersonData);
            flettSikkerhetstiltak(fodselsnummer, pdlPersonData);
        }

        flettBarnInformasjon(personDataFraPdl.getFamilierelasjoner(), pdlPersonData);
        flettPartnerInformasjon(personDataFraPdl.getSivilstand(), pdlPersonData, userToken);
        flettDigitalKontaktinformasjon(fodselsnummer, pdlPersonData);
        flettGeografiskEnhet(pdlPersonData);
        flettKodeverk(pdlPersonData);

        return pdlPersonData;
    }

    public List<Familiemedlem> hentOpplysningerTilBarna(String[] barnasFnrs) {
        List<HentPdlPerson.PdlPersonBolk> barnasaInformasjon = pdlClient.hentPersonBolk(barnasFnrs);

        return barnasaInformasjon.stream()
                .filter(barn -> barn.getCode().equals("ok"))
                .map(HentPdlPerson.PdlPersonBolk::getPerson)
                .map(PdlPersonDataMappper::familiemedlemMapper)
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

    private void flettBarnInformasjon(List<HentPdlPerson.Familierelasjoner> familierelasjoner, PdlPersonData pdlPersonData) {
        String[] barnasFnrListe = hentFnrTilBarna(familierelasjoner);
        List<Familiemedlem> barnasInformasjon = hentOpplysningerTilBarna(barnasFnrListe);
        pdlPersonData.setBarn(barnasInformasjon);
    }

    public String hentFnrTilPartner(List<HentPdlPerson.Sivilstand> personsSivilstand){
        return Optional.ofNullable(PdlPersonDataMappper.getFirstElement(personsSivilstand))
                .map(HentPdlPerson.Sivilstand::getRelatertVedSivilstand)
                .orElse(null);
    }

    public void flettPartnerInformasjon(List<HentPdlPerson.Sivilstand> personsSivilstand, PdlPersonData pdlPersonData, String userToken) {
        String fnrTilPartner = hentFnrTilPartner(personsSivilstand);
        HentPdlPerson.PersonsFamiliemedlem partnerInformasjon =  pdlClient.hentPartnerOpplysninger(fnrTilPartner, userToken);
        pdlPersonData.setPartner(PdlPersonDataMappper.familiemedlemMapper(partnerInformasjon));
    }

    public String executeGqlRequest(String gqlRequest) {
        return pdlClient.rawRequest(gqlRequest, authService.getInnloggetBrukerToken());
    }

    private void flettPersoninfoFraPortefolje(PdlPersonData personData, String fodselsnummer) {
        Personinfo personinfo = veilarbportefoljeClient.hentPersonInfo(Fnr.of(fodselsnummer));
        personData.setSikkerhetstiltak(personinfo.sikkerhetstiltak);
        personData.setEgenAnsatt(personinfo.egenAnsatt);
    }

    private void flettEgenAnsatt(String fnr, PdlPersonData pdlPersonData) {
        pdlPersonData.setEgenAnsatt(egenAnsattClient.erEgenAnsatt(Fnr.of(fnr)));
    }

    private void flettSikkerhetstiltak(String fnr, PdlPersonData pdlPersonData) {
        try {
            String sikkerhetstiltak = personClient.hentSikkerhetstiltak(Fnr.of(fnr));
            pdlPersonData.setSikkerhetstiltak(sikkerhetstiltak);
        } catch (Exception e) {
            log.warn("Kunne ikke flette Sikkerhetstiltak", e);
        }
    }

    private void flettGeografiskEnhet(PdlPersonData pdlPersonData) {
        String geografiskTilknytning = pdlPersonData.getGeografiskTilknytning();

        if (geografiskTilknytning != null && geografiskTilknytning.matches("\\d+")) {
            try {
                Enhet enhet = fraNorg2Enhet(norg2Client.hentTilhorendeEnhet(geografiskTilknytning));
                pdlPersonData.setGeografiskEnhet(enhet);
            } catch (Exception e) {
                log.error("Klarte ikke å flette inn geografisk enhet", e);
            }
        }
    }

    private void flettKodeverk(PdlPersonData pdlPersonData) {
        pdlPersonData.setStatsborgerskap(kodeverkService.getBeskrivelseForLandkode(pdlPersonData.getStatsborgerskap()));
        pdlPersonData.setPoststedUnderBostedsAdresse(kodeverkService.getPoststedForPostnummer(pdlPersonData.getPostnummerFraBostedsadresse()));
        pdlPersonData.setBeskrivelseForLandkodeIKontaktadresse(kodeverkService.getBeskrivelseForLandkode(pdlPersonData.getLandKodeFraKontaktadresse()));
    }

    private void flettDigitalKontaktinformasjon(String fnr, PdlPersonData pdlPersonData) {
        try {
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(Fnr.of(fnr));
            pdlPersonData.setEpost(kontaktinfo.getEpostadresse());
            pdlPersonData.setMalform(kontaktinfo.getSpraak());
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
