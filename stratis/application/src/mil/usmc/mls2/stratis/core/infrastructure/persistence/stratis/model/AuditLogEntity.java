package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogCategoryEnum;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogSourceEnum;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "audit_log")
@EqualsAndHashCode(of = "auditLogId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuditLogEntity implements Serializable {

  /*
  Note: allocationSize is required to be set to 1 for Hibernate to use an internal Oracle Sequence correctly, if that sequence is used outside of
  hibernate as well.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_log_generator")
  @SequenceGenerator(name = "audit_log_generator", sequenceName = "audit_log_id_seq", allocationSize = 1)
  @Column(name = "AUDIT_LOG_ID")
  private Integer auditLogId;

  @Column(name = "SOURCE")
  private AuditLogSourceEnum source;

  @Column(name = "TYPE")
  private AuditLogTypeEnum type;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;

  @Column(name = "CATEGORY")
  private AuditLogCategoryEnum category;

  @Column(name = "EVENT")
  private Integer event;

  @Column(name = "USER_ID")
  private Integer userId;

  @Column(name = "EQUIPMENT_NUMBER")
  private Integer equipmentNumber;

  @Column(name = "DESCRIPTION")
  private String description;
}
