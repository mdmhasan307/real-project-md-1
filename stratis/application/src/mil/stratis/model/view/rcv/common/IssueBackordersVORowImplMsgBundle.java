package mil.stratis.model.view.rcv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class IssueBackordersVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {{"Rsuffix_LABEL", "Suffix"},
      {"Ui_LABEL", "UI"}, {"ProjectCode_LABEL", "Prj Code"},
      {"IdocumentNumber_LABEL", "Doc Num"},
      {"QuantityInducted_LABEL", "Qty Inducted"},
      {"Iqty_LABEL", "Backorder Qty"}, {"Scn_LABEL", "SCN"},
      {"RdocumentNumber_LABEL", "Doc Num"},
      {"Niin_LABEL", "NIIN"}, {"Scc_LABEL", "SCC"},
      {"Isuffix_LABEL", "Suffix"},
      {"PartNumber_LABEL", "Part Num"},
      {"QuantityReleased_LABEL", "Qty Released"}, {"Cc_LABEL", "CC"},
      {"Fsc_LABEL", "FSC"}};
  
  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
