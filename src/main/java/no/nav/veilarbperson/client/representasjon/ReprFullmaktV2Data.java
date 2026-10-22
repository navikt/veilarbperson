package no.nav.veilarbperson.client.representasjon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import no.nav.common.types.identer.Fnr;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReprFullmaktV2Data {
    private final Fnr fullmaktsgiver;
    private final Fnr fullmektig;
    private final LocalDate gyldigFraOgMed;
    private final LocalDate gyldigTilOgMed;
    private final List<String> leserettigheter;
    private final List<String> skriverettigheter;

    @JsonCreator
    public ReprFullmaktV2Data(
            @JsonProperty(value = "fullmaktsgiver", required = true) Fnr fullmaktsgiver,
            @JsonProperty(value = "fullmektig", required = true) Fnr fullmektig,
            @JsonProperty(value = "gyldigFraOgMed", required = true) LocalDate gyldigFraOgMed,
            @JsonProperty(value = "gyldigTilOgMed", required = true) LocalDate gyldigTilOgMed,
            @JsonProperty(value = "leserettigheter", required = true) List<String> leserettigheter,
            @JsonProperty(value = "skriverettigheter", required = true) List<String> skriverettigheter) {
        this.fullmaktsgiver = Objects.requireNonNull(fullmaktsgiver, "Mangler fullmaktsgiver");
        this.fullmektig = Objects.requireNonNull(fullmektig, "Mangler fullmektig");
        if (fullmaktsgiver.get().isBlank() || fullmektig.get().isBlank()) {
            throw new IllegalArgumentException("Tom personident i representasjon");
        }
        this.gyldigFraOgMed = Objects.requireNonNull(gyldigFraOgMed, "Mangler startdato");
        // Feltet skal finnes i responsen; bare eksplisitt null betyr ingen sluttdato.
        this.gyldigTilOgMed = gyldigTilOgMed;
        this.leserettigheter = validerRettigheter(leserettigheter);
        this.skriverettigheter = validerRettigheter(skriverettigheter);
    }

    private static List<String> validerRettigheter(List<String> rettigheter) {
        if (rettigheter == null || rettigheter.stream().anyMatch(tema -> tema == null || tema.isBlank())) {
            throw new IllegalArgumentException("Manglende eller ugyldige rettigheter i representasjon");
        }
        return List.copyOf(rettigheter);
    }
}
