package no.nav.veilarbperson.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Slf4j
@Component
public class FnrUsageLoggerInterceptor implements HandlerInterceptor {

    private static final String NAV_CONSUMER_ID_HEADER_NAME = "Nav-Consumer-Id";
    private static final String MDC_ENDPOINT_KEY = "endpoint";

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String fnrPattern = "(^|\\W)\\d{11}(?=$|\\W)";

        boolean hasFnrInRequestURI = StringUtils.notNullOrEmpty(requestURI) && requestURI.matches(fnrPattern);
        boolean hasFnrInQueryString = StringUtils.notNullOrEmpty(queryString) && requestURI.matches(fnrPattern);

        if (hasFnrInRequestURI || hasFnrInQueryString) {
            String consumerId = Optional.ofNullable(request.getHeader(NAV_CONSUMER_ID_HEADER_NAME)).orElse("unknown");

            MDC.put(MDC_ENDPOINT_KEY, requestURI);
            log.info("Konsument {} forespurte endepunkt {} som matcher fnr-regex.", consumerId, requestURI);
            MDC.remove(MDC_ENDPOINT_KEY);
        }

        return true;
    }
}
