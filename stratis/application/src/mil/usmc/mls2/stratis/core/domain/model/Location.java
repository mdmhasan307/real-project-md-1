package mil.usmc.mls2.stratis.core.domain.model;

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
public class Location {

  private Integer locationId;
  private String aisle;
  private String side;
  private String bay;
  private String locLevel;
  private String availabilityFlag;
  private String locationLabel;
  private String slot;
  private Wac wac;
  private Integer locationHeaderBinId;
  private String mechanizedFlag;
  private Integer locClassificationId;
  private Integer dividerIndex;
  private int cube;
  private int weight;
  private OffsetDateTime lastStowDate;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
}
