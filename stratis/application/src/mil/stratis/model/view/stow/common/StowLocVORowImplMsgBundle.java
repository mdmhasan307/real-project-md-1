package mil.stratis.model.view.stow.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class StowLocVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"ExpirationDate_LABEL", "Exp Date"},
          {"Sid_LABEL", "SID"}, {"StowQty_LABEL", "Stow Qty"}, {"LocationLabel_LABEL", "Loc Label"},
          {"Status_LABEL", "Stow Status"},
          {"Rcn_LABEL", "RCN"},
          {"DateOfManufacture_LABEL", "Manf. Date"},
          {"QtyToBeStowed_LABEL", "Qty To Stow"}};
  
  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
