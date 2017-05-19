package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.person.Familiemedlem;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSDoedsdato;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSFamilierelasjon;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSFamilierelasjoner;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSPerson;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static no.nav.fo.veilarbperson.consumer.tps.mappers.MapperTestUtils.lagDato;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


public class BarnMapperTest {

    @Test
    public void resultatSkalInneholdeDodsdato() throws Exception {
        List<WSFamilierelasjon> familierelasjoner = new ArrayList<>();

        familierelasjoner.add(new WSFamilierelasjon()
                .withTilRolle(new WSFamilierelasjoner().withValue("BARN"))
                .withTilPerson(new WSPerson().withDoedsdato(new WSDoedsdato().withDoedsdato(lagDato(2017,03,30)))));
        BarnMapper barnMapper = new BarnMapper();

        List<Familiemedlem> familimedlemmer = barnMapper.familierelasjonerTilBarn(familierelasjoner);
        assertThat(familimedlemmer.get(0).getDodsdato(), notNullValue());
    }

    @Test
    public void resultatSkalHaNullSomDoedsdato() throws Exception {
        List<WSFamilierelasjon> familierelasjoner = new ArrayList<>();

        familierelasjoner.add(new WSFamilierelasjon()
                .withTilRolle(new WSFamilierelasjoner().withValue("BARN"))
                .withTilPerson(new WSPerson()));
        BarnMapper barnMapper = new BarnMapper();

        List<Familiemedlem> familimedlemmer = barnMapper.familierelasjonerTilBarn(familierelasjoner);
        assertThat(familimedlemmer.get(0).getDodsdato(), nullValue());
    }


}