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
public class RefDasf {

  private Integer refDasfId;
  private String documentNumber;
  private String ric;
  private String fsc;
  private String niin;
  private String smic;
  private String unitOfIssue;
  private Double unitPrice;
  private Integer quantityInvoiced;
  private String supplementaryAddress;
  private String signalCode;
  private String fundCode;
  private String distributionCode;
  private String projectCode;
  private String priorityDesignatorCode;
  private String hostSystem;
  private Integer quantityDue;
  private OffsetDateTime timestamp;
}
