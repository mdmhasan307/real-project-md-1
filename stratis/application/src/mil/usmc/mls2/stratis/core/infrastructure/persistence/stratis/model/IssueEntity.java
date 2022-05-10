package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "issue")
@EqualsAndHashCode(of = "scn")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IssueEntity implements Serializable {

  @Id
  @Column(name = "SCN")
  private String scn;

  @Column(name = "DOCUMENT_ID")
  private String documentId;

  @Column(name = "DOCUMENT_NUMBER")
  private String documentNumber;

  @Column(name = "QTY")
  private Integer qty;

  @Column(name = "ISSUE_TYPE")
  private String issueType;

  @Column(name = "ISSUE_PRIORITY_DESIGNATOR")
  private String issuePriorityDesignator;

  @Column(name = "ISSUE_PRIORITY_GROUP")
  private String issuePriorityGroup;

  @ManyToOne
  @JoinColumn(name = "CUSTOMER_ID")
  private CustomerEntity customer;

  @Column(name = "NIIN_ID")
  private Integer niinId;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "PACKING_CONSOLIDATION_ID")
  private Integer packingConsolidationId;

  @Column(name = "SUFFIX")
  private String suffix;

  @Column(name = "RDD")
  private String rdd;

  @Column(name = "SUPPLEMENTARY_ADDRESS")
  private String supplementaryAddress;

  @Column(name = "FUND_CODE")
  private String fundCode;

  @Column(name = "MEDIA_AND_STATUS_CODE")
  private String mediaAndStatusCode;

  @Column(name = "ROUTING_ID_FROM")
  private String routingIdFrom;

  @Column(name = "SIGNAL_CODE")
  private String signalCode;

  @Column(name = "DISTRIBUTION_CODE")
  private String distributionCode;

  @Column(name = "PROJECT_CODE")
  private String projectCode;

  @Column(name = "ADVICE_CODE")
  private String adviceCode;

  @Column(name = "ERO_NUMBER")
  private String eroNumber;

  @Column(name = "CC")
  private String cc;

  @Column(name = "DATE_BACK_ORDERED")
  private String dateBackOrdered;

  @Column(name = "COST_JON")
  private String costJon;

  @Column(name = "QTY_ISSUED")
  private Integer qtyIssued;

  @Column(name = "RCN")
  private String rcn;

  @Column(name = "REL_TO_SHIPPING_BY")
  private Integer relToShippingBy;

  @Column(name = "REL_TO_SHIPPING_DATE")
  private OffsetDateTime relToShippingDate;

  @Column(name = "DISPOSAL_CODE")
  private String disposalCode;

  @Column(name = "RON")
  private String ron;

  @Column(name = "DEMIL_CODE")
  private String demilCode;

  @Column(name = "SECURITY_MARK_CLASS")
  private String securityMarkClass;
}
