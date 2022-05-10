package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * STRATIS_USERS is a view, so this entity is Immutable
 */
@Getter
@Entity
@Table(name = "STRATIS_USERS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@ToString
@EqualsAndHashCode(of = "id")
public class CommonUserEntity implements Serializable {

  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "siteName", column = @Column(name = "SITE_NAME")),
      @AttributeOverride(name = "cacNumber", column = @Column(name = "CAC_NUMBER"))
  })
  private CommonUserCompositeKey id;

  @Column(name = "DESCN")
  private String description;

  @Column(name = "USERNAME")
  private String userName;

  @Column(name = "FIRST_NAME")
  private String firstName;

  @Column(name = "MIDDLE_NAME")
  private String middleName;

  @Column(name = "LAST_NAME")
  private String lastName;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "LOCKED")
  private String locked;

  @Column(name = "LAST_LOGIN")
  private OffsetDateTime lastLogin;

  @Column(name = "EFF_START_DT")
  private OffsetDateTime effStartDate;
}




