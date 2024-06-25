package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.representasjon.ReprFullmaktData;
import no.nav.veilarbperson.domain.FullmaktData;
import no.nav.veilarbperson.domain.VergeOgFullmaktData;
import no.nav.veilarbperson.service.KodeverkService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VergeOgFullmaktDataMapper {

    public static VergeOgFullmaktData toVergeOgFullmaktData(HentPerson.VergeOgFullmakt vergeOgFullmaktFraPdl) {
        return new VergeOgFullmaktData()
                .setVergemaalEllerFremtidsfullmakt(vergemaalEllerFremtidsfullmaktMapper(vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt()))
                .setFullmakt(fullmaktMapper(vergeOgFullmaktFraPdl.getFullmakt()));
    }

    public static FullmaktData toFullmaktData(List<ReprFullmaktData.Fullmakt> fullmaktFraRepresentasjon, KodeverkService kodeverkService) {
        return new FullmaktData()
                .setFullmakt(representasjonFullmaktMapper(fullmaktFraRepresentasjon, kodeverkService)
        );
    }

    public static List<FullmaktData.Fullmakt> representasjonFullmaktMapper(List<ReprFullmaktData.Fullmakt> fullmaktFraRepresentasjon, KodeverkService kodeverkService) {
        return fullmaktFraRepresentasjon.stream()
            .map(fullmakt ->
                new FullmaktData.Fullmakt()
                        .setFullmaktsgiver(fullmakt.getFullmaktsgiver())
                        .setFullmaktsgiverNavn(fullmakt.getFullmaktsgiverNavn())
                        .setFullmektig(fullmakt.getFullmektig())
                        .setFullmektig(fullmakt.getFullmektig())
                        .setOmraade(omraadeMapper(fullmakt.getOmraade(), kodeverkService))
                        .setStatus(fullmakt.getStatus())
                        .setGyldigFraOgMed(fullmakt.getGyldigFraOgMed())
                        .setGyldigTilOgMed(fullmakt.getGyldigTilOgMed()))
            .collect(Collectors.toList());
    }

    public static List<FullmaktData.OmraadeMedHandling> omraadeMapper(List<ReprFullmaktData.OmraadeMedHandling> omraade, KodeverkService kodeverkService) {
        List<FullmaktData.OmraadeMedHandling> omraadeMedHandlinger = omraade.stream().map(omraadeMedHandling -> {
            String temaBeskrivelse = kodeverkService.getBeskrivelseForTema(omraadeMedHandling.getTema());
            List<ReprFullmaktData.OmraadeHandlingType> reprHandlingTyper = omraadeMedHandling.getHandling();
            List<FullmaktData.OmraadeHandlingType> fullmaktHandlingTyper =
                    reprHandlingTyper.stream().map(
                        omraadeHandlingType -> FullmaktData.OmraadeHandlingType.valueOf(omraadeHandlingType.name()))
                    .toList();
            FullmaktData.OmraadeMedHandling flettetOmraadeMedHandling = new FullmaktData.OmraadeMedHandling();
            flettetOmraadeMedHandling.setTema(temaBeskrivelse).setHandling(fullmaktHandlingTyper);
             return flettetOmraadeMedHandling;
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
