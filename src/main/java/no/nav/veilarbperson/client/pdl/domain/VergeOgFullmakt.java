package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain=true)
public class VergeOgFullmakt {
    private List<HentPdlPerson.VergemaalEllerFremtidsfullmakt> vergeEllerFremtidsfullmakt;
    private List<HentPdlPerson.Fullmakt> fullmakt;

    public VergeOgFullmakt() {
        vergeEllerFremtidsfullmakt = new ArrayList<>();
        fullmakt = new ArrayList<>();
    }
}


