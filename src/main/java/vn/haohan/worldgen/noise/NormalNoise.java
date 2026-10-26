package vn.haohan.worldgen.noise;

import org.bukkit.util.noise.PerlinNoiseGenerator;

/**
 * Deterministic octave-based Perlin noise generator replicating Minecraft's NormalNoise.
 * Computes multi-octave noise samples with specified firstOctave and amplitude weights.
 */
public final class NormalNoise {

    private final PerlinNoiseGenerator[] octaves;
    private final double[] frequencies;
    private final double[] amplitudes;
    private final double totalWeight;

    public NormalNoise(long seed, String noiseKey, int firstOctave, double... amplitudes) {
        this.amplitudes = amplitudes;
        this.octaves = new PerlinNoiseGenerator[amplitudes.length];
        this.frequencies = new double[amplitudes.length];

        long keyHash = noiseKey.hashCode();
        double weightSum = 0.0;

        for (int i = 0; i < amplitudes.length; i++) {
            long octaveSeed = seed ^ (keyHash * 31L + (long) i * 0x5deece66dL);
            this.octaves[i] = new PerlinNoiseGenerator(octaveSeed);
            this.frequencies[i] = Math.scalb(1.0, firstOctave + i);
            weightSum += Math.abs(amplitudes[i]);
        }
        this.totalWeight = weightSum > 0.0 ? weightSum : 1.0;
    }

    public double sample(double x, double z) {
        return sample(x, z, 1.0);
    }

    public double sample3D(double x, double y, double z) {
        double sum = 0.0;
        for (int i = 0; i < octaves.length; i++) {
            double freq = frequencies[i];
            sum += amplitudes[i] * octaves[i].noise(x * freq, y * freq, z * freq);
        }
        return (sum * 1.6) / totalWeight;
    }

    public double sample(double x, double z, double xzScale) {
        double sum = 0.0;
        for (int i = 0; i < octaves.length; i++) {
            double freq = frequencies[i] * xzScale;
            sum += amplitudes[i] * octaves[i].noise(x * freq, z * freq);
        }
        // Minecraft's NormalNoise uses 3D permutation gradients where raw 2D Perlin values
        // have peak ~0.6-0.7. Scaling by ~1.6 maps the effective dynamic range to [-1.0, 1.0].
        return (sum * 1.6) / totalWeight;
    }
}
