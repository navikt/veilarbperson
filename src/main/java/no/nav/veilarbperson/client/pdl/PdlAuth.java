package no.nav.veilarbperson.client.pdl;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

@Data
@AllArgsConstructor
public class PdlAuth {
    String authToken;
    Optional<String> navConsumerToken;
}
