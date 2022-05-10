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
public class SiteInterface {

  private Integer interfaceId;
  private String interfaceName;
  private String fileName;
  private String filePath;
  private OffsetDateTime lastFtpDate;
  private OffsetDateTime lastImpExpDate;
  private Integer connectionId;
  private String status;
}
