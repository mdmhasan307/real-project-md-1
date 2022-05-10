package mil.stratis.model.view.ship.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class UndeliveredContainersListRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"FloorLocation_LABEL", "Floor Location"},
          {"Aac_LABEL", "AAC"},
          {"Manifest_TOOLTIP", "Local Delivery Contract Number"},
          {"Manifest_LABEL", "LD CON"},
          {"LeadTcn_LABEL", "Lead TCN"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
