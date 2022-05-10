package mil.stratis.view.reports.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportGabfInfo {

  private int serviceableQty;
  private int unserviceableQty;
  private String niin;
  private int roThreshold;
  private String operationCode;
  private String requirementCode;
  private int qty;
  private String activityAddressCode;
  private String cc;
  private int serviceableDiff;
  private int unserviceableDiff;

  public ReportGabfInfo(int serviceableQty, int unserviceableQty, int qty, String cc, int serviceableDiff, int unserviceableDiff) {
    this.serviceableQty = serviceableQty;
    this.unserviceableQty = unserviceableQty;
    this.qty = qty;
    this.cc = cc;
    this.serviceableDiff = serviceableDiff;
    this.unserviceableDiff = unserviceableDiff;
  }
}
