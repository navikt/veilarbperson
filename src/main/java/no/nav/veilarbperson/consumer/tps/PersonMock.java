package no.nav.veilarbperson.consumer.tps;


import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;

class PersonMock implements PersonV3 {

    @Override
    public HentPersonResponse hentPerson(HentPersonRequest wsHentPersonRequest) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        return null;
    }

    @Override
    public HentGeografiskTilknytningResponse hentGeografiskTilknytning(HentGeografiskTilknytningRequest wsHentGeografiskTilknytningRequest) throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {
        return null;
    }

    @Override
    public HentVergeResponse hentVerge(HentVergeRequest hentVergeRequest) throws HentVergePersonIkkeFunnet, HentVergeSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentEkteskapshistorikkResponse hentEkteskapshistorikk(HentEkteskapshistorikkRequest hentEkteskapshistorikkRequest) throws HentEkteskapshistorikkPersonIkkeFunnet, HentEkteskapshistorikkSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentPersonerMedSammeAdresseResponse hentPersonerMedSammeAdresse(HentPersonerMedSammeAdresseRequest hentPersonerMedSammeAdresseRequest) throws HentPersonerMedSammeAdresseIkkeFunnet, HentPersonerMedSammeAdresseSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentPersonhistorikkResponse hentPersonhistorikk(HentPersonhistorikkRequest hentPersonhistorikkRequest) throws HentPersonhistorikkPersonIkkeFunnet, HentPersonhistorikkSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentSikkerhetstiltakResponse hentSikkerhetstiltak(HentSikkerhetstiltakRequest wsHentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public HentPersonnavnBolkResponse hentPersonnavnBolk(HentPersonnavnBolkRequest wsHentPersonnavnBolkRequest) {
        return null;
    }
}
