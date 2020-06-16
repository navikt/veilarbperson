package no.nav.veilarbperson.client.kodeverk;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.veilarbperson.config.CacheConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class KodeverkClientImpl implements KodeverkClient {

    private final static String KODEVERK_LANDKODER = "Landkoder";
    private final static String KODEVERK_SIVILSTANDER = "Sivilstander";
    private final static String KODEVERK_POSTNUMMER = "Postnummer";

    private final String kodeverkUrl;

    private final OkHttpClient client;

    public KodeverkClientImpl(String kodeverkUrl) {
        this.kodeverkUrl = kodeverkUrl;
        this.client = RestClient.baseClient();
    }

    @Override
    public String getBeskrivelseForLandkode(String kode) {
        return finnBeskrivelse(KODEVERK_LANDKODER, kode);
    }

    @Override
    public String getBeskrivelseForSivilstand(String kode) {
        return finnBeskrivelse(KODEVERK_SIVILSTANDER, kode);
    }

    @Override
    public String getPoststed(String postnummer) {
        return finnBeskrivelse(KODEVERK_POSTNUMMER, postnummer);
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(kodeverkUrl, "/internal/isAlive"), client);
    }

    @Cacheable(CacheConfig.KODEVERK_BETYDNING_CACHE_NAME)
    @SneakyThrows
    public Map<String, String> hentKodeverkBeskrivelser(String kodeverksnavn) {
        Request request = new Request.Builder()
                .url(joinPaths(kodeverkUrl, format("/api/v1/kodeverk/%s/koder/betydninger?ekskluderUgyldige=true&spraak=nb", kodeverksnavn)))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            Optional<String> maybeJsonResponse = RestUtils.getBodyStr(response);

            if (!maybeJsonResponse.isPresent()) {
                throw new IllegalStateException("JSON is missing from body");
            }

            return parseKodeverkBetydningJson(maybeJsonResponse.get());
        }
    }

    private String finnBeskrivelse(String kodeverksnavn, String kode) {
        Map<String, String> betydninger = hentKodeverkBeskrivelser(kodeverksnavn);
        String betydning = betydninger.get(kode);

        if (betydning == null) {
            throw new IllegalStateException(format("Fant ikke kode %s i kodeverk %s", kode, kodeverksnavn));
        }

        return betydning;
    }

    @SneakyThrows
    private Map<String, String> parseKodeverkBetydningJson(String responseJson) {
        Map<String, String> betydningerMap = new HashMap<>();

        JsonNode rootNode = JsonUtils.getMapper().readTree(responseJson);
        JsonNode betydninger = rootNode.get("betydninger");

        betydninger.fieldNames().forEachRemaining((betydningName) -> {
            JsonNode betydningNode = betydninger.get(betydningName).get(0);

            // Noen koder mangler informasjon
            if (betydningNode == null) {
                return;
            }

            JsonNode betydningBeskrivelserNode = betydningNode.get("beskrivelser");
            JsonNode beskrivelseNbNode = betydningBeskrivelserNode.get("nb");
            String beskrivelseNb = beskrivelseNbNode.get("tekst").asText();

            betydningerMap.put(betydningName, beskrivelseNb);
        });

        return betydningerMap;
    }

}
