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
public class InventoryHistoryRptView {

  private String id;
  private OffsetDateTime completedDate;
  private String completedBy;
  private String status;
  private String niin;
  private String nomenclature;
  private String cc;
  private Integer qty;
  private String locationLabel;
  private Integer negAdj;
  private Integer posAdj;
  private Double price;
  private Double totalValue;
  private String wac;
}
