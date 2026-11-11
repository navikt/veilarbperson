Veilarbperson
================

API for uthenting av informasjon om brukere

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles ved å opprette en issue.

## For Nav-ansatte

Interne henvendelser kan sendes via Slack i kanalen #po-arbeidsoppfølging.

# GraphQL — visittkortdata

Endepunktet er tiltenkt `veilarbvisittkortfs` og returnerer feltene visittkortvisningen trenger.

```
POST /veilarbperson/graphql
Content-Type: application/json
Authorization: Bearer <Azure AD-token>
```

Eksempelspørring:
```json
{
  "query": "{ person(fnr: \"12345678901\", behandlingsnummer: \"B643\") { fornavn mellomnavn etternavn fodselsdato dodsdato kjonn diskresjonskode egenAnsatt sikkerhetstiltak telefon { telefonNr prioritet registrertDato master } } }"
}
```

`behandlingsnummer` er påkrevd og skal være konsumentens eget B-nummer fra behandlingskatalogen.

# GraphQL — visittkortetiketter

**Under implementering:** Skjema, mapping og klienter for PDL, representasjon, skjerming,
arbeidssøkerregister, KVP, gjeldende § 14 a-vedtak og eskaleringsvarsel er lagt til.
Klientimplementasjonen for de øvrige oppfølgingsfeltene mangler.
Hele 22-feltsoppslaget er derfor ikke klart for bruk.
Eksisterende `person` og hent-personalia er uendret og får ingen etikettliste.

Etikettene hentes fra samme GraphQL-endpoint. Konsumenten velger hvert felt eksplisitt:

```graphql
query VisittkortEtiketter($fnr: ID!, $behandlingsnummer: String) {
  visittkortEtiketter(fnr: $fnr, behandlingsnummer: $behandlingsnummer) {
    dod
    diskresjonskode
    utmeldingskandidat
    sikkerhetstiltak {
      etikett
      beskrivelse
    }
    skjerming
    vergemaalOppfolging
    fullmaktOppfolging
    tegnspraaktolk
    talespraaktolk
    kvp
    manuellOppfolging
    reservertKrr
    utdatertIKrr
    ikkeUnderOppfolging
    varsel
    ikkeRegistrertKrr
    trengerOppfolgingsvedtak
    sykmeldt
    iArbeidssokerregisteret
    antattGodeMuligheter
    antattBehovForVeiledning
    oppgittHindringer
  }
}
```

Hvert etikettfelt har sin egen enumtype i `src/main/resources/graphql/schema.graphqls`.
Positiv enumverdi betyr at etiketten gjelder, `null` at den ikke gjelder etter et gyldig oppslag,
og `UKJENT_*` at nødvendige opplysninger ikke kunne hentes på grunn av transportfeil eller HTTP 5xx.
Felt som ikke etterspørres, utelates og utløser ikke egne kildeoppslag.

`sikkerhetstiltak` er det eneste objektfeltet. Ved gyldig fravær er hele objektet `null`.
Ved kildefeil inneholder det `UKJENT_SIKKERHETSTILTAK` og `beskrivelse: null`; en tidligere
beskrivelse brukes aldri som fallback. Frontend beholder tekster, rekkefølge, Tag-varianter og tooltip.
Ikke bruk truthiness for enumverdier: også `UKJENT_*` er ikke-tomme strenger.
`FOLKEREGISTERSTATUS_UKJENT` og `FOLKEREGISTERSTATUS_INGEN_STATUS` er positive
utmeldingskandidattagger, ikke kildefeil.

## Tilgang og feil

Oppslaget krever intern bruker og auditert lesetilgang til personen før etikettkildene kalles.
FNR må bestå av elleve sifre. Når et PDL-felt velges, kreves konsumentens behandlingsnummer
på formatet `B` etterfulgt av ett til ti sifre. Nummeret videresendes uendret.
De fem PDL-feltene er `dod`, `diskresjonskode`, `sikkerhetstiltak`, `tegnspraaktolk` og `talespraaktolk`.
Én operasjon kan inneholde høyst ett aktivt `visittkortEtiketter`-oppslag, også med aliaser og fragmenter.

