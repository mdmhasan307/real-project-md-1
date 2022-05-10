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
public class ErrorQueue {

  private int errorQueueId;
  private String status;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private int eid;
  private String keyType;
  private String keyNum;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private String notes;
}
