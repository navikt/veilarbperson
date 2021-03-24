package no.nav.veilarbperson.client.pdl;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.pdl.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class HentPdlPerson {
    public PdlPerson hentPerson;
    public List<Barn> hentPersonBolk;
    public GeografiskTilknytning hentGeografiskTilknytning;

    @Data
    public static class PdlPerson {
        private List<Navn> navn;
        private List<Foedsel> foedsel;
        private List<Kjoenn> kjoenn;
        private List<Folkeregisteridentifikator> folkeregisteridentifikator;
        private List<Statsborgerskap> statsborgerskap;

        private List<Doedsfall> doedsfall;
        private List<Sivilstand> sivilstand;
        private List<Familierelasjoner> familierelasjoner;
        private List<Telefonnummer> telefonnummer;

        private List<Sikkerhetstiltak> sikkerhetstiltak;
        private List<Adressebeskyttelse> adressebeskyttelse;
        private List<Bostedsadresse> bostedsadresse;
        private List<Oppholdsadresse> oppholdsadresse;
        private List<Kontaktadresse> kontaktadresse;
    }

    @Data
    public static class Barn {
        private String ident;
        private Familiemedlem person;
        private String code;
    }

    @Data
    public static class Familiemedlem {
        private List<Navn> navn;
        private List<Foedsel> foedsel;
        private List<Kjoenn> kjoenn;
        private List<Folkeregisteridentifikator> folkeregisteridentifikator;
        private List<Doedsfall> doedsfall;
        private List<Bostedsadresse> bostedsadresse;
    }

    @Data
    public static class Partner {
        public Familiemedlem hentPerson;
    }

    @Data
    public static class Navn {
        private String fornavn;
        private String mellomnavn;
        private String etternavn;
        private String forkortetNavn;
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
        private String identifikasjonsnummer;
        private String status;
        private String type;
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
    public static class VergeNavn {
        private String fornavn;
        private String mellomnavn;
        private String etternavn;
    }

    @Data
    public static class VergeEllerFullmektig {
        private VergeNavn navn;
        private String motpartsPersonident;
        private VergemaalEllerFullmaktOmfangType omfang;
    }

    @Data
    public static class VergemaalEllerFremtidsfullmakt {
        private Vergetype type;
        private String embete;
        private VergeEllerFullmektig vergeEllerFullmektig;
        private Folkeregistermetadata folkeregistermetadata;
    }

    @Data
    public static class Folkeregistermetadata {
        public LocalDateTime ajourholdstidspunkt;
        public LocalDateTime gyldighetstidspunkt;
    }

    @Data
    public static class Fullmakt {
        private String motpartsPersonident;
        private String motpartsRolle;
        private String[] omraader;
        private LocalDate gyldigFraOgMed;
        private LocalDate gyldigTilOgMed;
    }

    @Data
    public static class VergeOgFullmakt {
        private List<VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmakt;
        private List<Fullmakt> fullmakt;
    }

    @Data
    public static class HentVergeOgFullmakt {
        public VergeOgFullmakt hentPerson;
    }

    @Data
    public static class PersonNavn {
        public List<Navn> navn;
    }

    @Data
    public static class HentFullmaktNavn {
        public PersonNavn hentPerson;
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
