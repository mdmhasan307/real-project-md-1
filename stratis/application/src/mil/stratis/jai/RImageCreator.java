//
// created from java4less rbarcode library
//

package mil.stratis.jai;

import lombok.NoArgsConstructor;
import lombok.val;

import java.awt.*;
import java.awt.image.BufferedImage;

@NoArgsConstructor
public class RImageCreator {

  private Graphics g;

  public Image getImage(int w, int h) {
    int s = w;
    if (h > w) {
      s = h;
    }

    val im = new BufferedImage(s, s, 13);
    this.g = im.createGraphics();
    return im;
  }

  public Graphics getGraphics() {
    return this.g;
  }
}
