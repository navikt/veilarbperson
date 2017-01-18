package no.nav.fo.veilarbperson;

import org.slf4j.Logger;
import no.nav.fo.veilarbperson.services.PersonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
public class APIController {

    private static final Logger logger = getLogger(APIController.class);

    @Autowired
    PersonFletter personFletter;

    @RequestMapping(value = "/person/{personnummer}", produces = "application/json")
    public PersonData person(@PathVariable String personnummer) {
        logger.info("Henter persondata med personnummer: " + personnummer );
        return personFletter.hentPerson(personnummer);
    }

}

