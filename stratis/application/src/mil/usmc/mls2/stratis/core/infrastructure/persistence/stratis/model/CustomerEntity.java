package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "customer")
@EqualsAndHashCode(of = "customerId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CustomerEntity implements Serializable {

  /*
Note: allocationSize is required to be set to 1 for Hibernate to use an internal Oracle Sequence correctly, if that sequence is used outside of
hibernate as well.
 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_generator")
  @SequenceGenerator(name = "customer_generator", sequenceName = "customer_id_seq", allocationSize = 1)
  @Column(name = "customer_id")
  private Integer customerId;

  @Column(name = "ADDRESS_1")
  private String address1;

  @Column(name = "CITY")
  private String city;

  @Column(name = "STATE")
  private String state;

  @Column(name = "ZIP_CODE")
  private String zipCode;

  @Column(name = "AAC")
  private String aac;

  @Column(name = "ADDRESS_2")
  private String address2;

  @ManyToOne
  @JoinColumn(name = "ROUTE_ID")
  private RouteEntity route;

  @Column(name = "RESTRICT_SHIP")
  private String restrictShip;

  @Column(name = "NAME")
  private String name;

  @Column(name = "FLOOR_LOCATION_ID")
  private Integer floorLocationId;

  @Column(name = "SUPPORTED")
  private String supported;

  @ManyToOne
  @JoinColumn(name = "SHIPPING_ROUTE_ID")
  private ShippingRouteEntity shippingRoute;

  @Column(name = "SECURITY_MARK_CLASS")
  private String securityMarkClass;
}
