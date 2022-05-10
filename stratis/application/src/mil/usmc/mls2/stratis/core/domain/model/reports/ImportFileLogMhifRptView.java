package mil.usmc.mls2.stratis.core.domain.model.reports;

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
public class ImportFileLogMhifRptView {

  private Integer id;
  private String status;
  private String interfaceName;
  private String description;
  private Integer lineNumber;
  private OffsetDateTime createdDate;
}
