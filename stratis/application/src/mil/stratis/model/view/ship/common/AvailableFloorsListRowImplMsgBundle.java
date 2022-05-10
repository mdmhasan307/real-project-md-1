package mil.stratis.model.view.ship.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class AvailableFloorsListRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"FloorLocation_LABEL", "Floor Location"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
