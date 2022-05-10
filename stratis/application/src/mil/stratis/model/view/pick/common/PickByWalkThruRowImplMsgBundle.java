package mil.stratis.model.view.pick.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class PickByWalkThruRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"WalkThru_LABEL", "Document Number"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
