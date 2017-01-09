package no.nav.fo.veilarbperson;

import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
public class APIController {

    private static final Logger logger = getLogger(APIController.class);

    @RequestMapping(value = "/person/{fnr}", produces = "application/json")
    public String person(@PathVariable String fnr) {
        return getPerson(fnr);
    }

    private String getPerson(String fnr) {

        logger.info("Henter person med fnr:" + fnr );

        return "{\"fornavn\": \"Liv\"," +
                "\"etternavn\": \"Ullmann\"," +
                "\"fnr\" : \""+ fnr + "\"}";
    }
}

