package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "REF_MATS")
@EqualsAndHashCode(of = "refMatsId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefMatsEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_mats_generator")
  @SequenceGenerator(name = "ref_mats_generator", sequenceName = "ref_mats_id_seq", allocationSize = 1)
  @Column(name = "REF_MATS_ID")
  private Integer refMatsId;

  @Column(name = "DOCUMENT_IDENTIFIER")
  private String documentIdentifier;

  @Column(name = "ROUTING_IDENTIFIER_FROM")
  private String routingIdentifierFrom;

  @Column(name = "MEDIA_AND_STATUS_CODE")
  private String mediaAndStatusCode;

  @Column(name = "FSC")
  private String fsc;

  @Column(name = "NIIN")
  private String niin;

  @Column(name = "UNIT_OF_ISSUE")
  private String unitOfIssue;

  @Column(name = "TRANSACTION_QUANTITY")
  private Integer transactionQuantity;

  @Column(name = "DOCUMENT_NUMBER")
  private String documentNumber;

  @Column(name = "DEMAND_SUFFIX_CODE")
  private String demandSuffixCode;

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

  @Column(name = "ISSUE_PRIORITY_DESIGNATOR")
  private String issuePriorityDesignator;

  @Column(name = "REQUIRED_DELIVERY_DATE")
  private String requiredDeliveryDate;

  @Column(name = "ADVICE_CODE")
  private String adviceCode;

  @Column(name = "ROUTING_IDENTIFIER_TO")
  private String routingIdentifierTo;

  @Column(name = "CONDITION_CODE")
  private String conditionCode;

  @Column(name = "SHIP_TO_ADDRESS_1")
  private String shipToAddress1;

  @Column(name = "SHIP_TO_ADDRESS_2")
  private String shipToAddress2;

  @Column(name = "SHIP_TO_ADDRESS_3")
  private String shipToAddress3;

  @Column(name = "SHIP_TO_ADDRESS_4")
  private String shipToAddress4;

  @Column(name = "ERO_NUMBER")
  private String eroNumber;

  @Column(name = "UNIT_PRICE")
  private Double unitPrice;

  @Column(name = "SHIP_TO_ADDRESS_CITY")
  private String shipToAddressCity;

  @Column(name = "SHIP_TO_ADDRESS_STATE")
  private String shipToAddressState;

  @Column(name = "SHIP_TO_ADDRESS_ZIP_CODE")
  private String shipToAddressZipCode;

  @Column(name = "SHIP_TO_ADDRESS_COUNTRY")
  private String shipToAddressCountry;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;

  @Column(name = "DISPOSAL_CODE")
  private String disposalCode;

  @Column(name = "RON")
  private String ron;

  @Column(name = "DEMIL_CODE")
  private String demilCode;
}