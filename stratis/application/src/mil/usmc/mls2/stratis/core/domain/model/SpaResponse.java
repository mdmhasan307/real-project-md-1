package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface SpaResponse {

  @RequiredArgsConstructor(staticName = "of")
  @Getter
  class SpaResponseMessage {

    private final String message;
    private final SpaPostResponse.SpaResponseMessageType type;
  }

  void addException(String message);

  void addWarning(String message);

  void setDevProfileActive(boolean active);

  enum SpaResponseResult {
    SUCCESS,
    SESSION_INVALID,
    VALIDATION_WARNINGS,
    EXCEPTION,
    REDIRECT_HOME //Used for if we need to display a message, and then force a redirect to the SMV Home
  }

  enum SpaResponseMessageType {
    NOTIFICATION,
    VALIDATION_WARNING,
    EXCEPTION,
    EVENT_MESSAGE //used to publish results of events that finish behind the scenes the user should be notified about.
  }

  enum SpaResponseType {
    GET,
    POST
  }
}
