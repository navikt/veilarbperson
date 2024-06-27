package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.representasjon.ReprFullmaktData;
import no.nav.veilarbperson.domain.FullmaktDTO;
import no.nav.veilarbperson.domain.VergeOgFullmaktData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VergeOgFullmaktDataMapper {

    public static VergeOgFullmaktData toVergeOgFullmaktData(HentPerson.VergeOgFullmakt vergeOgFullmaktFraPdl) {
        return new VergeOgFullmaktData()
                .setVergemaalEllerFremtidsfullmakt(vergemaalEllerFremtidsfullmaktMapper(vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt()))
                .setFullmakt(fullmaktMapper(vergeOgFullmaktFraPdl.getFullmakt()));
    }

    public static FullmaktDTO toFullmaktDTO(List<ReprFullmaktData.Fullmakt> fullmaktListe) {
        return new FullmaktDTO()
                .setFullmakt(representasjonFullmaktMapper(fullmaktListe)
        );
    }

    public static List<FullmaktDTO.Fullmakt> representasjonFullmaktMapper(List<ReprFullmaktData.Fullmakt> fullmaktListe) {
        return fullmaktListe.stream()
            .map(fullmakt ->
                new FullmaktDTO.Fullmakt()
                        .setFullmaktsgiver(fullmakt.getFullmaktsgiver())
                        .setFullmaktsgiverNavn(fullmakt.getFullmaktsgiverNavn())
                        .setFullmektig(fullmakt.getFullmektig())
                        .setFullmektigsNavn(fullmakt.getFullmektigsNavn())
                        .setOmraade(omraadeMapper(fullmakt.getOmraade()))
                        .setGyldigFraOgMed(fullmakt.getGyldigFraOgMed())
                        .setGyldigTilOgMed(fullmakt.getGyldigTilOgMed()))
            .collect(Collectors.toList());
    }

    public static List<FullmaktDTO.OmraadeMedHandling> omraadeMapper(List<ReprFullmaktData.OmraadeMedHandling> omraade) {
        List<FullmaktDTO.OmraadeMedHandling> omraadeMedHandlinger = omraade.stream().map(omraadeMedHandling -> {
            List<ReprFullmaktData.OmraadeHandlingType> reprHandlingTyper = omraadeMedHandling.getHandling();
            List<FullmaktDTO.OmraadeHandlingType> fullmaktHandlingTyper =
                    reprHandlingTyper.stream().map(
                        omraadeHandlingType -> FullmaktDTO.OmraadeHandlingType.valueOf(omraadeHandlingType.name()))
                    .toList();
            FullmaktDTO.OmraadeMedHandling omraadeMedHandlingMapper = new FullmaktDTO.OmraadeMedHandling();
            omraadeMedHandlingMapper
                    .setTema(omraadeMedHandling.getTema())
                    .setHandling(fullmaktHandlingTyper);
            return omraadeMedHandlingMapper;
        }).toList();
        return omraadeMedHandlinger;
    }

    public static List<VergeOgFullmaktData.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktMapper(List<HentPerson.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktListe) {
            return vergemaalEllerFremtidsfullmaktListe.stream()
                  .map(vergemaalEllerFremtidsfullmakt ->
                       new VergeOgFullmaktData.VergemaalEllerFremtidsfullmakt()
                             .setType(vergemaalEllerFremtidsfullmakt.getType())
                             .setEmbete(vergemaalEllerFremtidsfullmakt.getEmbete())
                             .setVergeEllerFullmektig(vergeEllerFullmektigMapper(vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig()))
                             .setFolkeregistermetadata(folkeregisterMetadataMapper(vergemaalEllerFremtidsfullmakt.getFolkeregistermetadata())))
                  .collect(Collectors.toList());
    }

    public static VergeOgFullmaktData.VergeEllerFullmektig vergeEllerFullmektigMapper(HentPerson.VergeEllerFullmektig vergeEllerFullmektig) {
        return new VergeOgFullmaktData.VergeEllerFullmektig()
                .setNavn(vergeNavnMapper(vergeEllerFullmektig.getNavn()))
                .setMotpartsPersonident(vergeEllerFullmektig.getMotpartsPersonident())
                .setOmfang(vergeEllerFullmektig.getOmfang());
    }

    public static VergeOgFullmaktData.VergeNavn vergeNavnMapper(HentPerson.VergeNavn vergeNavn) {
        return (vergeNavn!=null)
                ? new VergeOgFullmaktData.VergeNavn().setFornavn(vergeNavn.getFornavn()).setMellomnavn(vergeNavn.getMellomnavn()).setEtternavn(vergeNavn.getEtternavn())
                : null;
    }

    public static VergeOgFullmaktData.Navn personNavnMapper(List<HentPerson.Navn> motpartspersonnavn) {
        HentPerson.Navn navn = PersonV2DataMapper.getFirstElement(motpartspersonnavn);
        return new VergeOgFullmaktData.Navn().setFornavn(navn.getFornavn()).setMellomnavn(navn.getMellomnavn()).setEtternavn(navn.getEtternavn()).setForkortetNavn(navn.getForkortetNavn());
    }

    public static VergeOgFullmaktData.Folkeregistermetadata folkeregisterMetadataMapper(HentPerson.Folkeregistermetadata folkeregistermetadata) {
        return new VergeOgFullmaktData.Folkeregistermetadata()
                .setAjourholdstidspunkt(folkeregistermetadata.getAjourholdstidspunkt())
                .setGyldighetstidspunkt(folkeregistermetadata.getGyldighetstidspunkt());
    }

    public static List<VergeOgFullmaktData.Fullmakt> fullmaktMapper(List<HentPerson.Fullmakt> fullmaktListe) {
        List<VergeOgFullmaktData.Fullmakt> fullmakter = new ArrayList<>();
        fullmaktListe.forEach(fullmakt -> {
                    ArrayList<VergeOgFullmaktData.Omraade> omraadeKodeListe = new ArrayList<>();
                    fullmakt.getOmraader().forEach(omraade -> omraadeKodeListe.add(new VergeOgFullmaktData.Omraade().setKode(omraade)));

                    fullmakter.add(new VergeOgFullmaktData.Fullmakt()
                            .setMotpartsPersonident(fullmakt.getMotpartsPersonident())
                            .setMotpartsRolle(fullmakt.getMotpartsRolle())
                            .setOmraader(omraadeKodeListe)
                            .setGyldigFraOgMed(fullmakt.getGyldigFraOgMed())
                            .setGyldigTilOgMed(fullmakt.getGyldigTilOgMed()));
                }
        );
        return fullmakter;
    }
}
