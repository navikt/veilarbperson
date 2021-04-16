package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.domain.VergemaalEllerFullmaktOmfangType;
import no.nav.veilarbperson.client.pdl.domain.Vergetype;
import no.nav.veilarbperson.domain.VergeOgFullmaktData;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

public class VergeOgFullmaktDataMapper {

    public static VergeOgFullmaktData toVergeOgFullmaktData(HentPerson.VergeOgFullmakt vergeOgFullmaktFraPdl) {
        return new VergeOgFullmaktData()
                .setVergemaalEllerFremtidsfullmakt(vergemaalEllerFremtidsfullmaktMapper(vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt()))
                .setFullmakt(fullmaktMapper(vergeOgFullmaktFraPdl.getFullmakt()));
    }

    public static List<VergeOgFullmaktData.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktMapper(List<HentPerson.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktListe) {
        if(vergemaalEllerFremtidsfullmaktListe.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person har ikke vergemål eller fremtidsfullmakt i PDL");
        }

        List<VergeOgFullmaktData.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmakter = new ArrayList<>();
        vergemaalEllerFremtidsfullmaktListe.forEach(vergemaalEllerFremtidsfullmakt -> {
                    vergemaalEllerFremtidsfullmakter.add(
                            new VergeOgFullmaktData.VergemaalEllerFremtidsfullmakt()
                                    .setType(Vergetype.getVergetype(vergemaalEllerFremtidsfullmakt.getType()))
                                    .setEmbete(vergemaalEllerFremtidsfullmakt.getEmbete())
                                    .setVergeEllerFullmektig(vergeEllerFullmektigMapper(vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig()))
                                    .setFolkeregistermetadata(folkeregisterMetadataMapper(vergemaalEllerFremtidsfullmakt.getFolkeregistermetadata()))
                    );
                }
        );
        return vergemaalEllerFremtidsfullmakter;
    }

    public static VergeOgFullmaktData.VergeEllerFullmektig vergeEllerFullmektigMapper(HentPerson.VergeEllerFullmektig vergeEllerFullmektig) {
        return new VergeOgFullmaktData.VergeEllerFullmektig()
                .setNavn(vergeNavnMapper(vergeEllerFullmektig.getNavn()))
                .setMotpartsPersonident(vergeEllerFullmektig.getMotpartsPersonident())
                .setOmfang(VergemaalEllerFullmaktOmfangType.getOmfang(vergeEllerFullmektig.getOmfang()));
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
        if(fullmaktListe.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person har ikke fullmakt i PDL");
        }

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
