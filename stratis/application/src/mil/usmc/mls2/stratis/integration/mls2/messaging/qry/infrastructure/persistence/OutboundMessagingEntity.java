package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import mil.usmc.mls2.stratis.common.domain.model.HibernateTypes;
import mil.usmc.mls2.stratis.common.domain.model.TableNames;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@ToString
@Immutable
@Entity(name = "QryOutboundMessagingEntity")
@Table(name = TableNames.MLS2_OUTBOUND_MESSAGE)
@EqualsAndHashCode(of = "id")
@Accessors(fluent = true)
public class OutboundMessagingEntity {

  @Id
  @Type(type = HibernateTypes.UUID_BINARY)
  @Column(name = "id")
  private UUID id;

  @Type(type = HibernateTypes.UUID_BINARY)
  @Column(name = "destination_system_id")
  private UUID destinationSystemId;

  @Column(name = "type_id")
  private Integer type;

  @Column(name = "status_id")
  private Integer status;

  @Column(name = "status_message")
  private String statusMessage;

  @Type(type = HibernateTypes.UUID_BINARY)
  @Column(name = "related_inbound_msg_id")
  private UUID relatedInboundMessageId;

  @Column(name = "sent_count")
  private Integer sentCount;

  @Column(name = "date_queued")
  private OffsetDateTime dateQueued;

  @Column(name = "date_sent")
  private OffsetDateTime dateSent;

  @Column(name = "date_processed")
  private OffsetDateTime dateProcessed;

  @Column(name = "processed_count")
  private Integer processedCount;

  @Type(type = HibernateTypes.UUID_BINARY)
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