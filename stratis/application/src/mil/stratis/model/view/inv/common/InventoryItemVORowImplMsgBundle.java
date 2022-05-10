package mil.stratis.model.view.inv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class InventoryItemVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {
      {"Status_LABEL", "Status"},
      {"MechanizedFlag_LABEL", "MechanizedFlag"},
      {"DividerIndex_LABEL", "DividerIndex"},
      {"NiinId_LABEL", "NiinId"},
      {"DividerTypeId_LABEL", "DividerTypeId"},
      {"LocLevel_LABEL", "LocLevel"},
      {"NiinLocId_FMT_FORMATTER", "oracle.jbo.format.DefaultNumberFormatter"},
      {"LocationLabel_LABEL", "Location Label"},
      {"Slot_LABEL", "Slot"},
      {"LocationId_LABEL", "LocationId"},
      {"Bay_LABEL", "Bay"},
      {"WacId_LABEL", "WacId"},
      {"Niin_LABEL", "NIIN"},
      {"InventoryItemId_LABEL", "InventoryItemId"},
      {"NiinLocId_FMT_FORMAT", "0000"},
      {"Qty_LABEL", "NiinLoc Qty"},
      {"Side_LABEL", "Side"},
      {"Nomenclature_LABEL", "Nomenclature"},
      {"NiinLocId_LABEL", "NiinLocId"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
