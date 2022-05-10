package mil.stratis.model.view.rcv.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class ReceiptViewRowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings = {{"QuantityStowed_LABEL", "Qty Stowed"},
      {"Pc_LABEL", "Purpose Code"},
      {"QuantityMeasured_LABEL", "Qty Measured"},
      {"ProjectCode_LABEL", "Project Code"},
      {"QuantityInducted_LABEL", "Qty Received"},
      {"Consignee_LABEL", "Ship To"},
      {"Rcn_LABEL", "RCN"}, {"Rdd_LABEL", "RDD"},
      {"ShippedFrom_LABEL", "Shipped From"},
      {"Rpd_TOOLTIP", "Requisition Priority Designator"},
      {"DodDistCode_LABEL", "Dod Dist Code"},
      {"RoutingId_LABEL", "RI"}, {"DocumentNumber_LABEL", "Document Number"},
      {"PartNumber_LABEL", "Part Number"},
      {"QuantityReleased_LABEL", "Qty Released"},
      {"SupplementaryAddress_LABEL", "Supplementary Address"},
      {"Ri_LABEL", "RI"},
      {"Cc_LABEL", "CC"},
      {"Fsc_LABEL", "FSC"},
      {"Rpd_LABEL", "RPD"},
      {"ShelfLifeCode_LABEL", "Shelf Life Code"},
      {"QuantityBackordered_LABEL", "Qty Backorder"},
      {"CognizanceCode_LABEL", "Cognizance Code"},
      {"QuantityInvoiced_LABEL", "Qty Invoiced"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
