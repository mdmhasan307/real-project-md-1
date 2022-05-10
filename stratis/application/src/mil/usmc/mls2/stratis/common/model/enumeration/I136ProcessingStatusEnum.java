package mil.usmc.mls2.stratis.common.model.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum I136ProcessingStatusEnum implements PersistableEnum<Integer, String> {
  SUCCESS(1, "Success", true),
  SUCCESS_PARTIAL(2, "Partial Success", true),
  SUCCESS_MESSAGE_SENT_TO_MIGS_NO_WAIT(4, "Success - Message sent to MIGS but not waiting for a response, check back later", true),
  SUCCESS_RESPONSE_FROM_MIGS_DELAYED(5, "Success - Response from MIGS Delayed, check back later", true),
  SUCCESS_RESPONSE_FROM_MIGS_RECEIVED(6, "Success - Response from MIGS Received", true),
  FAILURE(20, "Failure - No NIINs to send to GCSS", false),
  FAILURE_INTERFACES_OFF(21, "Failure - Interfaces turned off", false),
  FAILURE_ERROR_COMPRESSING_DATA_TO_SEND(22, "Failure - Error compressing data to send", false),
  FAILURE_ERROR_DECOMPRESSING_RESULT(23, "Failure - Error decompressing result", false),
  FAILURE_NO_RESPONSE(24, "Failure - No Response from GCSS", false),
  FAILURE_ERROR_RESPONSE(25, "Failure - Error Response from GCSS", false),
  FAILURE_ERROR_GETTING_DATA_FROM_MIGS_API(26, "Failure - Retrieving Data from the MIGS API", false),
  FAILURE_ERROR_SENDING_MESSAGE_TO_MIGS(27, "Failure - Error sending message to MIGS", false);

  private final Integer id;
  private final String label;
  private final boolean success;
}
