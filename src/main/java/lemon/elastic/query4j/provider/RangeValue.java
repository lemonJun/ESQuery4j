package lemon.elastic.query4j.provider;

public class RangeValue {

    private double start;

    private double end;

    public RangeValue() {

    }

    public RangeValue(double start, double end) {
        this.start = start;
        this.end = end;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

}
