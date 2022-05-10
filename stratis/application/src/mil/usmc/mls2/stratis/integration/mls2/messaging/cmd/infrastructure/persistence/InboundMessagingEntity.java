package mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.infrastructure.persistence;

import lombok.*;
import lombok.experimental.Accessors;
import mil.usmc.mls2.stratis.common.domain.model.HibernateTypes;
import mil.usmc.mls2.stratis.common.domain.model.TableNames;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * MLS2 Inbound Message Entity
 * <p>
 * payload: JSON representation of the Messaging
 * payloadMessageId: Unique identifier used for the payload (MLS2 Message)
 * payloadClass: Messaging Class
 * payloadClassVersion: Messaging Class Version
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CmdInboundMessagingEntity")
@DynamicUpdate
@Table(name = TableNames.MLS2_INBOUND_MESSAGE)
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
class InboundMessagingEntity implements Serializable {

  @Id
  @Type(type = HibernateTypes.UUID_BINARY)
  @Column(name = "id")
  private UUID id;

  @Column(name = "source_system_id")
  private UUID sourceSystemId;

  @Column(name = "type_id")
  private Integer type;

  @Column(name = "status_id")
  private Integer status;

  @Column(name = "status_message")
  private String statusMessage;

  @Column(name = "date_received")
  private OffsetDateTime dateReceived;

  @Column(name = "date_processed")
  private OffsetDateTime dateProcessed;

  @Column(name = "received_count")
  private Integer receivedCount;

  @Column(name = "processed_count")
  private Integer processedCount;

  @Column(name = "payload_message_id")
  private UUID payloadMessageId;

  @Column(name = "payload_class")
  private String payloadClass;

  @Column(name = "payload_class_version")
  private Long payloadClassVersion;

  @Column(name = "site_identifier")
  private String siteIdentifier;

  @Lob
  @Basic(fetch = FetchType.LAZY)
  @Column(name = "payload")
  private String payload;
}