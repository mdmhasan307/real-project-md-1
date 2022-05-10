package mil.stratis.model.view.inv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class LocSurveyVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"NiinId_FMT_FORMATTER", "oracle.jbo.format.DefaultNumberFormatter"}, {"NiinId_FMT_FORMAT", "0000"},
          {"NiinLocId_FMT_FORMATTER", "oracle.jbo.format.DefaultNumberFormatter"},
          {"Qty_LABEL", "Qty"}, {"InventoryItemId_FMT_FORMAT", "0000"}, {"NiinLocId_LABEL", "NiinLocId"},
          {"InventoryItemId_FMT_FORMATTER", "oracle.jbo.format.DefaultNumberFormatter"},
          {"NiinId_LABEL", "NIIN ID"},
          {"NiinLocId_FMT_FORMAT", "0000"},
          {"LocationLabel_LABEL", "Location Label"}, {"Niin_LABEL", "NIIN"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
