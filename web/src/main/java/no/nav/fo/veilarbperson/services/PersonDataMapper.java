package no.nav.fo.veilarbperson.services;

import no.nav.fo.veilarbperson.domain.*;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static no.nav.fo.veilarbperson.utils.Personnummer.personnummerTilFodselsdato;
import static no.nav.fo.veilarbperson.utils.Personnummer.personnummerTilKjoenn;

public class PersonDataMapper {

    private static final String BARN = "BARN";
    private static final String KODE_6 = "6";
    private static final String KODE_7 = "7";
    private static final String EKTEFELLE = "EKTE";

    PersonData tilPersonData(WSPerson person){
        return new PersonData()
                .withFornavn(person.getPersonnavn().getFornavn())
                .withMellomnavn(person.getPersonnavn().getMellomnavn())
                .withEtternavn(person.getPersonnavn().getEtternavn())
                .withSammensattNavn(person.getPersonnavn().getSammensattNavn())
                .withPersonnummer(person.getIdent().getIdent())
                .withFodselsdato(datoTilString(person.getFoedselsdato().getFoedselsdato().toGregorianCalendar()))
                .withKjoenn(person.getKjoenn().getKjoenn().getValue())
                .withBarn(familierelasjonerTilBarn(person.getHarFraRolleI()))
                .withDiskresjonskode(kanskjeDiskresjonskode(person))
                .withKontonummer(kanskjeKontonummer(person))
                .withAnsvarligEnhetsnummer(ansvarligEnhetsnummer(person))
                .withStatsborgerskap(kanskjeStatsborgerskap(person))
                .withSivilstand(hentSivilstand(person))
                .withPartner(partner(person.getHarFraRolleI()))
                .withBostedsadresse(kanskjeBostedsadresse(person))
                .withDodsdato(Optional.of(person)
                        .map(WSPerson::getDoedsdato)
                        .map(WSDoedsdato::getDoedsdato)
                        .map(XMLGregorianCalendar::toGregorianCalendar)
                        .map(dato -> datoTilString(dato))
                        .orElse(null));
    }

    private Bostedsadresse kanskjeBostedsadresse(WSPerson person) {
        Bostedsadresse bostedsadresse = null;

        WSBostedsadresse wsBostedsadresse = person.getBostedsadresse();
        if (wsBostedsadresse != null) {
            bostedsadresse = new Bostedsadresse();

            WSStrukturertAdresse wsStrukturertadresse = wsBostedsadresse.getStrukturertAdresse();

            if (wsStrukturertadresse instanceof WSGateadresse) {
                bostedsadresse.withStrukturertAdresse(tilGateAdresse((WSGateadresse) wsStrukturertadresse));
            }

            if (wsStrukturertadresse instanceof WSPostboksadresseNorsk) {
                bostedsadresse.withPostboksadresseNorsk(tilPostboksadresseNorsk((WSPostboksadresseNorsk) wsStrukturertadresse));
            }

            if (wsStrukturertadresse instanceof WSMatrikkeladresse) {
                bostedsadresse.withMatrikkeladresse(tilMatrikkeladresse((WSMatrikkeladresse) wsStrukturertadresse));
            }

            if (wsStrukturertadresse.getLandkode() != null) {
                bostedsadresse.getStrukturertAdresse().withLandkode(wsStrukturertadresse.getLandkode().getValue());
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

    private StrukturertAdresse tilGateAdresse(WSGateadresse wsGateadresse) {

        return new Gateadresse()
                .withGatenavn(ofNullable(wsGateadresse.getGatenavn()).orElse(null))
                .withHusnummer(ofNullable(wsGateadresse.getHusnummer()).orElse(null))
                .withHusbokstav(ofNullable(wsGateadresse.getHusbokstav()).orElse(null))
                .withGatenummer(ofNullable(wsGateadresse.getGatenummer()).orElse(null))
                .withKommunenummer(ofNullable(wsGateadresse.getKommunenummer()).orElse(null))
                .withPostnummer(ofNullable(wsGateadresse.getPoststed().getValue()).orElse(null));
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

    private  List<Familiemedlem> familierelasjonerTilBarn(List<WSFamilierelasjon> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> BARN.equals(familierelasjon.getTilRolle().getValue()))
                .map(relasjon -> familierelasjonTilFamiliemedlem(relasjon))
                .collect(toList());
    }

    private  Familiemedlem partner(List<WSFamilierelasjon> familierelasjoner) {
        for (WSFamilierelasjon relasjon : familierelasjoner) {
            if (EKTEFELLE.equals(relasjon.getTilRolle().getValue())) {
                return familierelasjonTilFamiliemedlem(relasjon);
            }
        }
        return null;
    }

    private  Familiemedlem familierelasjonTilFamiliemedlem(WSFamilierelasjon familierelasjon) {

        WSPerson person = familierelasjon.getTilPerson();
        final String personnummer = person.getIdent().getIdent();

        return new Familiemedlem()
                .withFornavn(person.getPersonnavn().getFornavn())
                .withEtternavn(person.getPersonnavn().getEtternavn())
                .withSammensattnavn(person.getPersonnavn().getSammensattNavn())
                .withHarSammeBosted(familierelasjon.isHarSammeBosted())
                .withPersonnummer(personnummer)
                .withFodselsdato(personnummerTilFodselsdato(personnummer))
                .withKjoenn(personnummerTilKjoenn(personnummer));
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