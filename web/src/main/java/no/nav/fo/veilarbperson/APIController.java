package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.services.PersonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class APIController {

    @Autowired
    PersonFletter personFletter;

    @RequestMapping(value = "/person/{fnr}", produces = "application/json")
    public PersonData person(@PathVariable String fnr) {
        return personFletter.hentPerson(fnr);
    }

}

