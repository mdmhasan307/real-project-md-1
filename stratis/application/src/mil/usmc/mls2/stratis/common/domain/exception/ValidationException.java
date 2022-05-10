package mil.usmc.mls2.stratis.common.domain.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@SuppressWarnings("unused")
public class ValidationException
    extends RuntimeException {

  @Getter
  private final List<String> errorMessages = new ArrayList<>();

  public ValidationException(List<String> errorMessages) {
    this.errorMessages.addAll(errorMessages);
  }

  public ValidationException(String errorMessage) {
    this.errorMessages.add(errorMessage);
  }

  public String getFirstErrorMessage() {
    if (!this.errorMessages.isEmpty()) {
      return this.errorMessages.get(0);
    }

    return "";
  }

  public String getAllErrorMessages() {
    StringBuilder sb = new StringBuilder(200);

    int i = 0;
    if (!this.errorMessages.isEmpty()) {
      i++;

      sb.append(this.errorMessages.get(0)).append(i < this.errorMessages.size() ? ", " : ".");
    }

    return sb.toString();
  }

  public String getLastErrorMessage() {
    if (!this.errorMessages.isEmpty()) {
      return errorMessages.get(errorMessages.size() - 1);
    }

    return "";
  }
}