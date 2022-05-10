package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class ReceivingWorkflowContainer implements Serializable {

  private ReceivingHomeInput receivingHomeInput;
  private ReceivingDetailInput receivingDetailInput;
  private ReceivingControlledNiinDetailsInput receivingControlledNiinDetailsInput;
  private boolean uiError;
  private String wac;
  private Receipt receipt;

  public void setDetailsData(Receipt newReceipt, ReceivingDetailInput receivingDetailInput) {
    this.receipt = newReceipt;
    this.wac = receivingDetailInput.getWac();
    this.receivingDetailInput = receivingDetailInput;
    this.receivingHomeInput.setDocNumber(receivingHomeInput.getDocNumber());
  }
}
