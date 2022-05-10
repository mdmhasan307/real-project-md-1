package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "location")
@EqualsAndHashCode(of = "locationId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD)
public class LocationEntity implements Serializable {

  @Id
  @Column(name = "LOCATION_ID")
  private int locationId;

  @Column(name = "AISLE")
  private String aisle;

  @Column(name = "SIDE")
  private String side;

  @Column(name = "BAY")
  private String bay;

  @Column(name = "LOC_LEVEL")
  private String locLevel;

  @Column(name = "AVAILABILITY_FLAG")
  private String availabilityFlag;

  @Column(name = "LOCATION_LABEL")
  private String locationLabel;

  @Column(name = "SLOT")
  private String slot;

  @ManyToOne
  @JoinColumn(name = "WAC_ID")
  private WacEntity wac;

  @Column(name = "LOCATION_HEADER_BIN_ID")
  private Integer locationHeaderBinId;

  @Column(name = "MECHANIZED_FLAG")
  private String mechanizedFlag;

  @Column(name = "LOC_CLASSIFICATION_ID")
  private Integer locClassificationId;

  @Column(name = "DIVIDER_INDEX")
  private Integer dividerIndex;

  @Column(name = "CUBE")
  private int cube;

  @Column(name = "WEIGHT")
  private int weight;

  @Column(name = "LAST_STOW_DATE")
  private OffsetDateTime lastStowDate;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;
}
