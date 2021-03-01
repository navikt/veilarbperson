package no.nav.veilarbperson;

import org.junit.Test;

public class TestApplicationTest {

    @Test
    public void smoketest() {
        System.setProperty("server.port", "0");
        TestApplication.main(new String[0]);
    }

}

