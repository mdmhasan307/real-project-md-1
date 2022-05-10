package mil.stratis.model.view.stow.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class StowingGenQryVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"Aqty_LABEL", "Current NIIN Balance at this Location"},
      {"Ui_LABEL", "U/I"},
      {"LocLevel_LABEL", "Location Level"}, {"Rcn_LABEL", "RCN"}, {"Bay_LABEL", "Bay"},
      {"Niin_LABEL", "NIIN"},
      {"Rstatus_LABEL", "Receipt Status"},
      {"Sqty_LABEL", "Stow Qty"}, {"QtyToBeStowed_LABEL", "Qty to Stow"},
      {"Side_LABEL", "Side"}, {"Status_LABEL", "Stow Status"}, {"SexpDate_LABEL", "Expiration Date"},
      {"DocumentNumber_LABEL", "Document Number"},
      {"Sid_LABEL", "SID"},
      {"PartNumber_LABEL", "Part Num"},
      {"LocationLabel_LABEL", "Location Label"},
      {"Slot_LABEL", "Slot"},
      {"Cc_LABEL", "CC"}, {"Fsc_LABEL", "FSC"},
      {"DocumentId_LABEL", "Document ID"},
      {"Rcc_LABEL", "CC"},
      {"ExpirationDate_LABEL", "Expiration Date"},
      {"StowQty_LABEL", "Stowed Qty"},
      {"Nomenclature_LABEL", "Nomenclature"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
