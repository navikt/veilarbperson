package no.nav.veilarbperson;

import org.junit.Test;

public class TestApplicationTest {
    @Test
    public void somktest() {
        System.setProperty("server.port", "0");
        TestApplication.main(new String[0]);
    }
}

