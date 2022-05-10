package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "USERS")
@EqualsAndHashCode(of = "userId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserEntity implements Serializable {

  @Id
  @Column(name = "user_id")
  private String userId;

  @Column(name = "username")
  private String username;

  @Column(name = "cac_number")
  private String cacNumber;
}
