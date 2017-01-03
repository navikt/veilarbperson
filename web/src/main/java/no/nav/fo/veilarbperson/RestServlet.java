package no.nav.fo.veilarbperson;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestServlet {

    @RequestMapping("/person")
    public String index() {
        return "{\"fornavn\": \"Liv\"," +
                "\"etternavn\": \"Ullmann\"}";
    }
}

