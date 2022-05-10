package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteRemoteConnection {

  private Integer connectionId;
  private String hostName;
  private String ipAddress;
  private String protocol;
  private String port;
  private String userId;
  private String password;
  private String hostDir;
  private String localDir;
  private String userCertificateFlag;
  private String certificatePath;
  private String defMode;
}
