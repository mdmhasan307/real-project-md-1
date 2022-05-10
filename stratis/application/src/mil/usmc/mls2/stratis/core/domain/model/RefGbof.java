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
public class RefGbof {

  private Integer refGbofId;
  private String primeNsn;
  private String activityAddressCode;
  private String processSequenceCode;
  private String dateBackOrdered;
  private String documentIdentifierCode;
  private String routingIdentifierCode;
  private String mediaAndStatusCode;
  private String nationalStockNumber;
  private String unitOfIssue;
  private String transactionQuantity;
  private String documentNumber;
  private String demandCode;
  private String supplementaryAddress;
  private String signalCode;
  private String fundCode;
  private String distributionCode;
  private String projectCode;
  private String priorityDesignatorCode;
  private String requiredDeliveryDate;
  private String adviceCode;
  private String controlCode;
  private String passControlCode;
  private String costCode;
  private String typeUnitCode;
  private String crossSupportGroupCode;
  private String loadedUnitMimmsCode;
  private String loadDateIndicator;
  private String transactionDate;
  private String nonsystemItemIndicator;
  private String originalDocIdCode;
  private String transactionRoutingCode;
  private String extensionQuantity;
  private String mimmsManagedCode;
  private String combatEssentialityCode;
  private String extensionUnitPrice;
  private String extensionStatusCode;
  private String storesAccountCode;
  private String materielIdentificationCode;
  private String controlledItemCode;
  private String totalBackOrderIndicator;
  private String pendingFundsField;
  private String analystCode;
  private String pdri;
  private String exceptionCode;
  private String lastTransactionCode;
  private String mrpServiceCode;
  private String smic;
  private OffsetDateTime timestamp;
}