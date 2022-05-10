package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model;

import lombok.*;
import mil.usmc.mls2.stratis.common.domain.model.HibernateTypes;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(name = "SETTINGS_SYSTEM")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SettingsSystemEntity implements Serializable {

  @Id
  @Type(type = HibernateTypes.UUID_BINARY)
  @Column(name = "ID")
  private UUID id;

  @Type(type = HibernateTypes.UUID_BINARY)
  @Column(name = "SYSTEM_UUID")
  private UUID systemUuid;
}