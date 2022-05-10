package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@ToString
public class SpaPostResponse implements SpaResponse {

  //only needs to be provided if they change.
  private String userRole;
  private String userWorkstation;

  private String redirectUrl;
  @Builder.Default
  private List<SpaResponseMessage> messages = new ArrayList<>();
  @Builder.Default
  private List<String> responseFlags = new ArrayList<>();
  private SpaResponseResult result;

  @Builder.Default
  private boolean devProfileActive = false;

  public void addNotification(String message) {
    messages.add(SpaResponseMessage.of(message, SpaResponseMessageType.NOTIFICATION));
  }

  public void addEventMessage(String message) {
    messages.add(SpaResponseMessage.of(message, SpaResponseMessageType.EVENT_MESSAGE));
  }

  public void addWarning(String message) {
    messages.add(SpaResponseMessage.of(message, SpaResponseMessageType.VALIDATION_WARNING));
  }

  public void addException(String message) {
    messages.add(SpaResponseMessage.of(message, SpaResponseMessageType.EXCEPTION));
  }

  public void addFlag(String flag) { responseFlags.add(flag); }

  public void setDevProfileActive(boolean active) {
    devProfileActive = active;
  }
}
