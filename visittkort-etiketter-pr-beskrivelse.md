## Visittkortetiketter – beskrivelse PR https://github.com/navikt/veilarbperson/pull/571

### Beskriv endringene

**Feltene frontend ber om, styrer hvilke kilder som kalles.** Under er flyten i faktisk kallrekkefølge; unødvendige kildeoppslag hoppes over.

### Mottak og kontroll

1. **GraphQL mottar spørringen.** Skjemaet definerer argumentene og hvilke etikettfelt som kan etterspørres. Spørringen kobles til metoden `visittkortEtiketter()` i controlleren. (`schema.graphqls`, `VisittkortEtiketterGraphQLController.kt`)

2. **Controlleren validerer og kontrollerer tilgang.** Den krever intern bruker, validerer fødselsnummer og behandlingsnummer og tillater bare ett aktivt etikettoppslag per operasjon. Behandlingsnummer er påkrevd når PDL-felt etterspørres. Deretter kontrolleres lesetilgang til personen før etikettkildene kontaktes. (`VisittkortEtiketterGraphQLController.kt`, `AuthService.java`)

3. **Tjenesten mottar bestillingen.** Controlleren sender fødselsnummer, behandlingsnummer og settet med etterspurte felt til `hent()`. I dev sjekkes feilflagget: Er det på, hoppes etikettkildene over, og mapperen får alle sju kildene som `Ukjent`. Ellers fortsetter normalflyten under. (`VisittkortEtiketterService.kt`)

### Oppslag mot kildene

4. **PDL hentes ved behov.** De etterspurte PDL-feltene hentes samlet med behandlingsnummer. Svaret valideres og omformes til grunnlag for dødsfall, adressebeskyttelse, sikkerhetstiltak og tolkebehov. (`PdlClientImpl.java`, `hentVisittkortEtiketter.gql`, `PdlVisittkortEtiketterData.kt`)

5. **Representasjon hentes ved behov.** Vergemål og fullmakt hentes samlet og filtreres til rettigheter som gjelder oppfølging. Resultatet angir om hver av de to etikettene er aktuell. (`RepresentasjonClientImpl.kt`)

6. **Skjerming hentes ved behov.** Klienten undersøker om personen er skjermet og returnerer et boolsk resultat. (`SkjermetClientImpl.java`)

7. **Oppfølgingsgrunnlaget hentes ved behov.** Tjenesten sender relevante etikettfelt til `hentEtikettgrunnlag()`. Klienten oversetter dette til `include`-flagg og gjør **ett samlet GraphQL-kall**. KVP og KRR inngår her; de hentes ikke med egne kall fra denne flyten. (`VisittkortEtiketterService.kt`, `VeilarboppfolgingClientImpl.java`, `hentVisittkortOppfolging.gql`)

8. **Gjeldende vedtak undersøkes ved behov.** Vedtaksoppslaget brukes til profileringsetikettene og, avhengig av oppfølgingsstatus, `trengerOppfolgingsvedtak`. Resultatet sier bare om et gjeldende vedtak finnes. (`VisittkortEtiketterService.kt`, `VisittkortVedtakClientImpl.kt`)

9. **Arbeidssøkerregisteret hentes ved behov.** Tjenesten finner aktiv arbeidssøkerperiode og henter eventuelt siste opplysninger og matchende profilering. Dette kan innebære opptil tre HTTP-kall. Er gjeldende vedtak funnet, hentes ikke profileringsgrunnlaget; registreringsstatus kan fortsatt hentes dersom den er etterspurt. (`OppslagArbeidssoekerregisteretService.kt`, `OppslagArbeidssoekerregisteretClientImpl.kt`)

10. **Varsel hentes ved behov.** Hvis `varsel` er etterspurt, undersøkes det om personen har et gjeldende eskaleringsvarsel i veilarbdialog. (`VisittkortVarselClientImpl.kt`)

### Sammensetting og retur

11. **Kilderesultatene samles og mappes til etiketter.** Resultatene samles i `VisittkortEtikettgrunnlag`, med `Hentet(data)` eller `Ukjent` for kildene som er brukt. Mapperen bruker etikettreglene og kombinerer flere kilder der det trengs, særlig for vedtak og profilering. En etikett blir eksempelvis `SKJERMET` når den gjelder, `null` ved kjent fravær eller `UKJENT_SKJERMING` når grunnlaget er ukjent. (`VisittkortKildeResultat.kt`, `VisittkortEtiketterMapper.kt`)

