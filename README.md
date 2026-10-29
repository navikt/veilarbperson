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

Visittkortets tolkeoppslag bruker en egen klientmetode,
`hentTilrettelagtKommunikasjonVisittkort`. Servicen skiller mellom et vellykket
oppslag (`TolkeOppslag.Hentet`) og en utilgjengelig kilde (`TolkeOppslag.Ukjent`).
Transportfeil og HTTP 5xx gir `TOLK_UKJENT` i visittkortmodellen, uten å fjerne
representasjonsetiketter. Et vellykket oppslag med tom tolkeliste gir ingen
tolkeetiketter. Andre HTTP-feil, GraphQL-feil og ugyldige responser feiler
visittkortkallet. Eksisterende PDL-metoder og reglene for faktiske
tolkeetiketter er uendret. Eksponering av etiketter i GraphQL-skjemaet
er et separat steg.

# Fullmakter

Eksisterende `hent-fullmakt` bruker representasjons-API-ets
`POST /api/v2/internbruker/fullmakt/bruker-som-fullmaktsgiver/alle-fullmakter`
med `{"ident": "<personident>"}` i body. OBO og adgangskontroll er uendret.
Denne integrasjonen er avgrenset til veilederflyten; systemkonsumenter er ikke del av leveransen.

Fullmakter beholdes når `gyldigTilOgMed` er null eller datoen er i dag eller senere.
Fremtidige startdatoer beholdes. Datogrensen følger JVM-ens standardtidssone
via `LocalDate.now()`. Dette forutsetter at leverandøren setter faktisk sluttdato
ved tidlig opphør, også for fullmakter som tidligere ikke hadde sluttdato.
Tomt utvalg etter filtrering gir fortsatt 204.

Fullmaktsgiver kontrolleres mot forespurt person før navneoppslag. Begge navn
hentes fra PDL med requestens behandlingsnummer, én gang per unik identifikator
per request. Navn vises som fornavn, eventuelt mellomnavn og etternavn, ikke
forkortet navn. Manglende eller ufullstendig navn og PDL-feil feiler hele kallet;
HTTP-status bevares uten å videreføre eventuell feilbody fra PDL.

Responsen beholder `FullmaktDTO`. Temaer samles i rekkefølgen lesetemaer, deretter
nye skrivetemaer, uten duplikater. Hvert tema får `LES` og/eller `SKRIV`, i den
rekkefølgen; `SKRIV` gir ikke automatisk `LES`. `KOMMUNISER` genereres ikke.
Eksisterende temabeskrivelser og vergemål beholdes.

Klienten krever at alle seks feltene den bruker finnes: `fullmaktsgiver`,
`fullmektig`, `gyldigFraOgMed`, `gyldigTilOgMed`, `leserettigheter` og
`skriverettigheter`. Bare sluttdato kan være null; rettighetslistene kan være
tomme. Manglende sluttdato tolkes ikke som eksplisitt null. Dette er en
bevisst streng konsumentkontrakt inntil leverandørens required-felter er bekreftet.
Responsmodellen har ingen egen blankvalidering av personidenter eller temaer;
tomme og blanke temastrenger aksepteres.
Ubrukte felter, inkludert endringslogg, ignoreres. Ugyldig eller manglende
respons feiler uten fallback til gammel kilde.

Før produksjonsutrulling må leverandøren bekrefte opphørssemantikk og kontrakt.
Dev-prøven må dekke OBO, PDL-403, fremtidige fullmakter, frontend uten
`KOMMUNISER`, ekstra PDL-last og responstid. Eksisterende PDL-feilrapportering må
også kontrolleres for persondata uten å logge testpayload eller token.
Rollback må gjenopprette gammel klient og mapping samlet og krever at gammel
API-versjon fortsatt er tilgjengelig.

# PDL
PDL dok anbefaler å bruke Altair programvare for å kjøre graphql eller for å gjøre oppslag mot PDL. 

Bruk denne URLen med POST metod i Altair: https://pdl-api.dev.adeo.no/graphql

For å teste hentPerson operasjonen, kan du kopiere graphql fra hentPerson.gql og lim den inn i query 
feltet i Altair. 
sett gjeldende input variabler i Variables feltet i Altair. 
For hentPerson operasjonen input variabler kan være "ident" og "historikk". 
For ex:-
{ "ident": "TESTFNR", "historikk": false }
