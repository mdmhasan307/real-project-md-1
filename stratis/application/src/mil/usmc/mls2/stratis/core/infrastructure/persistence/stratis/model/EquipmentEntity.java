package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "Equip")
@EqualsAndHashCode(of = "equipmentNumber")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EquipmentEntity implements Serializable {

  @Id
  @Column(name = "EQUIPMENT_NUMBER")
  private int equipmentNumber;

  @Column(name = "COM_PORT_PRINTER_ID")
  private Integer comPortPrinterId;

  @Column(name = "COM_PORT_EQUIPMENT_ID")
  private Integer comPortEquipmentId;

  @Column(name = "NAME")
  private String name;

  @Column(name = "DESCRIPTION")
  private String description;

  @Column(name = "WAREHOUSE_ID")
  private Integer warehouseId;

  @Column(name = "CURRENT_USER_ID")
  private Integer currentUserId;

  @Column(name = "SHIPPING_AREA")
  private String shippingArea;

  @Column(name = "PACKING_GROUP")
  private String packingGroup;

  @Column(name = "HAS_CUBISCAN")
  private String hasCubiscan;

  @Column(name = "PRINTER_NAME")
  private String printerName;

  @ManyToOne
  @JoinColumn(name = "WAC_ID")
  private WacEntity wac;
}
