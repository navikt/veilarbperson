package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.person.v2.informasjon.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;


class PersonDataMapper{

    private static final String BARN = "BARN";

    public static PersonData tilPersonData(WSPerson person){
        return new PersonData()
                .medFornavn(person.getPersonnavn().getFornavn())
                .medMellomnavn(person.getPersonnavn().getMellomnavn())
                .medEtternavn(person.getPersonnavn().getEtternavn())
                .medSammensattNavn(person.getPersonnavn().getSammensattNavn())
                .medPersonnummer(person.getIdent().getIdent())
                .medFodselsdato(fodseldatoTilString(person.getFoedselsdato().getFoedselsdato().toGregorianCalendar()))
                .medKjoenn(person.getKjoenn().getKjoenn().getValue())
                .medBarn(familierelasjonerTilBarn(person.getHarFraRolleI()))
                .medDiskresjonskode(kanskjeDiskresjonskode(person))
                .medKontonummer(kanskjeKontonummer(person));
    }

    private static String kanskjeKontonummer(WSPerson person) {
        WSBankkonto bankkonto = person.getBankkonto();
        String kontonummer = null;

        if(bankkonto instanceof WSBankkontoNorge){
            WSBankkontoNorge bankkontoNorge = (WSBankkontoNorge) bankkonto;
            kontonummer = bankkontoNorge.getBankkonto().getBankkontonummer();
        }

            if(bankkonto instanceof WSBankkontoUtland){
            WSBankkontoUtland WSBankkontoUtland = (WSBankkontoUtland) bankkonto;
            kontonummer =  WSBankkontoUtland.getBankkontoUtland().getBankkontonummer();
        }

        return kontonummer;
    }

    private static String kanskjeDiskresjonskode(WSPerson person) {
        return ofNullable(person.getDiskresjonskode())
                //.filter(diskresjonskode -> aksepterteKoder(diskresjonskode))
                .map(WSDiskresjonskoder::getValue)
                .orElse(null);
    }

    private static boolean aksepterteKoder(WSDiskresjonskoder diskresjonskode) {
        return "6".equals(diskresjonskode.getValue()) || "7".equals(diskresjonskode.getValue());
    }

    private static List<Barn> familierelasjonerTilBarn(List<WSFamilierelasjon> familierelasjoner) {
       return  familierelasjoner.stream()
                .filter(familierelasjon -> BARN.equals(familierelasjon.getTilRolle().getValue()))
                .map(barnWS -> familierelasjonTilBarn(barnWS))
                .collect(toList());
    }

    private static Barn familierelasjonTilBarn(WSFamilierelasjon familierelasjon) {

        WSPerson person = familierelasjon.getTilPerson();

        return new Barn()
                .medFornavn(person.getPersonnavn().getFornavn())
                .medEtternavn(person.getPersonnavn().getEtternavn())
                .medSammensattnavn(person.getPersonnavn().getSammensattNavn())
                .medHarSammeBosted(familierelasjon.isHarSammeBosted())
                .medPersonnummer(person.getIdent().getIdent());

    }

    private static String fodseldatoTilString(GregorianCalendar foedselsdato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(foedselsdato.getTimeZone());
        return formatter.format(foedselsdato.getTime());
    }
}