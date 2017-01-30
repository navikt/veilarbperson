package no.nav.fo.veilarbperson.consumer.person.mappers;

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

    public PersonData tilPersonData(WSPerson person){
        return new PersonData()
                .withFornavn(kanskjeFornavn(person))
                .withMellomnavn(kanskjeMellomnavn(person))
                .withEtternavn(kanskjeEtternavn(person))
                .withSammensattNavn(kanskjeSammensattnavn(person))
                .withPersonnummer(kanskjePersonnummer(person))
                .withFodselsdato(kanskjeFodselsdato(person))
                .withKjoenn(person.getKjoenn().getKjoenn().getValue())
                .withBarn(barnMapper.familierelasjonerTilBarn(person.getHarFraRolleI()))
                .withDiskresjonskode(kanskjeDiskresjonskode(person))
                .withKontonummer(kanskjeKontonummer(person))
                .withAnsvarligEnhetsnummer(ansvarligEnhetsnummer(person))
                .withStatsborgerskap(kanskjeStatsborgerskap(person))
                .withSivilstand(hentSivilstand(person))
                .withPartner(familiemedlemMapper.partner(person.getHarFraRolleI()))
                .withBostedsadresse(kanskjeBostedsadresse(person))
                .withDodsdato(Optional.of(person)
                        .map(WSPerson::getDoedsdato)
                        .map(WSDoedsdato::getDoedsdato)
                        .map(XMLGregorianCalendar::toGregorianCalendar)
                        .map(dato -> datoTilString(dato))
                        .orElse(null));
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

    private String kanskjeMellomnavn (WSPerson person) {
        return ofNullable(person.getPersonnavn())
                .map(WSPersonnavn::getMellomnavn)
                .orElse(null);
    }

    private Bostedsadresse kanskjeBostedsadresse(WSPerson person) {
        Bostedsadresse bostedsadresse = null;

        WSBostedsadresse wsBostedsadresse = person.getBostedsadresse();
        if (wsBostedsadresse != null) {
            bostedsadresse = new Bostedsadresse();

            WSStrukturertAdresse strukturertadresse = wsBostedsadresse.getStrukturertAdresse();

            if (strukturertadresse.getLandkode() != null) {
                bostedsadresse.withLandkode(strukturertadresse.getLandkode().getValue());
            }

            if (strukturertadresse instanceof WSGateadresse) {
                bostedsadresse.withGateadresse(tilGateAdresse((WSGateadresse) strukturertadresse));
            }

            if (strukturertadresse instanceof WSPostboksadresseNorsk) {
                bostedsadresse.withPostboksadresseNorsk(tilPostboksadresseNorsk((WSPostboksadresseNorsk) strukturertadresse));
            }

            if (strukturertadresse instanceof WSMatrikkeladresse) {
                bostedsadresse.withMatrikkeladresse(tilMatrikkeladresse((WSMatrikkeladresse) strukturertadresse));
            }
        }

        return bostedsadresse;
    }

    private static Matrikkeladresse tilMatrikkeladresse(WSMatrikkeladresse wsMatrikkeladresse) {
        Optional<WSMatrikkelnummer> kanskjeMatrikkelnummer = ofNullable(wsMatrikkeladresse.getMatrikkelnummer());
        return new Matrikkeladresse()
                .withEiendomsnavn(ofNullable(wsMatrikkeladresse.getEiendomsnavn()).orElse(null))
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
                        .orElse(null));
    }

    private static PostboksadresseNorsk tilPostboksadresseNorsk(WSPostboksadresseNorsk wsPostboksadresseNorsk) {
        return new PostboksadresseNorsk()
                .withPostnummer(ofNullable(wsPostboksadresseNorsk.getPoststed().getValue()).orElse(null))
                .withPostboksanlegg(ofNullable(wsPostboksadresseNorsk.getPostboksanlegg()).orElse(null))
                .withPostboksnummer(ofNullable(wsPostboksadresseNorsk.getPostboksnummer()).orElse(null));

    }

    private Gateadresse tilGateAdresse(WSGateadresse wsGateadresse) {
        return new Gateadresse()
                .withGatenavn(ofNullable(wsGateadresse.getGatenavn()).orElse(null))
                .withHusnummer(ofNullable(wsGateadresse.getHusnummer()).orElse(null))
                .withHusbokstav(ofNullable(wsGateadresse.getHusbokstav()).orElse(null))
                .withGatenummer(ofNullable(wsGateadresse.getGatenummer()).orElse(null))
                .withPostnummer(ofNullable(wsGateadresse.getPoststed().getValue()).orElse(null))
                .withKommunenummer(ofNullable(wsGateadresse.getKommunenummer()).orElse(null));
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

    private  String kanskjeStatsborgerskap(WSPerson person) {
        String statsborgerskap = null;
        Optional<WSStatsborgerskap> wsStatsborgerskap = ofNullable(person.getStatsborgerskap());
        if (wsStatsborgerskap.isPresent()) {
            statsborgerskap = person.getStatsborgerskap().getLand().getValue();
        }
        return statsborgerskap;
    }

    private  String kanskjeKontonummer(WSPerson person) {
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

    private  String kanskjeDiskresjonskode(WSPerson person) {
        return ofNullable(person.getDiskresjonskode())
                .filter(diskresjonskode -> KODE_6.equals(diskresjonskode.getValue()) || KODE_7.equals(diskresjonskode.getValue()))
                .map(WSDiskresjonskoder::getValue)
                .orElse(null);
    }



    private Sivilstand hentSivilstand(WSPerson person) {
        WSSivilstand wsSivilstand = person.getSivilstand();
        return new Sivilstand()
                .withSivilstand(wsSivilstand.getSivilstand().getValue())
                .withFraDato(datoTilString(wsSivilstand.getFomGyldighetsperiode().toGregorianCalendar()));
    }

    private String datoTilString(GregorianCalendar dato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(dato.getTimeZone());
        return formatter.format(dato.getTime());
    }
}