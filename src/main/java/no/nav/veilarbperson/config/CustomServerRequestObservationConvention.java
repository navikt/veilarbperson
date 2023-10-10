package no.nav.veilarbperson.config;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
public class CustomServerRequestObservationConvention extends DefaultServerRequestObservationConvention {

    @NotNull
    @Override
    public KeyValues getLowCardinalityKeyValues(@NotNull ServerRequestObservationContext context) {
        return super
                .getLowCardinalityKeyValues(context)
                .and(consumerId(context));
    }

    protected KeyValue consumerId(ServerRequestObservationContext context) {
        HttpServletRequest httpRequest = context.getCarrier();
        String consumerId = ofNullable(httpRequest.getHeader("Nav-Consumer-Id"))
                .filter(v -> !v.isBlank())
                .orElse("unknown");

        return KeyValue.of("consumerId", consumerId);
    }
}
