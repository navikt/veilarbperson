package no.nav.veilarbperson.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {

    private final FnrUsageLoggerInterceptor fnrUsageLoggerInterceptor;

    @Autowired
    public CustomWebMvcConfigurer(final FnrUsageLoggerInterceptor fnrUsageLoggerInterceptor) {
        this.fnrUsageLoggerInterceptor = fnrUsageLoggerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> personApiPaths = List.of(
                "/api/person",
                "/api/person/**",
                "/api/v2/person",
                "/api/v2/person/**",
                "/api/v3/person",
                "/api/v3/person/**"
        );

        registry.addInterceptor(fnrUsageLoggerInterceptor).addPathPatterns(personApiPaths);
    }
}
