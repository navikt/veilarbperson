package no.nav.veilarbperson.client.tps.mappers;

import no.nav.veilarbperson.domain.person.Familiemedlem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static no.nav.veilarbperson.client.tps.mappers.MapperTestUtils.lagDato;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


public class BarnMapperTest {

    @Test
    public void resultatSkalInneholdeDodsdato() throws Exception {
        List<no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon> familierelasjoner = new ArrayList<>();

        familierelasjoner.add(new no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon()
                .withTilRolle(new no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjoner().withValue("BARN"))
                .withTilPerson(new no.nav.tjeneste.virksomhet.person.v3.informasjon.Person().withDoedsdato(new no.nav.tjeneste.virksomhet.person.v3.informasjon.Doedsdato().withDoedsdato(lagDato(2017,03,30)))));
        BarnMapper barnMapper = new BarnMapper();

        List<Familiemedlem> familimedlemmer = barnMapper.familierelasjonerTilBarn(familierelasjoner);
        assertThat(familimedlemmer.get(0).getDodsdato(), notNullValue());
    }

    @Test
    public void resultatSkalHaNullSomDoedsdato() throws Exception {
        List<no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon> familierelasjoner = new ArrayList<>();

        familierelasjoner.add(new no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon()
                .withTilRolle(new no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjoner().withValue("BARN"))
                .withTilPerson(new no.nav.tjeneste.virksomhet.person.v3.informasjon.Person()));
        BarnMapper barnMapper = new BarnMapper();

        List<Familiemedlem> familimedlemmer = barnMapper.familierelasjonerTilBarn(familierelasjoner);
        assertThat(familimedlemmer.get(0).getDodsdato(), nullValue());
    }


}