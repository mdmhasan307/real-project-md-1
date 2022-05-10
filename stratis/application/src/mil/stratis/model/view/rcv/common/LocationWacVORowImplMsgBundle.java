package mil.stratis.model.view.rcv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class LocationWacVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"wacIdStr_LABEL", "Select WAC"},
          {"wacIdStr_TOOLTIP", "Needed for when wanting to list new locations."}};
  
  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
