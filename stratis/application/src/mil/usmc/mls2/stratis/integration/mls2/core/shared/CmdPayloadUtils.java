package mil.usmc.mls2.stratis.integration.mls2.core.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.integration.common.Mls2Message;
import mil.usmc.mls2.integration.migs.gcss.i009.inbound.message.ShipmentReceipt;
import mil.usmc.mls2.integration.migs.gcss.i033.inbound.message.GabfDataRequest;
import mil.usmc.mls2.integration.migs.gcss.i033.outbound.message.GabfDataResponse;
import mil.usmc.mls2.integration.migs.gcss.i111.inbound.message.DasfDataRequest;
import mil.usmc.mls2.integration.migs.gcss.i111.outbound.message.DasfDataResponse;
import mil.usmc.mls2.integration.migs.gcss.i112.inbound.message.GbofDataRequest;
import mil.usmc.mls2.integration.migs.gcss.i112.inbound.message.MatsDataRequest;
import mil.usmc.mls2.integration.migs.gcss.i112.outbound.message.GbofDataResponse;
import mil.usmc.mls2.integration.migs.gcss.i112.outbound.message.MatsDataResponse;
import mil.usmc.mls2.integration.stratis.outbound.message.StratisAudit;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.utility.JsonUtils;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.OutboundMessaging;

/**
 * Provides utilities to convert from Mls2InboundMessage and Mls2OutboundMessage payloads to Mls2Messages
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unused")
public class CmdPayloadUtils {

  private static final String MSG_FAILED_TO_MLS2_MESSAGE = "failed to convert to mls2 message";

  /**
   * Converts the payload of the outbound message to an Mls2Message
   */
  public static Mls2Message toMls2Message(OutboundMessaging message, boolean raiseExceptionOnError) {
    switch (message.type()) {
      case I111_DASF_REQUEST:
        return toMls2Message(message.payload().message(), DasfDataRequest.class, raiseExceptionOnError);
      case I033_GABF_REQUEST:
        return toMls2Message(message.payload().message(), GabfDataRequest.class, raiseExceptionOnError);
      case I112_MATS_REQUEST:
        return toMls2Message(message.payload().message(), MatsDataRequest.class, raiseExceptionOnError);
      case I112_GBOF_REQUEST:
        return toMls2Message(message.payload().message(), GbofDataRequest.class, raiseExceptionOnError);
      case STRATIS_AUDIT:
        return toMls2Message(message.payload().message(), StratisAudit.class, raiseExceptionOnError);
      case I009_SHIPMENT_RECEIPT:
        return toMls2Message(message.payload().message(), ShipmentReceipt.class, raiseExceptionOnError);
      default:
        throw new StratisRuntimeException(String.format("Message Type %s is unsupported for deserialization.", message.type()));
    }
  }

  //
  //  /**
  //   * Converts the payload of the inbound message to an Mls2Message
  //   */
  public static Mls2Message toMls2Message(InboundMessaging message, boolean raiseExceptionOnError) {
    switch (message.type()) {
      case I033_GABF_RESPONSE:
        return toMls2Message(message, GabfDataResponse.class, raiseExceptionOnError);
      case I111_DASF_RESPONSE:
        return toMls2Message(message, DasfDataResponse.class, raiseExceptionOnError);
      case I112_MATS_RESPONSE:
        return toMls2Message(message, MatsDataResponse.class, raiseExceptionOnError);
      case I112_GBOF_RESPONSE:
        return toMls2Message(message, GbofDataResponse.class, raiseExceptionOnError);
      default:
        throw new StratisRuntimeException(String.format("Message Type %s is unsupported for deserialization.", message.type()));
    }
  }

  public static <T extends Mls2Message> T toMls2Message(InboundMessaging message, Class<T> payloadClass, boolean raiseExceptionOnError) {
    return toMls2Message(message.payload().message(), payloadClass, raiseExceptionOnError);
  }

  private static <T extends Mls2Message> T toMls2Message(String message, Class<T> messageClass, boolean raiseExceptionOnError) {
    try {
      // Was using JsonConverterUtils.toObject(message, messageClass, false) in MIGS, which appears to be JsonUtils in STRATIS.
      return JsonUtils.toObject(message, messageClass, false);
    }
    catch (RuntimeException ge) {
      if (raiseExceptionOnError) throw ge;
      return null;
    }
    catch (Exception e) {
      log.error("Could not convert to message type: {}, payload: {}", messageClass.toString(), message);
      if (raiseExceptionOnError) throw new StratisRuntimeException(MSG_FAILED_TO_MLS2_MESSAGE, e);
      return null;
    }
  }
}