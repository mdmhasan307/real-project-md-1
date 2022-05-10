package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "floor_location")
@EqualsAndHashCode(of = "floorLocationId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD)
public class FloorLocationEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "floor_location_generator")
  @SequenceGenerator(name = "floor_location_generator", sequenceName = "floor_location_id_seq", allocationSize = 1)
  @Column(name = "FLOOR_LOCATION_ID")
  private int floorLocationId;

  @Column(name = "FLOOR_LOCATION")
  private String floorLocation;

  @ManyToOne
  @JoinColumn(name = "ROUTE_ID")
  private RouteEntity route;

  @Column(name = "IN_USE")
  private String inUse;

  @ManyToOne
  @JoinColumn(name = "WAREHOUSE_ID")
  private WarehouseEntity warehouse;
}
