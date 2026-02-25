package Utils;

public class Country {

    private final String name;
    private final String isoCode;
    private final String dialCode;
    private final String flagUrl;

    public Country(String name, String isoCode, String dialCode, String flagUrl) {
        this.name = name;
        this.isoCode = isoCode;
        this.dialCode = dialCode;
        this.flagUrl = flagUrl;
    }

    public String getName() { return name; }
    public String getIsoCode() { return isoCode; }
    public String getDialCode() { return dialCode; }
    public String getFlagUrl() { return flagUrl; }

    @Override
    public String toString() {
        return name + " (" + dialCode + ")";
    }
}