package no.nav.fo.veilarbperson;

import no.nav.sbl.dialogarena.common.web.selftest.SelfTestBaseServlet;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import java.util.*;

import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;

public class SelftestServlet extends SelfTestBaseServlet {

    private ApplicationContext applicationContext;

    @Override
    public void init() throws ServletException {
        super.init();
        applicationContext = getWebApplicationContext(getServletContext());
    }

    @Override
    public String getApplicationName() {
        return "veilarbperson";
    }

    @Override
    public Collection<? extends Pingable> getPingables() {
        final List<Pingable> pingables = new ArrayList<>(applicationContext.getBeansOfType(Pingable.class).values());
        return pingables;
    }

}
