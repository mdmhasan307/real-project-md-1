package mil.stratis.model.view.rcv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class ReceiptTransshipmentVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {{"Rsuffix_LABEL", "Suffix"}, {"ProjectCode_LABEL", "Prj Code"},
          {"QuantityInducted_LABEL", "Quantity"}, {"RdocumentNumber_LABEL", "Doc No"},
          {"Rcn_LABEL", "RCN"},
          {"Scc_LABEL", "SCC"},
          {"Ui_LABEL", "U/I"}, {"Niin_LABEL", "NIIN"}, {"Fsc_LABEL", "FSC"}, {"Cc_LABEL", "CC"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
