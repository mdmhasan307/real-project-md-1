package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "REF_GBOF")
@EqualsAndHashCode(of = "refGbofId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefGbofEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_gbof_generator")
  @SequenceGenerator(name = "ref_gbof_generator", sequenceName = "ref_gbof_id_seq", allocationSize = 1)
  @Column(name = "REF_GBOF_ID")
  private Integer refGbofId;

  @Column(name = "PRIME_NSN")
  private String primeNsn;

  @Column(name = "ACTIVITY_ADDRESS_CODE")
  private String activityAddressCode;

  @Column(name = "PROCESS_SEQUENCE_CODE")
  private String processSequenceCode;

  @Column(name = "DATE_BACK_ORDERED")
  private String dateBackOrdered;

  @Column(name = "DOCUMENT_IDENTIFIER_CODE")
  private String documentIdentifierCode;

  @Column(name = "ROUTING_IDENTIFIER_CODE")
  private String routingIdentifierCode;

  @Column(name = "MEDIA_AND_STATUS_CODE")
  private String mediaAndStatusCode;

  @Column(name = "NATIONAL_STOCK_NUMBER")
  private String nationalStockNumber;

  @Column(name = "UNIT_OF_ISSUE")
  private String unitOfIssue;

  @Column(name = "TRANSACTION_QUANTITY")
  private String transactionQuantity;

  @Column(name = "DOCUMENT_NUMBER")
  private String documentNumber;

  @Column(name = "DEMAND_CODE")
  private String demandCode;

  @Column(name = "SUPPLEMENTARY_ADDRESS")
  private String supplementaryAddress;

  @Column(name = "SIGNAL_CODE")
  private String signalCode;

  @Column(name = "FUND_CODE")
  private String fundCode;

  @Column(name = "DISTRIBUTION_CODE")
  private String distributionCode;

  @Column(name = "PROJECT_CODE")
  private String projectCode;

  @Column(name = "PRIORITY_DESIGNATOR_CODE")
  private String priorityDesignatorCode;

  @Column(name = "REQUIRED_DELIVERY_DATE")
  private String requiredDeliveryDate;

  @Column(name = "ADVICE_CODE")
  private String adviceCode;

  @Column(name = "CONTROL_CODE")
  private String controlCode;

  @Column(name = "PASS_CONTROL_CODE")
  private String passControlCode;

  @Column(name = "COST_CODE")
  private String costCode;

  @Column(name = "TYPE_UNIT_CODE")
  private String typeUnitCode;

  @Column(name = "CROSS_SUPPORT_GROUP_CODE")
  private String crossSupportGroupCode;

  @Column(name = "LOADED_UNIT_MIMMS_CODE")
  private String loadedUnitMimmsCode;

  @Column(name = "LOAD_DATE_INDICATOR")
  private String loadDateIndicator;

  @Column(name = "TRANSACTION_DATE")
  private String transactionDate;

  @Column(name = "NONSYSTEM_ITEM_INDICATOR")
  private String nonsystemItemIndicator;

  @Column(name = "ORIGINAL_DOC_ID_CODE")
  private String originalDocIdCode;

  @Column(name = "TRANSACTION_ROUTING_CODE")
  private String transactionRoutingCode;

  @Column(name = "EXTENSION_QUANTITY")
  private String extensionQuantity;

  @Column(name = "MIMMS_MANAGED_CODE")
  private String mimmsManagedCode;

  @Column(name = "COMBAT_ESSENTIALITY_CODE")
  private String combatEssentialityCode;

  @Column(name = "EXTENSION_UNIT_PRICE")
  private String extensionUnitPrice;

  @Column(name = "EXTENSION_STATUS_CODE")
  private String extensionStatusCode;

  @Column(name = "STORES_ACCOUNT_CODE")
  private String storesAccountCode;

  @Column(name = "MATERIEL_IDENTIFICATION_CODE")
  private String materielIdentificationCode;

  @Column(name = "CONTROLLED_ITEM_CODE")
  private String controlledItemCode;

  @Column(name = "TOTAL_BACK_ORDER_INDICATOR")
  private String totalBackOrderIndicator;

  @Column(name = "PENDING_FUNDS_FIELD")
  private String pendingFundsField;

  @Column(name = "ANALYST_CODE")
  private String analystCode;

  @Column(name = "PDRI")
  private String pdri;

  @Column(name = "EXCEPTION_CODE")
  private String exceptionCode;

  @Column(name = "LAST_TRANSACTION_CODE")
  private String lastTransactionCode;

  @Column(name = "MRP_SERVICE_CODE")
  private String mrpServiceCode;

  @Column(name = "SMIC")
  private String smic;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;
}