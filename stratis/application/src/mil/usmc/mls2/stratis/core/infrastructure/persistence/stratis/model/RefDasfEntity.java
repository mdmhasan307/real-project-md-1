package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "REF_DASF")
@EqualsAndHashCode(of = "refDasfId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefDasfEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_dasf_generator")
  @SequenceGenerator(name = "ref_dasf_generator", sequenceName = "ref_dasf_id_seq", allocationSize = 1)
  @Column(name = "REF_DASF_ID")
  private Integer refDasfId;

  @Column(name = "DOCUMENT_NUMBER")
  private String documentNumber;

  @Column(name = "ROUTING_IDENTIFIER_CODE")
  private String ric;

  @Column(name = "RECORD_FSC")
  private String fsc;

  @Column(name = "RECORD_NIIN")
  private String niin;

  @Column(name = "RECORD_SMIC")
  private String smic;

  @Column(name = "UNIT_OF_ISSUE")
  private String unitOfIssue;

  @Column(name = "UNIT_PRICE")
  private Double unitPrice;

  @Column(name = "QUANTITY_INVOICED")
  private Integer quantityInvoiced;

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

  @Column(name = "HOST_SYSTEM")
  private String hostSystem;

  @Column(name = "QUANTITY_DUE")
  private Integer quantityDue;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;
}
