package no.nav.fo.veilarbperson.consumer.tps;

import no.nav.fo.veilarbperson.consumer.tps.mappers.PersonDataMapper;
import no.nav.fo.veilarbperson.domain.person.PersonData;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSInformasjonsbehov;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSNorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSPersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentPersonResponse;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import static no.nav.fo.veilarbperson.config.CacheConfig.PERSON;
import static org.slf4j.LoggerFactory.getLogger;

public class PersonService {

    private static final Logger logger = getLogger(PersonService.class);

    private final PersonV3 personV3;
    private final PersonDataMapper personDataMapper;

    public PersonService(PersonV3 personV3) {
        this.personV3 = personV3;
        this.personDataMapper = new PersonDataMapper();
    }

    @Cacheable(PERSON)
    public PersonData hentPerson(String ident) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        try {
            WSHentPersonResponse wsPerson = personV3.hentPerson(lagHentPersonRequest(ident));
            return personDataMapper.tilPersonData(wsPerson.getPerson());
        } catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
            logger.info("Person ikke funnet: " + ident);
            throw hentPersonPersonIkkeFunnet;
        } catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
            logger.info("Saksbehandler har ikke tilgang til: " + ident);
            throw hentPersonSikkerhetsbegrensning;
        }
    }

    private WSHentPersonRequest lagHentPersonRequest(String ident) {
        return new WSHentPersonRequest().withAktoer(new WSPersonIdent().withIdent(new WSNorskIdent().withIdent(ident)))
                .withInformasjonsbehov(WSInformasjonsbehov.ADRESSE, WSInformasjonsbehov.BANKKONTO,
                        WSInformasjonsbehov.FAMILIERELASJONER, WSInformasjonsbehov.KOMMUNIKASJON);
    }

}