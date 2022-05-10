package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingStowDetailLocationInput {

  String location;

  //From ReceivingControlledNiinDetailsInput needed for ease of display of form read only data.
  @Builder.Default
  private List<String> serials = new ArrayList<>();

  //From ReceivingDetailInput needed for ease of display of form read only data.
  private Integer qtyReceived;
  private Integer qtyInducted;
  private String ri;
  private String cc;
  private String expDate;
  private String wac;
  private String bulk;
  private String dom;

  //From ReceivingHomeInput needed for ease of display of form read only data.
  private String docNumber;
  private String nsn;
  private String barcode;
  private String chkPartialShipment;

  public ReceivingStowDetailLocationInput(ReceivingWorkflowContainer receivingWorkflowContainer) {
    docNumber = receivingWorkflowContainer.getReceivingHomeInput().getDocNumber();
    nsn = receivingWorkflowContainer.getReceivingHomeInput().getNsn();
    barcode = receivingWorkflowContainer.getReceivingHomeInput().getBarcode();
    chkPartialShipment = "on".equalsIgnoreCase(receivingWorkflowContainer.getReceivingHomeInput().getChkPartialShipment()) ? "Yes" : "No";

    qtyReceived = receivingWorkflowContainer.getReceivingDetailInput().getQtyReceived();
    qtyInducted = receivingWorkflowContainer.getReceivingDetailInput().getQtyInducted();
    ri = receivingWorkflowContainer.getReceivingDetailInput().getRi();
    cc = receivingWorkflowContainer.getReceivingDetailInput().getCc();
    expDate = receivingWorkflowContainer.getReceivingDetailInput().getExpDate();
    wac = receivingWorkflowContainer.getReceivingDetailInput().getWac();
    bulk = receivingWorkflowContainer.getReceivingDetailInput().getBulk();
    dom = receivingWorkflowContainer.getReceivingDetailInput().getDom();

    if (receivingWorkflowContainer.getReceivingControlledNiinDetailsInput() != null)
      serials = receivingWorkflowContainer.getReceivingControlledNiinDetailsInput().getSerials();
  }
}
