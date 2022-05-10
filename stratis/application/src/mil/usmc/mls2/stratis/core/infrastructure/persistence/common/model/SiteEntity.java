package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * STRATIS_SITES is a view, so this entity is Immutable
 */

@Getter
@Entity
@Table(name = "STRATIS_SITES")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@ToString
@EqualsAndHashCode(of = "siteName")
public class SiteEntity implements Serializable {

  @Id
  @Column(name = "SITE_NAME")
  private String siteName;

  @Column(name = "DESCN")
  private String description;

  @Column(name = "db_status")
  private String status;

  @Column(name = "local_time")
  private String localTime;

  @Column(name = "status_id")
  private Integer statusId;
}




