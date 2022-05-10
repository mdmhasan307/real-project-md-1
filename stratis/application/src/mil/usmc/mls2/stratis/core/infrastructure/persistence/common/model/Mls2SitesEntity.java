package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model;

import lombok.*;
import mil.usmc.mls2.stratis.common.domain.model.HibernateTypes;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

/**
 * MLS2_SITES tables. used to get connection pool information for active sites.
 */

@Getter
@Entity
@Table(name = "MLS2_SITES")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@ToString
@EqualsAndHashCode(of = "siteName")
public class Mls2SitesEntity implements Serializable {

  @Id
  @Type(type = HibernateTypes.UUID_BINARY)
  @Column(name = "SITE_ID")
  private UUID siteId;

  @Column(name = "SITE_NAME")
  private String siteName;

  @Column(name = "DESCN")
  private String description;

  @Column(name = "PDB_NAME")
  private String pdbName;

  @Column(name = "PDB_USERNAME")
  private String pdbUsername;

  @Column(name = "PDB_PASSWORD")
  private String pdbPassword;

  @Column(name = "STATUS_ID")
  private Integer statusId;

  @Column(name = "FLYWAY_USERNAME")
  private String flywayUsername;

  @Column(name = "FLYWAY_PASSWORD")
  private String flywayPassword;

  @Column(name = "FLYWAY_SCHEMA")
  private String flywaySchema;

  @Column(name = "SORT_ORDER")
  private String sortOrder;
}


