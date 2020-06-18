package no.nav.veilarbperson.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    // Veilarbproxy trenger dette endepunktet for å sjekke at tjenesten lever
    // /internal kan ikke brukes siden det blir stoppet før det kommer frem

    @GetMapping("/ping")
    public void ping() {}

}
