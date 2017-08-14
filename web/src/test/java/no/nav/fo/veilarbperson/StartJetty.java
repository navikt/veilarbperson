package no.nav.fo.veilarbperson;

import no.nav.dialogarena.config.DevelopmentSecurity;

import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;

class StartJetty {

    private static final int PORT = 8438;

    public static void main(String []args) {
        DevelopmentSecurity.setupISSO(usingWar()
                        .at("/veilarbperson")
                        .port(PORT)
                        .loadProperties("/environment-test.properties")
                , new DevelopmentSecurity.ISSOSecurityConfig("veilarbperson", "t6")
        ).buildJetty().start();
    }

}
