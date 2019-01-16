package pgc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Thomas Povinelli
 * Created 2019-Jan-16
 * In Pressure-Gradient-Calculator-for-MS-Stars
 */
public class InputData {
  static class Parameters {
    private int limit;
    private long radius, steps;
    private double density;

    public Parameters(final int limit, final long radius, final long steps, final double density) {
      this.limit = limit;
      this.radius = radius;
      this.steps = steps;
      this.density = density;
    }

    public int getLimit() {
      return limit;
    }

    public long getRadius() {
      return radius;
    }

    public long getSteps() {
      return steps;
    }

    public double getDensity() {
      return density;
    }
  }

  private String limitText;
  private String radiusText;
  private String densityText;
  private String stepText;

  public InputData(
      @NotNull final String limitText,
      @NotNull final String radiusText,
      @NotNull final String densityText,
      @NotNull final String stepText
  ) {
    this.limitText = limitText.trim();
    this.radiusText = radiusText.trim();
    this.densityText = densityText.trim();
    this.stepText = stepText.trim();
  }

  @Nullable
  public Parameters parse() {
    final int limit;
    if (limitText.equals("")) {
      limit = 45;
    } else {
      try {
        limit = Integer.parseInt(limitText);
      } catch (NumberFormatException e1) {
        Controller.displayAlert("Please enter a valid integer for limit");
        return null;
      }
    }
    final long radius;
    if (radiusText.equals("")) {
      radius = 23193333L;
    } else {
      try {
        radius = Long.parseLong(radiusText);
      } catch (NumberFormatException e2) {
        Controller.displayAlert("Please enter a valid long integer for radius");
        return null;
      }
    }
    final long steps;
    if (stepText.equals("")) {
      steps = 45000000L;
    } else {
      try {
        steps = Long.parseLong(stepText);
      } catch (NumberFormatException e1) {
        Controller.displayAlert("Please enter a valid long integer for steps");
        return null;
      }
    }
    final double density;
    if (densityText.equals("")) {
      density = 4.5;
    } else {
      try {
        density = Double.parseDouble(densityText);
      } catch (NumberFormatException e1) {
        Controller.displayAlert("Please enter a valid double precision floating point number for density");
        return null;
      }
    }
    return new Parameters(limit, radius, steps, density);
  }

}
