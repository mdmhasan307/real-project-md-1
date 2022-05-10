package mil.stratis.model.view.inv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class SerialOrLotNumListInvRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"SerialNumber_LABEL", "Serial#"},
      {"QtyLot_LABEL", "LotQty"},
      {"Cc_LABEL", "CC"},
      {"LotConNum_LABEL", "Lot#"},
      {"ExpirationDate_LABEL", "Exp Dt"},
      {"ExpirationDate_FMT_FORMAT", "yyyy-MM-dd"},
      {"ExpirationDate_FMT_FORMATTER", "oracle.jbo.format.DefaultDateFormatter"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
