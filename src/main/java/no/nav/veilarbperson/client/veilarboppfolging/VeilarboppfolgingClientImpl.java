package no.nav.veilarbperson.client.veilarboppfolging;

import tools.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.VisittkortKildeHttpClient;
import no.nav.veilarbperson.client.VisittkortKildeKontraktException;
import no.nav.veilarbperson.config.CacheConfig;
import no.nav.veilarbperson.domain.visittkort.OppfolgingEtikettgrunnlag;
import no.nav.veilarbperson.domain.visittkort.UtmeldingskandidatEtikett;
import no.nav.veilarbperson.utils.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class VeilarboppfolgingClientImpl implements VeilarboppfolgingClient {

    private final String veilarboppfolgingUrl;

    private final Supplier<String> userTokenProvider;

    private final OkHttpClient client;

    private final String oppfolgingQuery = FileUtils.getResourceFileAsString("graphql/hentVisittkortOppfolging.gql");

    public VeilarboppfolgingClientImpl(String veilarboppfolgingUrl, Supplier<String> userTokenProvider) {
        this(veilarboppfolgingUrl, userTokenProvider, RestClient.baseClient());
    }

    public VeilarboppfolgingClientImpl(String veilarboppfolgingUrl, Supplier<String> userTokenProvider, OkHttpClient client) {
        this.veilarboppfolgingUrl = veilarboppfolgingUrl;
        this.userTokenProvider = userTokenProvider;
        this.client = client;
    }

    @Override
    public boolean hentKvpStatus(Fnr fnr) {
        Boolean kvp = hentEtikettgrunnlag(fnr, Set.of("kvp")).getKvp();
        if (kvp == null) {
            throw new VisittkortKildeKontraktException();
        }
        return kvp;
    }

    @Override
    public OppfolgingEtikettgrunnlag hentEtikettgrunnlag(Fnr fnr, Set<String> felter) {
        Utvalg utvalg = Utvalg.fra(felter);
        Request request = new Request.Builder()
                .url(joinPaths(veilarboppfolgingUrl, "/api/graphql"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + userTokenProvider.get())
                .post(RestUtils.toJsonRequestBody(Map.of("query", oppfolgingQuery, "variables", utvalg.variabler(fnr))))
                .build();
        JsonNode root = new VisittkortKildeHttpClient(client).hent(request);
        if (root == null || !root.isObject()) {
            throw new VisittkortKildeKontraktException();
        }
        JsonNode errors = root.get("errors");
        if (errors != null && !errors.isNull() && (!errors.isArray() || !errors.isEmpty())) {
            throw new VisittkortKildeKontraktException();
        }
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new VisittkortKildeKontraktException();
        }
        return utvalg.les(data);
    }

    @Cacheable(CacheConfig.VEILARBOPPFOLGING_UNDER_OPPFOLGING_CACHE_NAME)
    @SneakyThrows
    @Override
    public UnderOppfolging hentUnderOppfolgingStatus(Fnr fnr) {
        Request request = new Request.Builder()
                .url(joinPaths(veilarboppfolgingUrl, "/api/v2/hent-underOppfolging"))
                .post(RestUtils.toJsonRequestBody(new HentUnderoppfolgingRequest(fnr)))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + userTokenProvider.get())
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseOrThrow(response, UnderOppfolging.class);
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(veilarboppfolgingUrl, "/internal/isAlive"), client);
    }

    // Beregnes en gang per kall for GraphQL-variabler og parsing av svaret.
    private record Utvalg(
            boolean kvp,
            boolean manuell,
            boolean reservertKrr,
            boolean registrertKrr,
            boolean kanVarsles,
            boolean sykmeldt,
            boolean utmeldingskandidat,
            boolean oppfolging,
            boolean kreverUnderOppfolging
    ) {
        static Utvalg fra(Set<String> felter) {
            if (felter == null) {
                throw new IllegalArgumentException("No relevant follow-up fields requested");
            }
            boolean utdatertIKrr = felter.contains("utdatertIKrr");
            boolean kreverUnderOppfolging = felter.contains("ikkeUnderOppfolging")
                    || felter.contains("trengerOppfolgingsvedtak");
            boolean profil = felter.contains("antattGodeMuligheter")
                    || felter.contains("antattBehovForVeiledning")
                    || felter.contains("oppgittHindringer");
            Utvalg utvalg = new Utvalg(
                    felter.contains("kvp"),
                    felter.contains("manuellOppfolging") || utdatertIKrr,
                    felter.contains("reservertKrr") || utdatertIKrr,
                    felter.contains("ikkeRegistrertKrr") || utdatertIKrr,
                    utdatertIKrr,
                    felter.contains("sykmeldt"),
                    felter.contains("utmeldingskandidat"),
                    kreverUnderOppfolging || profil,
                    kreverUnderOppfolging
            );
            if (!utvalg.brukerStatus() && !utvalg.oppfolging && !utvalg.utmeldingskandidat) {
                throw new IllegalArgumentException("No relevant follow-up fields requested");
            }
            return utvalg;
        }

        boolean krr() {
            return reservertKrr || registrertKrr || kanVarsles;
        }

        boolean brukerStatus() {
            return kvp || manuell || krr() || sykmeldt;
        }

        Map<String, Object> variabler(Fnr fnr) {
            Map<String, Object> variabler = new LinkedHashMap<>();
            variabler.put("fnr", fnr.get());
            variabler.put("hentBrukerStatus", brukerStatus());
            variabler.put("hentKvp", kvp);
            variabler.put("hentManuell", manuell);
            variabler.put("hentKrr", krr());
            variabler.put("hentKanVarsles", kanVarsles);
            variabler.put("hentReservertKrr", reservertKrr);
            variabler.put("hentRegistrertKrr", registrertKrr);
            variabler.put("hentSykmeldt", sykmeldt);
            variabler.put("hentOppfolging", oppfolging);
            variabler.put("hentUtmeldingskandidat", utmeldingskandidat);
            return variabler;
        }

        OppfolgingEtikettgrunnlag les(JsonNode data) {
            BrukerStatus status = brukerStatus()
                    ? lesBrukerStatus(objekt(data.get("brukerStatus")))
                    : BrukerStatus.TOM;
            Boolean underOppfolging = lesUnderOppfolging(data);
            UtmeldingskandidatEtikett etikett = lesUtmeldingskandidat(data);

            return new OppfolgingEtikettgrunnlag(
                    underOppfolging, status.erManuell(), status.krr().reservertIKrr(), status.krr().registrertIKrr(),
                    status.krr().kanVarsles(), status.erSykmeldt(), etikett, status.erKontorsperret()
            );
        }

        private BrukerStatus lesBrukerStatus(JsonNode brukerStatus) {
            Boolean erKontorsperret = kvp ? boolsk(brukerStatus.get("erKontorsperret")) : null;
            Boolean erManuell = manuell ? boolsk(objekt(brukerStatus.get("manuell")).get("erManuell")) : null;
            KrrStatus krrStatus = lesKrr(brukerStatus);
            Boolean erSykmeldt = sykmeldt ? lesSykmeldt(brukerStatus.get("sykmeldtStatus")) : null;
            return new BrukerStatus(erKontorsperret, erManuell, krrStatus, erSykmeldt);
        }

        private KrrStatus lesKrr(JsonNode brukerStatus) {
            if (!krr()) {
                return KrrStatus.TOM;
            }
            JsonNode krr = objekt(brukerStatus.get("krr"));
            Boolean kanVarslesKrr = kanVarsles ? boolsk(krr.get("kanVarsles")) : null;
            Boolean reservertIKrr = reservertKrr ? boolsk(krr.get("reservertIKrr")) : null;
            Boolean registrertIKrr = registrertKrr ? boolsk(krr.get("registrertIKrr")) : null;
            return new KrrStatus(reservertIKrr, registrertIKrr, kanVarslesKrr);
        }

        private static boolean lesSykmeldt(JsonNode sykmeldtStatus) {
            if (sykmeldtStatus == null || !(sykmeldtStatus.isNull() || sykmeldtStatus.isString())) {
                throw new VisittkortKildeKontraktException();
            }
            return !sykmeldtStatus.isNull();
        }

        @Nullable
        private Boolean lesUnderOppfolging(JsonNode data) {
            if (!oppfolging) {
                return null;
            }
            JsonNode erUnderOppfolging = objekt(data.get("oppfolging")).get("erUnderOppfolging");
            if (erUnderOppfolging == null) {
                throw new VisittkortKildeKontraktException();
            }
            if (kreverUnderOppfolging || !erUnderOppfolging.isNull()) {
                return boolsk(erUnderOppfolging);
            }
            return null;
        }

        private UtmeldingskandidatEtikett lesUtmeldingskandidat(JsonNode data) {
            if (!utmeldingskandidat) {
                return null;
            }
            JsonNode tag = objekt(data.get("utmeldingskandidat")).get("tag");
            if (tag == null || !(tag.isNull() || tag.isString())) {
                throw new VisittkortKildeKontraktException();
            }
            return tag.isNull() ? null : utmeldingskandidatEtikett(tag.stringValue());
        }

        private static UtmeldingskandidatEtikett utmeldingskandidatEtikett(String tag) {
            for (UtmeldingskandidatEtikett etikett : UtmeldingskandidatEtikett.getEntries()) {
                if (!etikett.getErUkjent() && etikett.name().equals(tag)) {
                    return etikett;
                }
            }
            throw new VisittkortKildeKontraktException();
        }

        private static JsonNode objekt(JsonNode node) {
            if (node == null || !node.isObject()) {
                throw new VisittkortKildeKontraktException();
            }
            return node;
        }

        private static boolean boolsk(JsonNode node) {
            if (node == null || !node.isBoolean()) {
                throw new VisittkortKildeKontraktException();
            }
            return node.booleanValue();
        }

        private record KrrStatus(Boolean reservertIKrr, Boolean registrertIKrr, Boolean kanVarsles) {
            static final KrrStatus TOM = new KrrStatus(null, null, null);
        }

        private record BrukerStatus(Boolean erKontorsperret, Boolean erManuell, KrrStatus krr, Boolean erSykmeldt) {
            static final BrukerStatus TOM = new BrukerStatus(null, null, KrrStatus.TOM, null);
        }
    }
}
