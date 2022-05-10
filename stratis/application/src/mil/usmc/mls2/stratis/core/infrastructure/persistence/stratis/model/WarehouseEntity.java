package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "warehouse")
@EqualsAndHashCode(of = "warehouseId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD)
public class WarehouseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_generator")
  @SequenceGenerator(name = "warehouse_generator", sequenceName = "warehouse_id_seq", allocationSize = 1)
  @Column(name = "WAREHOUSE_ID")
  private int warehouseId;

  @Column(name = "BUILDING")
  private String building;

  @Column(name = "COMPLEX")
  private String complex;

  @Column(name = "SITE_ID")
  private Integer siteId;

  @Column(name = "DESCRIPTION")
  private String description;

  @Column(name = "LOCAL_DELIVERY_PREFIX")
  private String locationDeliveryPrefix;

  @Column(name = "LOCAL_DELIVERY_SUFFIX")
  private String locationDeliverySuffix;

  @Column(name = "CARRIER_NAME")
  private String carrierName;
}
