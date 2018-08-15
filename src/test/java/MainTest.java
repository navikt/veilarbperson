import no.nav.fo.veilarbperson.TestContext;
import no.nav.testconfig.ApiAppTest;

class MainTest {

    public static final String TEST_PORT = "8802";

    public static void main(String[] args) {
        TestContext.setup();

        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName("veilarbperson").build());

        Main.main(TEST_PORT);
    }

}