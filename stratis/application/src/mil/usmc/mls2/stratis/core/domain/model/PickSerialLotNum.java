package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickSerialLotNum {

  private Integer pickSerialLotNum;
  private SerialLotNumTrack serialLotNumTrack;
  private OffsetDateTime timestamp;
  private Integer pid;
  private Integer qty;
  private String lotConNum;
  private String serialNumber;
  private String scn;
  private Integer locationId;
  private LocalDate expirationDate;
}
