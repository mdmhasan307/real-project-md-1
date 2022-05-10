package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogCategoryEnum;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogSourceEnum;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog implements Serializable {

  private Integer auditLogId;
  private AuditLogSourceEnum source;
  private AuditLogTypeEnum type;
  private OffsetDateTime timestamp;
  private AuditLogCategoryEnum category;
  private Integer event;
  private Integer userId;
  private Integer equipmentNumber;
  private String description;

  private String username;
}
