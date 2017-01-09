package filter;

import no.nav.modig.core.context.SubjectHandlerUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class FakeLoginFilter implements Filter {

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        if (req.getRequestURI().matches("^(.*internal/selftest.*)|(.*index.html)|(.*feil.*)|((.*)\\.(js|css|jpg))")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        SubjectHandlerUtils.setInternBruker("Z990322");

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

}
