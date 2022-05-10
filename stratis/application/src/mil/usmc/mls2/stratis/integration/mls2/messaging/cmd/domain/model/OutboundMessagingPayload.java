package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Stores the contents and metadata for a Messaging aggregate.
 */
@Value
@Builder
@Accessors(fluent = true)
public class OutboundMessagingPayload {

  public static final long VERSION = 1;

  String payloadClass;
  String message;

  @Builder.Default
  long payloadClassVersion = VERSION;

  public static OutboundMessagingPayload of(String message, String payloadClass, long payloadClassVersion) {
    return OutboundMessagingPayload.builder()
        .message(message)
        .payloadClass(payloadClass)
        .payloadClassVersion(payloadClassVersion)
        .build();
  }

  public static OutboundMessagingPayload of(String message, Class<?> payloadClass, long payloadClassVersion) {
    return OutboundMessagingPayload.of(message, payloadClass.getName(), payloadClassVersion);
  }
}