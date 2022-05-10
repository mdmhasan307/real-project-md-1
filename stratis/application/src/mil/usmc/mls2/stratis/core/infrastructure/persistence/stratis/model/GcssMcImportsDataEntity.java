package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "GCSSMC_IMPORTS_DATA")
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcssMcImportsDataEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gcssmc_imports_generator")
  @SequenceGenerator(name = "gcssmc_imports_generator", sequenceName = "gcssmc_imports_data_id_seq", allocationSize = 1)
  @Column(name = "GCSSMC_IMPORTS_DATA_ID")
  private Integer id;

  @Column(name = "INTERFACE_NAME", length = 20)
  private String interfaceName;

  @Column(name = "STATUS", length = 20)
  private String status;

  @Lob
  @Column(name = "REC_DATA")
  private String xml;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;
}