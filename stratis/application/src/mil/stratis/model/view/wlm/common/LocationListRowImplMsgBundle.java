package mil.stratis.model.view.wlm.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class LocationListRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {{"LocQty_LABEL", "Qty"}, {"LocationLabel_LABEL", "Location Label"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
