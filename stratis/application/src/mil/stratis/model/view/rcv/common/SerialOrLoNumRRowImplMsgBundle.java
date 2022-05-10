package mil.stratis.model.view.rcv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class SerialOrLoNumRRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {{"ExpirationDate_TOOLTIP", "yyyy-MM-dd"},
      {"QtyLot_LABEL", "LotQty"},
      {"SerialOrLoNum_LABEL", "Seril #"},
      {"ExpirationDate_LABEL", "Exp Date"},
      {"ExpirationDate_FMT_FORMAT", "yyyy-MM-dd"},
      {"ExpirationDate_FMT_FORMATTER", "oracle.jbo.format.DefaultDateFormatter"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
