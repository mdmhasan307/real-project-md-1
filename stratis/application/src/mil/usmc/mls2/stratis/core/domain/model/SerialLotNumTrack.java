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
public class SerialLotNumTrack {

  private int serialLotNumTrackId;
  private Integer niinId;
  private String serialNumber;
  private String lotConNum;
  private String cc;
  private LocalDate expirationDate;
  private String issuedFlag;
  private OffsetDateTime timestamp;
  private Integer qty;
  private Integer locationId;
  private String iuid;
  private String recallFlag;
  private OffsetDateTime recallDate;
}
