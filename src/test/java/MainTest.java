import no.nav.testconfig.ApiAppTest;

class MainTest {

    public static final String TEST_PORT = "8438";

    public static void main(String[] args) {
        // TODO: Gjør det mulig å kjøre applikasjonen lokalt
        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName("veilarbperson").build());
        Main.main(TEST_PORT);
    }

}
