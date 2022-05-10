package mil.stratis.model.view.walkThru.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class WalkThruQueueVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"WalkThru_LABEL", "Walk Thru"}};
    
  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
