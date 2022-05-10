package mil.stratis.model.view.rcv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class StowViewRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"Pid_LABEL", "PID"}, {"Rcn_LABEL", "RCN"},
      {"LotConNum_LABEL", "Lot Control Number"},
      {"DateOfManufacture_FMT_FORMATTER", "oracle.jbo.format.DefaultDateFormatter"},
      {"CaseWeightQty_LABEL", "Case Weight Qty"}, {"CancelReason_LABEL", "Reason"},
      {"QtyToBeStowed_LABEL", "Qty to Stow"},
      {"PackedDate_FMT_FORMAT", "yyyy-MM-dd"},
      {"Status_LABEL", "Status"}, {"DateOfManufacture_FMT_FORMAT", "yyyy-MM-dd"},
      {"PackedDate_FMT_FORMATTER", "oracle.jbo.format.DefaultDateFormatter"}, {"Sid_LABEL", "SID"},
      {"DateOfManufacture_LABEL", "Manufacture Date"},
      {"LocationLabel_LABEL", "Location"},
      {"PackedDate_LABEL", "Packed Date"},
      {"ExpirationDate_LABEL", "Expiration Date"},
      {"ExpirationDate_FMT_FORMAT", "yyyy-MM-dd"},
      {"ExpirationDate_FMT_FORMATTER", "oracle.jbo.format.DefaultDateFormatter"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
