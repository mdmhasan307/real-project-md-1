package mil.stratis.model.view.inv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class ShelfLifeVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"ExpirationDate_LABEL", "Expiration Date"}, {"WacNumber_LABEL", "Wac Num"},
          {"Building_LABEL", "Building"}, {"Niin_LABEL", "NIIN"},
          {"Fsc_LABEL", "FSC"}, {"Cc_LABEL", "CC"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
