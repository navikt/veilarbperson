package no.nav.veilarbperson.service

import lombok.extern.slf4j.Slf4j
import no.nav.common.client.norg2.Enhet
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.client.digdir.DigdirClient
import no.nav.veilarbperson.client.digdir.KRRPostPersonerRequest
import no.nav.veilarbperson.client.nom.SkjermetClient
import no.nav.veilarbperson.client.pdl.HentPerson
import no.nav.veilarbperson.client.pdl.HentPerson.*
import no.nav.veilarbperson.client.pdl.PdlClient
import no.nav.veilarbperson.client.pdl.domain.*
import no.nav.veilarbperson.client.pdl.domain.Adresse.Utenlandskadresse
import no.nav.veilarbperson.client.pdl.domain.Adresse.Vegadresse
import no.nav.veilarbperson.client.pdl.domain.Bostedsadresse.UkjentBosted
import no.nav.veilarbperson.client.pdl.domain.Kontaktadresse.PostadresseIFrittFormat
import no.nav.veilarbperson.client.pdl.domain.Kontaktadresse.UtenlandskAdresseIFrittFormat
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient
import no.nav.veilarbperson.domain.*
import no.nav.veilarbperson.utils.PersonV2DataMapper
import no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.time.ZonedDateTime
import java.util.*
import java.util.Set
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.listOf

