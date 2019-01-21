package no.nav.fo.veilarbperson.utils;

import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SubjectHandler;

public class AutentiseringHjelper {

    public static boolean erEksternBruker() {
        IdentType identType = SubjectHandler.getIdentType().orElse(null);
        return IdentType.EksternBruker.equals(identType);
    }

    public static boolean erInternBruker() {
        IdentType identType = SubjectHandler.getIdentType().orElse(null);
        return IdentType.InternBruker.equals(identType);
    }


}
