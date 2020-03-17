Veilarbperson
================

* Applikasjonen kjører på Embedded Jetty lokalt.
* Jersey er brukt som rest-API.
* Vær nøye med at alle tjenester som dras inn fra tjenestespesifikasjonen bruker samme versjon av jaxb2-basics-runtime.
Ellers kan du få problem med den genererte hashcode-metoden.
* I koden benyttes Project Lombok for å slippe å manuelt lage masse boilerplate rundt setters, getters, builders, hashCode osv. Dersom IntelliJ ikke skjønner det, og du tilsynelatende har masse kompileringsfeil, mangler du Project Lombok-pluginet. Installér pronto!

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles ved å opprette en issue.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #pto.
