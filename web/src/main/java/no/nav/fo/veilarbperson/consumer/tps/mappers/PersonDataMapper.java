package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.person.*;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.*;

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


    public PersonData tilPersonData(WSPerson person) {
        return PersonData.builder()
                .fornavn(kanskjeFornavn(person))
                .mellomnavn(kanskjeMellomnavn(person))
                .etternavn(kanskjeEtternavn(person))
                .sammensattnavn(kanskjeSammensattnavn(person))
                .personnummer(kanskjePersonnummer(person))
                .fodselsdato(kanskjeFodselsdato(person))
                .kjonn(kanskjeKjonn(person))
                .barn(barnMapper.familierelasjonerTilBarn(person.getHarFraRolleI()))
                .diskresjonskode(kanskjeDiskresjonskode(person))
                .kontonummer(kanskjeKontonummer(person))
                .ansvarligEnhetsnummer(ansvarligEnhetsnummer(person))
                .statsborgerskap(kanskjeStatsborgerskap(person))
                .sivilstand(kanskjeSivilstand(person))
                .partner(familiemedlemMapper.partner(person.getHarFraRolleI()))
                .bostedsadresse(kanskjeBostedsadresse(person))
                .midlertidigAdresseNorge(kanskjeMidlertidigAdresseNorge(person))
                .midlertidigAdresseUtland(kanskjeMidlertidigAdresseUtland(person))
                .postAdresse(kanskjePostAdresse(person))
                .dodsdato(dodsdatoTilString(person))
                .build();
    }


    private String kanskjeKjonn(WSPerson person) {
        return ofNullable(person.getKjoenn())
                .map(WSKjoenn::getKjoenn)
                .map(WSKodeverdi::getValue)
                .orElse(null);
    }

    private String kanskjeFodselsdato(WSPerson person) {
        return ofNullable(person.getFoedselsdato())
                .map(WSFoedselsdato::getFoedselsdato)
                .map(dato -> datoTilString(dato.toGregorianCalendar()))
                .orElse(null);
    }

    private String kanskjePersonnummer(WSPerson person) {
        return ofNullable(person.getIdent())
                .map(WSNorskIdent::getIdent)
                .orElse(null);
    }

    private String kanskjeSammensattnavn(WSPerson person) {
        return ofNullable(person.getPersonnavn())
                .map(WSPersonnavn::getSammensattNavn)
                .orElse(null);
    }

    private String kanskjeEtternavn(WSPerson person) {
        return ofNullable(person.getPersonnavn())
                .map(WSPersonnavn::getEtternavn)
                .orElse(null);
    }

    private String kanskjeFornavn(WSPerson person) {
        return ofNullable(person.getPersonnavn())
                .map(WSPersonnavn::getFornavn)
                .orElse(null);
    }

    private String kanskjeMellomnavn(WSPerson person) {
        return ofNullable(person.getPersonnavn())
                .map(WSPersonnavn::getMellomnavn)
                .orElse(null);
    }

    private static Bostedsadresse kanskjeBostedsadresse(WSPerson person) {
        Bostedsadresse bostedsadresse = null;

        WSBostedsadresse wsBostedsadresse = person.getBostedsadresse();
        if (wsBostedsadresse != null) {
            bostedsadresse = new Bostedsadresse();
            bostedsadresse.withStrukturertAdresse(mapStrukturertAdresse(wsBostedsadresse.getStrukturertAdresse()));
        }
        return bostedsadresse;
    }

    private static MidlertidigAdresseNorge kanskjeMidlertidigAdresseNorge(WSPerson person) {
        MidlertidigAdresseNorge midlertidigAdresseNorge = null;

        if (person instanceof WSBruker) {
            WSMidlertidigPostadresse wsMidlertidigPostadresse = ((WSBruker) person).getMidlertidigPostadresse();
            if (wsMidlertidigPostadresse != null && wsMidlertidigPostadresse instanceof WSMidlertidigPostadresseNorge) {
                midlertidigAdresseNorge = new MidlertidigAdresseNorge();
                midlertidigAdresseNorge.withStrukturertAdresse(mapStrukturertAdresse(
                        ((WSMidlertidigPostadresseNorge) wsMidlertidigPostadresse).getStrukturertAdresse()));
            }
        }
        return midlertidigAdresseNorge;
    }

    private static MidlertidigAdresseUtland kanskjeMidlertidigAdresseUtland(WSPerson person) {
        MidlertidigAdresseUtland midlertidigAdresseUtland = null;

        if (person instanceof WSBruker) {
            WSMidlertidigPostadresse wsMidlertidigPostadresse = ((WSBruker) person).getMidlertidigPostadresse();
            if (wsMidlertidigPostadresse != null && wsMidlertidigPostadresse instanceof WSMidlertidigPostadresseUtland) {
                midlertidigAdresseUtland = new MidlertidigAdresseUtland();
                midlertidigAdresseUtland.withUstrukturertAdresse(tilUstrukturertAdresse(
                        ((WSMidlertidigPostadresseUtland) wsMidlertidigPostadresse).getUstrukturertAdresse()
                ));
            }
        }
        return midlertidigAdresseUtland;
    }

    private static PostAdresse kanskjePostAdresse(WSPerson person) {
        PostAdresse postAdresse = null;

        WSPostadresse wsPostadresse = person.getPostadresse();
        if (wsPostadresse != null) {
            postAdresse = new PostAdresse();
            postAdresse.withUstrukturertAdresse(tilUstrukturertAdresse(wsPostadresse.getUstrukturertAdresse()));
        }
        return postAdresse;
    }

    private static StrukturertAdresse mapStrukturertAdresse(WSStrukturertAdresse wsStrukturertadresse) {
        StrukturertAdresse strukturertAdresse = null;
        if (wsStrukturertadresse instanceof WSGateadresse) {
            strukturertAdresse = tilGateAdresse((WSGateadresse) wsStrukturertadresse);
        }

        if (wsStrukturertadresse instanceof WSPostboksadresseNorsk) {
            strukturertAdresse = tilPostboksadresseNorsk((WSPostboksadresseNorsk) wsStrukturertadresse);
        }

        if (wsStrukturertadresse instanceof WSMatrikkeladresse) {
            strukturertAdresse = tilMatrikkeladresse((WSMatrikkeladresse) wsStrukturertadresse);
        }

        if (wsStrukturertadresse.getLandkode() != null) {
            if (strukturertAdresse == null) {
                strukturertAdresse = new StrukturertAdresse();
            }
            strukturertAdresse.withLandkode(wsStrukturertadresse.getLandkode().getValue());
        }
        return strukturertAdresse;
    }

    private static StrukturertAdresse tilMatrikkeladresse(WSMatrikkeladresse wsMatrikkeladresse) {
        Optional<WSMatrikkelnummer> kanskjeMatrikkelnummer = ofNullable(wsMatrikkeladresse.getMatrikkelnummer());
        return new Matrikkeladresse()
                .withEiendomsnavn(ofNullable(wsMatrikkeladresse.getEiendomsnavn())
                        .orElse(null))
                .withGardsnummer(kanskjeMatrikkelnummer
                        .map(WSMatrikkelnummer::getGaardsnummer)
                        .orElse(null))
                .withBruksnummer(kanskjeMatrikkelnummer
                        .map(WSMatrikkelnummer::getBruksnummer)
                        .orElse(null))
                .withFestenummer(kanskjeMatrikkelnummer
                        .map(WSMatrikkelnummer::getFestenummer)
                        .orElse(null))
                .withSeksjonsnummer(kanskjeMatrikkelnummer
                        .map(WSMatrikkelnummer::getSeksjonsnummer)
                        .orElse(null))
                .withUndernummer(kanskjeMatrikkelnummer
                        .map(WSMatrikkelnummer::getUndernummer)
                        .orElse(null))
                .withPostnummer(ofNullable(wsMatrikkeladresse.getPoststed().getValue())
                        .orElse(null));


    }

    private static PostboksadresseNorsk tilPostboksadresseNorsk(WSPostboksadresseNorsk wsPostboksadresseNorsk) {
        return new PostboksadresseNorsk()
                .withPostnummer(ofNullable(wsPostboksadresseNorsk.getPoststed().getValue()).orElse(null))
                .withPostboksanlegg(ofNullable(wsPostboksadresseNorsk.getPostboksanlegg()).orElse(null))
                .withPostboksnummer(ofNullable(wsPostboksadresseNorsk.getPostboksnummer()).orElse(null));

    }

    private static StrukturertAdresse tilGateAdresse(WSGateadresse wsGateadresse) {

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

    private static UstrukturertAdresse tilUstrukturertAdresse(WSUstrukturertAdresse wsUstrukturertAdresse) {
        return new UstrukturertAdresse()
                .withAdresselinje1(ofNullable(wsUstrukturertAdresse.getAdresselinje1())
                        .orElse(null))
                .withAdresselinje2(ofNullable(wsUstrukturertAdresse.getAdresselinje2())
                        .orElse(null))
                .withAdresselinje3(ofNullable(wsUstrukturertAdresse.getAdresselinje3())
                        .orElse(null))
                .withAdresselinje4(ofNullable(wsUstrukturertAdresse.getAdresselinje4())
                        .orElse(null));
    }

    private static String ansvarligEnhetsnummer(WSPerson person) {
        if (person instanceof WSBruker) {
            return of(person)
                    .map(wsPerson -> ((WSBruker) wsPerson).getHarAnsvarligEnhet())
                    .map(WSAnsvarligEnhet::getEnhet)
                    .map(WSOrganisasjonsenhet::getOrganisasjonselementID).orElse(null);
        }
        return null;
    }

    private static String kanskjeStatsborgerskap(WSPerson person) {
        return ofNullable(person.getStatsborgerskap())
                .map(WSStatsborgerskap::getLand)
                .map(WSLandkoder::getValue)
                .orElse(null);
    }

    private static String kanskjeKontonummer(WSPerson person) {
        WSBankkonto bankkonto = person.getBankkonto();
        String kontonummer = null;

        if (bankkonto instanceof WSBankkontoNorge) {
            WSBankkontoNorge bankkontoNorge = (WSBankkontoNorge) bankkonto;
            kontonummer = ofNullable(bankkontoNorge.getBankkonto()).map(WSBankkontonummer::getBankkontonummer).orElse(null);
        }

        if (bankkonto instanceof WSBankkontoUtland) {
            WSBankkontoUtland wsBankkontoUtland = (WSBankkontoUtland) bankkonto;
            kontonummer = ofNullable(wsBankkontoUtland.getBankkontoUtland()).map(WSBankkontonummerUtland::getBankkontonummer).orElse(null);
        }

        return kontonummer;
    }

    private static String kanskjeDiskresjonskode(WSPerson person) {
        return ofNullable(person.getDiskresjonskode())
                .filter(diskresjonskode -> KODE_6.equals(diskresjonskode.getValue()) || KODE_7.equals(diskresjonskode.getValue()))
                .map(WSDiskresjonskoder::getValue)
                .orElse(null);
    }


    private static Sivilstand kanskjeSivilstand(WSPerson person) {
        return ofNullable(person.getSivilstand())
                .map(wsSivilstand -> new Sivilstand()
                        .withSivilstand(wsSivilstand.getSivilstand().getValue())
                        .withFraDato(datoTilString(wsSivilstand.getFomGyldighetsperiode().toGregorianCalendar())))
                .orElse(null);
    }


    private static String dodsdatoTilString(WSPerson person) {
        return of(person)
                .map(WSPerson::getDoedsdato)
                .map(WSDoedsdato::getDoedsdato)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(PersonDataMapper::datoTilString)
                .orElse(null);
    }

    private static String datoTilString(GregorianCalendar dato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(dato.getTimeZone());
        return formatter.format(dato.getTime());
    }
}