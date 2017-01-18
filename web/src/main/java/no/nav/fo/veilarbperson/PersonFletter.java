package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.dkif.DigitalKontaktinformasjon;
import no.nav.fo.veilarbperson.dkif.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.services.PersonData;
import no.nav.fo.veilarbperson.services.PersonService;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonFletter {

    @Autowired
    PersonService personService;

    @Autowired
    DigitalKontaktinformasjonService dkifService;

    public PersonData hentPerson(String fnr){
        PersonData personData = personService.hentPerson(fnr);

        DigitalKontaktinformasjon kontaktinformasjon;
        try {
            kontaktinformasjon = dkifService.hentDigitalKontaktinformasjon(fnr);
            personData
                    .medTelefon(kontaktinformasjon.getTelefon())
                    .medEpost(kontaktinformasjon.getEpost());
        } catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing | HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet | HentDigitalKontaktinformasjonPersonIkkeFunnet hentDigitalKontaktinformasjonSikkerhetsbegrensing) {
            hentDigitalKontaktinformasjonSikkerhetsbegrensing.printStackTrace();
        }
        //TODO: Fyll personData med mer data fra TPS, Digital kontaktinfo. norg2, felles kodeverk og TPSWS-egensatt

        return personData;
    }
}
