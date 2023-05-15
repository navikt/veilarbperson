package no.nav.veilarbperson.client.kontoregister;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class HentKontoResponseDTO {

    private HttpStatus status;

    private String kontohaver;
    private String kontonummer;

    private class UtenlandskKontoDTO {
        private String banknavn;
        private String bankkode;
        private String bankLandkode;
        private String valutakode;
        private String swiftBicKode;
        private String bankadresse1;
        private String bankadresse2;
        private String bankadresse3;
    }

    private LocalDateTime gyldigFom;
    private LocalDateTime gyldigTom;
    private String endretAv;
    private String opprettetAv;
}
