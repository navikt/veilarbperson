package no.nav.veilarbperson.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.getunleash.DefaultUnleash;
import io.getunleash.FakeUnleash;
import io.getunleash.Unleash;
import no.nav.common.utils.EnvironmentUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class UnleashClientConfigTest {
    private static final String TEST_FLAG = "veilarbperson.visittkort_etiketter_test_feil";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(context -> {
                context.getEnvironment().getPropertySources()
                        .remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                context.getEnvironment().getPropertySources()
                        .remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
            })
            .withUserConfiguration(UnleashClientConfig.class);

    @Test
    void prodStarterUtenUnleashProperties() {
        assertDisabledClient(Optional.of(false));
    }

    @Test
    void ukjentMiljoStarterUtenUnleashProperties() {
        assertDisabledClient(Optional.empty());
    }

    @Test
    void prodIgnorererUnleashProperties() {
        try (MockedStatic<EnvironmentUtils> environment = mockStatic(EnvironmentUtils.class)) {
            environment.when(EnvironmentUtils::isDevelopment).thenReturn(Optional.of(false));

            contextRunner.withPropertyValues(
                    "UNLEASH_SERVER_API_URL=invalid-url",
                    "UNLEASH_SERVER_API_TOKEN=test-token"
            ).run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(Unleash.class);
                assertThat(context.getBean(Unleash.class)).isExactlyInstanceOf(FakeUnleash.class);
                assertThat(context.getBean(Unleash.class).isEnabled(TEST_FLAG, true)).isFalse();
            });
        }
    }

    @Test
    void devKreverUnleashUrl() {
        assertMissingDevProperty("UNLEASH_SERVER_API_URL", "UNLEASH_SERVER_API_TOKEN=test-token");
    }

    @Test
    void devKreverUnleashToken() {
        assertMissingDevProperty("UNLEASH_SERVER_API_TOKEN", "UNLEASH_SERVER_API_URL=http://unleash.test");
    }

    @Test
    void devOppretterEkteUnleashKlient() {
        WireMockServer server = new WireMockServer(options().dynamicPort());
        server.start();
        try (MockedStatic<EnvironmentUtils> environment = mockStatic(EnvironmentUtils.class)) {
            environment.when(EnvironmentUtils::isDevelopment).thenReturn(Optional.of(true));
            server.stubFor(get(urlPathEqualTo("/api/client/features"))
                    .willReturn(aResponse().withHeader("Content-Type", "application/json")
                            .withBody("{\"version\":2,\"features\":[]}")));
            server.stubFor(post(urlPathMatching("/api/client/.*"))
                    .willReturn(aResponse().withStatus(200)));

            contextRunner.withPropertyValues(
                    "UNLEASH_SERVER_API_URL=" + server.baseUrl(),
                    "UNLEASH_SERVER_API_TOKEN=test-token"
            ).run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(Unleash.class);
                assertThat(context.getBean(Unleash.class)).isExactlyInstanceOf(DefaultUnleash.class);
            });
        } finally {
            server.stop();
        }
    }

    private void assertDisabledClient(Optional<Boolean> development) {
        try (MockedStatic<EnvironmentUtils> environment = mockStatic(EnvironmentUtils.class)) {
            environment.when(EnvironmentUtils::isDevelopment).thenReturn(development);

            contextRunner.run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(Unleash.class);
                assertThat(context.getBean(Unleash.class)).isExactlyInstanceOf(FakeUnleash.class);
                assertThat(context.getBean(Unleash.class).isEnabled(TEST_FLAG)).isFalse();
                assertThat(context.getBean(Unleash.class).isEnabled(TEST_FLAG, true)).isFalse();
            });
        }
    }

    private void assertMissingDevProperty(String missingProperty, String suppliedProperty) {
        try (MockedStatic<EnvironmentUtils> environment = mockStatic(EnvironmentUtils.class)) {
            environment.when(EnvironmentUtils::isDevelopment).thenReturn(Optional.of(true));

            contextRunner.withPropertyValues(suppliedProperty).run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                        .hasRootCauseInstanceOf(IllegalStateException.class)
                        .hasStackTraceContaining("Required key '" + missingProperty + "' not found");
            });
        }
    }
}
