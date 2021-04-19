package no.nav.veilarbperson.service;

import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import static java.lang.String.format;

@Service
@Slf4j
public class KodeverkService {

    public final static String KODEVERK_LANDKODER = "Landkoder";
    public final static String KODEVERK_SIVILSTANDER = "Sivilstander";
    public final static String KODEVERK_POSTNUMMER = "Postnummer";
    public final static String KODEVERK_KOMMUNER = "Kommuner";
    public final static String KODEVERK_SPRAAK = "Språk";
    public final static String KODEVERK_TEMA = "Tema";
    private final KodeverkClient kodeverkClient;

    @Autowired
    public KodeverkService(KodeverkClient kodeverkClient) {
        this.kodeverkClient = kodeverkClient;
    }

    public String getBeskrivelseForLandkode(String kode) {
        return finnBeskrivelse(KODEVERK_LANDKODER, kode);
    }

    public String getBeskrivelseForSivilstand(String kode) {
        return finnBeskrivelse(KODEVERK_SIVILSTANDER, kode);
    }

    public String getPoststedForPostnummer(String postnummer) {
        return finnBeskrivelse(KODEVERK_POSTNUMMER, postnummer);
    }

    public String getBeskrivelseForKommunenummer(String kommunenummer) {
        return finnBeskrivelse(KODEVERK_KOMMUNER, kommunenummer);
    }

    public String getBeskrivelseForSpraakKode(String spraakKode) {
        return finnBeskrivelse(KODEVERK_SPRAAK, spraakKode);
    }

    public String getBeskrivelseForTema(String temaKode) {
        return finnBeskrivelse(KODEVERK_TEMA, temaKode);
    }

    private String finnBeskrivelse(String kodeverksnavn, String kode) {
        Map<String, String> betydninger = kodeverkClient.hentKodeverkBeskrivelser(kodeverksnavn);
        String betydning = betydninger.get(kode);

        if (betydning == null) {
            log.error(format("Fant ikke kode %s i kodeverk %s", kode, kodeverksnavn));
            return "Ikke tilgjengelig";
        }

        return betydning;
    }

}
