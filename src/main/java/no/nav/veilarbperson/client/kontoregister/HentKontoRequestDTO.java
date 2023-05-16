package no.nav.veilarbperson.client.kontoregister;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HentKontoRequestDTO {
    private String kontohaver;
    public String getKontohaver() {
        return kontohaver;
    }

    public void setKontohaver(String kontohaver) {
        this.kontohaver = kontohaver;
    }


}
