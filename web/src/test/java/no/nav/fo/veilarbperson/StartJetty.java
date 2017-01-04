package no.nav.fo.veilarbperson;

import no.nav.sbl.dialogarena.common.jetty.Jetty;

import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;
import static no.nav.sbl.dialogarena.common.jetty.JettyStarterUtils.*;

public class StartJetty {

    private static final int PORT = 8488;

    public static void main(String []args) {
        Jetty jetty = usingWar()
                .at("/veilarbperson")
                .port(PORT)
                .overrideWebXml()
                .buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }

}
