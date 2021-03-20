package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.domain.VergeOgFullmaktData;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

public class VergeOgFullmaktDataMapper {

    public static VergeOgFullmaktData toVergeOgFullmaktData(HentPdlPerson.VergeOgFullmakt vergeOgFullmaktFraPdl) {
        return new VergeOgFullmaktData()
                .setVergemaalEllerFremtidsfullmakt(vergemaalEllerFremtidsfullmaktMapper(vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt()))
                .setFullmakt(fullmaktMapper(vergeOgFullmaktFraPdl.getFullmakt()));
    }

    public static List<VergeOgFullmaktData.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktMapper(List<HentPdlPerson.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktListe) {
        List<VergeOgFullmaktData.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmakts = new ArrayList<>();

        vergemaalEllerFremtidsfullmaktListe.forEach(vergemaalEllerFremtidsfullmakt -> {
                    HentPdlPerson.VergeNavn navn = ofNullable(vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig()).map(HentPdlPerson.VergeEllerFullmektig::getNavn).orElse(null);
                    vergemaalEllerFremtidsfullmakts.add(
                            new VergeOgFullmaktData.VergemaalEllerFremtidsfullmakt()
                                    .setType(vergemaalEllerFremtidsfullmakt.getType())
                                    .setEmbete(vergemaalEllerFremtidsfullmakt.getEmbete())
                                    .setVergeEllerFullmektig(vergeEllerFullmektigMapper(vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig()))
                                    .setFolkeregistermetadata(folkeregisterMetadataMapper(vergemaalEllerFremtidsfullmakt.getFolkeregistermetadata()))
                    );
                }
        );
        return vergemaalEllerFremtidsfullmakts;
    }

    public static VergeOgFullmaktData.VergeEllerFullmektig vergeEllerFullmektigMapper(HentPdlPerson.VergeEllerFullmektig vergeEllerFullmektig) {
        return new VergeOgFullmaktData.VergeEllerFullmektig()
                .setNavn(vergeNavnMapper(vergeEllerFullmektig.getNavn()))
                .setMotpartsPersonident(vergeEllerFullmektig.getMotpartsPersonident())
                .setOmfang(vergeEllerFullmektig.getOmfang());
    }

    public static VergeOgFullmaktData.VergeNavn vergeNavnMapper(HentPdlPerson.VergeNavn vergeNavn) {
        return (vergeNavn!=null)
                ? new VergeOgFullmaktData.VergeNavn().setFornavn(vergeNavn.getFornavn()).setMellomnavn(vergeNavn.getMellomnavn()).setEtternavn(vergeNavn.getEtternavn())
                : null;
    }

    public static VergeOgFullmaktData.Navn personNavnMapper(HentPdlPerson.Navn navn) {
        return new VergeOgFullmaktData.Navn().setFornavn(navn.getFornavn()).setMellomnavn(navn.getMellomnavn()).setEtternavn(navn.getEtternavn()).setForkortetNavn(navn.getForkortetNavn());
    }

    public static VergeOgFullmaktData.Folkeregistermetadata folkeregisterMetadataMapper(HentPdlPerson.Folkeregistermetadata folkeregistermetadata) {
        return new VergeOgFullmaktData.Folkeregistermetadata()
                .setAjourholdstidspunkt(folkeregistermetadata.getAjourholdstidspunkt())
                .setGyldighetstidspunkt(folkeregistermetadata.getGyldighetstidspunkt());
    }

    public static List<VergeOgFullmaktData.Fullmakt> fullmaktMapper(List<HentPdlPerson.Fullmakt> fullmaktListe) {
        List<VergeOgFullmaktData.Fullmakt> fullmakter = new ArrayList<>();

        fullmaktListe.forEach(fullmakt -> {
                    fullmakter.add(new VergeOgFullmaktData.Fullmakt()
                            .setMotpartsPersonident(fullmakt.getMotpartsPersonident())
                            .setMotpartsRolle(fullmakt.getMotpartsRolle())
                            .setOmraader(fullmakt.getOmraader())
                            .setGyldigFraOgMed(fullmakt.getGyldigFraOgMed())
                            .setGyldigTilOgMed(fullmakt.getGyldigTilOgMed()));
                }
        );
        return fullmakter;
    }
}
