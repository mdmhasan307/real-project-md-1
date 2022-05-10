package mil.usmc.mls2.stratis.core.domain.model.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptHistRptView {

  private Integer rcn;
  private String sid;
  private String niin;
  private OffsetDateTime dateReceived;
  private String documentNumber;
  private Integer qtyReceived;
  private Integer stowedQty;
  private Integer qtyBackordered;
  private String locationLabel;
  private String receivedBy;
  private String stowedBy;
  private OffsetDateTime stowedDate;
}
