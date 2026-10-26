package vn.haohan.worldgen.noise;

/**
 * Cubic Hermite spline evaluator replicating Minecraft's minecraft:spline density function.
 */
public final class CubicSpline {

    public record Point(double location, double value, double derivative) {
        public Point(double location, double value) {
            this(location, value, 0.0);
        }
    }

    private final Point[] points;

    public CubicSpline(Point... points) {
        this.points = points;
    }

    public double sample(double x) {
        if (x <= points[0].location()) return points[0].value();
        if (x >= points[points.length - 1].location()) return points[points.length - 1].value();

        int low = 0;
        int high = points.length - 2;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (x < points[mid].location()) {
                high = mid - 1;
            } else if (x > points[mid + 1].location()) {
                low = mid + 1;
            } else {
                Point p0 = points[mid];
                Point p1 = points[mid + 1];
                double dx = p1.location() - p0.location();
                double t = (x - p0.location()) / dx;

                double t2 = t * t;
                double t3 = t2 * t;
                double h00 = 2 * t3 - 3 * t2 + 1;
                double h10 = t3 - 2 * t2 + t;
                double h01 = -2 * t3 + 3 * t2;
                double h11 = t3 - t2;

                return h00 * p0.value() + h10 * dx * p0.derivative()
                        + h01 * p1.value() + h11 * dx * p1.derivative();
            }
        }
        return points[0].value();
    }
}
