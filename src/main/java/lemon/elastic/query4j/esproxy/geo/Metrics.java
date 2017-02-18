
package lemon.elastic.query4j.esproxy.geo;

/**
 * Commonly used {@link Metric}s.
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @since 1.8
 */
public enum Metrics implements Metric {

    KILOMETERS(6378.137, "km"), MILES(3963.191, "mi"), NEUTRAL(1, "");

    private final double multiplier;
    private final String abbreviation;

    /**
     * Creates a new {@link Metrics} using the given muliplier.
     * 
     * @param multiplier the earth radius at equator.
     */
    private Metrics(double multiplier, String abbreviation) {

        this.multiplier = multiplier;
        this.abbreviation = abbreviation;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public String getAbbreviation() {
        return abbreviation;
    }
}
