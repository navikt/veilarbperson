package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.services.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class APIController {

    @Autowired
    PersonService personService;

    @RequestMapping(value = "/person/{fnr}", produces = "application/json")
    public String person(@PathVariable String fnr) {
        return getPerson(fnr);
    }

    private String getPerson(String fnr) {

        String navn = personService.hentNavn(fnr);

        return "{\"fornavn\": \"" + navn + "\"," +
                "\"etternavn\": \"Ullmann\"}";
    }
}

