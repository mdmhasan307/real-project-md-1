package mil.stratis.jai;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import java.awt.*;
import java.awt.image.*;
import java.io.OutputStream;
import java.util.List;

@Slf4j
public class ImageUtils {

  public static final String FILE_LOAD = "fileload";
  public static final String STREAM = "stream";
  public static final String URL = "URL";
  public static final String ENCODE = "encode";
  public static final String SCALE = "scale";
  public static final String TIFF = "TIFF";

  // function to create a selection slot image

  public void createSlotImage(List<String> xList, List<String> yList, List<String> xLength, List<String> yLength, List<String> index,
                              final OutputStream os, int selectedIndex, List<String> displays) {
    int baseWidth = 300;
    int baseHeight = 300; // Dimensions of the image
    byte[] data = new byte[baseWidth * baseHeight * 3]; // Image data array.
    int count = 0; // Temporary counter.
    for (int w = 0; w < baseWidth; w++) // Fill the array with a pattern.
      for (int h = 0; h < baseHeight; h++) {
        data[count] = (byte) 255;
        data[count + 1] = (byte) 255;
        data[count + 2] = (byte) 255;
        count += 3;
      }

    // Create a Data Buffer from the values on the single image array.
    DataBufferByte dbuffer = new DataBufferByte(data, baseWidth * baseHeight * 3);
    // Create an pixel interleaved data sample model.
    SampleModel sampleModel = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, baseWidth, baseHeight, 3);
    // Create a compatible ColorModel.
    ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
    // Create a WritableRaster.
    Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point(0, 0));
    // Create a TiledImage using the SampleModel.
    TiledImage tiledImage = new TiledImage(0, 0, baseWidth, baseHeight, 0, 0, sampleModel, colorModel);
    // Set the data of the tiled image to be the raster.
    tiledImage.setData(raster);

    // Define a color for the annotation.
    final Color textColor = new Color(Integer.parseInt("000000", 16));
    final Color fillColor = new Color(Integer.parseInt("CCCCCC", 16));

    BufferedImage bufImg = tiledImage.getAsBufferedImage();
    Graphics2D g2d = bufImg.createGraphics();
    g2d.setColor(textColor);
    // draw the boxes & text

    int totalBoxes = xList.size();

    for (int i = 0; i < totalBoxes; i++) {
      // draw the box
      g2d.drawRect(Integer.parseInt(xList.get(i)), Integer.parseInt(yList.get(i)), Integer.parseInt(xLength.get(i)), Integer.parseInt(yLength.get(i)));

      // check for matching index
      if (Integer.parseInt(index.get(i)) == selectedIndex) {
        // set the fill color
        g2d.setColor(fillColor);

        // fill the box
        g2d.fillRect(Integer.parseInt(xList.get(i)) + 1, Integer.parseInt(yList.get(i)) + 1, Integer.parseInt(xLength.get(i)) - 1, Integer.parseInt(yLength.get(i)) - 1);

        // put the color back
        g2d.setColor(textColor);
      }

      // string to draw
      String output;

      output = displays.get(i);
      int x = Integer.parseInt(xList.get(i)) + 3;
      int y = (Integer.parseInt(yList.get(i)) - 1) + Integer.parseInt(yLength.get(i));

      g2d.drawString(output, x, y);
    }

    try {
      ImageIO.write(bufImg, "JPEG", os);
    }
    catch (Exception e) {
      log.error("Error occurred writing image", e);
    }
  }
}
