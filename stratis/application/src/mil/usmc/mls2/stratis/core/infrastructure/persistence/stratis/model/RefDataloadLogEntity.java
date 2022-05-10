package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "REF_DATALOAD_LOG")
@EqualsAndHashCode(of = "refDataloadLogId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefDataloadLogEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_dataload_log_generator")
  @SequenceGenerator(name = "ref_dataload_log_generator", sequenceName = "ref_dataload_log_id_seq", allocationSize = 1)
  @Column(name = "REF_DATALOAD_LOG_ID")
  private Integer refDataloadLogId;

  @Column(name = "INTERFACE_NAME")
  private String interfaceName;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "CREATED_BY")
  private String createdBy;

  @Lob
  @Column(name = "DATA_ROW")
  private String dataRow;

  @Column(name = "DESCRIPTION")
  private String description;

  @Column(name = "LINE_NO")
  private Integer lineNumber;

  @Column(name = "GCSSMC_IMPORTS_DATA_ID")
  private Integer gcssMcImportsDataId;
}