12. **Svaret returneres til frontend.** Mapperen lager et `VisittkortEtiketter`-objekt. Tjenesten registrerer varighet og, i normalflyten, ukjente etiketter. Controlleren returnerer objektet, og GraphQL sender bare feltene frontend har bedt om. (`VisittkortEtiketter.kt`, `VisittkortEtiketterService.kt`, `VisittkortEtiketterGraphQLController.kt`)

### Feilflyt

**Nettverks-/IO-feil og HTTP 5xx** gjør kilderesultatet til `Ukjent`, og behandlingen fortsetter. Mapperen avgjør hvilke etiketter som må bli `UKJENT_*`; andre etiketter kan fortsatt returneres. (`VisittkortKildeHttpClient.kt`, `VisittkortEtiketterService.kt`, `VisittkortEtiketterMapper.kt`)

**Tilgangsfeil, manglende klientkonfigurasjon og ugyldige kildesvar** avbryter derimot etikettoppslaget. Feilen oversettes til en fast GraphQL-feilkode, og `visittkortEtiketter` blir `null` med tilhørende oppføring i `errors`. Også GraphQL-`errors` fra oppfølgingskallet regnes i dagens klient som ugyldig kildesvar, ikke som et vanlig `Ukjent`-resultat. (`VisittkortEtiketterExceptionResolver.kt`, `VeilarboppfolgingClientImpl.java`)

## Filoversikt

Oversikten omfatter de **46 filene som ble gjennomgått: 29 nye og 17 endrede**. Denne dokumentasjonsfilen kommer i tillegg.

### Konfigurasjon og dokumentasjon

| Fil | Status | Hva brukes filen eller endringen til? |
|---|---|---|
| `.github/workflows/build-deploy-feature-branch-dev-gcp.yaml` | Endret | Deployer Unleash-tokenressursen før appen ved utrulling av en feature-branch til dev. |
| `.github/workflows/deploy-unleash-apitoken.yaml` | Ny | Egen workflow for å deploye Unleash-tokenressursen til dev. |
| `README.md` | Endret | Dokumenterer etikettspørringen, feltverdier, kildeavhengigheter, tilgang, feilhåndtering og driftsmetrikker. |
| `nais-dev-gcp.yaml` | Endret | Setter opp Unleash samt URL-er, dev-scopes og outbound-tilgang til dialog og vedtaksstøtte. |
| `nais-prod-gcp.yaml` | Endret | Legger til URL-er, prod-scopes og outbound-tilgang til dialog og vedtaksstøtte. |
| `pom.xml` | Endret | Legger til Unleash-SDK og utelukker SDK-ens OkHttp-avhengighet for å bruke prosjektets versjon. |
| `unleash-apitoken-dev.yaml` | Ny | Bestiller Unleash-token og tilhørende Kubernetes-secret for development gjennom Nais. |

### Produksjonskode

Filene ligger under `src/main/java/no/nav/veilarbperson/`.

