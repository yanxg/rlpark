package rlpark.plugin.rltoysview.internal.vectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import rlpark.plugin.rltoysview.internal.ColorScale;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.SparseVector;
import rltoys.math.vector.SparseVector.ElementIterator;
import zephyr.plugin.core.helpers.ClassViewProvider;
import zephyr.plugin.core.utils.Colors;
import zephyr.plugin.core.views.helpers.BackgroundCanvasView;

public class RealVectorView extends BackgroundCanvasView<RealVector> {
  public static class Provider extends ClassViewProvider {
    public Provider() {
      super(RealVector.class);
    }
  }

  private RealVector copy;
  private final Colors colors = new Colors();
  final ColorScale colorScale = new ColorScale(new RGB(255, 255, 255));

  private final ElementIterator normalizerIterator = new ElementIterator() {
    @Override
    public void element(int index, double value) {
      colorScale.update(value);
    }
  };

  @Override
  public boolean synchronize() {
    copy = instance.current().copy();
    return true;
  }

  @Override
  public void paint(PainterMonitor painterListener, Image image, final GC gc) {
    Rectangle clipping = gc.getClipping();
    if (clipping.isEmpty())
      return;
    gc.setBackground(colors.color(gc, Colors.COLOR_BLACK));
    gc.fillRectangle(clipping);
    if (copy == null)
      return;
    updateNormalizer();
    gc.setAntialias(SWT.OFF);
    double rootSize = Math.ceil(Math.sqrt(copy.getDimension()));
    double ratio = (double) clipping.width / (double) clipping.height;
    final double xSize = Math.ceil(rootSize * ratio);
    double ySize = Math.ceil(rootSize / ratio);
    final double xPixelSize = clipping.width / xSize;
    final double yPixelSize = clipping.height / ySize;
    final int xPixelDisplaySize = (int) Math.max(xPixelSize, 1);
    final int yPixelDisplaySize = (int) Math.max(yPixelSize, 1);
    if (copy instanceof SparseVector)
      ((SparseVector) copy).forEach(new ElementIterator() {
        @Override
        public void element(int index, double value) {
          drawWeight(gc, xSize, xPixelSize, yPixelSize, xPixelDisplaySize, yPixelDisplaySize, index, value);
        }
      });
    else
      for (int i = 0; i < copy.getDimension(); i++)
        drawWeight(gc, xSize, xPixelSize, yPixelSize, xPixelDisplaySize, yPixelDisplaySize, i, copy.getEntry(i));
  }

  protected void drawWeight(GC gc, double xSize, double xPixelSize, double yPixelSize, int xPixelDisplaySize,
      int yPixelDisplaySize, int index, double value) {
    int xCoord = (int) (index % xSize * xPixelSize);
    int yCoord = (int) ((int) (index / xSize) * yPixelSize);
    gc.setBackground(colors.color(gc, colorScale.color(value)));
    gc.fillRectangle(xCoord, yCoord, xPixelDisplaySize, yPixelDisplaySize);
  }

  protected void updateNormalizer() {
    colorScale.discount(0.99);
    if (copy instanceof SparseVector) {
      if (((SparseVector) copy).nonZeroElements() < copy.getDimension())
        colorScale.update(0.0);
      ((SparseVector) copy).forEach(normalizerIterator);
      return;
    }
    for (int i = 0; i < copy.getDimension(); i++)
      colorScale.update(copy.getEntry(i));
  }

  @Override
  public void set(RealVector instance) {
    colorScale.init();
    setViewName();
  }

  @Override
  public void unset() {
    copy = null;
  }

  @Override
  public void dispose() {
    super.dispose();
    colors.dispose();
  }

  @Override
  protected Class<?> classSupported() {
    return RealVector.class;
  }
}
