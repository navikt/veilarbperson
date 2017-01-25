Applikasjonen kjører på Embedded Jetty lokalt.
Spring REST er brukt som rest-API.

Var nøye med at alle tjenester som dras inn fra tjenestespesifikasjonen bruker samme versjon av jaxb2-basics-runtime.
Ellers kan du få problem med den genererte hashcode-metoden.
