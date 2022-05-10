package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "shipping_route")
@EqualsAndHashCode(of = "routeId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShippingRouteEntity implements Serializable {

  /*
Note: allocationSize is required to be set to 1 for Hibernate to use an internal Oracle Sequence correctly, if that sequence is used outside of
hibernate as well.
 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipping_route_generator")
  @SequenceGenerator(name = "shipping_route_generator", sequenceName = "shipping_route_id_seq", allocationSize = 1)
  @Column(name = "route_id")
  private Integer routeId;

  @Column(name = "route_name")
  private String routeName;

  @Column(name = "description")
  private String description;
}
