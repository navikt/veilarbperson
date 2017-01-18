package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.person.v2.informasjon.*;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

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
                .withFodselsdato(fodseldatoTilString(person.getFoedselsdato().getFoedselsdato().toGregorianCalendar()))
                .withKjoenn(person.getKjoenn().getKjoenn().getValue())
                .withBarn(familierelasjonerTilBarn(person.getHarFraRolleI()))
                .withDiskresjonskode(kanskjeDiskresjonskode(person))
                .withKontonummer(kanskjeKontonummer(person))
                .withAnsvarligEnhetsnummer(ansvarligEnhetsnummer(person))
                .withPartner(partner(person.getHarFraRolleI()));
    }

    private static Familiemedlem partner(List<WSFamilierelasjon> familierelasjoner) {
        for (WSFamilierelasjon relasjon : familierelasjoner) {
            if (EKTEFELLE.equals(relasjon.getTilRolle().getValue())) {
                WSPerson person = relasjon.getTilPerson();
                return new Familiemedlem()
                        .withFornavn(person.getPersonnavn().getFornavn())
                        .withEtternavn(person.getPersonnavn().getEtternavn())
                        .withSammensattnavn(person.getPersonnavn().getSammensattNavn())
                        .withPersonnummer(person.getIdent().getIdent());
            }
        }
        return null;
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
                .map(WSDiskresjonskoder::getValue)
                .orElse(null);
    }

    private static List<Familiemedlem> familierelasjonerTilBarn(List<WSFamilierelasjon> familierelasjoner) {
       return  familierelasjoner.stream()
                .filter(familierelasjon -> BARN.equals(familierelasjon.getTilRolle().getValue()))
                .map(barnWS -> familierelasjonTilBarn(barnWS))
                .collect(toList());
    }

    private static Familiemedlem familierelasjonTilBarn(WSFamilierelasjon familierelasjon) {

        WSPerson person = familierelasjon.getTilPerson();

        return new Familiemedlem()
                .withFornavn(person.getPersonnavn().getFornavn())
                .withEtternavn(person.getPersonnavn().getEtternavn())
                .withSammensattnavn(person.getPersonnavn().getSammensattNavn())
                .withHarSammeBosted(familierelasjon.isHarSammeBosted())
                .withPersonnummer(person.getIdent().getIdent());

    }

    private static String ansvarligEnhetsnummer(WSPerson person) {
        if (person instanceof WSBruker) {
            WSAnsvarligEnhet ansvarligEnhet = ((WSBruker) person).getHarAnsvarligEnhet();
            if (ansvarligEnhet != null && ansvarligEnhet.getEnhet() != null) {
                return ansvarligEnhet.getEnhet().getOrganisasjonselementID();
            }
        }
        return null;
    }

    private static String fodseldatoTilString(GregorianCalendar foedselsdato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(foedselsdato.getTimeZone());
        return formatter.format(foedselsdato.getTime());
    }
}