package no.nav.veilarbperson.client.veilarboppfolging;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UnderOppfolging {

    private boolean underOppfolging;

    private boolean erManuell;

}
