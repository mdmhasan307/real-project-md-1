package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "ERROR")
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error_id_generator")
  @SequenceGenerator(name = "error_id_generator", sequenceName = "error_id_seq", allocationSize = 1)
  @Column(name = "eid")
  private Integer id;

  @Column(name = "error_code")
  private String code;

  @Column(name = "error_description")
  private String description;

  @Column(name = "security_level")
  private Integer securityLevel;

  @Column(name = "error_label")
  private String label;

  @Column(name = "error_title")
  private String title;
}


