package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "PICK_SERIAL_LOT_NUM")
@EqualsAndHashCode(of = "pickSerialLotNum")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PickSerialLotNumEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pick_serial_lot_Num_generator")
  @SequenceGenerator(name = "pick_serial_lot_Num_generator", sequenceName = "pick_serial_lot_Num_seq", allocationSize = 1)
  @Column(name = "PICK_SERIAL_LOT_NUM")
  private Integer pickSerialLotNum;

  @ManyToOne
  @JoinColumn(name = "SERIAL_LOT_NUM_TRACK_ID")
  private SerialLotNumTrackEntity serialLotNumTrack;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;

  @Column(name = "PID")
  private Integer pid;

  @Column(name = "QTY")
  private Integer qty;

  @Column(name = "LOT_CON_NUM")
  private String lotConNum;

  @Column(name = "SERIAL_NUMBER")
  private String serialNumber;

  @Column(name = "SCN")
  private String scn;

  @Column(name = "LOCATION_ID")
  private Integer locationId;

  @Column(name = "EXPIRATION_DATE")
  private LocalDate expirationDate;
}
