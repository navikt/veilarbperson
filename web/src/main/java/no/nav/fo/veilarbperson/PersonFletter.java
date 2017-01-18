package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.domain.Sikkerhetstiltak;
import no.nav.fo.veilarbperson.services.PersonData;
import no.nav.fo.veilarbperson.services.PersonService;
import no.nav.tjeneste.virksomhet.person.v2.HentSikkerhetstiltakPersonIkkeFunnet;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonFletter {

    @Autowired
    PersonService personService;

    public PersonData hentPerson(String fnr){
        PersonData personData = personService.hentPerson(fnr);

        try {
            Sikkerhetstiltak sikkerhetstiltak = personService.hentSikkerhetstiltak(fnr);
            personData.medSikkerhetstiltak(sikkerhetstiltak.getSikkerhetstiltaksbeskrivelse());
        } catch (HentSikkerhetstiltakPersonIkkeFunnet hentSikkerhetstiltakPersonIkkeFunnet) {
            hentSikkerhetstiltakPersonIkkeFunnet.printStackTrace();
        }
        //TODO: Fyll personData med mer data fra TPS, Digital kontaktinfo. norg2, felles kodeverk og TPSWS-egensatt

        return personData;
    }
}
