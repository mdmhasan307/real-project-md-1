package mil.stratis.model.view.pick.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class PickingByPriorityQryVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"IssuePriorityGroup_LABEL", "Priority"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
