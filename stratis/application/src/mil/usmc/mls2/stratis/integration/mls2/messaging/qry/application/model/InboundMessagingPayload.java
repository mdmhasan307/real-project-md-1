package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Stores the contents and metadata for a Messaging aggregate.
 */
@Value
@Builder
@Accessors(fluent = true)
public class InboundMessagingPayload {

  public static final long VERSION = 1;

  String payloadClass;
  String message;

  @Builder.Default
  long payloadClassVersion = VERSION;

  public static InboundMessagingPayload of(String message, String payloadClass, long payloadClassVersion) {
    return InboundMessagingPayload.builder()
        .message(message)
        .payloadClass(payloadClass)
        .payloadClassVersion(payloadClassVersion)
        .build();
  }

  public static InboundMessagingPayload of(String message, Class<?> payloadClass, long payloadClassVersion) {
    return InboundMessagingPayload.of(message, payloadClass.getName(), payloadClassVersion);
  }
}