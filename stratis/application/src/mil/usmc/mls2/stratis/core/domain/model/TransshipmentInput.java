package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.StringUtil;

@Slf4j
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransshipmentInput {

  String contractNumber;
  String shipmentNumber;
  String tcn;
  String nsn;
  String niin;
  String smic;
  String barcode;
  String trackingNumber;
  String barcodeToParse;
  String ric;
  String route;
  String ui;
  String quantityReceived;
  String quantityInvoiced;
  String pc;
  String cc;
  String up;
  String aac;
  String customerId;
  String dasfId;
  String documentNumber;
  String suffix;
  String callNumber;
  String lineNumber;
  String billAmount;
  String tailDate;
  OneDBarcode oneDBarcode;

  public String getContractNumber() { return StringUtil.trimUpperCaseClean(contractNumber); }
  public String getShipmentNumber() { return StringUtil.trimUpperCaseClean(shipmentNumber); }
  public String getTcn() { return StringUtil.trimUpperCaseClean(tcn); }
  public String getNsn() { return StringUtil.trimUpperCaseClean(nsn); }
  public String getNiin() { return StringUtil.trimUpperCaseClean(niin); }
  public String getSmic() { return StringUtil.trimUpperCaseClean(smic); }
  public String getBarcode() { return StringUtil.trimUpperCaseClean(barcode); }
  public String getTrackingNumber() { return StringUtil.trimUpperCaseClean(trackingNumber); }
  public String getRic() { return StringUtil.trimUpperCaseClean(ric); }
  public String getRoute() { return StringUtil.trimUpperCaseClean(route); }
  public String getUi() { return StringUtil.trimUpperCaseClean(ui); }
  public String getQuantityReceived() { return StringUtil.trimUpperCaseClean(quantityReceived); }
  public String getQuantityInvoiced() { return StringUtil.trimUpperCaseClean(quantityInvoiced); }
  public String getPc() { return StringUtil.trimUpperCaseClean(pc); }
  public String getCc() { return StringUtil.trimUpperCaseClean(cc); }
  public String getUp() { return StringUtil.trimUpperCaseClean(up); }
  public String getAac() { return StringUtil.trimUpperCaseClean(aac); }
  public String getCustomerId() { return StringUtil.trimUpperCaseClean(customerId); }
  public String getDasfId() { return StringUtil.trimUpperCaseClean(dasfId); }
  public String getDocumentNumber() { return StringUtil.trimUpperCaseClean(documentNumber); }
  public String getSuffix() { return StringUtil.trimUpperCaseClean(suffix); }
  public String getCallNumber() { return StringUtil.trimUpperCaseClean(callNumber); }
  public String getLineNumber() { return StringUtil.trimUpperCaseClean(lineNumber); }
  public String getBillAmount() { return StringUtil.trimUpperCaseClean(billAmount); }
}
