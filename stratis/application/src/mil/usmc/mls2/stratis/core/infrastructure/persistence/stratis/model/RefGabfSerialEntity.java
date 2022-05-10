package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "REF_GABF_SERIAL")
@EqualsAndHashCode(of = "refGabfSerialId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefGabfSerialEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_gabf_serial_generator")
  @SequenceGenerator(name = "ref_gabf_serial_generator", sequenceName = "ref_gabf_serial_id_seq", allocationSize = 1)
  @Column(name = "REF_GABF_SERIAL_ID")
  private Integer refGabfSerialId;

  @Column(name = "REF_GABF_ID")
  private Integer refGabfId;

  @Column(name = "SERIAL_NUMBER")
  private String serialNumber;

  @Column(name = "LOT_CON_NUM")
  private String lotConNum;

  @Column(name = "CC")
  private String cc;

  @Column(name = "QTY")
  private Long quantity;
}