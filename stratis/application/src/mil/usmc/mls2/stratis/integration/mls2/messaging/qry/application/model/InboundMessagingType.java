package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model;

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
public enum InboundMessagingType implements StandardType<Integer> {

  UNKNOWN(-1, "UNKNOWN - INVALID", "Unsupported/Invalid message", null),
  I111_DASF_RESPONSE(2, "I111 GCSS DASF Data Response", "Response to STRATIS to retrieve new DASF DAC data by ID.", MessageType.STRATIS_GCSS_DASF_RESPONSE),
  I033_GABF_RESPONSE(3, "I033 GCSS GABF Data Response", "Response to STRATIS to read page of GABF data available by Tamcn.", MessageType.STRATIS_GCSS_GABF_RESPONSE),
  I112_MATS_RESPONSE(4, "I112 GCSS MATS Data Response", "Response to STRATIS to read page of MATS data available.", MessageType.STRATIS_GCSS_MATS_RESPONSE),
  I112_GBOF_RESPONSE(5, "I112 GCSS GBOF Data Response", "Response to STRATIS to read page of GBOF data available.", MessageType.STRATIS_GCSS_GBOF_RESPONSE);

  public final Integer id;
  public final String label;
  public final String description;
  public final boolean response = false;
  public final MessageType messageType;

  public static Optional<InboundMessagingType> valueOf(Integer id) {
    if (id == null) return Optional.empty();
    return Arrays.stream(values()).filter(x -> x.id.equals(id)).findFirst();
  }

  public static Optional<InboundMessagingType> valueOf(MessageType input) {
    if (input == null) return Optional.empty();
    return Arrays.stream(InboundMessagingType.values())
        .filter(x -> x.messageType == input)
        .findFirst();
  }
}