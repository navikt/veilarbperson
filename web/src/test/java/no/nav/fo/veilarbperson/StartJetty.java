package no.nav.fo.veilarbperson;

import no.nav.brukerdialog.security.context.JettySubjectHandler;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.test.SystemProperties;
import org.apache.geronimo.components.jaspi.AuthConfigFactoryImpl;

import javax.security.auth.message.config.AuthConfigFactory;

import static java.security.Security.setProperty;
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;

class StartJetty {

    private static final int PORT = 8488;

    public static void main(String []args) {

        setupAutentisering();

        Jetty jetty = usingWar()
                .at("/veilarbperson")
                .port(PORT)
                .loadProperties("/environment-test.properties")
                .overrideWebXml()
                .configureForJaspic()
                .buildJetty();
        jetty.start();
    }

    private static void setupAutentisering() {
        SystemProperties.setFrom("environment-test.properties");
        System.setProperty("develop-local", "true");
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", JettySubjectHandler.class.getName());
        System.setProperty("org.apache.geronimo.jaspic.configurationFile", "web/src/test/resources/jaspiconf.xml");
        setProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY, AuthConfigFactoryImpl.class.getCanonicalName());
    }
}
