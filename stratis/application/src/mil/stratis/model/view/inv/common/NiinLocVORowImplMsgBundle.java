package mil.stratis.model.view.inv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class NiinLocVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"ExpirationDate_LABEL", "Exp. Date ( MMYYYY format)"},
          {"NewRow_LABEL", "NewRow"},
          {"Comments_LABEL", "ACTION"},
          {"Niin_LABEL", "NIIN"}, {"Cc_LABEL", "CC ( A or F )"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
