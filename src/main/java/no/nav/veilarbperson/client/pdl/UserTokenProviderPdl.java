package no.nav.veilarbperson.client.pdl;

import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.utils.DownstreamApi;

import java.util.function.Supplier;

public class UserTokenProviderPdl {
    private final Supplier<String> supplier;

    public UserTokenProviderPdl(AuthService authService, String cluster) {
        supplier = authService.contextAwareUserTokenSupplier(new DownstreamApi(cluster, "pdl","pdl-api"));
    }

    public UserTokenProviderPdl(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    public Supplier<String> get() {
        return supplier;
    }
}
