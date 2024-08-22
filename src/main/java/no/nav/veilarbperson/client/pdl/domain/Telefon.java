package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Data
@Accessors(chain = true)
public class Telefon implements Comparable<Telefon> {
    private String prioritet;
    private String telefonNr;
    private String registrertDato;
    private String master;

    @Override
    public int compareTo(@NotNull Telefon o) {
        return getRegistrertDato().compareTo(o.getRegistrertDato());
    }
}
