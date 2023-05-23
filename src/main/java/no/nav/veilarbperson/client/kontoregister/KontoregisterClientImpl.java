package no.nav.veilarbperson.client.kontoregister;


import lombok.extern.slf4j.Slf4j;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.utils.UrlUtils;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.veilarbperson.config.CacheConfig;
import no.nav.veilarbperson.domain.Enhet;
import no.nav.veilarbperson.domain.PersonDataKontoregister;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class KontoregisterClientImpl implements KontoregisterClient {

    private final String kontoregisterUrl;
    private static final String KONTOREGISTER_API_URL = "/api/system/v1/hent-aktiv-konto";

    private final Supplier<String> systemUserTokenProvider;

    private final OkHttpClient client;

    public KontoregisterClientImpl(String kontoregisterUrl, Supplier<String> systemUserTokenProvider) {
        this.kontoregisterUrl = kontoregisterUrl;
        this.systemUserTokenProvider = systemUserTokenProvider;
        this.client = RestClient.baseClient();
    }

    @Cacheable(CacheConfig.KONTOREGISTER_CACHE_NAME)
    @Override
    public HentKontoResponseDTO hentKontonummer(HentKontoRequestDTO kontohaver) {
        log.info("I hentKontonummerImpl Url={}, kontohaver.getKontohaver()={}, token={}", UrlUtils.joinPaths(kontoregisterUrl, KONTOREGISTER_API_URL), kontohaver.getKontohaver(), systemUserTokenProvider.toString());
        Request request = new Request.Builder()
                .url(UrlUtils.joinPaths(kontoregisterUrl, KONTOREGISTER_API_URL))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + systemUserTokenProvider.get())
                .post(RestUtils.toJsonRequestBody(kontohaver))
                .build();
        log.info("Request til kontoreg url = {},  auth = {}", request.url(), systemUserTokenProvider.get());

        try (Response response = client.newCall(request).execute()) {
            log.info("svar fra kontoreg: message = {}, challenges = {}, TokenProvider = {}, responsKod = {}, responsBody = {}", response.message(), response.challenges(), systemUserTokenProvider.get(), response.code(), response.body());
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponse(response, HentKontoResponseDTO.class)
                    .orElseThrow(() -> new IllegalStateException("HentKontonummer body is missing"));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        Request request = new Request.Builder()
                .url(joinPaths(kontoregisterUrl, "/rest/ping"))
                .header(AUTHORIZATION, "Bearer " + systemUserTokenProvider.get())
                .build();

        return HealthCheckUtils.pingUrl(request, client);
    }

    public static class PersonDataMapper {

        public static PersonDataKontoregister tilKontoregisterPerson(Person person) {
            return new PersonDataKontoregister().setKontonummer(kanskjeKontonummer(person));
        }

        private static String kanskjeKontonummer(Person person) {
            if (person instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) {
                no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkonto bankkonto =
                        ((no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) person).getBankkonto();
                return kanskjeKontonummer(bankkonto);
            }
            return null;
        }

        private static String kanskjeKontonummer(no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkonto bankkonto) {
            if (bankkonto instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge bankkontoNorge) {
                return ofNullable(bankkontoNorge.getBankkonto())
                        .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer::getBankkontonummer)
                        .orElse(null);
            } else if (bankkonto instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland wsBankkontoUtland) {
                return ofNullable(wsBankkontoUtland.getBankkontoUtland())
                        .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontonummerUtland::getBankkontonummer)
                        .orElse(null);
            }
            return null;
        }
    }

    public static class Mappers {
        public static Enhet fraNorg2Enhet(no.nav.common.client.norg2.Enhet enhet) {
            return new Enhet(enhet.getEnhetNr(), enhet.getNavn());
        }
    }
}


