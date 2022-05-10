package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "SERIAL_LOT_NUM_TRACK")
@EqualsAndHashCode(of = "serialLotNumTrackId")
@Builder
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SerialLotNumTrackEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "serial_lot_num_track_generator")
  @SequenceGenerator(name = "serial_lot_num_track_generator", sequenceName = "serial_lot_num_track_seq", allocationSize = 1)
  @Column(name = "SERIAL_LOT_NUM_TRACK_ID")
  private int serialLotNumTrackId;

  @Column(name = "NIIN_ID")
  private Integer niinId;

  @Column(name = "SERIAL_NUMBER")
  private String serialNumber;

  @Column(name = "LOT_CON_NUM")
  private String lotConNum;

  @Column(name = "CC")
  private String cc;

  @Column(name = "EXPIRATION_DATE")
  private LocalDate expirationDate;

  @Column(name = "ISSUED_FLAG")
  private String issuedFlag;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;

  @Column(name = "QTY")
  private Integer qty;

  @Column(name = "LOCATION_ID")
  private Integer locationId;

  @Column(name = "IUID")
  private String iuid;

  @Column(name = "RECALL_FLAG")
  private String recallFlag;

  @Column(name = "RECALL_DATE")
  private OffsetDateTime recallDate;
}
