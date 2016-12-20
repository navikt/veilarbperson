package no.nav.fo.veilarbperson;

import org.slf4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class IsAliveServlet extends HttpServlet {

    private static final Logger logger = getLogger(IsAliveServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("{status : \"ok\", message: \"Veileder arbeid person fungerer!\"}");
    }
}
