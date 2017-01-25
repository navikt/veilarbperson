package no.nav.fo.veilarbperson.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.fo.veilarbperson.domain.Bostedsadresse;
import no.nav.fo.veilarbperson.domain.Sivilstand;

import java.util.List;

public class PersonData{
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String sammensattNavn;
    private String personnummer;
    private String fodselsdato;
    private String dodsdato;

    @JsonProperty("kjonn")
    private String kjoenn;
    private List<Familiemedlem> barn;
    private String diskresjonskode;
    private String kontonummer;
    private String ansvarligEnhetsnummer;
    private Enhet behandlendeEnhet;
    private String telefon;
    private String epost;
    private String statsborgerskap;
    private String sikkerhetstiltak;
    private Sivilstand sivilstand;
    private Familiemedlem partner;
    private Bostedsadresse bostedsadresse;

    private boolean egenAnsatt;

    public String getStatsborgerskap() {
        return statsborgerskap;
    }
    public String getFornavn() {
        return fornavn;
    }

    public String getMellomnavn() {
        return mellomnavn;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public String getSammensattNavn() {
        return sammensattNavn;
    }

    public String getPersonnummer() {
        return personnummer;
    }

    public String getFodselsdato() {
        return fodselsdato;
    }

    public String getDodsdato() {
        return dodsdato;
    }

    public String getKjoenn() {
        return kjoenn;
    }

    public List<Familiemedlem> getBarn() {
        return barn;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    public String getKontonummer() {
        return kontonummer;
    }

    public Enhet getBehandlendeEnhet() {
        return this.behandlendeEnhet;
    }

    public String getAnsvarligEnhetsnummer() {
        return this.ansvarligEnhetsnummer;
    }

    public String getTelefon() {
        return telefon;
    }

    public String getEpost() {
        return epost;
    }

    public String getSikkerhetstiltak() {
        return sikkerhetstiltak;
    }

    public Sivilstand getSivilstand(){
        return sivilstand;
    }
    public Familiemedlem getPartner() {
        return this.partner;
    }

    public Bostedsadresse getBostedsadresse() {
        return bostedsadresse;
    }


    public boolean isEgenAnsatt() {
        return egenAnsatt;
    }

    public PersonData withFornavn(String fornavn){
        this.fornavn = fornavn;
        return this;
    }

    public PersonData withMellomnavn(String mellomnavn){
        this.mellomnavn = mellomnavn;
        return this;
    }

    public PersonData withEtternavn(String etternavn){
        this.etternavn = etternavn;
        return this;
    }

    public PersonData withSammensattNavn(String sammensattNavn) {
        this.sammensattNavn = sammensattNavn;
        return this;
    }

    public PersonData withPersonnummer(String personnummer){
        this.personnummer = personnummer;
        return this;
    }

    public PersonData withFodselsdato(String fodselsdato){
        this.fodselsdato = fodselsdato;
        return this;
    }

    public PersonData withDodsdato(String dodsdato){
        this.dodsdato = dodsdato;
        return this;
    }

    public PersonData withKjoenn(String kjoenn){
        this.kjoenn = kjoenn;
        return this;
    }

    public PersonData withBarn(List<Familiemedlem> barn){
        this.barn = barn;
        return this;
    }

    public PersonData withDiskresjonskode(String diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
        return this;
    }

    public PersonData withKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

    public PersonData withAnsvarligEnhetsnummer(String enhetsnummer) {
        this.ansvarligEnhetsnummer = enhetsnummer;
        return this;
    }

    public PersonData withBehandlendeEnhet(Enhet enhet) {
        this.behandlendeEnhet = enhet;
        return this;
    }

    public PersonData withSikkerhetstiltak(String sikkerhetstiltak) {
        this.sikkerhetstiltak = sikkerhetstiltak;
        return this;
    }

    public PersonData withTelefon(String telefon) {
        this.telefon = telefon;
        return this;
    }

    public PersonData withEpost(String epost) {
        this.epost = epost;
        return this;
    }

    public PersonData withStatsborgerskap(String statsborgerskap) {
        this.statsborgerskap = statsborgerskap;
        return this;
    }

    public PersonData withSivilstand(Sivilstand sivilstand) {
        this.sivilstand = sivilstand;
        return this;
    }

    public PersonData withPartner(Familiemedlem partner) {
        this.partner = partner;
        return this;
    }

    public PersonData withBostedsadresse(Bostedsadresse bostedsadresse) {
        this.bostedsadresse = bostedsadresse;
        return this;
    }

    public PersonData withEgenAnsatt(boolean egenAnsatt) {
        this.egenAnsatt = egenAnsatt;
        return this;
    }
}