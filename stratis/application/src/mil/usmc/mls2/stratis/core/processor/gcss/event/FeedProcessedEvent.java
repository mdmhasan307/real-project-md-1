package mil.usmc.mls2.stratis.core.processor.gcss.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import mil.usmc.mls2.stratis.core.domain.model.EventMeta;
import mil.usmc.mls2.stratis.integration.mls2.core.domain.event.AuditEventVisitor;

import javax.servlet.http.HttpSession;
import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class FeedProcessedEvent implements FeedEvent {

  private static final String LABEL = "Feed processed event";

  @Builder.Default
  private final EventMeta meta = EventMeta.of(LABEL);

  @Builder.Default
  private final OffsetDateTime messageTimestamp = OffsetDateTime.now();

  private final String feedResults;

  private final String feedType;

  private final HttpSession session;

  @Override
  public void accept(AuditEventVisitor visitor) {
    visitor.visit(this);
  }
}