| Fil | Status | Hva brukes filen eller endringen til? |
|---|---|---|
| [`VisittkortKildeHttpClient.kt`](src/main/java/no/nav/veilarbperson/client/VisittkortKildeHttpClient.kt) | Ny | Felles HTTP-håndtering, kildefeiltyper og valideringshjelpere for JSON-svar. |
| [`SkjermetClientImpl.java`](src/main/java/no/nav/veilarbperson/client/nom/SkjermetClientImpl.java) | Endret | Bruker felles HTTP-håndtering, krever boolsk svar og støtter injisert HTTP-klient i tester. |
| [`OppslagArbeidssoekerregisteretClientImpl.kt`](src/main/java/no/nav/veilarbperson/client/oppslagArbeidssoekerregisteret/OppslagArbeidssoekerregisteretClientImpl.kt) | Endret | Samler PAW-kall, validerer svar strengere og legger til en kompakt visittkortmodell. |
| [`PdlClient.java`](src/main/java/no/nav/veilarbperson/client/pdl/PdlClient.java) | Endret | Utvider grensesnittet med eget oppslag for etterspurte etikettopplysninger. |
| [`PdlClientImpl.java`](src/main/java/no/nav/veilarbperson/client/pdl/PdlClientImpl.java) | Endret | Henter valgte PDL-etikettfelt i ett OBO-kall med behandlingsnummer; gjør HTTP-klienten utskiftbar i tester. |
| [`PdlVisittkortEtiketterData.kt`](src/main/java/no/nav/veilarbperson/client/pdl/PdlVisittkortEtiketterData.kt) | Ny | Parser og validerer PDL-svaret til et minimalt grunnlag for de fem PDL-etikettene. |
| [`RepresentasjonClient.kt`](src/main/java/no/nav/veilarbperson/client/representasjon/RepresentasjonClient.kt) | Endret | Legger til samlet oppslag og resultatmodell for vergemål og fullmakt innen oppfølging. |
| [`RepresentasjonClientImpl.kt`](src/main/java/no/nav/veilarbperson/client/representasjon/RepresentasjonClientImpl.kt) | Endret | Henter vergemål og fullmakt samlet og filtrerer begge på `OPP`-rettigheter. |
| [`VisittkortVarselClient.kt`](src/main/java/no/nav/veilarbperson/client/veilarbdialog/VisittkortVarselClient.kt) | Ny | Grensesnitt for å avklare om en person har et gjeldende eskaleringsvarsel. |
| [`VisittkortVarselClientImpl.kt`](src/main/java/no/nav/veilarbperson/client/veilarbdialog/VisittkortVarselClientImpl.kt) | Ny | Henter og validerer gjeldende eskaleringsvarsel fra veilarbdialog; tolker HTTP 204 som fravær. |
| [`VeilarboppfolgingClient.java`](src/main/java/no/nav/veilarbperson/client/veilarboppfolging/VeilarboppfolgingClient.java) | Endret | Utvider grensesnittet med KVP-status og feltstyrt uthenting av oppfølgingsgrunnlag. |
| [`VeilarboppfolgingClientImpl.java`](src/main/java/no/nav/veilarbperson/client/veilarboppfolging/VeilarboppfolgingClientImpl.java) | Endret | Henter nødvendige oppfølgingsfelt i ett GraphQL-kall og validerer bare etterspurt grunnlag. |
| [`VisittkortVedtakClient.kt`](src/main/java/no/nav/veilarbperson/client/veilarbvedtaksstotte/VisittkortVedtakClient.kt) | Ny | Grensesnitt for å avklare om en person har et gjeldende oppfølgingsvedtak. |
| [`VisittkortVedtakClientImpl.kt`](src/main/java/no/nav/veilarbperson/client/veilarbvedtaksstotte/VisittkortVedtakClientImpl.kt) | Ny | Slår opp gjeldende § 14 a-vedtak og returnerer bare om vedtak finnes. |
| [`ClientConfig.java`](src/main/java/no/nav/veilarbperson/config/ClientConfig.java) | Endret | Registrerer vedtaks- og varselklientene med URL/scope og setter opp Unleash-klienten. |
| [`VisittkortEtiketterExceptionResolver.kt`](src/main/java/no/nav/veilarbperson/controller/VisittkortEtiketterExceptionResolver.kt) | Ny | Oversetter tilgangs-, konfigurasjons- og kildefeil til faste GraphQL-feilkoder uten rå kildedata. |
| [`VisittkortEtiketterGraphQLController.kt`](src/main/java/no/nav/veilarbperson/controller/VisittkortEtiketterGraphQLController.kt) | Ny | Eksponerer etikettspørringen, validerer input/feltvalg og krever intern bruker og auditert lesetilgang. |
| [`VisittkortEtiketter.kt`](src/main/java/no/nav/veilarbperson/domain/visittkort/VisittkortEtiketter.kt) | Ny | Definerer svarmodellen, enumtypene for alle 22 etiketter og identifikasjon av ukjente felt. |
| [`VisittkortKildeResultat.kt`](src/main/java/no/nav/veilarbperson/domain/visittkort/VisittkortKildeResultat.kt) | Ny | Modellerer hentet eller ukjent kilderesultat og samler grunnlagsdataene som mapperen bruker. |
| [`OppslagArbeidssoekerregisteretService.kt`](src/main/java/no/nav/veilarbperson/service/OppslagArbeidssoekerregisteretService.kt) | Endret | Gjenbruker utvalg av siste opplysninger og matchende profilering i et nytt, minimalt visittkortoppslag. |
| [`VisittkortEtiketterService.kt`](src/main/java/no/nav/veilarbperson/service/VisittkortEtiketterService.kt) | Ny | Koordinerer kilder og feltavhengigheter, måler oppslagene og støtter feilsimulering i dev. |
| [`VisittkortEtiketterMapper.kt`](src/main/java/no/nav/veilarbperson/utils/VisittkortEtiketterMapper.kt) | Ny | Utleder de 22 etikettene og skiller mellom gjeldende etikett, gyldig fravær og ukjent grunnlag. |

### GraphQL-ressurser

Filene ligger under `src/main/resources/graphql/`.

