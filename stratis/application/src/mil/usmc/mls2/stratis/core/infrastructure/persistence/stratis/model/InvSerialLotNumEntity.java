package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "INV_SERIAL_LOT_NUM")
@EqualsAndHashCode(of = "invSerialLotNumId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InvSerialLotNumEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_serial_lot_num_generator")
  @SequenceGenerator(name = "inv_serial_lot_num_generator", sequenceName = "inv_serial_Lot_num_id_seq", allocationSize = 1)
  @Column(name = "INV_SERIAL_LOT_NUM_ID")
  private int invSerialLotNumId;

  @Column(name = "INVENTORY_ITEM_ID")
  private Integer inventoryItemId;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;

  @Column(name = "INV_DONE_FLAG")
  private String invDoneFlag;

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

  @Column(name = "QTY")
  private Integer qty;

  @Column(name = "LOCATION_ID")
  private Integer locationId;
}
