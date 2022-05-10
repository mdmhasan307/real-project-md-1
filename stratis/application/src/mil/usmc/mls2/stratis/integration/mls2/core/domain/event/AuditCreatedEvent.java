package mil.usmc.mls2.stratis.integration.mls2.core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import mil.usmc.mls2.stratis.core.domain.model.AuditLog;
import mil.usmc.mls2.stratis.core.domain.model.EventMeta;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class AuditCreatedEvent implements AuditEvent {

  private static final String LABEL = "Audit entry created";

  @Builder.Default
  private final EventMeta meta = EventMeta.of(LABEL);

  @Builder.Default
  private final OffsetDateTime messageTimestamp = OffsetDateTime.now();

  private final AuditLog auditLog;

  private final String siteIdentifier;

  @Override
  public void accept(AuditEventVisitor visitor) {
    visitor.visit(this);
  }
}
