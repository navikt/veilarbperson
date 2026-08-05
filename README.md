Veilarbperson
================

API for uthenting av informasjon om brukere

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles ved å opprette en issue.

## For Nav-ansatte

Interne henvendelser kan sendes via Slack i kanalen #po-arbeidsoppfølging.

# GraphQL — visittkortdata

Endepunktet er tiltenkt `veilarbvisittkortfs` og returnerer de feltene visittkortvisningen trenger.

```
POST /veilarbperson/graphql
Content-Type: application/json
Authorization: Bearer <TokenX-token>
```

Eksempelspørring:
```json
{
  "query": "{ person(fnr: \"12345678901\", behandlingsnummer: \"B643\") { fornavn mellomnavn etternavn fodselsdato dodsdato kjonn diskresjonskode egenAnsatt sikkerhetstiltak telefon { telefonNr prioritet registrertDato master } } }"
}
```

`behandlingsnummer` er påkrevd og skal være konsumentens eget B-nummer fra behandlingskatalogen.
Feltet `fnr` skal ikke hentes ut fra responsen — det er ikke med i svaret.

# PDL
PDL dok anbefaler å bruke Altair programvare for å kjøre graphql eller for å gjøre oppslag mot PDL. 

Bruk denne URLen med POST metod i Altair: https://pdl-api.dev.adeo.no/graphql

For å teste hentPerson operasjonen, kan du kopiere graphql fra hentPerson.gql og lim den inn i query 
feltet i Altair. 
sett gjeldende input variabler i Variables feltet i Altair. 
For hentPerson operasjonen input variabler kan være "ident" og "historikk". 
For ex:-
{ "ident": "TESTFNR", "historikk": false }
