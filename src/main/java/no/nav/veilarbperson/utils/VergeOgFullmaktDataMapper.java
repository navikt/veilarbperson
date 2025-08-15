package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.representasjon.ReprFullmaktData;
import no.nav.veilarbperson.domain.FullmaktDTO;
import no.nav.veilarbperson.domain.PersonNavnV2;
import no.nav.veilarbperson.domain.VergeData;

import java.util.List;
import java.util.stream.Collectors;

public class VergeOgFullmaktDataMapper {
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

    public static VergeData.VergemaalEllerFremtidsfullmakt toVergemaalEllerFremtidsfullmakt(HentPerson.VergemaalEllerFremtidsfullmakt vergemaalEllerFremtidsfullmakt, PersonNavnV2 navn) {
        return new VergeData.VergemaalEllerFremtidsfullmakt()
                .setType(vergemaalEllerFremtidsfullmakt.getType())
                .setEmbete(vergemaalEllerFremtidsfullmakt.getEmbete())
                .setVergeEllerFullmektig(vergeEllerFullmektigMapper(vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig(), navn))
                .setFolkeregistermetadata(folkeregisterMetadataMapper(vergemaalEllerFremtidsfullmakt.getFolkeregistermetadata()));
    }

    public static VergeData.VergeEllerFullmektig vergeEllerFullmektigMapper(HentPerson.VergeEllerFullmektig vergeEllerFullmektig, PersonNavnV2 navn) {
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

    public static VergeData.VergeNavn personnavnTilVergenavnMapper(PersonNavnV2 navn) {
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
