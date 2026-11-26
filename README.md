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

`visittkortEtiketter` samler opptil 22 etiketter fra sju kilder via `POST /veilarbperson/graphql`.
Konsumenten velger feltene, og bare nødvendige kilder kalles.

Oppslaget krever intern bruker og lesetilgang til personen. Behandlingsnummer er påkrevd for PDL-felt.

Se [filoversikt og kodeflyt](visittkort-etiketter-pr-beskrivelse.md) for kildeavhengigheter, mapping og feilhåndtering.

## Testflagg i dev

`veilarbperson.visittkort_etiketter_test_feil` kan aktiveres i Unleash-miljøet `development`
for å simulere at alle etikettkildene er utilgjengelige. Testatferden er sperret utenfor dev.

Unleash-klienten opprettes bare i dev, der `UNLEASH_SERVER_API_URL` og
`UNLEASH_SERVER_API_TOKEN` er påkrevd. Prod og miljøer som ikke er identifisert som dev,
bruker en deaktivert klient uten Unleash-konfigurasjon eller nettverkstrafikk.

# PDL
PDL dok anbefaler å bruke Altair programvare for å kjøre graphql eller for å gjøre oppslag mot PDL. 

Bruk denne URLen med POST metod i Altair: https://pdl-api.dev.adeo.no/graphql

For å teste hentPerson operasjonen, kan du kopiere graphql fra hentPerson.gql og lim den inn i query 
feltet i Altair. 
sett gjeldende input variabler i Variables feltet i Altair. 
For hentPerson operasjonen input variabler kan være "ident" og "historikk". 
For ex:-
{ "ident": "TESTFNR", "historikk": false }
