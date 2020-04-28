package no.nav.fo.veilarbperson.domain.person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjon;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class Kontaktinfo {

    private String epost;
    private String telefon;

    public static Kontaktinfo fraKrr(DigitalKontaktinformasjon digitalKontaktinformasjon) {
        return new Kontaktinfo()
                .setEpost(digitalKontaktinformasjon.getEpost())
                .setTelefon(digitalKontaktinformasjon.getTelefon());
    }
}
