package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.*;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Optional.ofNullable;

public class PersonDataMapper {


    private static final String KODE_6 = "6";
    private static final String KODE_7 = "7";

    private final BarnMapper barnMapper = new BarnMapper();
    private final FamiliemedlemMapper familiemedlemMapper = new FamiliemedlemMapper();

    public PersonData tilPersonData(WSPerson person) {
        return new PersonData()
                .withFornavn(kanskjeFornavn(person))
                .withMellomnavn(kanskjeMellomnavn(person))
                .withEtternavn(kanskjeEtternavn(person))
                .withSammensattNavn(kanskjeSammensattnavn(person))
                .withPersonnummer(kanskjePersonnummer(person))
                .withFodselsdato(kanskjeFodselsdato(person))
                .withKjoenn(kanskjeKjonn(person))
                .withBarn(barnMapper.familierelasjonerTilBarn(person.getHarFraRolleI()))
                .withDiskresjonskode(kanskjeDiskresjonskode(person))
                .withKontonummer(kanskjeKontonummer(person))
                .withAnsvarligEnhetsnummer(ansvarligEnhetsnummer(person))
                .withStatsborgerskap(kanskjeStatsborgerskap(person))
                .withSivilstand(kanskjeSivilstand(person))
                .withPartner(familiemedlemMapper.partner(person.getHarFraRolleI()))
                .withBostedsadresse(kanskjeBostedsadresse(person))
                .withDodsdato(dodsdatoTilString(person));
    }

    private String kanskjeKjonn(WSPerson person) {
        return ofNullable(person.getKjoenn())
                .map(WSKjoenn::getKjoenn)
                .map(kjonn -> kjonn.getValue())
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

            WSStrukturertAdresse wsStrukturertadresse = wsBostedsadresse.getStrukturertAdresse();

            if (wsStrukturertadresse instanceof WSGateadresse) {
                bostedsadresse.withStrukturertAdresse(tilGateAdresse((WSGateadresse) wsStrukturertadresse));
            }

            if (wsStrukturertadresse instanceof WSPostboksadresseNorsk) {
                bostedsadresse.withStrukturertAdresse(tilPostboksadresseNorsk((WSPostboksadresseNorsk) wsStrukturertadresse));
            }

            if (wsStrukturertadresse instanceof WSMatrikkeladresse) {
                bostedsadresse.withStrukturertAdresse(tilMatrikkeladresse((WSMatrikkeladresse) wsStrukturertadresse));
            }

            if (wsStrukturertadresse.getLandkode() != null) {
                bostedsadresse.getStrukturertAdresse().withLandkode(wsStrukturertadresse.getLandkode().getValue());
            }

        }

        return bostedsadresse;
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

    private static String ansvarligEnhetsnummer(WSPerson person) {
        if (person instanceof WSBruker) {
            return Optional.of(person)
                    .map(wsPerson -> ((WSBruker) wsPerson).getHarAnsvarligEnhet())
                    .map(WSAnsvarligEnhet::getEnhet)
                    .map(WSOrganisasjonsenhet::getOrganisasjonselementID).orElse(null);
        }
        return null;
    }

    private static String kanskjeStatsborgerskap(WSPerson person) {
        String statsborgerskap = null;
        Optional<WSStatsborgerskap> wsStatsborgerskap = ofNullable(person.getStatsborgerskap());
        if (wsStatsborgerskap.isPresent()) {
            statsborgerskap = person.getStatsborgerskap().getLand().getValue();
        }
        return statsborgerskap;
    }

    private static String kanskjeKontonummer(WSPerson person) {
        WSBankkonto bankkonto = person.getBankkonto();
        String kontonummer = null;

        if (bankkonto instanceof WSBankkontoNorge) {
            WSBankkontoNorge bankkontoNorge = (WSBankkontoNorge) bankkonto;
            kontonummer = bankkontoNorge.getBankkonto().getBankkontonummer();
        }

        if (bankkonto instanceof WSBankkontoUtland) {
            WSBankkontoUtland wsBankkontoUtland = (WSBankkontoUtland) bankkonto;
            kontonummer = wsBankkontoUtland.getBankkontoUtland().getBankkontonummer();
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
                .map(wsSivilstand -> {
                            return new Sivilstand()
                                    .withSivilstand(wsSivilstand.getSivilstand().getValue())
                                    .withFraDato(datoTilString(wsSivilstand.getFomGyldighetsperiode().toGregorianCalendar()));
                        })
                .orElse(null);
    }


    private static String dodsdatoTilString(WSPerson person) {
        return Optional.of(person)
                .map(WSPerson::getDoedsdato)
                .map(WSDoedsdato::getDoedsdato)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(dato -> datoTilString(dato))
                .orElse(null);
    }

    private static String datoTilString(GregorianCalendar dato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(dato.getTimeZone());
        return formatter.format(dato.getTime());
    }
}