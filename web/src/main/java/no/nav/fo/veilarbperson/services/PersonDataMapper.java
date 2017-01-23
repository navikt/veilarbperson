package no.nav.fo.veilarbperson.services;

import no.nav.fo.veilarbperson.domain.Sivilstand;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.*;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;


class PersonDataMapper{

    private static final String BARN = "BARN";
    private static final String EKTEFELLE = "EKTE";

    public static PersonData tilPersonData(WSPerson person){
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
                .withPartner(partner(person.getHarFraRolleI()));
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
        if (wsStatsborgerskap.isPresent()){
            statsborgerskap =  person.getStatsborgerskap().getLand().getValue();
        }
        return statsborgerskap;
    }

    private static String kanskjeKontonummer(WSPerson person) {
        WSBankkonto bankkonto = person.getBankkonto();
        String kontonummer = null;

        if(bankkonto instanceof WSBankkontoNorge){
            WSBankkontoNorge bankkontoNorge = (WSBankkontoNorge) bankkonto;
            kontonummer = bankkontoNorge.getBankkonto().getBankkontonummer();
        }

            if(bankkonto instanceof WSBankkontoUtland){
            WSBankkontoUtland wsBankkontoUtland = (WSBankkontoUtland) bankkonto;
            kontonummer =  wsBankkontoUtland.getBankkontoUtland().getBankkontonummer();
        }

        return kontonummer;
    }

    private static String kanskjeDiskresjonskode(WSPerson person) {
        return ofNullable(person.getDiskresjonskode())
                .map(WSDiskresjonskoder::getValue)
                .orElse(null);
    }

    private static List<Familiemedlem> familierelasjonerTilBarn(List<WSFamilierelasjon> familierelasjoner) {
       return  familierelasjoner.stream()
                .filter(familierelasjon -> BARN.equals(familierelasjon.getTilRolle().getValue()))
                .map(barnWS -> familierelasjonTilFamiliemedlem(barnWS))
                .collect(toList());
    }

    private static Familiemedlem partner(List<WSFamilierelasjon> familierelasjoner) {
        for (WSFamilierelasjon relasjon : familierelasjoner) {
            if (EKTEFELLE.equals(relasjon.getTilRolle().getValue())) {
                return familierelasjonTilFamiliemedlem(relasjon);
            }
        }
        return null;
    }

    private static String personnummerTilFodselsdato(String personnummer) {
        final String aar = personnummerTilAarstall(personnummer);
        final String maaned = personnummerTilMaaned(personnummer);
        final String dag = personnummerTilDag(personnummer);
        return aar + "-" + maaned + "-" + dag;
    }

    private static String personnummerTilAarstall(String personnummer) {
        int aarsiffer = Integer.parseInt(personnummer.substring(4, 6));
        int individsiffer = Integer.parseInt(personnummer.substring(6, 9));
        int aarstall;
        if (individsiffer < 500) {
            aarstall = 1900 + aarsiffer;
        } else if (individsiffer < 750 && aarsiffer >= 54) {
            aarstall = 1800 + aarsiffer;
        } else if (individsiffer < 900 || aarsiffer < 40) {
            aarstall = 2000 + aarsiffer;
        } else {
            aarstall = 1900 + aarsiffer;
        }
        return Integer.toString(aarstall);
    }

    private static String personnummerTilMaaned(String personnummer) {
        int maanedsiffer = Integer.parseInt(personnummer.substring(2, 4));
        if (maanedsiffer > 12) {
            maanedsiffer -= 40;
        }
        return Integer.toString(maanedsiffer);
    }

    private static String personnummerTilDag(String personnummer) {
        int dagsiffer = Integer.parseInt(personnummer.substring(0, 2));
        if (dagsiffer > 31) {
            dagsiffer -= 40;
        }
        return Integer.toString(dagsiffer);
    }

    private static Familiemedlem familierelasjonTilFamiliemedlem(WSFamilierelasjon familierelasjon) {

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

    private static String personnummerTilKjoenn(String personnummer) {
        if (Integer.parseInt(personnummer.substring(8, 9)) % 2 == 0) {
            return "K";
        } else {
            return "M";
        }
    }

    private static Sivilstand hentSivilstand(WSPerson person) {
        WSSivilstand wsSivilstand = person.getSivilstand();
        return new Sivilstand()
                .withSivilstand(wsSivilstand.getSivilstand().getValue())
                .withFraDato(datoTilString(wsSivilstand.getFomGyldighetsperiode().toGregorianCalendar()));
    }

    private static String datoTilString(GregorianCalendar dato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(dato.getTimeZone());
        return formatter.format(dato.getTime());
    }
}