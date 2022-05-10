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
public class EmployeeWorkloadHistoryRptView {

  private String user;
  private OffsetDateTime transDate;
  private Integer receiptCount;
  private Double receiptsDollarValue;
  private Integer stowCount;
  private Double stowDollarValue;
  private Integer pickCount;
  private Double pickDollarValue;
  private Integer packConsolidationCount;
  private Double packDollarValue;
  private Integer inventoryItemCount;
  private Double inventoryDollarValue;
  private Integer otherCount;
}
