package mil.stratis.jai;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.BarcodeUtils;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.OutputStream;

@Slf4j
@NoArgsConstructor
public class BarcodeCreator {

  public static final String FILE_LOAD = "fileload";
  public static final String STREAM = "stream";
  public static final String URL = "URL";
  public static final String ENCODE = "encode";
  public static final String SCALE = "scale";
  public static final String TIFF = "TIFF";
  private int widthAdj = 1000;
  private int heightAdj = 500;

  // function to create the barcode needed

  public void createBarcodeImage(String barcodetext, String barcodetype,
                                 OutputStream os, int random) {
    try {
      val barcodeUtils = ContextUtils.getBean(BarcodeUtils.class);
      String barcodePath = barcodeUtils.getBarcodeImagePath();
      if (barcodetype == null || barcodetype.equals("")) {
        barcodetype = "BAR39";
      }

      if (barcodetype.equals("BAR39")) {
        BarCode bc = new BarCode();
        bc.setSize(widthAdj, heightAdj);
        bc.code = barcodetext;
        bc.barColor = Color.BLACK;
        bc.backColor = Color.WHITE;
        bc.doubleI = 1;
        bc.resolution = 1;
        bc.doubleX = 1;
        bc.doubleN = 3;
        bc.barType = BarCode.BAR39;
        bc.doubleH = 0.2;
        bc.checkCharacter = false;

        bc.textFont = new java.awt.Font("Lucida Sans", Font.BOLD, 16);

        // create image for first painting
        java.awt.image.BufferedImage imageBC = new java.awt.image.BufferedImage(bc.getSize().width, bc.getSize().height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics imgGraphics = imageBC.createGraphics();
        bc.paint(imgGraphics);

        // set size.  Added 5 pixels to the height so that full Lucida Sans font will show up.
        bc.setSize(bc.getPaintedArea().width + 5, bc.getPaintedArea().height + 5);

        new BarCodeEncoder(bc, barcodePath + "op_" + random + ".jpg");
      }
      else if (barcodetype.equals("LABEL")) {
        BarCode bc = new BarCode();
        bc.setSize(widthAdj, heightAdj);
        bc.barHeightCM = 80;

        bc.code = barcodetext;
        bc.barColor = Color.BLACK;
        bc.backColor = Color.WHITE;
        bc.doubleI = 1;
        bc.resolution = 1;
        bc.doubleX = 1;
        bc.doubleN = 3;
        bc.barType = BarCode.BAR39;
        bc.checkCharacter = false;

        bc.textFont = new java.awt.Font("Lucida Sans", Font.PLAIN, 0);

        // create image for first painting
        java.awt.image.BufferedImage imageBC = new java.awt.image.BufferedImage(bc.getSize().width, bc.getSize().height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics imgGraphics = imageBC.createGraphics();
        bc.paint(imgGraphics);
        bc.setSize(bc.getPaintedArea().width + 5, bc.getPaintedArea().height);

        new BarCodeEncoder(bc, barcodePath + "op_" + random + ".jpg");
      }
      else if (barcodetype.equals("PDF417")) {
        BarCode2D bc2d = new BarCode2D();
        bc2d.setSize(1000, 300);
        bc2d.resolution = 1;
        bc2d.PDFECLevel = 2;
        bc2d.PDFMode = BarCode2D.PDF_BINARY;
        bc2d.barType = BarCode.PDF417;
        bc2d.code = barcodetext;
        bc2d.PDFMaxRows = 10;  // reduce from 30 to squeeze onto 1 page
        bc2d.PDFColumns = 10;
        bc2d.PDFRows = 15;
        bc2d.doubleH = 0.3;
        bc2d.checkCharacter = true;

        // create image for first painting
        java.awt.image.BufferedImage imageBC = new java.awt.image.BufferedImage(bc2d.getSize().width, bc2d.getSize().height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics imgGraphics = imageBC.createGraphics();
        bc2d.paint(imgGraphics);

        // set size
        int nwidth = bc2d.getPaintedArea().width + 20;
        int nheight = bc2d.getPaintedArea().height;
        bc2d.setSize(nwidth, nheight);

        new BarCodeEncoder(bc2d, barcodePath + "op_" + random + ".jpg");
      }

      javaIoStreamImage(barcodePath + "op_" + random + ".jpg", os);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void javaIoStreamImage(String fileToLoad, OutputStream os) {
    try {
      val img = ImageIO.read(new File(fileToLoad));
      ImageIO.write(img, "JPG", os);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void createBarcodeImage(String barcodetext, String barcodetype, OutputStream os, int random,
                                 int width, int height) {

    if (width > 1000)
      this.widthAdj = width;
    if (height > 1000)
      this.heightAdj = height;
    createBarcodeImage(barcodetext, barcodetype, os, random);
  }
}