| Fil | Status | Hva brukes filen eller endringen til? |
|---|---|---|
| `hentVisittkortEtiketter.gql` | Ny | PDL-spørring med `include`-flagg for de fem etikettenes nødvendige personopplysninger. |
| `hentVisittkortOppfolging.gql` | Ny | Samlet oppfølgingsspørring med `include`-flagg for nødvendige felt og avhengigheter. |
| `schema.graphqls` | Endret | Utvider eksisterende GraphQL-kontrakt med `visittkortEtiketter`, de 22 feltene og tilhørende enumtyper. |

### Tester

Filene ligger under `src/test/java/no/nav/veilarbperson/`.

| Fil | Status | Hva brukes filen eller endringen til? |
|---|---|---|
| [`VeilarboppfolgingEtiketterClientTest.kt`](src/test/java/no/nav/veilarbperson/client/VeilarboppfolgingEtiketterClientTest.kt) | Ny | Dekker feltutvalg, GraphQL-variabler og mapping av gyldige oppfølgingssvar. |
| [`VeilarboppfolgingEtiketterClientTestBase.kt`](src/test/java/no/nav/veilarbperson/client/VeilarboppfolgingEtiketterClientTestBase.kt) | Ny | Felles HTTP-testoppsett, forventede variabler og representativt GraphQL-kildeskjema for oppfølging. |
| [`VeilarboppfolgingEtiketterKontraktOgSchemaTest.kt`](src/test/java/no/nav/veilarbperson/client/VeilarboppfolgingEtiketterKontraktOgSchemaTest.kt) | Ny | Dekker ugyldige svar, HTTP-feil og spørringens samsvar med det modellerte kildeskjemaet. |
| [`VisittkortKildeClientTest.kt`](src/test/java/no/nav/veilarbperson/client/VisittkortKildeClientTest.kt) | Ny | Verifiserer kildeklientenes kontrakter, filtrering, dataminimering og håndtering av ugyldige svar. |
| [`VisittkortKildeClientTestBase.kt`](src/test/java/no/nav/veilarbperson/client/VisittkortKildeClientTestBase.kt) | Ny | Felles simulerte HTTP-svar, forespørselsfangst, klientfabrikker og testdata for kildeklientene. |
| [`VisittkortKildeOppfolgingVedtakOgHttpTest.kt`](src/test/java/no/nav/veilarbperson/client/VisittkortKildeOppfolgingVedtakOgHttpTest.kt) | Ny | Tester KVP, vedtak og varsel samt HTTP-statuser, feil og gyldig fravær. |
| [`ClientTestConfig.java`](src/test/java/no/nav/veilarbperson/config/ClientTestConfig.java) | Endret | Oppdaterer testklientene slik at de implementerer de nye PDL- og oppfølgingsmetodene. |
| [`VisittkortEtiketterGraphQLControllerTest.kt`](src/test/java/no/nav/veilarbperson/controller/VisittkortEtiketterGraphQLControllerTest.kt) | Ny | Dekker GraphQL-svar, input, feltutvalg, aliaser, fragmenter, behandlingsnummer og feilkoder. |
| [`VisittkortEtiketterTilgangTest.kt`](src/test/java/no/nav/veilarbperson/controller/VisittkortEtiketterTilgangTest.kt) | Ny | Kontrollerer ekte `AuthService`, POAO-policy og auditlogging før kildeoppslag. |
| [`OppslagArbeidssoekerregisteretServiceTest.kt`](src/test/java/no/nav/veilarbperson/service/OppslagArbeidssoekerregisteretServiceTest.kt) | Ny | Dekker registreringsstatus, siste opplysninger, matchende profilering og eksisterende API-oppførsel. |
| [`VisittkortEtiketterServiceTest.kt`](src/test/java/no/nav/veilarbperson/service/VisittkortEtiketterServiceTest.kt) | Ny | Dekker deling av oppslag, feltavhengigheter, kildefeil og feilsimuleringsflagget på og av. |
| [`VisittkortEtiketterServiceTestBase.kt`](src/test/java/no/nav/veilarbperson/service/VisittkortEtiketterServiceTestBase.kt) | Ny | Felles tjenestemocker, metrikkregister, HTTP-hjelpere og Unleash-mock med flagget avslått. |
| [`VisittkortEtiketterServiceVedtakProfileringOgFeilTest.kt`](src/test/java/no/nav/veilarbperson/service/VisittkortEtiketterServiceVedtakProfileringOgFeilTest.kt) | Ny | Tester vedtaks-/profileringsregler, manglende klientkonfigurasjon og feilflyt med ekte klientimplementasjoner. |
| [`VisittkortEtiketterMapperTest.kt`](src/test/java/no/nav/veilarbperson/utils/VisittkortEtiketterMapperTest.kt) | Ny | Dekker etikettreglene, kryssavhengigheter og skillet mellom gyldig fravær og ukjent grunnlag. |
