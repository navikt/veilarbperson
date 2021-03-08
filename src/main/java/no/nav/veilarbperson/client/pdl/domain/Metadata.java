package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;

@Data
public class Metadata {

    private String master;
    private Endringer endringer;

    @Data
    private static class Endringer {
        private String type;
        private String registrert;
        private String registrertAv;
        private String systemkilde;
        private String kilde;
    }
}
