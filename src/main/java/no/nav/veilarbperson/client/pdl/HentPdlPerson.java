package no.nav.veilarbperson.client.pdl;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.pdl.domain.Bostedsadresse;
import no.nav.veilarbperson.client.pdl.domain.KontaktAdresse;

import java.util.List;

@Data
@Accessors(chain = true)
public class HentPdlPerson {
    public PdlPerson pdlPerson;
    public List<PdlPersonBolk> pdlPersonBolk;
    public PersonsFamiliemedlem personsPartner;
    public GeografiskTilknytning geografiskTilknytning;

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
        List<KontaktAdresse> kontaktadresse;
    }

    @Data
    public static class PdlPersonBolk {
        String ident;
        PersonsFamiliemedlem person;
        String code;
    }

    @Data
    public static class PersonsFamiliemedlem {
        List<Navn> navn;
        List<Foedsel> foedsel;
        List<Kjoenn> kjoenn;
        List<Folkeregisteridentifikator> folkeregisteridentifikator;
        List<Doedsfall> doedsfall;
    }

    @Data
    public static class Navn {
        String fornavn;
        String mellomnavn;
        String etternavn;
        String forkortetnavn;
    }

    @Data
    public static class Telefonnummer {
        private String landkode;
        private String nummer;
        private String prioritet;
    }

    @Data
    public static class Adressebeskyttelse {
        private String gradering;
    }

    @Data
    public static class Doedsfall {
        private String doedsdato;
    }

    @Data
    public static class Familierelasjoner {
        private String minRolleForPerson;
        private String relatertPersonsRolle;
        private String relatertPersonsIdent;
    }

    @Data
    public static class Foedsel {
        private String foedselsdato;
    }

    @Data
    public static class Folkeregisteridentifikator {
        String identifikasjonsnummer;
        String status;
        String type;
    }

    @Data
    public static class GeografiskTilknytning {
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
        String gyldigFraOgMed;
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
        private String gyldigFraOgMed;
        private String gyldigTilOgMed;
    }

}