Tilgangsfeil, ugyldig input, ugyldige kildesvar og interne feil gir `null` rotfelt med GraphQL-errors,
ikke vanlige tomme etiketter. Feilkoden finnes i `errors[].extensions.code`:
`UGYLDIG_FORESPORSEL`, `IKKE_AUTENTISERT`, `INGEN_TILGANG`, `PERSON_IKKE_FUNNET`,
`UGYLDIG_KILDESVAR`, `KILDE_IKKE_KONFIGURERT` eller `INTERN_FEIL`.
Konsumenten må håndtere errors før den tolker `null` som fravær av en etikett.

En ukonfigurert kilde gir eksplisitt `KILDE_IKKE_KONFIGURERT`, aldri en syntetisk `UKJENT_*`.
Dette lar eksisterende API starte uten de nye klientene, men gjør ikke de avhengige feltene klare for bruk.
Oppfølgingsfeltene hentes med eksisterende `VeilarboppfolgingClient`.
Vedtaks- og varselskilder må fortsatt være eksplisitt konfigurert før de avhengige feltene kan brukes.

## Kilder og gjenværende kobling

PDL, representasjon, skjerming og arbeidssøkerregister bruker eksisterende klienter, URL-er og scopes.
Arbeidssøkeretikettene gjenbruker utledningen i `OppslagArbeidssoekerregisteretService`.
Klientene registreres i `ClientConfig`; det opprettes ikke parallelle klienter for etikettoppslagene.
Kildene hentes synkront med eksisterende veilederkontekst eller kildens eksisterende M2M-token.
Resultater deles bare innen samme oppslag. Skjerming bruker eksisterende cache og har samme TTL.

Oppfølgingsfeltene bruker eksisterende `VEILARBOPPFOLGING_URL` og `VEILARBOPPFOLGING_SCOPE` med OBO-token.
`VeilarboppfolgingClient.hentEtikettgrunnlag` sender ett `POST /api/graphql` for de valgte feltene
og deres avhengigheter. Spørringen bruker `@include` for å unngå unødvendig innhenting:
KVP, manuell oppfølging, KRR-flagg, sykmeldingsstatus, oppfølgingsstatus og utmeldingskandidat
hentes bare når de trengs. KVP og øvrige oppfølgingsfelter deler samme kall.

KVP leses fra `brukerStatus.erKontorsperret`, som dagens frontend mapper til `underKvp`.
Verdien `true` gir `KVP`, og `false` gir `null`.
Et KVP-oppslag trenger verken behandlingsnummer eller vedtaks- og varselskildene:

```graphql
query Kvp($fnr: ID!) {
  visittkortEtiketter(fnr: $fnr) {
    kvp
  }
}
```

Når oppfølgingskilden er konfigurert, men utilgjengelig på grunn av transportfeil eller HTTP 5xx,
gir de avhengige feltene relevante `UKJENT_*`-verdier.
Manglende eller ugyldige nødvendige boolske verdier, GraphQL-errors og andre HTTP-feil blir ikke
tolket som negative oppfølgingsflagg. Ikke-etterspurte flagg blir heller ikke fylt med `false`.
`sykmeldtStatus: null` og `utmeldingskandidat.tag: null` er gyldig fravær, mens manglende felt er feil.
De ni domenetaggene for utmeldingskandidat godtas. Andre tagger, inkludert
`UKJENT_UTMELDINGSKANDIDAT`, er kontraktsfeil. Den sistnevnte verdien er reservert
for utilgjengelig kilde i etikett-API-et.

Profilering krever et gyldig oppfølgingsobjekt, men ikke at `underOppfolging` er `true`.
Når oppfølgingsobjektet bare brukes som grunnlag for profilering, kan `erUnderOppfolging` være `null`;
verdien gjøres ikke om til `false`. `ikkeUnderOppfolging` og `trengerOppfolgingsvedtak` krever
derimot en eksplisitt boolsk verdi. De eksisterende reglene for vedtaksbehov og profilering er uendret.

