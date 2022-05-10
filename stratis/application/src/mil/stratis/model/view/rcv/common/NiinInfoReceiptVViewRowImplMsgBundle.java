package mil.stratis.model.view.rcv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class NiinInfoReceiptVViewRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {{"CodeValue_LABEL", "SecureCode"},
          {"Price_FMT_FORMATTER", "oracle.jbo.format.DefaultCurrencyFormatter"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
