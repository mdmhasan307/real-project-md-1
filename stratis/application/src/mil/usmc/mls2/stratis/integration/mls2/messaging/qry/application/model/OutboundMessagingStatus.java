package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import mil.usmc.mls2.stratis.common.domain.model.StandardType;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
public enum OutboundMessagingStatus implements StandardType<Integer> {

  PENDING(1, "Pending"),
  RECEIVED_BY_REMOTE_SYSTEM(2, "Received by Remote System"),
  PROCESSING(3, "Processing"),
  PROCESSED(4, "Process Completed"),
  EXCEPTION(5, "Process Failed"),
  CANCELED(6, "Canceled"),
  UNKNOWN(-1, "UNKNOWN - INVALID");

  private final Integer id;
  private final String label;

  public static Optional<OutboundMessagingStatus> valueOf(Integer id) {
    if (id == null) return Optional.empty();
    return Arrays.stream(values()).filter(x -> x.id.equals(id)).findFirst();
  }
}