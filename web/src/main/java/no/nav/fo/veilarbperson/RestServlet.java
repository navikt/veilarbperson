package no.nav.fo.veilarbperson;

import org.springframework.web.bind.annotation.*;

@RestController
public class RestServlet {

    @RequestMapping(value = "/person", produces = "application/json")
    public String index() {
        return "{\"fornavn\": \"Liv\"," +
                "\"etternavn\": \"Ullmann\"}";
    }
}

