package no.nav.fo.veilarbperson.domain;

public class Familiemedlem extends Person{


    private Boolean harSammeBosted;

    public Boolean getHarSammeBosted() {
        return harSammeBosted;
    }


    public void setHarSammeBosted(Boolean borHjemme){
        this.harSammeBosted = borHjemme;
    }

}