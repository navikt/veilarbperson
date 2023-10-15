package no.nav.veilarbperson.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {

    private final FnrUsageLoggerInterceptor fnrUsageLoggerInterceptor;

    @Autowired
    public CustomWebMvcConfigurer(final FnrUsageLoggerInterceptor fnrUsageLoggerInterceptor) {
        this.fnrUsageLoggerInterceptor = fnrUsageLoggerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(fnrUsageLoggerInterceptor);
    }
}
