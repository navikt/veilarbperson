Veilarbperson
================

API for uthenting av informasjon om brukere

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles ved å opprette en issue.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #pto.

# PDL
PDL dok anbefaler å bruke Altair programvare for å kjøre graphql eller for å gjøre oppslag mot PDL. 

Bruk denne URLen med POST metod i Altair: https://pdl-api.dev.adeo.no/graphql

For å teste hentPerson operasjonen, kan du kopiere graphql fra hentPerson.gql og lim den inn i query 
feltet i Altair. 
sett gjeldende input variabler i Variables feltet i Altair. 
For hentPerson operasjonen input variabler kan være "ident" og "historikk". 
For ex:-
{ "ident": "TESTFNR", "historikk": false }
