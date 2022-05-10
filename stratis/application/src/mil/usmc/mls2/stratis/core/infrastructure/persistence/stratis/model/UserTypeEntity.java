package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "ACCT_TYPE_LU")
@EqualsAndHashCode(of = "userTypeId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserTypeEntity implements Serializable {

  @Id
  @Column(name = "acct_type_id")
  private Integer userTypeId;

  @Column(name = "ACCT_TYPE")
  private String type;

  @Column(name = "ACCT_TYPE_DESCN")
  private String description;

  @Column(name = "TRANS_TS")
  private OffsetDateTime timestamp;

  @Column(name = "MOD_BY_ID")
  private Integer updatedBy;
}
