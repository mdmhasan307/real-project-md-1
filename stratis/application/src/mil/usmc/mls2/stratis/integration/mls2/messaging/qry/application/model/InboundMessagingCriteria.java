package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import mil.usmc.mls2.stratis.common.domain.model.SearchCriteria;
import mil.usmc.mls2.stratis.common.domain.model.SearchType;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class InboundMessagingCriteria implements SearchCriteria {

  String keyword;
  long offset;
  int limit;

  @Builder.Default
  SearchType searchType = SearchType.FULL;

  @Builder.Default
  String sortColumn = "lastName";

  @Builder.Default
  SortOrder sortOrder = SortOrder.ASC;

  UUID id;
  UUID payloadMessageId;
  InboundMessagingType type;
  InboundMessagingStatus status;
  UUID relatedInboundMessageId;
  UUID sourceSystemId;
  UUID destinationSystemId;
  UUID routedSystemId;

  OffsetDateTime dateReceivedStart;
  OffsetDateTime dateReceivedEnd;
  String siteIdentifier;
}