package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvSerialLotNum {

  private int invSerialLotNumId;
  private Integer inventoryItemId;
  private OffsetDateTime timestamp;
  private String invDoneFlag;
  private Integer niinId;
  private String serialNumber;
  private String lotConNum;
  private String cc;
  private LocalDate expirationDate;
  private Integer qty;
  private Integer locationId;
}
