package mil.stratis.model.view.ship.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class UnprocessBarcodeRptViewRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"CreatedDate_FMT_FORMAT", "dd MMM yyyy kk:mm:ss"},
      {"CreatedDate_FMT_FORMATTER", "oracle.jbo.format.DefaultDateFormatter"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
