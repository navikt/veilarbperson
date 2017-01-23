package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.services.EnhetService;
import no.nav.fo.veilarbperson.digitalkontaktinformasjon.DigitalKontaktinformasjon;
import no.nav.fo.veilarbperson.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.domain.Sikkerhetstiltak;
import no.nav.fo.veilarbperson.services.EgenAnsattService;
import no.nav.fo.veilarbperson.services.PersonData;
import no.nav.fo.veilarbperson.services.PersonService;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.person.v2.HentSikkerhetstiltakPersonIkkeFunnet;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonFletter {

    @Autowired
    PersonService personService;

    @Autowired
    EgenAnsattService egenAnsattService;

    @Autowired
    EnhetService enhetService;

    @Autowired
    DigitalKontaktinformasjonService digitalKontaktinformasjonService;

    public PersonData hentPerson(String fnr){
        PersonData personData = personService.hentPerson(fnr);
        personData.withEgenAnsatt(egenAnsattService.erEgenAnsatt(fnr));

        if (personData.getAnsvarligEnhetsnummer() != null) {
            personData.withBehandlendeEnhet(enhetService.hentBehandlendeEnhet(personData.getAnsvarligEnhetsnummer()));
        }

        hentPersondata(fnr, personData);
        hentDigitalKontaktinformasjon(fnr, personData);

        //TODO: Fyll personData med mer data fra TPS, Digital kontaktinfo. norg2, felles kodeverk og TPSWS-egensatt

        return personData;
    }

    private void hentDigitalKontaktinformasjon(String fnr, PersonData personData) {
        try {
            DigitalKontaktinformasjon kontaktinformasjon = digitalKontaktinformasjonService.hentDigitalKontaktinformasjon(fnr);
            personData
                    .withTelefon(kontaktinformasjon.getTelefon())
                    .withEpost(kontaktinformasjon.getEpost());
        } catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing |
                HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet |
                HentDigitalKontaktinformasjonPersonIkkeFunnet hentDigitalKontaktinformasjonSikkerhetsbegrensing) {
            hentDigitalKontaktinformasjonSikkerhetsbegrensing.printStackTrace();
        }
    }

    private void hentPersondata(String fnr, PersonData personData) {
        try {
            Sikkerhetstiltak sikkerhetstiltak = personService.hentSikkerhetstiltak(fnr);
            personData.withSikkerhetstiltak(sikkerhetstiltak.getSikkerhetstiltaksbeskrivelse());
        } catch (HentSikkerhetstiltakPersonIkkeFunnet hentSikkerhetstiltakPersonIkkeFunnet) {
            hentSikkerhetstiltakPersonIkkeFunnet.printStackTrace();
        }
    }

}
