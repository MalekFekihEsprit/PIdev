package Entities;

import java.util.Objects;

public class CountryInfo {
    private final String currency;
    private final String flagUrl;
    private final String languages;

    public CountryInfo(String currency, String flagUrl, String languages) {
        this.currency = currency;
        this.flagUrl = flagUrl;
        this.languages = languages;
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

    @Override
    public String toString() {
        return String.format("CountryInfo{currency='%s', flagUrl='%s', languages='%s'}",
                currency, flagUrl, languages);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryInfo that = (CountryInfo) o;
        return Objects.equals(currency, that.currency) &&
                Objects.equals(flagUrl, that.flagUrl) &&
                Objects.equals(languages, that.languages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, flagUrl, languages);
    }
}