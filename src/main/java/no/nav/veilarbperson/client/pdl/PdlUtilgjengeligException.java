package no.nav.veilarbperson.client.pdl;

public class PdlUtilgjengeligException extends RuntimeException {
    public PdlUtilgjengeligException() {
        super("Tolkeoppslaget i PDL er utilgjengelig");
    }
}
