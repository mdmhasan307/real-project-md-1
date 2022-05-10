package mil.usmc.mls2.stratis.integration.mls2.core.domain.event;

import mil.usmc.mls2.stratis.core.domain.model.EventVisitor;

public interface AuditEventVisitor extends EventVisitor {

  default void visit(AuditCreatedEvent event) {}
}
