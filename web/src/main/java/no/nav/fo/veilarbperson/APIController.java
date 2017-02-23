package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.fo.veilarbperson.domain.PersonData;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import javax.ws.rs.*;

import static org.slf4j.LoggerFactory.getLogger;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Component
@Path("/person/{personnummer}")
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

    @GET
    @Produces(APPLICATION_JSON)
    public PersonData person(@PathParam("personnummer") String personnummer) {
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

