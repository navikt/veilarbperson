package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.services.EnhetService;
import no.nav.fo.veilarbperson.services.PersonData;
import no.nav.fo.veilarbperson.services.PersonService;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonFletter {

    @Autowired
    PersonService personService;

    @Autowired
    EnhetService enhetService;

    public PersonData hentPerson(String fnr) {
        PersonData personData = personService.hentPerson(fnr);

        if (personData.getAnsvarligEnhetsnummer() != null) {
            personData.medBehandlendeEnhet(enhetService.hentBehandlendeEnhet(personData.getAnsvarligEnhetsnummer()));
        }
        //TODO: Fyll personData med mer data fra TPS, Digital kontaktinfo. norg2, felles kodeverk og TPSWS-egensatt

        return personData;
    }

}
