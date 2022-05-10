package mil.stratis.view.reports;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class StdReportColumnBean implements Serializable {

  private String docNumber;
  private String unitOfIssue;
  private String qtyDue;
  private String qtyInvoiced;
  private String recordFsc;
  private String recordNiin;
  private String signalCode;
  private String supplAddr;
  private String unitPriceSum;
  private String priority;
  private String outputDate;
  private String transType;
  private String route;
  private String niin;
  private String ui;
  private String qty;
  private String cc;
  private String transDate;
  private String locationlabel;
  private String nomenclature;
  private String packArea;
  private String priceSum;
  private String shelfLifeCode;
  private String scc;
  private String expirationDate;
  private String lastInvDate;
  private String employee;
  private String receipts;
  private String stows;
  private String picks;
  private String packs;
  private String invs;
  private String totals;
  private String availabilityFlag;
  private String manufactureDate;
  private String receiptsDollarValue;
  private String stowsDollarValue;
  private String picksDollarValue;
  private String packsDollarValue;
  private String invsDollarValue;
}