Vedtaksbeanen opprettes når både `VEILARBVEDTAKSSTOTTE_URL` og `VEILARBVEDTAKSSTOTTE_SCOPE` er satt.
URL-en skal inkludere `/veilarbvedtaksstotte`. Klienten bruker OBO-token og
`POST /api/hent-gjeldende-14a-vedtak` med `{ "fnr": "..." }`.
Denne ruta kontrollerer både fagsystemroller for Modia arbeidsrettet oppfølging og populasjonstilgang.
Ruta med `/ekstern` brukes ikke som fallback.

Controlleren returnerer nullable `Gjeldende14aVedtakDto`. HTTP 200 med tom body eller JSON `null`
betyr at gjeldende vedtak mangler; en vedtaksrespons med `innsatsgruppe` betyr at det finnes.
Klienten beholder bare denne boolske informasjonen, ikke innsatsgruppe, hovedmål eller dato.
Ugyldig JSON eller svarstruktur er en kontraktsfeil. HTTP 401/403/404 og uventede 2xx-statuser
er ikke gyldig fravær; transportfeil og HTTP 5xx håndteres som utilgjengelig vedtakskilde.
Tillatelse til tom HTTP 200-body gjelder bare dette oppslaget, ikke de øvrige kildeklientene.

Varselklienten opprettes bare når både `VEILARBDIALOG_URL` og `VEILARBDIALOG_SCOPE` er satt.
URL-en skal inkludere `/veilarbdialog`; klienten bruker
`GET /api/eskaleringsvarsel/gjeldende` med `fnr` som query-parameter og OBO-token.
HTTP 204 er gyldig fravær av varsel; 404 er ikke fravær.
URL-er, query-parametre, GraphQL-variabler og rå kildesvar skal ikke logges.

Før varselfeltet kan brukes i et cluster, må `veilarbperson` få eksplisitt outbound-regel
og tillates i `veilarbdialog` sin inbound-policy. URL, scope og policyendringer må avklares med eierteamet.
Ingen Nais-manifester, scopes eller tilgangsregler er endret i denne leveransen.
Den delte frontend-kontrakten er grunnlaget for oppfølgingsintegrasjonen.
For vedtak må URL, OBO-scope, nettverkstilgang og mulig tilbakekall
til `veilarbperson` verifiseres før utrulling. Den delte controller-metoden viser tilgangskontrollen
og nullable returtype, men ikke implementasjonen av den underliggende vedtakstjenesten.

Begge representasjonsetikettene filtreres på `OPP` i lese- eller skriverettighetene.
Dette er en tilsiktet innstramming for vergemål sammenlignet med den gamle frontend-regelen.
Et feilet vedtakskall betyr ikke at vedtak mangler. Profilering krever gyldige oppfølgingsdata,
men ikke `underOppfolging = true`; gjeldende vedtak undertrykker profileringsetikettene.

## Drift og innføring

Micrometer måler samlet varighet (`visittkort_etiketter_varighet`), varighet per kilde
(`visittkort_kilde_varighet`), tilgjengelighetsfeil (`visittkort_kilde_utilgjengelig`) og
ukjente etiketter (`visittkort_etikett_ukjent`). Tags inneholder bare faste kilde- og feltnavn,
ikke personidentifikatorer, behandlingsnummer eller profileringsutfall.

Behold dagens frontend-flyt til kildeklientene er ferdige og hele feltsettet er kontrollert i dev.
Sammenlign med syntetiske testpersoner før gradvis aktivering bak konsumentens toggle.
Tilbakeføring er å deaktivere ny konsumentflyt; tilgangsavslag skal ikke utløse automatisk fallback.
Frontend-koblingen er ikke endret her.

# PDL
PDL dok anbefaler å bruke Altair programvare for å kjøre graphql eller for å gjøre oppslag mot PDL. 

Bruk denne URLen med POST metod i Altair: https://pdl-api.dev.adeo.no/graphql

For å teste hentPerson operasjonen, kan du kopiere graphql fra hentPerson.gql og lim den inn i query 
feltet i Altair. 
sett gjeldende input variabler i Variables feltet i Altair. 
For hentPerson operasjonen input variabler kan være "ident" og "historikk". 
For ex:-
{ "ident": "TESTFNR", "historikk": false }
