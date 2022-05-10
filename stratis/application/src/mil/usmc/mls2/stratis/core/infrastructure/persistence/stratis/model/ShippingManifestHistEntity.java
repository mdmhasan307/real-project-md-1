package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "shipping_manifest_hist")
@EqualsAndHashCode(of = "shippingManifestId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShippingManifestHistEntity implements Serializable {

  //FUTURE INNOV Backlog this isn't really an ID field as the SHIPPING_MANIFEST_ID is not a Primary Key for this table....
  @Id
  @Column(name = "SHIPPING_MANIFEST_ID")
  private Integer shippingManifestId;

  @ManyToOne
  @JoinColumn(name = "CUSTOMER_ID")
  private CustomerEntity customer;

  @Column(name = "LEAD_TCN")
  private String leadTcn;

  @Column(name = "MANIFEST")
  private String manifest;

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

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "MANIFEST_PRINT_DATE")
  private OffsetDateTime manifestPrintDate;

  @Column(name = "DRIVER")
  private String driver;

  @Column(name = "MODE_OF_SHIPMENT")
  private String modeOfShipment;

  @Column(name = "USER_ID")
  private Integer userId;

  @Column(name = "TRANSACTION")
  private String transaction;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;

  @Column(name = "EQUIPMENT_NUMBER")
  private Integer equipmentNumber;

  @Column(name = "FLOOR_LOCATION")
  private String floorLocation;

  @Column(name = "BUILDING")
  private Integer building;

  @Column(name = "MANIFESTED_BY")
  private Integer manifestedBy;
}
