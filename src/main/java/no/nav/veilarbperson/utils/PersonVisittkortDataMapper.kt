package no.nav.veilarbperson.utils

import no.nav.veilarbperson.client.pdl.HentPerson
import no.nav.veilarbperson.client.pdl.domain.Diskresjonskode
import no.nav.veilarbperson.client.pdl.domain.Telefon
import no.nav.veilarbperson.domain.PersonVisittkortData

object PersonVisittkortDataMapper {

    fun tilPersonVisittkortData(
        person: HentPerson.Person,
        erSkjermet: Boolean,
        krrTelefon: String?,
        krrTelefonOppdatert: String?
    ): PersonVisittkortData {
        val navn = PersonV2DataMapper.hentGjeldeneNavn(person.navn).orElse(null)

        val telefoner = PersonV2DataMapper.mapTelefonNrFraPdl(person.telefonnummer).toMutableList()
        leggKrrTelefonIListe(krrTelefon, krrTelefonOppdatert, telefoner)

        return PersonVisittkortData(
            fornavn = navn?.fornavn,
            mellomnavn = navn?.mellomnavn,
            etternavn = navn?.etternavn,
            fodselsdato = PersonV2DataMapper.getFirstElement(person.foedselsdato)?.foedselsdato?.toString(),
            dodsdato = PersonV2DataMapper.getFirstElement(person.doedsfall)?.doedsdato?.toString(),
            kjonn = PersonV2DataMapper.getFirstElement(person.kjoenn)?.kjoenn,
            diskresjonskode = PersonV2DataMapper.getFirstElement(person.adressebeskyttelse)
                ?.gradering
                ?.let { Diskresjonskode.mapKodeTilTall(it) },
            egenAnsatt = erSkjermet,
            sikkerhetstiltak = PersonV2DataMapper.getFirstElement(person.sikkerhetstiltak)?.beskrivelse,
            telefon = telefoner
        )
    }

    /** KRR-telefon får alltid prioritet 1. PDL-nummer med samme nummer fjernes. Øvrige PDL-prioriteter bumpes. */
    private fun leggKrrTelefonIListe(krrTelefon: String?, oppdatert: String?, liste: MutableList<Telefon>) {
        if (krrTelefon == null) return
        liste.removeIf { krrTelefon == it.telefonNr }
        liste.filter { it.master != "KRR" }
            .forEach { it.setPrioritet((it.prioritet.toInt() + 1).toString()) }
        liste.add(0, Telefon().setPrioritet("1").setTelefonNr(krrTelefon).setRegistrertDato(oppdatert).setMaster("KRR"))
    }
}
