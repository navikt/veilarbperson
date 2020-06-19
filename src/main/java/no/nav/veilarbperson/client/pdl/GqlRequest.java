package no.nav.veilarbperson.client.pdl;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GqlRequest<V> {
    String query;
    V variables;
}
