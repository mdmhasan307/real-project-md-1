package mil.stratis.model.view.rcv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class StowCancelVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"Nomenclature_LABEL", "NOMENCLATURE"},
          {"Sid_LABEL", "SID"},
          {"CancelReason_LABEL", "Reason"},
          {"Rcn_LABEL", "RCN"},
          {"sidStr_LABEL", "Scan or Enter SID"}, {"Niin_LABEL", "NIIN"}, {"Fsc_LABEL", "FSC"},
          {"Pc_LABEL", "PC"}, {"Cc_LABEL", "CC"},
          {"DocumentNumber_LABEL", "Document Number"}};
  
  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
