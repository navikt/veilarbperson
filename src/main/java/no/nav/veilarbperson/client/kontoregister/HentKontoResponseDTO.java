package no.nav.veilarbperson.client.kontoregister;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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

    public String getNorskKontonummer() {
        return kontonummer;
    }
    public void setNorskKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
    }

}
