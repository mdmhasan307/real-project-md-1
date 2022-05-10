package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@ToString
public class SpaGetResponse implements SpaResponse {

  private String responseBody;
  private String pageTitle;

  @Builder.Default
  private List<SpaResponseMessage> messages = new ArrayList<>();
  private SpaResponseResult result;

  @Builder.Default
  private boolean devProfileActive = false;

  public void addWarning(String message) {
    messages.add(SpaResponseMessage.of(message, SpaResponseMessageType.VALIDATION_WARNING));
  }

  public void addEventMessage(String message) {
    messages.add(SpaResponseMessage.of(message, SpaResponseMessageType.EVENT_MESSAGE));
  }

  public void addException(String message) {
    messages.add(SpaResponseMessage.of(message, SpaResponseMessageType.EXCEPTION));
  }

  public void setDevProfileActive(boolean active) {
    devProfileActive = active;
  }
}
