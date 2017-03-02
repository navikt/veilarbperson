# VeilArbPerson

* Applikasjonen kjører på Embedded Jetty lokalt.
* Jersey er brukt som rest-API.
* Var nøye med at alle tjenester som dras inn fra tjenestespesifikasjonen bruker samme versjon av jaxb2-basics-runtime.
Ellers kan du få problem med den genererte hashcode-metoden.
* I koden benyttes Project Lombok for å slippe å manuelt lage masse boilerplate rundt setters, getters, builders, hashCode osv. Dersom IntelliJ ikke skjønner det, og du tilsynelatende har masse kompileringsfeil, mangler du Project Lombok-pluginnet. Installer pronto!
