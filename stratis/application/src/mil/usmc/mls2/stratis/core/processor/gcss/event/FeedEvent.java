package mil.usmc.mls2.stratis.core.processor.gcss.event;

import mil.usmc.mls2.stratis.core.domain.model.Event;
import mil.usmc.mls2.stratis.integration.mls2.core.domain.event.AuditEventVisitor;

public interface FeedEvent extends Event {

  public static final String SESSION_IDENTIFIER = "EVENT_MESSAGE";
  
  void accept(AuditEventVisitor visitor);
}
