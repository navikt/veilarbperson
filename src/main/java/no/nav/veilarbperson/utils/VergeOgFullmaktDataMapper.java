package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.representasjon.ReprFullmaktData;
import no.nav.veilarbperson.domain.FullmaktDTO;
import no.nav.veilarbperson.domain.VergeData;

import java.util.List;
import java.util.stream.Collectors;

public class VergeOgFullmaktDataMapper {

    public static VergeData toVerge(HentPerson.Verge vergeOgFullmaktFraPdl) {
        return new VergeData()
                .setVergemaalEllerFremtidsfullmakt(vergemaalEllerFremtidsfullmaktMapper(vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt()));
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
        return omraade.stream().map(omraadeMedHandling -> {
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
    }

    public static List<VergeData.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktMapper(List<HentPerson.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktListe) {
            return vergemaalEllerFremtidsfullmaktListe.stream()
                  .map(vergemaalEllerFremtidsfullmakt ->
                       new VergeData.VergemaalEllerFremtidsfullmakt()
                             .setType(vergemaalEllerFremtidsfullmakt.getType())
                             .setEmbete(vergemaalEllerFremtidsfullmakt.getEmbete())
                             .setVergeEllerFullmektig(vergeEllerFullmektigMapper(vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig()))
                             .setFolkeregistermetadata(folkeregisterMetadataMapper(vergemaalEllerFremtidsfullmakt.getFolkeregistermetadata())))
                  .collect(Collectors.toList());
    }

    public static VergeData.VergeEllerFullmektig vergeEllerFullmektigMapper(HentPerson.VergeEllerFullmektig vergeEllerFullmektig) {
        return new VergeData.VergeEllerFullmektig()
                .setNavn(vergeNavnMapper(vergeEllerFullmektig.getNavn()))
                .setIdentifiserendeInformasjon(identifiserendeInformasjonMapper(vergeEllerFullmektig.getIdentifiserendeInformasjon()))
                .setMotpartsPersonident(vergeEllerFullmektig.getMotpartsPersonident())
                .setOmfang(vergeEllerFullmektig.getOmfang());
    }

    public static VergeData.IdentifiserendeInformasjon identifiserendeInformasjonMapper(HentPerson.IdentifiserendeInformasjon identifiserendeInformasjon) {
        return new VergeData.IdentifiserendeInformasjon()
                .setNavn(vergeNavnMapper(identifiserendeInformasjon.getNavn()));
    }

    public static VergeData.VergeNavn vergeNavnMapper(HentPerson.VergeNavn vergeNavn) {
        return (vergeNavn!=null)
                ? new VergeData.VergeNavn().setFornavn(vergeNavn.getFornavn()).setMellomnavn(vergeNavn.getMellomnavn()).setEtternavn(vergeNavn.getEtternavn())
                : null;
    }

    public static VergeData.Navn personNavnMapper(List<HentPerson.Navn> motpartspersonnavn) {
        HentPerson.Navn navn = PersonV2DataMapper.getFirstElement(motpartspersonnavn);
        return new VergeData.Navn().setFornavn(navn.getFornavn()).setMellomnavn(navn.getMellomnavn()).setEtternavn(navn.getEtternavn()).setForkortetNavn(navn.getForkortetNavn());
    }

    public static VergeData.Folkeregistermetadata folkeregisterMetadataMapper(HentPerson.Folkeregistermetadata folkeregistermetadata) {
        return new VergeData.Folkeregistermetadata()
                .setAjourholdstidspunkt(folkeregistermetadata.getAjourholdstidspunkt())
                .setGyldighetstidspunkt(folkeregistermetadata.getGyldighetstidspunkt())
                .setOpphoerstidspunkt(folkeregistermetadata.getOpphoerstidspunkt());
    }
}
