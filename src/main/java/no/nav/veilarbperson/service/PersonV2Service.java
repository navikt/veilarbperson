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
import no.nav.veilarbperson.client.pdl.domain.Bostedsadresse;
import no.nav.veilarbperson.client.pdl.domain.Familiemedlem;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.domain.Enhet;
import no.nav.veilarbperson.domain.PersonData;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.PersonDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static no.nav.veilarbperson.utils.Mappers.fraNorg2Enhet;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.getFirstElement;

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
        HentPdlPerson.PdlPerson personDataFraPdl = ofNullable(pdlClient.hentPerson(fodselsnummer, userToken)).orElseThrow(() -> new Exception("Fant ikke person i hentPerson operasjonen i PDL"));
        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(personDataFraPdl, personDataFraTps);

        flettBarnInformasjon(personDataFraPdl.getFamilierelasjoner(), personV2Data);
        flettPartnerInformasjon(personDataFraPdl.getSivilstand(), personV2Data, userToken);
        flettDigitalKontaktinformasjon(fodselsnummer, getFirstElement(personDataFraPdl.getTelefonnummer()), personV2Data);
        flettGeografiskEnhet(fodselsnummer, userToken, personV2Data);
        flettKodeverk(personV2Data);

        return personV2Data;
    }

    public List<Familiemedlem> hentOpplysningerTilBarna(String[] barnasFnrs, Bostedsadresse foreldresBostedsAdresse) {
        List<HentPdlPerson.Barn> barnasInformasjon = pdlClient.hentPersonBolk(barnasFnrs);

        return ofNullable(barnasInformasjon)
                .stream()
                .flatMap(Collection::stream)
                .filter(barn -> barn.getCode().equals("ok"))
                .map(HentPdlPerson.Barn::getPerson)
                .map(barn -> PersonV2DataMapper.familiemedlemMapper(barn, foreldresBostedsAdresse))
                .collect(Collectors.toList());
    }

    public String[] hentFnrTilBarna(List<HentPdlPerson.Familierelasjoner> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getRelatertPersonsRolle()))
                .map(HentPdlPerson.Familierelasjoner::getRelatertPersonsIdent)
                .toArray(String[]::new);
    }

    public void flettBarnInformasjon(List<HentPdlPerson.Familierelasjoner> familierelasjoner, PersonV2Data personV2Data) {
        if (familierelasjoner != null) {
            String[] barnasFnrListe = hentFnrTilBarna(familierelasjoner);

            if (barnasFnrListe.length != 0) {
                List<Familiemedlem> barnasInformasjon = hentOpplysningerTilBarna(barnasFnrListe, personV2Data.getBostedsadresse());
                personV2Data.setBarn(barnasInformasjon);
            }
        }
    }

    public String hentFnrTilPartner(List<HentPdlPerson.Sivilstand> personsSivilstand) {
        return ofNullable(getFirstElement(personsSivilstand))
                .map(HentPdlPerson.Sivilstand::getRelatertVedSivilstand).orElse(null);
    }

    public void flettPartnerInformasjon(List<HentPdlPerson.Sivilstand> personsSivilstand, PersonV2Data personV2Data, String userToken) {
        String fnrTilPartner = hentFnrTilPartner(personsSivilstand);

        if(fnrTilPartner != null) {
            HentPdlPerson.Familiemedlem partnerInformasjon = pdlClient.hentPartner(fnrTilPartner, userToken);
            personV2Data.setPartner(ofNullable(partnerInformasjon)
                                    .map(partner -> PersonV2DataMapper.familiemedlemMapper(partner, personV2Data.getBostedsadresse()))
                                    .orElse(null)
            );
        }
    }

    private void flettGeografiskEnhet(String fodselsnummer,  String userToken, PersonV2Data personV2Data) {
        String geografiskTilknytning = ofNullable(pdlClient.hentGeografiskTilknytning(fodselsnummer, userToken))
                .map(HentPdlPerson.GeografiskTilknytning::getGtKommune).orElse(null);

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
        ofNullable(personV2Data.getStatsborgerskap()).map(kodeverkService::getBeskrivelseForLandkode).ifPresent(personV2Data::setStatsborgerskap);
        personV2Data.getPostnummerFraBostedsadresse().map(kodeverkService::getPoststedForPostnummer).ifPresent(personV2Data::setPoststedUnderBostedsAdresse);
        personV2Data.getLandKodeFraKontaktadresse().map(kodeverkService::getBeskrivelseForLandkode).ifPresent(personV2Data::setBeskrivelseForLandkodeIKontaktadresse);
    }

    private void flettDigitalKontaktinformasjon(String fnr, HentPdlPerson.Telefonnummer telefonnummerFraPdl, PersonV2Data personV2Data) {
        try {
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(Fnr.of(fnr));

            personV2Data.setTelefon(leggKrrTelefonNrIListe(kontaktinfo.getMobiltelefonnummer(), telefonnummerFraPdl));
            personV2Data.setEpost(kontaktinfo.getEpostadresse());
            personV2Data.setMalform(kontaktinfo.getSpraak());
        } catch (Exception e) {
            log.warn("Kunne ikke flette digitalkontaktinfo fra KRR", e);
        }
    }

    /* Legger telefonnummer fra PDL og KRR til en liste. Hvis de er like da kan liste inneholde kun en av dem */
    public List<String> leggKrrTelefonNrIListe(String telefonNummerFraKrr, HentPdlPerson.Telefonnummer telefonNummerFraPdl) {
        String telefonNrFraPdl = PersonV2DataMapper.telefonNummerMapper(telefonNummerFraPdl);
        List<String> telefonList = new ArrayList<>();

        if (telefonNrFraPdl != null) {
            telefonList.add(telefonNrFraPdl);
        }
        if (telefonNummerFraKrr != null && !telefonNummerFraKrr.equals(telefonNrFraPdl)) {
            telefonList.add(telefonNummerFraKrr);
        }

        return telefonList;
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
