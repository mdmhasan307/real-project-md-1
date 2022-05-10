package mil.stratis.model.view.ship.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class TempShippingReviewRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"LastReviewDate_LABEL", "Last Review Date"},
          {"FloorLocation_LABEL", "Floor Location"},
          {"Tcn_LABEL", "TCN"},
          {"Aac_LABEL", "AAC"},
          {"LeadTcn_LABEL", "Lead TCN"},
          {"ShippingBarcode_LABEL", "Shipping Barcode"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
