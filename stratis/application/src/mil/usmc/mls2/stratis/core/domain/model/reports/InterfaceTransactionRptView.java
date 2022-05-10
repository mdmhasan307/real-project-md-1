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
public class InterfaceTransactionRptView {

  private Integer id;
  private String timestamp;
  private String docNumber;
  private String route;
  private String niin;
  private String ui;
  private Integer qty;
  private String cc;
  private OffsetDateTime date;
  private String priority;
  private String transactionType;
}
