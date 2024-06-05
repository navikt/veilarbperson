package no.nav.veilarbperson.client.representasjon;

import okhttp3.Response;

import java.io.IOException;

public interface RepresentasjonClient {


    RepresentasjonFullmakt getFullmakt(String kryptertIdent) throws IOException;
}
