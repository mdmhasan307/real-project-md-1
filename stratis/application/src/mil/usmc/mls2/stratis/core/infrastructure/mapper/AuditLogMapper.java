package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.AuditLog;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.AuditLogEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class AuditLogMapper {

  private final AuditLogMapper auditLogMapper;

  public AuditLogMapper(@Lazy AuditLogMapper auditLogMapper) {
    this.auditLogMapper = auditLogMapper;
  }

  public AuditLog map(AuditLogEntity input) {
    if (input == null) return null;

    return AuditLog.builder()
        .auditLogId(input.getAuditLogId())
        .category(input.getCategory())
        .description(input.getDescription())
        .equipmentNumber(input.getEquipmentNumber())
        .event(input.getEvent())
        .source(input.getSource())
        .timestamp(input.getTimestamp())
        .type(input.getType())
        .userId(input.getUserId())
        .build();
  }
}