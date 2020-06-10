package no.nav.veilarbperson.consumer.kodeverk;

public interface Kodeverk {

    public String getNavn(String kode, String sprak);

    public static class KodeverkFallback implements Kodeverk {
        public String getNavn(String key, String sprak) {
            return key;
        }
    }
}
