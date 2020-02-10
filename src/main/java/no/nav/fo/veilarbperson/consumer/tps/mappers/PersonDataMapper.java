package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.person.*;
import no.nav.fo.veilarbperson.domain.person.Bostedsadresse;
import no.nav.fo.veilarbperson.domain.person.Gateadresse;
import no.nav.fo.veilarbperson.domain.person.Matrikkeladresse;
import no.nav.fo.veilarbperson.domain.person.PostboksadresseNorsk;
import no.nav.fo.veilarbperson.domain.person.Sivilstand;
import no.nav.fo.veilarbperson.domain.person.StrukturertAdresse;
import no.nav.fo.veilarbperson.domain.person.UstrukturertAdresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class PersonDataMapper {

    private static final String KODE_6 = "6";
    private static final String KODE_7 = "7";

    private final BarnMapper barnMapper = new BarnMapper();
    private final FamiliemedlemMapper familiemedlemMapper = new FamiliemedlemMapper();

    public PersonData tilPersonData(Person person) {
        return PersonData.builder()
                .fornavn(kanskjeFornavn(person))
                .mellomnavn(kanskjeMellomnavn(person))
                .etternavn(kanskjeEtternavn(person))
                .sammensattnavn(kanskjeSammensattnavn(person))
                .fodselsnummer(kanskjeFodselsnummer(person))
                .fodselsdato(kanskjeFodselsdato(person))
                .kjonn(kanskjeKjonn(person))
                .barn(barnMapper.familierelasjonerTilBarn(person.getHarFraRolleI()))
                .diskresjonskode(kanskjeDiskresjonskode(person))
                .kontonummer(kanskjeKontonummer(person))
                .geografiskTilknytning(geografiskTilknytning(person))
                .statsborgerskap(kanskjeStatsborgerskap(person))
                .sivilstand(kanskjeSivilstand(person))
                .partner(familiemedlemMapper.partner(person.getHarFraRolleI()))
                .bostedsadresse(kanskjeBostedsadresse(person))
                .midlertidigAdresseNorge(kanskjeMidlertidigAdresseNorge(person))
                .midlertidigAdresseUtland(kanskjeMidlertidigAdresseUtland(person))
                .postAdresse(kanskjePostAdresse(person))
                .dodsdato(dodsdatoTilString(person))
                .malform(kanskjeMalform(person))
                .build();
    }

    private String kanskjeKjonn(Person person) {
        return ofNullable(person.getKjoenn())
                .map(Kjoenn::getKjoenn)
                .map(Kodeverdi::getValue)
                .orElse(null);
    }

    private String kanskjeFodselsdato(Person person) {
        return ofNullable(person.getFoedselsdato())
                .map(Foedselsdato::getFoedselsdato)
                .map(dato -> datoTilString(dato.toGregorianCalendar()))
                .orElse(null);
    }

    private static String kanskjeFodselsnummer(Person person) {
        Aktoer aktoer = person.getAktoer();
        if (aktoer instanceof PersonIdent) {
            return kanskjeNorskIdent((PersonIdent) aktoer);
        }
        return null;
    }

    private static String kanskjeNorskIdent(PersonIdent aktoer) {
        return ofNullable(aktoer.getIdent())
                .map(NorskIdent::getIdent)
                .orElse(null);
    }

    private String kanskjeSammensattnavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getSammensattNavn)
                .orElse(null);
    }

    private String kanskjeEtternavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getEtternavn)
                .orElse(null);
    }

    private String kanskjeFornavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getFornavn)
                .orElse(null);
    }

    private String kanskjeMellomnavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getMellomnavn)
                .orElse(null);
    }

    private static Bostedsadresse kanskjeBostedsadresse(Person person) {
        Bostedsadresse bostedsadresse = null;

        no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse wsBostedsadresse = person.getBostedsadresse();
        if (wsBostedsadresse != null) {
            bostedsadresse = new Bostedsadresse();
            bostedsadresse.withStrukturertAdresse(mapStrukturertAdresse(wsBostedsadresse.getStrukturertAdresse()));
        }
        return bostedsadresse;
    }

    private static MidlertidigAdresseNorge kanskjeMidlertidigAdresseNorge(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        MidlertidigAdresseNorge midlertidigAdresseNorge = null;

        if (person instanceof Bruker) {
            MidlertidigPostadresse wsMidlertidigPostadresse = ((Bruker) person).getMidlertidigPostadresse();
            if (wsMidlertidigPostadresse != null && wsMidlertidigPostadresse instanceof MidlertidigPostadresseNorge) {
                midlertidigAdresseNorge = new MidlertidigAdresseNorge();
                midlertidigAdresseNorge.withStrukturertAdresse(mapStrukturertAdresse(
                        ((MidlertidigPostadresseNorge) wsMidlertidigPostadresse).getStrukturertAdresse()));
            }
        }
        return midlertidigAdresseNorge;
    }

    private static MidlertidigAdresseUtland kanskjeMidlertidigAdresseUtland(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        MidlertidigAdresseUtland midlertidigAdresseUtland = null;

        if (person instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresse wsMidlertidigPostadresse =
                    ((no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) person).getMidlertidigPostadresse();
            if (wsMidlertidigPostadresse != null && wsMidlertidigPostadresse instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland) {
                midlertidigAdresseUtland = new MidlertidigAdresseUtland();
                midlertidigAdresseUtland.withUstrukturertAdresse(tilUstrukturertAdresse(
                        ((no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland) wsMidlertidigPostadresse).getUstrukturertAdresse()
                ));
            }
        }
        return midlertidigAdresseUtland;
    }

    private static PostAdresse kanskjePostAdresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        PostAdresse postAdresse = null;

        no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse wsPostadresse = person.getPostadresse();
        if (wsPostadresse != null) {
            postAdresse = new PostAdresse();
            postAdresse.withUstrukturertAdresse(tilUstrukturertAdresse(wsPostadresse.getUstrukturertAdresse()));
        }
        return postAdresse;
    }

    private static StrukturertAdresse mapStrukturertAdresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.StrukturertAdresse wsStrukturertadresse) {
        StrukturertAdresse strukturertAdresse = null;
        if (wsStrukturertadresse instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse) {
            strukturertAdresse = tilGateAdresse((no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse) wsStrukturertadresse);
        } else if (wsStrukturertadresse instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk) {
            strukturertAdresse = tilPostboksadresseNorsk((no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk) wsStrukturertadresse);
        } else if (wsStrukturertadresse instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse) {
            strukturertAdresse = tilMatrikkeladresse((no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse) wsStrukturertadresse);
        }

        if (wsStrukturertadresse.getLandkode() != null) {
            if (strukturertAdresse == null) {
                strukturertAdresse = new StrukturertAdresse();
            }
            strukturertAdresse.withLandkode(wsStrukturertadresse.getLandkode().getValue());
        }
        if (wsStrukturertadresse.getTilleggsadresse() != null) {
            if (strukturertAdresse == null) {
                strukturertAdresse = new StrukturertAdresse();
            }
            strukturertAdresse.withTilleggsadresse(wsStrukturertadresse.getTilleggsadresse());
        }
        return strukturertAdresse;
    }

    private static StrukturertAdresse tilMatrikkeladresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse wsMatrikkeladresse) {
        Optional<Matrikkelnummer> kanskjeMatrikkelnummer = ofNullable(wsMatrikkeladresse.getMatrikkelnummer());
        return new Matrikkeladresse()
                .withEiendomsnavn(ofNullable(wsMatrikkeladresse.getEiendomsnavn())
                        .orElse(null))
                .withGardsnummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getGaardsnummer)
                        .orElse(null))
                .withBruksnummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getBruksnummer)
                        .orElse(null))
                .withFestenummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getFestenummer)
                        .orElse(null))
                .withSeksjonsnummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getSeksjonsnummer)
                        .orElse(null))
                .withUndernummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getUndernummer)
                        .orElse(null))
                .withPostnummer(ofNullable(wsMatrikkeladresse.getPoststed().getValue())
                        .orElse(null));
    }

    private static PostboksadresseNorsk tilPostboksadresseNorsk(no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk wsPostboksadresseNorsk) {
        return new PostboksadresseNorsk()
                .withPostnummer(ofNullable(wsPostboksadresseNorsk.getPoststed().getValue()).orElse(null))
                .withPostboksanlegg(ofNullable(wsPostboksadresseNorsk.getPostboksanlegg()).orElse(null))
                .withPostboksnummer(ofNullable(wsPostboksadresseNorsk.getPostboksnummer()).orElse(null));
    }

    private static StrukturertAdresse tilGateAdresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse wsGateadresse) {
        return new Gateadresse()
                .withGatenavn(ofNullable(wsGateadresse.getGatenavn())
                        .orElse(null))
                .withHusnummer(ofNullable(wsGateadresse.getHusnummer())
                        .orElse(null))
                .withHusbokstav(ofNullable(wsGateadresse.getHusbokstav())
                        .orElse(null))
                .withGatenummer(ofNullable(wsGateadresse.getGatenummer())
                        .orElse(null))
                .withKommunenummer(ofNullable(wsGateadresse.getKommunenummer())
                        .orElse(null))
                .withPostnummer(ofNullable(wsGateadresse.getPoststed().getValue())
                        .orElse(null));
    }

    private static UstrukturertAdresse tilUstrukturertAdresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse wsUstrukturertAdresse) {
        return new UstrukturertAdresse()
                .withAdresselinje1(ofNullable(wsUstrukturertAdresse.getAdresselinje1())
                        .orElse(null))
                .withAdresselinje2(ofNullable(wsUstrukturertAdresse.getAdresselinje2())
                        .orElse(null))
                .withAdresselinje3(ofNullable(wsUstrukturertAdresse.getAdresselinje3())
                        .orElse(null))
                .withAdresselinje4(ofNullable(wsUstrukturertAdresse.getAdresselinje4())
                        .orElse(null))
                .withLandkode(ofNullable(wsUstrukturertAdresse.getLandkode().getValue())
                        .orElse(null));
    }

   public static String geografiskTilknytning(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        if (person instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) {
            return of(person)
                    .map(wsPerson -> ((no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) wsPerson).getGeografiskTilknytning())
                    .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.GeografiskTilknytning::getGeografiskTilknytning)
                    .orElse(null);
        }
        return null;
    }

    private static String kanskjeStatsborgerskap(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        return ofNullable(person.getStatsborgerskap())
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Statsborgerskap::getLand)
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Landkoder::getValue)
                .orElse(null);
    }

    private static String kanskjeKontonummer(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        if (person instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkonto bankkonto =
                    ((no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) person).getBankkonto();
            return kanskjeKontonummer(bankkonto);
        }
        return null;
    }

    private static String kanskjeKontonummer(no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkonto bankkonto) {
        if (bankkonto instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge bankkontoNorge =
                    (no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge) bankkonto;
            return ofNullable(bankkontoNorge.getBankkonto())
                    .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer::getBankkontonummer)
                    .orElse(null);
        } else if (bankkonto instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland wsBankkontoUtland =
                    (no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland) bankkonto;
            return ofNullable(wsBankkontoUtland.getBankkontoUtland())
                    .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontonummerUtland::getBankkontonummer)
                    .orElse(null);
        }
        return null;
    }

    private static String kanskjeDiskresjonskode(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        return ofNullable(person.getDiskresjonskode())
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Kodeverdi::getValue)
                .map(DiskresjonskodeMapper::mapTilTallkode)
                .orElse(null);
    }

    private static Sivilstand kanskjeSivilstand(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        return ofNullable(person.getSivilstand())
                .map(wsSivilstand -> new Sivilstand()
                        .withSivilstand(wsSivilstand.getSivilstand().getValue())
                        .withFraDato(datoTilString(wsSivilstand.getFomGyldighetsperiode().toGregorianCalendar())))
                .orElse(null);
    }


    private static String dodsdatoTilString(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        return of(person)
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person::getDoedsdato)
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Doedsdato::getDoedsdato)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(PersonDataMapper::datoTilString)
                .orElse(null);
    }

    private static String kanskjeMalform(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        if (person instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) {
            return of(person)
                    .map(wsPerson -> (no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) wsPerson)
                    .flatMap(wsBruker -> ofNullable(wsBruker.getMaalform()))
                    .map(Spraak::getValue)
                    .orElse(null);
        } else {
            return null;
        }
    }

    private static String datoTilString(GregorianCalendar dato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(dato.getTimeZone());
        return formatter.format(dato.getTime());
    }
}