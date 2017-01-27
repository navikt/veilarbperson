package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.services.*;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
public class APIController {

    private static final Logger logger = getLogger(APIController.class);

    private final EnhetService enhetService;
    private final DigitalKontaktinformasjonService digitalKontaktinformasjonService;
    private final PersonService personService;
    private final EgenAnsattService egenAnsattService;
    private final KodeverkService kodeverkService;

    public APIController(EnhetService enhetService,
                         DigitalKontaktinformasjonService digitalKontaktinformasjonService,
                         PersonService personService,
                         EgenAnsattService egenAnsattService,
                         KodeverkService kodeverkService) {
        this.enhetService = enhetService;
        this.digitalKontaktinformasjonService = digitalKontaktinformasjonService;
        this.personService = personService;
        this.egenAnsattService = egenAnsattService;
        this.kodeverkService = kodeverkService;
    }

    @RequestMapping(value = "/person/{personnummer}", produces = "application/json")
    public PersonData person(@PathVariable String personnummer) {
        final PersonFletter personFletter = new PersonFletter(
                enhetService,
                digitalKontaktinformasjonService,
                personService,
                egenAnsattService,
                kodeverkService);
        logger.info("Henter persondata med personnummer: " + personnummer);
        return personFletter.hentPerson(personnummer);
    }

}

