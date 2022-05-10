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
public class GcssMcImportsData {

  private Integer id;
  private String interfaceName;
  private String status;
  private String xml;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;

  public void processingComplete() {
    status = "COMPLETED";
    modifiedDate = OffsetDateTime.now();
  }

  public void processingFailed() {
    status = "FAILED";
    modifiedDate = OffsetDateTime.now();
  }
}