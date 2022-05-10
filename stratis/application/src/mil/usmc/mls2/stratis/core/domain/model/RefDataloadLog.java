package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefDataloadLog {

  private Integer refDataloadLogId;
  private String interfaceName;
  private OffsetDateTime createdDate;
  private String createdBy;
  private String dataRow;
  private String description;
  private Integer lineNumber;
  private Integer gcssMcImportsDataId;
}