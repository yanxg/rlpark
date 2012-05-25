package rlpark.plugin.rltoys.algorithms.predictions.td;

import rlpark.plugin.rltoys.algorithms.traces.ATraces;
import rlpark.plugin.rltoys.algorithms.traces.EligibilityTraceAlgorithm;
import rlpark.plugin.rltoys.algorithms.traces.Traces;
import rlpark.plugin.rltoys.math.vector.MutableVector;
import rlpark.plugin.rltoys.math.vector.RealVector;
import rlpark.plugin.rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

@Monitor
public class GTDLambda implements OnPolicyTD, GVF, EligibilityTraceAlgorithm {
  private static final long serialVersionUID = 8687476023177671278L;
  protected double gamma;
  final public double alpha_v;
  public final double alpha_w;
  protected double lambda;
  private double gamma_t;
  @Monitor(level = 4)
  final public PVector v;
  @Monitor(level = 4)
  protected final PVector w;
  private final Traces e;
  protected double v_t;
  protected double delta_t;
  private final MutableVector e_delta01;
  private final MutableVector e_delta02;
  private final MutableVector correction;

  public GTDLambda(double lambda, double gamma, double alpha_v, double alpha_w, int nbFeatures) {
    this(lambda, gamma, alpha_v, alpha_w, nbFeatures, new ATraces());
  }

  public GTDLambda(double lambda, double gamma, double alpha_v, double alpha_w, int nbFeatures, Traces prototype) {
    this.alpha_v = alpha_v;
    this.gamma = gamma;
    this.lambda = lambda;
    this.alpha_w = alpha_w;
    v = new PVector(nbFeatures);
    w = new PVector(nbFeatures);
    e = prototype.newTraces(nbFeatures);
    e_delta01 = e.vect().newInstance(nbFeatures);
    e_delta02 = e.vect().newInstance(nbFeatures);
    correction = e.vect().newInstance(nbFeatures);
  }

  @Override
  public double update(double rho_t, RealVector x_t, RealVector x_tp1, double r_tp1, double gamma_tp1, double z_tp1) {
    if (x_t == null) {
      return initEpisode(gamma_tp1);
    }
    v_t = v.dotProduct(x_t);
    delta_t = r_tp1 + (1 - gamma_tp1) * z_tp1 + gamma_tp1 * v.dotProduct(x_tp1) - v_t;
    // Update traces
    e.update(gamma_t * lambda, x_t);
    e.vect().mapMultiplyToSelf(rho_t);
    // Prepare buffers
    e_delta01.set(e.vect());
    e_delta01.mapMultiplyToSelf(delta_t);
    e_delta02.set(e_delta01);
    // Compute correction
    correction.clear();
    if (x_tp1 != null)
      correction.addToSelf(e.vect().dotProduct(w) * gamma_tp1 * (1 - lambda), x_tp1);
    // Update parameters
    v.addToSelf(alpha_v, e_delta01.subtractToSelf(correction));
    w.addToSelf(alpha_w, e_delta02.addToSelf(-w.dotProduct(x_t), x_t));
    gamma_t = gamma_tp1;
    return delta_t;
  }

  protected double initEpisode(double gamma_tp1) {
    gamma_t = gamma_tp1;
    e.clear();
    v_t = 0;
    return 0;
  }

  @Override
  public void resetWeight(int index) {
    v.data[index] = 0;
    e.vect().setEntry(index, 0);
  }

  @Override
  public double update(RealVector x_t, RealVector x_tp1, double r_tp1) {
    return update(1.0, x_t, x_tp1, r_tp1, gamma, 0);
  }

  @Override
  public double update(double rho_t, RealVector x_t, RealVector x_tp1, double r_tp1) {
    return update(rho_t, x_t, x_tp1, r_tp1, gamma, 0);
  }

  public double update(double rho_t, RealVector x_t, RealVector x_tp1, double r_tp1, double gamma_tp1) {
    return update(rho_t, x_t, x_tp1, r_tp1, gamma_tp1, 0);
  }

  @Override
  public double predict(RealVector phi) {
    return v.dotProduct(phi);
  }

  public double gamma() {
    return gamma;
  }

  @Override
  public PVector weights() {
    return v;
  }

  @Override
  public PVector secondaryWeights() {
    return w;
  }

  @Override
  public Traces trace() {
    return e;
  }

  @Override
  public double error() {
    return delta_t;
  }

  @Override
  public double prediction() {
    return v_t;
  }
}