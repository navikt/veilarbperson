package no.nav.veilarbperson.client.representasjon;

import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public interface RepresentasjonClient {
    List<Fullmakt> getFullmakt(String kryptertIdent) throws IOException;
}
