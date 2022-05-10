package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
@Builder
@AllArgsConstructor
public class ReceivingHomeInput implements Serializable {

  private String docNumber;
  private String nsn;
  private String barcode;
  private String chkPartialShipment;

  public void setDocNumber(String docNumber) { this.docNumber = docNumber.toUpperCase(); }

  public void updateNsnAndBarcode(String nsn, String barcode) {
    this.barcode = barcode;
    this.nsn = nsn;
  }

  public static class ReceivingHomeInputBuilder {

    public ReceivingHomeInputBuilder docNumber(String docNumber) {
      this.docNumber = docNumber.toUpperCase();
      return this;
    }
  }
}
