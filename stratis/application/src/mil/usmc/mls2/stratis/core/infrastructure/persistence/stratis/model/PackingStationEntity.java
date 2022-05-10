package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Table(name = "packing_station")
@EqualsAndHashCode(of = "packingStationId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PackingStationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "packing_station_generator")
  @SequenceGenerator(name = "packing_station_generator", sequenceName = "packing_station_id_seq", allocationSize = 1)
  @Column(name = "PACKING_STATION_ID")
  private Integer packingStationId;

  @Column(name = "LEVELS")
  private Integer levels;

  @Column(name = "COLUMNS")
  private Integer columns;

  @Column(name = "TOTAL_ISSUES")
  private Integer totalIssues;

  @Column(name = "NUMBER_OF_SLOTS_IN_USE")
  private Integer numberOfSlotsInUse;

  @Column(name = "EQUIPMENT_NUMBER")
  private Integer equipmentNumber;
}
