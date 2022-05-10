package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "ERROR_QUEUE")
@EqualsAndHashCode(of = "errorQueueId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorQueueEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error_queue_id_generator")
  @SequenceGenerator(name = "error_queue_id_generator", sequenceName = "error_queue_id_seq", allocationSize = 1)
  @Column(name = "ERROR_QUEUE_ID")
  private int errorQueueId;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "EID")
  private int eid;

  @Column(name = "KEY_TYPE")
  private String keyType;

  @Column(name = "KEY_NUM")
  private String keyNum;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "NOTES")
  private String notes;
}
