package rlpark.alltests.rlparkview.continuousgridworld;

import java.util.Random;

import rltoys.environments.continuousgridworld.ContinuousFunction;
import rltoys.environments.continuousgridworld.ContinuousGridworld;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.environments.envio.observations.TRStep;
import rltoys.math.ranges.Range;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class TestContinuousGridworldRunnable implements Runnable {
  static final Range ObservationRange = new Range(-10, 10);
  private final Clock clock = new Clock("TestContinuousGridworld");
  private final ContinuousGridworld continuousGridworld;
  private final Random random = new Random(0);

  public TestContinuousGridworldRunnable() {
    continuousGridworld = new ContinuousGridworld(random, 2, ObservationRange, new Range(-1, 1), .1);
    continuousGridworld.setRewardFunction(new ContinuousFunction() {
      @Override
      public int nbDimension() {
        return 2;
      }

      @Override
      public double fun(double[] position) {
        return position[0] + position[1];
      }
    });
    Zephyr.advertise(clock, this);
  }

  @Override
  public void run() {
    TRStep step = continuousGridworld.initialize();
    Range[] actionRanges = continuousGridworld.actionRanges();
    while (clock.tick()) {
      if (step.isEpisodeEnding()) {
        step = continuousGridworld.initialize();
        continue;
      }
      step = continuousGridworld.step(new ActionArray(actionRanges[0].choose(random), actionRanges[1].choose(random)));
    }
  }
}
