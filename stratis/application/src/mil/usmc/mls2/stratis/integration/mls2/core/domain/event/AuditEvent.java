package mil.usmc.mls2.stratis.integration.mls2.core.domain.event;

import mil.usmc.mls2.stratis.core.domain.model.Event;

public interface AuditEvent extends Event {

  void accept(AuditEventVisitor visitor);
}
