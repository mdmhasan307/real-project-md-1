package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingDetailInput implements Serializable {

  private Integer qtyReceived;
  private Integer qtyInducted;
  private String ri;
  private String cc;
  private String expDate;
  private String wac;
  private String bulk;
  private String dom;
  private String dasfQtyOverride;

  //From ReceivingHomeInput needed for ease of display of form read only data.
  private String docNumber;
  private String nsn;
  private String barcode;
  private String chkPartialShipment;

  public ReceivingDetailInput(ReceivingWorkflowContainer receivingWorkflowContainer) {
    docNumber = receivingWorkflowContainer.getReceivingHomeInput().getDocNumber();
    nsn = receivingWorkflowContainer.getReceivingHomeInput().getNsn();
    barcode = receivingWorkflowContainer.getReceivingHomeInput().getBarcode();
    chkPartialShipment = "on".equalsIgnoreCase(receivingWorkflowContainer.getReceivingHomeInput().getChkPartialShipment()) ? "Yes" : "No";
    cc = receivingWorkflowContainer.getReceivingHomeInput().getBarcode().substring(10, 11);
  }
}
