package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserType implements Serializable {

  private Integer userTypeId;
  private String type;
  private String description;
  private OffsetDateTime timestamp;
  private Integer updatedBy;
}
