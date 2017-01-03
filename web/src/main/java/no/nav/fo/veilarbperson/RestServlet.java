package no.nav.fo.veilarbperson;

import org.springframework.web.bind.annotation.*;

@RestController
public class RestServlet {

    @RequestMapping(value = "/person/{fnr}", produces = "application/json")
    public String person(@PathVariable String fnr) {
        return getPerson(fnr);
    }

    private String getPerson(String fnr) {
        return "{\"fornavn\": \"Liv\"," +
                "\"etternavn\": \"Ullmann\"}";
    }
}

