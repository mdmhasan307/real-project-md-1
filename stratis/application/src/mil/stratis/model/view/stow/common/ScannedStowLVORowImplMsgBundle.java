package mil.stratis.model.view.stow.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class ScannedStowLVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"Sid_LABEL", "SID"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
