package mil.stratis.view.reports;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReconReportColumnBean implements Serializable {

  private String niin;
  private String matchingNum;
  private String unmatchingNum;
  private String stratisBalance;
  private String hostBalance;
  private String balanceDiff;
  private String price;
  private String adjustment;
  private String location;
  private String qtyOnHand;
  private String system;
  private String qtyBySerial;
  private String serialNum;
  private String servStratisBalance;
  private String servHostBalance;
  private String unServStratisBalance;
  private String unServHostBalance;

  public ReconReportColumnBean() {
    super();
  }
}
