package mil.stratis.view.reports.vo;

import lombok.Data;

@Data
public class ReportQuarterlyUsage {

  private String dateRange;
  private int receipts;
  private int qtyReceivedCodeA;
  private int qtyReceivedCodeF;
  private int qtyIssuedCodeA;
  private int qtyIssuedCodeF;

  public ReportQuarterlyUsage(String dateRange,
                              int receipts, int qtyReceivedCodeA,
                              int qtyReceivedCodeF, int qtyIssuedCodeA, int qtyIssuedCodeF) {
    this.dateRange = dateRange;
    this.receipts = receipts;
    this.qtyReceivedCodeA = qtyReceivedCodeA;
    this.qtyReceivedCodeF = qtyReceivedCodeF;
    this.qtyIssuedCodeA = qtyIssuedCodeA;
    this.qtyIssuedCodeF = qtyIssuedCodeF;
  }
}
