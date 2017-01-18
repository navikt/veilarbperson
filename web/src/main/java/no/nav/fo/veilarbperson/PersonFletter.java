package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.services.EgenAnsattService;
import no.nav.fo.veilarbperson.services.PersonData;
import no.nav.fo.veilarbperson.services.PersonService;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonFletter {

    @Autowired
    PersonService personService;

    @Autowired
    EgenAnsattService egenAnsattService;

    public PersonData hentPerson(String fnr){
        PersonData personData = personService.hentPerson(fnr);
        personData.medEgenAnsatt(egenAnsattService.erEgenAnsatt(fnr));

        //TODO: Fyll personData med mer data fra TPS, Digital kontaktinfo. norg2, felles kodeverk og TPSWS-egensatt

        return personData;
    }
}
