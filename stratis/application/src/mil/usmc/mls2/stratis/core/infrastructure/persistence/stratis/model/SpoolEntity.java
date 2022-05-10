package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "SPOOL")
@EqualsAndHashCode(of = "spoolId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpoolEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spool_generator")
  @SequenceGenerator(name = "spool_generator", sequenceName = "spool_id_seq", allocationSize = 1)
  @Column(name = "SPOOL_ID")
  private Integer spoolId;

  @Column(name = "SPOOL_DEF_MODE")
  private String spoolDefMode;

  @Column(name = "PRIORITY")
  private String priority;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;

  @Column(name = "TRANSACTION_TYPE")
  private String transactionType;

  @Column(name = "REC_DATA")
  private String recData;

  @Lob
  @Column(name = "GCSSMC_XML")
  private String xml;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "SPOOL_BATCH_NUM")
  private Integer spoolBatchNum;

  @Column(name = "TIMESTAMP_SENT")
  private OffsetDateTime timestampSent;

  @Column(name = "RCN")
  private Integer rcn;

  @Column(name = "RECALL_FLAG")
  private String recallFlag;

  @Column(name = "RECALL_DATE")
  private OffsetDateTime recallDate;

  @Column(name = "CORRECTION_FLAG")
  private String correctionFlag;

  @Column(name = "NIIN_ID")
  private Integer niinId;

  @Column(name = "REF_SPOOL_ID")
  private Long refSpoolId;

  @Column(name = "MOD_BY_ID")
  private Integer modById;

  @Column(name = "DOCUMENT_NUMBER")
  private String documentNumber;

  @Column(name = "SUFFIX")
  private String suffix;

  @Column(name = "SID")
  private String sid;
}
