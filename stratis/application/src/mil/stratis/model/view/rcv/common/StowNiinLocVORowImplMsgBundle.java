package mil.stratis.model.view.rcv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class StowNiinLocVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"ExpirationDate_LABEL", "Expiration Date"},
          {"Sid_LABEL", "SID"},
          {"Pid_TOOLTIP", "Pick ID"},
          {"PackedDate_LABEL", "Packed Date"},
          {"CaseWeightQty_LABEL", "Case Weight Qty"},
          {"QtyToBeStowed_TOOLTIP", "Quantity to be stowed"},
          {"Rcn_LABEL", "RCN"},
          {"LocationId_LABEL", "Location"},
          {"DateOfManufacture_LABEL", "Manufacture Date"},
          {"LotConNum_LABEL", "Lot Control Num"},
          {"Pid_LABEL", "PID"},
          {"QtyToBeStowed_LABEL", "Qty to Stow"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
