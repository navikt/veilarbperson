package no.nav.veilarbperson.client.pdl;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.pdl.domain.Bostedsadresse;
import no.nav.veilarbperson.client.pdl.domain.Kontaktadresse;
import no.nav.veilarbperson.client.pdl.domain.Metadata;
import no.nav.veilarbperson.client.pdl.domain.Oppholdsadresse;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class HentPdlPerson {
    public PdlPerson hentPerson;
    public List<Barn> hentPersonBolk;
    public GeografiskTilknytning hentGeografiskTilknytning;

    @Data
    public static class PdlPerson {
        List<Navn> navn;
        List<Foedsel> foedsel;
        List<Kjoenn> kjoenn;
        List<Folkeregisteridentifikator> folkeregisteridentifikator;
        List<Statsborgerskap> statsborgerskap;

        List<Doedsfall> doedsfall;
        List<Sivilstand> sivilstand;
        List<Familierelasjoner> familierelasjoner;
        List<Telefonnummer> telefonnummer;
        List<Sikkerhetstiltak> sikkerhetstiltak;

        List<Adressebeskyttelse> adressebeskyttelse;
        List<Bostedsadresse> bostedsadresse;
        List<Oppholdsadresse> oppholdsadresse;
        List<Kontaktadresse> kontaktadresse;
    }

    @Data
    public static class Barn {
        String ident;
        Familiemedlem person;
        String code;
    }

    @Data
    public static class Familiemedlem {
        List<Navn> navn;
        List<Foedsel> foedsel;
        List<Kjoenn> kjoenn;
        List<Folkeregisteridentifikator> folkeregisteridentifikator;
        List<Doedsfall> doedsfall;
        List<Bostedsadresse> bostedsadresse;
    }

    @Data
    public static class Partner {
        public Familiemedlem hentPerson;
    }

    @Data
    public static class Navn {
        String fornavn;
        String mellomnavn;
        String etternavn;
        String forkortetNavn;
    }

    @Data
    public static class Telefonnummer {
        private String landskode;
        private String nummer;
        private String prioritet;
        private Metadata metadata;
    }

    @Data
    public static class Adressebeskyttelse {
        private String gradering;
    }

    @Data
    public static class Doedsfall {
        private LocalDate doedsdato;
    }

    @Data
    public static class Familierelasjoner {
        private String minRolleForPerson;
        private String relatertPersonsRolle;
        private String relatertPersonsIdent;
    }

    @Data
    public static class Foedsel {
        private LocalDate foedselsdato;
    }

    @Data
    public static class Folkeregisteridentifikator {
        String identifikasjonsnummer;
        String status;
        String type;
    }

    @Data
    public static class GeografiskTilknytning {
        private String gtType;
        private String gtKommune;
        private String gtBydel;
        private String gtLand;
    }

    @Data
    public static class Kjoenn {
        private String kjoenn;
    }

    @Data
    public static class Sivilstand {
        String type;
        LocalDate gyldigFraOgMed;
        String relatertVedSivilstand;
    }

    @Data
    public static class Sikkerhetstiltak {
        private String tiltakstype;
        private String beskrivelse;
    }

    @Data
    public static class Statsborgerskap {
        private String land;
    }

    @Data
    public static class Tolk {
        private String spraak;
    }

    @Data
    public static class TilrettelagtKommunikasjon {
        private Tolk talespraaktolk;
        private Tolk tegnspraaktolk;
    }

    @Data
    public static class HentSpraakTolk {
        private List<TilrettelagtKommunikasjon> tilrettelagtKommunikasjon;
    }

    @Data
    public static class HentTilrettelagtKommunikasjon {
        public HentSpraakTolk hentPerson;
    }
}
