package Entities;

import java.util.Objects;

public class CountryInfo {
    private final String currency;
    private final String flagUrl;
    private final String languages;
    private final String region; // New field

    public CountryInfo(String currency, String flagUrl, String languages, String region) {
        this.currency = currency;
        this.flagUrl = flagUrl;
        this.languages = languages;
        this.region = region;
    }

    public String getCurrency() {
        return currency;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public String getLanguages() {
        return languages;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return String.format("CountryInfo{currency='%s', flagUrl='%s', languages='%s', region='%s'}",
                currency, flagUrl, languages, region);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryInfo that = (CountryInfo) o;
        return Objects.equals(currency, that.currency) &&
                Objects.equals(flagUrl, that.flagUrl) &&
                Objects.equals(languages, that.languages) &&
                Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, flagUrl, languages, region);
    }
}