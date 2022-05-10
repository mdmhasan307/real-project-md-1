package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "SITE_REMOTE_CONNECTIONS")
@EqualsAndHashCode(of = "connectionId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteRemoteConnectionEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site_remote_conn_generator")
  @SequenceGenerator(name = "site_remote_conn_generator", sequenceName = "SITE_REMOTE_CONN_ID_SEQ", allocationSize = 1)
  @Column(name = "CONNECTION_ID")
  private Integer connectionId;

  @Column(name = "HOST_NAME")
  private String hostName;

  @Column(name = "IP_ADDRESS")
  private String ipAddress;

  @Column(name = "PROTOCOL")
  private String protocol;

  @Column(name = "PORT")
  private String port;

  @Column(name = "USER_ID")
  private String userId;

  @Column(name = "PASSWORD")
  private String password;

  @Column(name = "HOST_DIR")
  private String hostDir;

  @Column(name = "LOCAL_DIR")
  private String localDir;

  @Column(name = "USE_CERTIFICATE_FLAG")
  private String userCertificateFlag;

  @Column(name = "CERTIFICATE_PATH")
  private String certificatePath;

  @Column(name = "DEF_MODE")
  private String defMode;
}