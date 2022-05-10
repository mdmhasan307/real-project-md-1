package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonUser {

  private String siteName;
  private String description;
  private String userName;
  private String firstName;
  private String middleName;
  private String lastName;
  private String status;
  private String locked;
  private OffsetDateTime lastLogin;
  private String cacNumber;
  private OffsetDateTime effStartDate;

  public boolean isAllowLogin() {
    return !"Y".equals(locked) && !"NON-ACTIVE".equals(status);
  }

  public String getDisableReason() {
    if ("Y".equals(locked)) {
      return "locked";
    }
    if ("NON-ACTIVE".equals(status)) {
      return "Inactive";
    }
    return null;
  }
}