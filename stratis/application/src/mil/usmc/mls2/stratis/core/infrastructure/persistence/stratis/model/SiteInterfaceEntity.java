package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "SITE_INTERFACES")
@EqualsAndHashCode(of = "interfaceId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteInterfaceEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site_interface_generator")
  @SequenceGenerator(name = "site_interface_generator", sequenceName = "site_interface_id_seq", allocationSize = 1)
  @Column(name = "INTERFACE_ID")
  private Integer interfaceId;

  @Column(name = "INTERFACE_NAME")
  private String interfaceName;

  @Column(name = "FILE_NAME")
  private String fileName;

  @Column(name = "FILE_PATH")
  private String filePath;

  @Column(name = "LAST_FTP_DATE")
  private OffsetDateTime lastFtpDate;

  @Column(name = "LAST_IMPEXP_DATE")
  private OffsetDateTime lastImpExpDate;

  @Column(name = "CONNECTION_ID")
  private Integer connectionId;

  @Column(name = "STATUS")
  private String status;
}
