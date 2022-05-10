package mil.stratis.model.view.inv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class NIINLocDetailsVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {
          {"LocationId_FMT_FORMATTER", "oracle.jbo.format.DefaultNumberFormatter"},
          {"NiinId_FMT_FORMATTER", "oracle.jbo.format.DefaultNumberFormatter"}, {"NiinId_FMT_FORMAT", "0000"},
          {"NiinLocId_FMT_FORMATTER", "oracle.jbo.format.DefaultNumberFormatter"}, {"LocationId_FMT_FORMAT", "0000"},
          {"Qty_FMT_FORMAT", "0000"},
          {"Qty_FMT_FORMATTER", "oracle.jbo.format.DefaultNumberFormatter"},
          {"NiinLocId_FMT_FORMAT", "0000"},
          {"WacId_FMT_FORMAT", "0000"},
          {"Niin_LABEL", "NIIN"},
          {"WacId_FMT_FORMATTER", "oracle.jbo.format.DefaultNumberFormatter"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
