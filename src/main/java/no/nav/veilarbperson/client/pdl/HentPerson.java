package no.nav.veilarbperson.client.pdl;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.pdl.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class HentPerson {
    public Person hentPerson;
    public List<PersonFraBolk> hentPersonBolk;
    public GeografiskTilknytning hentGeografiskTilknytning;

    @Data
    public static class Person {
        private List<Navn> navn;
        private List<Foedselsdato> foedselsdato;
        private List<Kjoenn> kjoenn;
        private List<Folkeregisteridentifikator> folkeregisteridentifikator;
        private List<Statsborgerskap> statsborgerskap;

        private List<Doedsfall> doedsfall;
        private List<Sivilstand> sivilstand;
        private List<ForelderBarnRelasjon> forelderBarnRelasjon;
        private List<Telefonnummer> telefonnummer;

        private List<Sikkerhetstiltak> sikkerhetstiltak;
        private List<Adressebeskyttelse> adressebeskyttelse;
        private List<Bostedsadresse> bostedsadresse;
        private List<Oppholdsadresse> oppholdsadresse;
        private List<Kontaktadresse> kontaktadresse;
    }

    @Data
    public static class PersonFraBolk {
        private String ident;
        private Familiemedlem person;
        private String code;
    }

    @Data
    public static class Familiemedlem {
        private List<Navn> navn;
        private List<Foedselsdato> foedselsdato;
        private List<Kjoenn> kjoenn;
        private List<Folkeregisteridentifikator> folkeregisteridentifikator;
        private List<Folkeregisterpersonstatus> folkeregisterpersonstatus;
        private List<Doedsfall> doedsfall;
        private List<Adressebeskyttelse> adressebeskyttelse;
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
        private MetadataNavn metadata;
    }

    @Data
    public static class Telefonnummer {
        private String landskode;
        private String nummer;
        private String prioritet;
        private Metadata metadata;
    }

    @Data
    public static class MetadataNavn {
        private PdlNavnMaster master;
    }

    public enum PdlNavnMaster {
        PDL(1),
        FREG(2),
        UVIST(3);

        public final int prioritet;

        PdlNavnMaster(int i) {
            prioritet = i;
        }

        @JsonCreator
        public static PdlNavnMaster fromString(String string) {
            try {
                return PdlNavnMaster.valueOf(string);
            } catch (IllegalArgumentException e) {
                return UVIST;
            }
        }
    }

    @Data
    public static class Metadata {
        private String master;
        private List<Endringer> endringer;

        @Data
        public static class Endringer {
            private String type;
            private LocalDateTime registrert;
            private String registrertAv;
            private String systemkilde;
            private String kilde;
        }
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
    public static class ForelderBarnRelasjon {
        private String minRolleForPerson;
        private String relatertPersonsRolle;
        private String relatertPersonsIdent;
        private RelatertPersonUtenFolkeregisteridentifikator relatertPersonUtenFolkeregisteridentifikator;
    }

    @Data
    public static class RelatertPersonUtenFolkeregisteridentifikator {
        private NavnUtenRelasjon navn;
        private LocalDate foedselsdato;
    }

    @Data
    public static class NavnUtenRelasjon {
        private String fornavn;
    }


    @Data
    public static class Foedselsdato {
        private LocalDate foedselsdato;
    }

    @Data
    public static class Folkeregisteridentifikator {
        private String identifikasjonsnummer;
        private String status;
        private String type;
    }

    @Data
    public static class Folkeregisterpersonstatus {
        private String forenkletStatus;
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
        private Metadata metadata;
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
        private List<String> omraader;
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
