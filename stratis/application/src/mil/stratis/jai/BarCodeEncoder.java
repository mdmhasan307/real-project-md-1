//
// created from java4less rbarcode library
//

package mil.stratis.jai;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

@Slf4j
public class BarCodeEncoder {

  String sFile;
  BarCode bc;

  public BarCodeEncoder(BarCode c, String psFile) {
    this.sFile = psFile;
    this.bc = c;
    this.encode();
  }

  private void encode() {
    try {
      BufferedImage image = new BufferedImage(this.bc.getSize().width, this.bc.getSize().height, 1);
      Graphics imgGraphics = image.createGraphics();
      this.bc.paint(imgGraphics);
      File f = new File(this.sFile);
      f.delete();
      FileOutputStream of = new FileOutputStream(f);
      ImageIO.write(image, "JPEG", of);
      of.close();
    }
    catch (Exception var6) {
      log.error("Error occurred encoding image", var6);
    }
  }
}
