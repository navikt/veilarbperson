package no.nav.veilarbperson.utils;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.representasjon.ReprFullmaktV2Data;
import no.nav.veilarbperson.domain.FullmaktDTO;
import no.nav.veilarbperson.domain.PersonNavn;
import no.nav.veilarbperson.domain.VergeData;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VergeOgFullmaktDataMapper {
    public static FullmaktDTO toFullmaktDTO(List<ReprFullmaktV2Data> fullmaktListe, Map<Fnr, PersonNavn> navn) {
        return new FullmaktDTO()
                .setFullmakt(representasjonFullmaktMapper(fullmaktListe, navn));
    }

    public static List<FullmaktDTO.Fullmakt> representasjonFullmaktMapper(List<ReprFullmaktV2Data> fullmaktListe, Map<Fnr, PersonNavn> navn) {
        return fullmaktListe.stream()
                .map(fullmakt ->
                        new FullmaktDTO.Fullmakt()
                                .setFullmaktsgiver(fullmakt.getFullmaktsgiver().get())
                                .setFullmaktsgiverNavn(fulltNavn(navn.get(fullmakt.getFullmaktsgiver())))
                                .setFullmektig(fullmakt.getFullmektig().get())
                                .setFullmektigsNavn(fulltNavn(navn.get(fullmakt.getFullmektig())))
                                .setOmraade(omraadeMapper(fullmakt.getLeserettigheter(), fullmakt.getSkriverettigheter()))
                                .setGyldigFraOgMed(fullmakt.getGyldigFraOgMed())
                                .setGyldigTilOgMed(fullmakt.getGyldigTilOgMed()))
                .collect(Collectors.toList());
    }

    private static String fulltNavn(PersonNavn navn) {
        Objects.requireNonNull(navn, "Mangler navn ved mapping av fullmakt");
        return Stream.of(navn.getFornavn(), navn.getMellomnavn(), navn.getEtternavn())
                .filter(del -> del != null && !del.isBlank())
                .collect(Collectors.joining(" "));
    }

    public static List<FullmaktDTO.OmraadeMedHandling> omraadeMapper(List<String> leserettigheter, List<String> skriverettigheter) {
        Set<String> lesetemaer = new LinkedHashSet<>(leserettigheter);
        Set<String> skrivetemaer = new LinkedHashSet<>(skriverettigheter);
        Set<String> temaer = new LinkedHashSet<>(lesetemaer);
        temaer.addAll(skrivetemaer);
        return temaer.stream().map(tema -> {
            List<FullmaktDTO.OmraadeHandlingType> handlinger = new ArrayList<>();
            if (lesetemaer.contains(tema)) {
                handlinger.add(FullmaktDTO.OmraadeHandlingType.LES);
            }
            if (skrivetemaer.contains(tema)) {
                handlinger.add(FullmaktDTO.OmraadeHandlingType.SKRIV);
            }
            return new FullmaktDTO.OmraadeMedHandling().setTema(tema).setHandling(handlinger);
        }).toList();
    }

    public static VergeData.VergemaalEllerFremtidsfullmakt toVergemaalEllerFremtidsfullmakt(HentPerson.VergemaalEllerFremtidsfullmakt vergemaalEllerFremtidsfullmakt, PersonNavn navn) {
        return new VergeData.VergemaalEllerFremtidsfullmakt()
                .setType(vergemaalEllerFremtidsfullmakt.getType())
                .setEmbete(vergemaalEllerFremtidsfullmakt.getEmbete())
                .setVergeEllerFullmektig(vergeEllerFullmektigMapper(vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig(), navn))
                .setFolkeregistermetadata(folkeregisterMetadataMapper(vergemaalEllerFremtidsfullmakt.getFolkeregistermetadata()));
    }

    public static VergeData.VergeEllerFullmektig vergeEllerFullmektigMapper(HentPerson.VergeEllerFullmektig vergeEllerFullmektig, PersonNavn navn) {
        VergeData.VergeNavn vergeNavn;

        if (navn != null) {
            vergeNavn = personnavnTilVergenavnMapper(navn);
        } else {
            vergeNavn = vergeNavnMapper(vergeEllerFullmektig.getIdentifiserendeInformasjon().getNavn());
        }

        return new VergeData.VergeEllerFullmektig()
                .setNavn(vergeNavn)
                .setMotpartsPersonident(vergeEllerFullmektig.getMotpartsPersonident())
                .setOmfang(vergeEllerFullmektig.getOmfang())
                .setTjenesteomraade(vergeEllerFullmektig.getTjenesteomraade().stream()
                        .map(tjenesteomraade -> new VergeData.Tjenesteomraade()
                                .setTjenesteoppgave(tjenesteomraade.getTjenesteoppgave())
                                .setTjenestevirksomhet(tjenesteomraade.getTjenestevirksomhet()))
                        .collect(Collectors.toList()));
    }

    public static VergeData.VergeNavn personnavnTilVergenavnMapper(PersonNavn navn) {
        return (navn != null)
                ? new VergeData.VergeNavn().setFornavn(navn.getFornavn()).setMellomnavn(navn.getMellomnavn()).setEtternavn(navn.getEtternavn())
                : null;
    }

    public static VergeData.VergeNavn vergeNavnMapper(HentPerson.VergeNavn vergeNavn) {
        return (vergeNavn != null)
                ? new VergeData.VergeNavn().setFornavn(vergeNavn.getFornavn()).setMellomnavn(vergeNavn.getMellomnavn()).setEtternavn(vergeNavn.getEtternavn())
                : null;
    }

    public static VergeData.Folkeregistermetadata folkeregisterMetadataMapper(HentPerson.Folkeregistermetadata folkeregistermetadata) {
        return new VergeData.Folkeregistermetadata()
                .setAjourholdstidspunkt(folkeregistermetadata.getAjourholdstidspunkt())
                .setGyldighetstidspunkt(folkeregistermetadata.getGyldighetstidspunkt())
                .setOpphoerstidspunkt(folkeregistermetadata.getOpphoerstidspunkt());
    }
}
