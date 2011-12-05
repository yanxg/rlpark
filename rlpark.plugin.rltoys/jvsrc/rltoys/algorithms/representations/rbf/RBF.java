package rltoys.algorithms.representations.rbf;

import rltoys.algorithms.representations.LocalFeature;


public class RBF implements LocalFeature {
  private final int[] patternIndexes;
  private final double[] patternValues;
  private final double variance;

  public RBF(int[] patternIndexes, double[] patternValues, double stddev) {
    this.patternIndexes = patternIndexes;
    this.patternValues = patternValues;
    this.variance = stddev * stddev;
  }

  @Override
  public double value(double[] input) {
    double distance = 0.0;
    for (int i = 0; i < patternIndexes.length; i++) {
      double diff = patternValues[i] - input[patternIndexes[i]];
      distance += diff * diff;
    }
    return Math.exp(-distance / (2 * variance));
  }

  public int[] patternIndexes() {
    return patternIndexes;
  }

  public double[] patternValues() {
    return patternValues;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < patternIndexes.length; i++) {
      builder.append(patternIndexes[i]);
      builder.append(":");
      builder.append(patternValues[i]);
      if (i < patternIndexes.length - 1)
        builder.append(",");
    }
    builder.append("]");
    return builder.toString();
  }
}
