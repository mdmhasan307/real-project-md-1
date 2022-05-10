package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "shipping_manifest")
@EqualsAndHashCode(of = "shippingManifestId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShippingManifestEntity implements Serializable {

  /*
Note: allocationSize is required to be set to 1 for Hibernate to use an internal Oracle Sequence correctly, if that sequence is used outside of
hibernate as well.
 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipping_manifest_generator")
  @SequenceGenerator(name = "shipping_manifest_generator", sequenceName = "shipping_manifest_id_seq", allocationSize = 1)
  @Column(name = "shipping_manifest_id")
  private Integer shippingManifestId;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "EQUIPMENT_NUMBER")
  private Integer equipmentNumber;

  @ManyToOne
  @JoinColumn(name = "CUSTOMER_ID")
  private CustomerEntity customer;

  @Column(name = "LEAD_TCN")
  private String leadTcn;

  @Column(name = "MANIFEST")
  private String manifest;

  @ManyToOne
  @JoinColumn(name = "FLOOR_LOCATION_ID")
  private FloorLocationEntity floorLocation;

  @Column(name = "MANIFEST_DATE")
  private OffsetDateTime manifestDate;

  @Column(name = "PICKED_UP_FLAG")
  private String pickedUpFlag;

  @Column(name = "DELIVERED_FLAG")
  private String deliveredFlag;

  @Column(name = "PICKED_UP_DATE")
  private OffsetDateTime pickedUpDate;

  @Column(name = "DELIVERED_DATE")
  private OffsetDateTime deliveredDate;

  @Column(name = "MANIFEST_PRINT_DATE")
  private OffsetDateTime manifestPrintDate;

  @Column(name = "DRIVER")
  private String driver;

  @Column(name = "MODE_OF_SHIPMENT")
  private String modeOfShipment;

  @Column(name = "MANIFESTED_BY")
  private Integer manifestedBy;
}
