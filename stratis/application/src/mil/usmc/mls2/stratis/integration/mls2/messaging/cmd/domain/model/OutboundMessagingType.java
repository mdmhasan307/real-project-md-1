package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import mil.usmc.mls2.integration.common.model.MessageType;
import mil.usmc.mls2.stratis.common.domain.model.StandardType;

import java.util.Arrays;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum OutboundMessagingType implements StandardType<Integer> {

  UNKNOWN(-1, "UNKNOWN - INVALID", "Unsupported/Invalid message", true, null),
  I111_DASF_REQUEST(2, "I111 GCSS DASF Data Request", "Request to MIGS to retrieve new DASF DAC data by ID.", true, MessageType.STRATIS_GCSS_DASF_REQUEST),
  I033_GABF_REQUEST(3, "I033 GCSS GABF Data Request", "Request to MIGS to retrieve new GABF data by ID.", true, MessageType.STRATIS_GCSS_GABF_REQUEST),
  I112_MATS_REQUEST(4, "I112 GCSS MATS Data Request", "Request to MIGS to retrieve new MATS data by ID.", true, MessageType.STRATIS_GCSS_MATS_REQUEST),
  I112_GBOF_REQUEST(5, "I112 GCSS GBOF Data Request", "Request to MIGS to retrieve new GBOF data by ID.", true, MessageType.STRATIS_GCSS_GBOF_REQUEST),
  I009_SHIPMENT_RECEIPT(6, "I009 GCSS Shipment Receipt", "Request to MIGS to send Shipment Receipt to GCSS.", true, MessageType.STRATIS_SHIPMENT_RECEIPT),
  STRATIS_AUDIT(100, "STRATIS AUDIT MESSAGE", "AUDIT MESSAGE TO STORE IN MIPS", false, MessageType.STRATIS_AUDIT);

  public final Integer id;
  public final String label;
  public final String description;
  public final boolean response;
  public final MessageType messageType;

  public static Optional<OutboundMessagingType> valueOf(Integer id) {
    if (id == null) return Optional.empty();
    return Arrays.stream(values()).filter(x -> x.id.equals(id)).findFirst();
  }
}