@Slf4j
@Service
class PersonV2Service @Autowired constructor(
    private val pdlClient: PdlClient,
    @param:Qualifier("authServiceWithoutAuditLog") private val authService: AuthService,
    private val digdirClient: DigdirClient,
    private val norg2Client: Norg2Client,
    private val skjermetClient: SkjermetClient,
    private val kodeverkService: KodeverkService,
    private val representasjonClient: RepresentasjonClient
) {
    fun hentPerson(personFraPdlRequest: PersonFraPdlRequest): HentPerson.Person {
        return pdlClient.hentPerson(PdlRequest(personFraPdlRequest.fnr, personFraPdlRequest.behandlingsnummer))
    }

    fun hentFlettetPerson(personFraPdlRequest: PersonFraPdlRequest): PersonV2Data {
        val personDataFraPdl = Optional.ofNullable(
            pdlClient.hentPerson(
                PdlRequest(
                    personFraPdlRequest.fnr,
                    personFraPdlRequest.behandlingsnummer
                )
            )
        )
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Fant ikke person i hentPerson operasjonen i PDL"
                )
            }

        val personV2Data = PersonV2DataMapper.toPersonV2Data(personDataFraPdl)
        flettInnEgenAnsatt(personV2Data, personFraPdlRequest.fnr)
        flettBarn(personDataFraPdl.forelderBarnRelasjon, personV2Data, personFraPdlRequest.behandlingsnummer)
        flettSivilstand(personDataFraPdl.sivilstand, personV2Data, personFraPdlRequest.behandlingsnummer)
        flettDigitalKontaktinformasjon(personFraPdlRequest.fnr, personV2Data)
        flettGeografiskEnhet(personFraPdlRequest, personV2Data)
        flettKodeverk(personV2Data)

        return personV2Data
    }

    fun hentFamiliemedlemOpplysninger(
        familemedlemFnr: List<Fnr>?,
        bostedsadresse: Bostedsadresse?,
        behandlingsnummer: String?
    ): List<Familiemedlem> {
        val familiemedlemInfo = pdlClient.hentPersonBolk(familemedlemFnr, behandlingsnummer)

        return familiemedlemInfo
            .stream()
            .filter { medlemInfo: PersonFraBolk -> medlemInfo.code == "ok" }
            .map<HentPerson.Familiemedlem> { PersonFraBolk.getPerson() }
            .filter { familiemedlem: HentPerson.Familiemedlem? -> PersonV2DataMapper.harGyldigIdent(familiemedlem) }
            .map<Familiemedlem> { familiemedlem: HentPerson.Familiemedlem ->
                mapFamiliemedlem(
                    familiemedlem,
                    bostedsadresse
                )
            }
            .collect(Collectors.toList<Familiemedlem>())
    }

    private fun erSkjermet(fnr: Fnr): Boolean {
        return skjermetClient.hentSkjermet(fnr)
    }

    fun mapFamiliemedlem(familiemedlem: HentPerson.Familiemedlem, bostedsadresse: Bostedsadresse?): Familiemedlem {
        val familiemedlemFnr = PersonV2DataMapper.hentFamiliemedlemFnr(familiemedlem)

        return PersonV2DataMapper.familiemedlemMapper(
            familiemedlem,
            erSkjermet(familiemedlemFnr),
            bostedsadresse,
            authService
        )
    }

    fun hentBarnaFnr(familierelasjoner: List<ForelderBarnRelasjon>): List<Fnr> {
        return familierelasjoner.stream()
            .filter { familierelasjon: ForelderBarnRelasjon -> "BARN" == familierelasjon.relatertPersonsRolle }
            .map<String> { ForelderBarnRelasjon.getRelatertPersonsIdent() }
            .filter { obj: String? -> Objects.nonNull(obj) }
            .map<Fnr> { fnrStr: String? -> Fnr.of(fnrStr) }
            .collect(Collectors.toList<Fnr>())
    }

    fun flettBarn(
        forelderBarnRelasjoner: List<ForelderBarnRelasjon>,
        personV2Data: PersonV2Data,
        behandlingsnummer: String?
    ) {
        val barnFnrListe = hentBarnaFnr(forelderBarnRelasjoner)
        val barnInfo = hentFamiliemedlemOpplysninger(barnFnrListe, personV2Data.bostedsadresse, behandlingsnummer)

        personV2Data.setBarn(barnInfo)
    }

    fun flettSivilstand(
        sivilstands: List<HentPerson.Sivilstand>,
        personV2Data: PersonV2Data,
        behandlingsnummer: String?
    ) {
        val mappetSivilstand = sivilstands.stream().flatMap { sivilstand: HentPerson.Sivilstand ->
            val relatert = Optional.ofNullable(sivilstand.relatertVedSivilstand)
                .map { fnrStr: String? -> Fnr.of(fnrStr) }
                .map { fnr: Fnr ->
                    hentFamiliemedlemOpplysninger(
                        java.util.List.of(fnr),
                        personV2Data.bostedsadresse,
                        behandlingsnummer
                    )
                }
                .flatMap { list: List<Familiemedlem> ->
                    list.stream().findFirst()
                }
            Stream.of(PersonV2DataMapper.sivilstandMapper(sivilstand, relatert))
        }.collect(Collectors.toList())

        personV2Data.setSivilstandliste(mappetSivilstand)
    }

    private fun flettInnEgenAnsatt(personV2Data: PersonV2Data, fodselsnummer: Fnr) {
        val egenAnsatt = skjermetClient.hentSkjermet(fodselsnummer)
        personV2Data.setEgenAnsatt(egenAnsatt)
    }

    fun hentGeografiskTilknytning(personFraPdlRequest: PersonFraPdlRequest): GeografiskTilknytning? {
        val geografiskTilknytning = pdlClient.hentGeografiskTilknytning(
            PdlRequest(
                personFraPdlRequest.fnr,
                personFraPdlRequest.behandlingsnummer
            )
        )
            ?: return null

        return when (geografiskTilknytning.gtType) {
            "KOMMUNE" -> GeografiskTilknytning(geografiskTilknytning.gtKommune)
            "BYDEL" -> GeografiskTilknytning(geografiskTilknytning.gtBydel)
            "UTLAND" -> GeografiskTilknytning(geografiskTilknytning.gtLand)
            else ->  // type == UDEFINERT
                null
        }
    }

    private fun flettGeografiskEnhet(personFraPdlRequest: PersonFraPdlRequest, personV2Data: PersonV2Data) {
        val geografiskTilknytning = Optional.ofNullable(hentGeografiskTilknytning(personFraPdlRequest))
            .map { obj: GeografiskTilknytning -> obj.geografiskTilknytning }
            .orElse(null)

        personV2Data.setGeografiskTilknytning(geografiskTilknytning)

        // Sjekk at geografiskTilknytning er satt og at det ikke er en tre-bokstavs landkode (ISO 3166 Alpha-3, for utenlandske brukere så blir landskode brukt istedenfor nummer)
        if (geografiskTilknytning != null && geografiskTilknytning.matches("\\d+".toRegex())) {
            try {
                // Henter geografisk enhet, derfor settes ikke diskresjonskode og skjermet
                val enhet = fraNorg2Enhet(norg2Client.hentTilhorendeEnhet(geografiskTilknytning, null, false))
                personV2Data.setGeografiskEnhet(enhet)
            } catch (e: Exception) {
                PersonV2Service.log.error("Klarte ikke å flette inn geografisk enhet", e)
            }
        }
    }

    private fun fraNorg2Enhet(enhet: Enhet): no.nav.veilarbperson.domain.Enhet {
        return Enhet(enhet.enhetNr, enhet.navn)
    }

    fun flettKodeverk(personV2Data: PersonV2Data) {
        val postnrIBostedsVegAdr = Optional.ofNullable<Bostedsadresse>(personV2Data.bostedsadresse)
            .map<Vegadresse> { obj: Bostedsadresse -> obj.vegadresse }
            .map<String>(
                Function<Vegadresse, String> { Bostedsadresse.Vegadresse.getPostnummer() })
        val postnrIBostedsMatrikkelAdr = Optional.ofNullable<Bostedsadresse>(personV2Data.bostedsadresse)
            .map<Bostedsadresse.Matrikkeladresse> { obj: Bostedsadresse -> obj.matrikkeladresse }
            .map<String>(
                Function<Bostedsadresse.Matrikkeladresse, String> { Bostedsadresse.Matrikkeladresse.getPostnummer() })
        val kommunenrIBostedsVegAdr = Optional.ofNullable<Bostedsadresse>(personV2Data.bostedsadresse)
            .map<Vegadresse> { obj: Bostedsadresse -> obj.vegadresse }
            .map<String>(
                Function<Vegadresse, String> { Bostedsadresse.Vegadresse.getKommunenummer() })
        val kommunenrIBostedsMatrikkelAdr = Optional.ofNullable<Bostedsadresse>(personV2Data.bostedsadresse)
            .map<Bostedsadresse.Matrikkeladresse> { obj: Bostedsadresse -> obj.matrikkeladresse }
            .map<String>(
                Function<Bostedsadresse.Matrikkeladresse, String> { Bostedsadresse.Matrikkeladresse.getKommunenummer() })
        val kommunenrIBostedsUkjentAdr = Optional.ofNullable<Bostedsadresse>(personV2Data.bostedsadresse)
            .map<UkjentBosted> { obj: Bostedsadresse -> obj.ukjentBosted }
            .map<String>(
                Function<UkjentBosted, String> { UkjentBosted.getBostedskommune() })
        val kommunenrIOppholdsVegAdr = Optional.ofNullable<Oppholdsadresse>(personV2Data.oppholdsadresse)
            .map<Vegadresse> { obj: Oppholdsadresse -> obj.vegadresse }
            .map<String>(
                Function<Vegadresse, String> { Oppholdsadresse.Vegadresse.getKommunenummer() })
        val kommunenrIOppholdsMatrikkelAdr = Optional.ofNullable<Oppholdsadresse>(personV2Data.oppholdsadresse)
            .map<Oppholdsadresse.Matrikkeladresse> { obj: Oppholdsadresse -> obj.matrikkeladresse }
            .map<String>(
                Function<Oppholdsadresse.Matrikkeladresse, String> { Oppholdsadresse.Matrikkeladresse.getKommunenummer() })
        val landkodeIBostedsUtenlandskAdr = Optional.ofNullable<Bostedsadresse>(personV2Data.bostedsadresse)
            .map<Utenlandskadresse> { obj: Bostedsadresse -> obj.utenlandskAdresse }
            .map<String>(
                Function<Utenlandskadresse, String> { Bostedsadresse.Utenlandskadresse.getLandkode() })
        val postnrIOppholdsVegAdr = Optional.ofNullable<Oppholdsadresse>(personV2Data.oppholdsadresse)
            .map<Vegadresse> { obj: Oppholdsadresse -> obj.vegadresse }
            .map<String>(
                Function<Vegadresse, String> { Oppholdsadresse.Vegadresse.getPostnummer() })
        val postnrIOppholdsMatrikkelAdr = Optional.ofNullable<Oppholdsadresse>(personV2Data.oppholdsadresse)
            .map<Oppholdsadresse.Matrikkeladresse> { obj: Oppholdsadresse -> obj.matrikkeladresse }
            .map<String>(
                Function<Oppholdsadresse.Matrikkeladresse, String> { Oppholdsadresse.Matrikkeladresse.getPostnummer() })
        val landkodeIOppholdsUtenlandskAdr = Optional.ofNullable<Oppholdsadresse>(personV2Data.oppholdsadresse)
            .map<Utenlandskadresse> { obj: Oppholdsadresse -> obj.utenlandskAdresse }
            .map<String>(
                Function<Utenlandskadresse, String> { Oppholdsadresse.Utenlandskadresse.getLandkode() })

        postnrIBostedsVegAdr.map { postnummer: String? ->
            kodeverkService.getPoststedForPostnummer(
                postnummer
            )
        }.ifPresent { poststed: String? -> personV2Data.setPoststedIBostedsVegadresse(poststed) }
        postnrIBostedsMatrikkelAdr.map { postnummer: String? ->
            kodeverkService.getPoststedForPostnummer(
                postnummer
            )
        }.ifPresent { poststed: String? -> personV2Data.setPoststedIBostedsMatrikkeladresse(poststed) }
        kommunenrIBostedsVegAdr.map { kommunenummer: String? ->
            kodeverkService.getBeskrivelseForKommunenummer(
                kommunenummer
            )
        }.ifPresent { kommunenummer: String? -> personV2Data.setKommuneIBostedsVegadresse(kommunenummer) }
        kommunenrIBostedsMatrikkelAdr.map { kommunenummer: String? ->
            kodeverkService.getBeskrivelseForKommunenummer(
                kommunenummer
            )
        }.ifPresent { kommunenummer: String? -> personV2Data.setKommuneIBostedsMatrikkeladresse(kommunenummer) }
        kommunenrIBostedsUkjentAdr.map { kommunenummer: String? ->
            kodeverkService.getBeskrivelseForKommunenummer(
                kommunenummer
            )
        }.ifPresent { kommunenummer: String? -> personV2Data.setKommuneIBostedsUkjentadresse(kommunenummer) }
        kommunenrIOppholdsVegAdr.map { kommunenummer: String? ->
            kodeverkService.getBeskrivelseForKommunenummer(
                kommunenummer
            )
        }.ifPresent { kommunenummer: String? -> personV2Data.setKommuneIOppholdssVegadresse(kommunenummer) }
        kommunenrIOppholdsMatrikkelAdr.map { kommunenummer: String? ->
            kodeverkService.getBeskrivelseForKommunenummer(
                kommunenummer
            )
        }.ifPresent { kommunenummer: String? -> personV2Data.setKommuneIOppholdsMatrikkeladresse(kommunenummer) }
        postnrIOppholdsVegAdr.map { postnummer: String? ->
            kodeverkService.getPoststedForPostnummer(
                postnummer
            )
        }.ifPresent { poststed: String? -> personV2Data.setPoststedIOppholdsVegadresse(poststed) }
        postnrIOppholdsMatrikkelAdr.map { postnummer: String? ->
            kodeverkService.getPoststedForPostnummer(
                postnummer
            )
        }.ifPresent { poststed: String? -> personV2Data.setPoststedIOppholdsMatrikkeladresse(poststed) }
        landkodeIBostedsUtenlandskAdr.map { kode: String? ->
            kodeverkService.getBeskrivelseForLandkode(
                kode
            )
        }.ifPresent { landkode: String? -> personV2Data.setLandkodeIBostedsUtenlandskadresse(landkode) }
        landkodeIOppholdsUtenlandskAdr.map { kode: String? ->
            kodeverkService.getBeskrivelseForLandkode(
                kode
            )
        }.ifPresent { landkode: String? -> personV2Data.setLandkodeIOppholdsUtenlandskadresse(landkode) }
        personV2Data.setStatsborgerskap(
            personV2Data.statsborgerskapKoder
                .stream()
                .map { kode: String? -> kodeverkService.getBeskrivelseForLandkode(kode) }
                .filter { obj: String? -> Objects.nonNull(obj) }
                .toList())

        val kontaktadresseList = personV2Data.kontaktadresser

        for (kontaktadresse in kontaktadresseList) {
            val postnrIKontaktsVegAdr = Optional.ofNullable<Kontaktadresse>(kontaktadresse)
                .map<Vegadresse> { obj: Kontaktadresse -> obj.vegadresse }
                .map<String>(
                    Function<Vegadresse, String> { Kontaktadresse.Vegadresse.getPostnummer() })
            val postnrIKontaktsPostboksAdr = Optional.ofNullable<Kontaktadresse>(kontaktadresse)
                .map<Kontaktadresse.Postboksadresse> { obj: Kontaktadresse -> obj.postboksadresse }
                .map<String>(
                    Function<Kontaktadresse.Postboksadresse, String> { Kontaktadresse.Postboksadresse.getPostnummer() })
            val postnrIPostAdresseIFrittFormat = Optional.ofNullable<Kontaktadresse>(kontaktadresse)
                .map<PostadresseIFrittFormat> { obj: Kontaktadresse -> obj.postadresseIFrittFormat }
                .map<String>(
                    Function<PostadresseIFrittFormat, String> { PostadresseIFrittFormat.getPostnummer() })
            val landkodeIKontaktsUtenlandskAdr = Optional.ofNullable<Kontaktadresse>(kontaktadresse)
                .map<Utenlandskadresse> { obj: Kontaktadresse -> obj.utenlandskAdresse }
                .map<String>(
                    Function<Utenlandskadresse, String> { Kontaktadresse.Utenlandskadresse.getLandkode() })
            val landkodeIUtenlandskAdresseIFrittFormat = Optional.ofNullable<Kontaktadresse>(kontaktadresse)
                .map<UtenlandskAdresseIFrittFormat> { obj: Kontaktadresse -> obj.utenlandskAdresseIFrittFormat }
                .map<String>(
                    Function<UtenlandskAdresseIFrittFormat, String> { UtenlandskAdresseIFrittFormat.getLandkode() })
            val kommunenrIKontaktsVegAdr = Optional.ofNullable<Kontaktadresse>(kontaktadresse)
                .map<Vegadresse> { obj: Kontaktadresse -> obj.vegadresse }
                .map<String>(
                    Function<Vegadresse, String> { Kontaktadresse.Vegadresse.getKommunenummer() })

            postnrIKontaktsVegAdr.map { postnummer: String? ->
                kodeverkService.getPoststedForPostnummer(
                    postnummer
                )
            }.ifPresent { poststed: String? ->
                kontaktadresse.vegadresse.setPoststed(
                    poststed
                )
            }
            kommunenrIKontaktsVegAdr.map { kommunenummer: String? ->
                kodeverkService.getBeskrivelseForKommunenummer(
                    kommunenummer
                )
            }.ifPresent { kommune: String? ->
                kontaktadresse.vegadresse.setKommune(
                    kommune
                )
            }
            postnrIKontaktsPostboksAdr.map { postnummer: String? ->
                kodeverkService.getPoststedForPostnummer(
                    postnummer
                )
            }.ifPresent { poststed: String? ->
                kontaktadresse.postboksadresse.poststed =
                    poststed
            }
            postnrIPostAdresseIFrittFormat.map { postnummer: String? ->
                kodeverkService.getPoststedForPostnummer(
                    postnummer
                )
            }.ifPresent { poststed: String? ->
                kontaktadresse.postadresseIFrittFormat.poststed =
                    poststed
            }
            landkodeIKontaktsUtenlandskAdr.map { kode: String? ->
                kodeverkService.getBeskrivelseForLandkode(
                    kode
                )
            }.ifPresent { landkode: String? ->
                kontaktadresse.utenlandskAdresse.landkode =
                    landkode
            }
            landkodeIUtenlandskAdresseIFrittFormat.map { kode: String? ->
                kodeverkService.getBeskrivelseForLandkode(
                    kode
                )
            }.ifPresent { landkode: String? ->
                kontaktadresse.utenlandskAdresseIFrittFormat.setLandkode(
                    landkode
                )
            }
        }
    }

    private fun flettDigitalKontaktinformasjon(fnr: Fnr, personV2Data: PersonV2Data) {
        val KRRPostPersonerRequest = KRRPostPersonerRequest(Set.of(fnr.get()))
        try {
            val kontaktinfo = digdirClient.hentKontaktInfo(KRRPostPersonerRequest)
            val digdirKontaktinfo = kontaktinfo?.personer?.get(fnr.get())
            if (digdirKontaktinfo != null) {
                val epostSisteOppdatert =
                    Optional.ofNullable(digdirKontaktinfo.epostadresseOppdatert).map { dato: String? ->
                        ZonedDateTime.parse(dato).format(PersonV2DataMapper.frontendDatoformat)
                    }
                val mobilSisteOppdatert =
                    Optional.ofNullable(digdirKontaktinfo.mobiltelefonnummerOppdatert).map { dato: String? ->
                        ZonedDateTime.parse(dato).format(PersonV2DataMapper.frontendDatoformat)
                    }
                val epost = if (digdirKontaktinfo.epostadresse != null)
                    Epost().setEpostAdresse(digdirKontaktinfo.epostadresse)
                        .setEpostSistOppdatert(epostSisteOppdatert.orElse(null)).setMaster("KRR")
                else
                    null
                personV2Data.setEpost(epost)
                personV2Data.setMalform(digdirKontaktinfo.spraak)
                leggKrrTelefonNrIListe(
                    digdirKontaktinfo.mobiltelefonnummer,
                    mobilSisteOppdatert.orElse(null),
                    personV2Data.telefon
                )
            } else {
                PersonV2Service.log.warn("Fant ikke kontaktinfo i KRR")
            }
        } catch (e: Exception) {
            PersonV2Service.log.warn("Kunne ikke flette digitalkontaktinfo fra KRR", e)
        }
    }

    /* Telefonnummer fra PDL og KRR legges sammen i en liste.
       KRR telefonnummeret vil alltid ha høyere prioritet enn PDL telefonnummeret.
       Hvis like nummer, fjernes PDL-nummeret
    */
    fun leggKrrTelefonNrIListe(
        telefonNummerFraKrr: String?,
        sistOppdatert: String?,
        telefonListe: MutableList<Telefon>
    ) {
        var prioritet: Int
        if (telefonNummerFraKrr != null) {
            telefonListe.removeIf { telefon: Telefon -> telefonNummerFraKrr == telefon.telefonNr }
            telefonListe.add(
                Telefon()
                    .setPrioritet(1.toString() + "")
                    .setTelefonNr(telefonNummerFraKrr)
                    .setRegistrertDato(sistOppdatert)
                    .setMaster("KRR")
            )
            for (telefon in telefonListe) {
                if (telefon.master != "KRR") {
                    prioritet = telefon.prioritet.toInt() + 1
                    telefon.setPrioritet(prioritet.toString() + "")
                }
            }
        }
    }

    fun hentSpraakTolkInfo(personFraPdlRequest: PersonFraPdlRequest): TilrettelagtKommunikasjonData {
        val spraakTolkInfo = pdlClient.hentTilrettelagtKommunikasjon(
            PdlRequest(
                personFraPdlRequest.fnr,
                personFraPdlRequest.behandlingsnummer
            )
        )

        if (spraakTolkInfo.tilrettelagtKommunikasjon.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.NO_CONTENT,
                "Ingen tilrettelagtkommunikasjon for person i PDL"
            )
        }

        val tilrettelagtKommunikasjon = PersonV2DataMapper.getFirstElement(spraakTolkInfo.tilrettelagtKommunikasjon)
        val tegnSpraak = Optional.ofNullable<HentPerson.TilrettelagtKommunikasjon>(tilrettelagtKommunikasjon)
            .map<Tolk>(Function<HentPerson.TilrettelagtKommunikasjon, Tolk> { HentPerson.TilrettelagtKommunikasjon.getTegnspraaktolk() })
            .map<String>(Function<Tolk, String> { Tolk.getSpraak() })
            .map<String?> { spraakKode: String? -> kodeverkService.getBeskrivelseForSpraakKode(spraakKode) }
            .orElse(null)
        val taleSpraak = Optional.ofNullable<HentPerson.TilrettelagtKommunikasjon>(tilrettelagtKommunikasjon)
            .map<Tolk>(Function<HentPerson.TilrettelagtKommunikasjon, Tolk> { HentPerson.TilrettelagtKommunikasjon.getTalespraaktolk() })
            .map<String>(Function<Tolk, String> { Tolk.getSpraak() })
            .map<String?> { spraakKode: String? -> kodeverkService.getBeskrivelseForSpraakKode(spraakKode) }
            .orElse(null)

        return TilrettelagtKommunikasjonData().setTegnspraak(tegnSpraak).setTalespraak(taleSpraak)
    }

    fun hentVerge(personFraPdlRequest: PersonFraPdlRequest): VergeData {
        val vergeOgFullmaktFraPdl =
            pdlClient.hentVerge(PdlRequest(personFraPdlRequest.fnr, personFraPdlRequest.behandlingsnummer))
        return VergeOgFullmaktDataMapper.toVerge(vergeOgFullmaktFraPdl)
    }

    @Throws(IOException::class)
    fun hentFullmakt(personRequest: PersonRequest): FullmaktDTO {
        val fullmaktListe = representasjonClient.hentFullmakt(personRequest.fnr.get())
        if (fullmaktListe.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NO_CONTENT, "Person har ikke fullmakt i representasjon")
        }
        val fullmaktDTO = VergeOgFullmaktDataMapper.toFullmaktDTO(fullmaktListe)
        flettBeskrivelseTilFullmaktTema(fullmaktDTO)
        return fullmaktDTO
    }

    fun flettBeskrivelseTilFullmaktTema(fullmaktDto: FullmaktDTO) {
        if (!fullmaktDto.getFullmakt().isEmpty()) {
            fullmaktDto.getFullmakt().forEach(Consumer { fullmakt: FullmaktDTO.Fullmakt ->
                if (!fullmakt.omraade.isEmpty()) {
                    fullmakt.omraade.forEach(Consumer { omraade: FullmaktDTO.OmraadeMedHandling ->
                        if (omraade.tema == "*") {
                            omraade.setTema("alle ytelser")
                        } else {
                            val beskrivelseForTema = kodeverkService.getBeskrivelseForTema(omraade.tema)
                            omraade.setTema(beskrivelseForTema)
                        }
                    })
                }
            })
        }
    }

    fun hentMalform(fnr: Fnr): String? {
        val KRRPostPersonerRequest = KRRPostPersonerRequest(Set.of(fnr.get()))
        try {
            val kontaktinfo = digdirClient.hentKontaktInfo(KRRPostPersonerRequest)
            if (kontaktinfo?.personer == null) {
                PersonV2Service.log.warn("Fant ikke kontaktinfo (målform) i KRR")
                return null
            }
            val digdirKontaktinfo = kontaktinfo.personer[fnr.get()]
            return digdirKontaktinfo!!.spraak
        } catch (e: Exception) {
            PersonV2Service.log.warn("Kunne ikke hente malform fra KRR", e)
        }
        return null
    }

    fun hentNavn(personFraPdlRequest: PersonFraPdlRequest): PersonNavnV2 {
        val personNavn =
            pdlClient.hentPersonNavn(PdlRequest(personFraPdlRequest.fnr, personFraPdlRequest.behandlingsnummer))

        if (personNavn.getNavn().isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke navn til person")
        }
        PersonV2Service.log.info("Ferdig med hentNavn i PersonV2Service")
        return PersonV2DataMapper.navnMapper(personNavn.getNavn())
    }

    fun hentAdressebeskyttelse(personFraPdlRequest: PersonFraPdlRequest): Adressebeskyttelse {
        val adressebeskyttelse = Optional.ofNullable(
            pdlClient.hentAdressebeskyttelse(
                PdlRequest(
                    personFraPdlRequest.fnr,
                    personFraPdlRequest.behandlingsnummer
                )
            )
        ).orElse(
            listOf()
        )
        return adressebeskyttelse.stream().findFirst().orElse(Adressebeskyttelse().setGradering("UGRADERT"))
    }
}
