package no.nav.fo.veilarbperson.internal;

import javax.servlet.http.*;
import java.io.IOException;

public class IsAliveServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("{status : \"ok\", message: \"Veileder arbeid person fungerer!\"}");
    }
}
