package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Data
@Entity
@Immutable
@Table(name = "V_IMPORTS_FILE_LOG_GABF_RPT")
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ImportFileLogGabfRptViewEntity {

  @Id
  @Column(name = "ID")
  private Integer id;

  @Column(name = "DATALOAD_STATUS")
  private String status;

  @Column(name = "INTERFACE_NAME")
  private String interfaceName;

  @Column(name = "DESCRIPTION")
  private String description;

  @Column(name = "LINE_NO")
  private Integer lineNumber;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;
}
