package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.integration.common.model.MessageType;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.KeywordUtils;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessagingCriteria;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessagingStatus;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.OutboundMessagingType;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused")
@CommonDbTransaction(propagation = Propagation.MANDATORY)
class OutboundMessagingEntityRepositoryCustomImpl implements OutboundMessagingEntityRepositoryCustom {

  private static final String ANY_WILDCARD = "*";

  @PersistenceContext(unitName = GlobalConstants.COMMON_PERSISTENCE_UNIT)
  private EntityManager entityManager;

  @Override
  public Stream<OutboundMessagingEntity> search(OutboundMessagingCriteria criteria) {
    val qOutboundMessaging = QOutboundMessagingEntity.outboundMessagingEntity;
    val query = selectFrom(qOutboundMessaging, entityManager);

    configurePredicate(query, createSearchPredicate(criteria, qOutboundMessaging));
    configureOrderBy(query, createSortPairs(criteria, qOutboundMessaging));
    configureLimits(query, criteria);

    return query.fetch().stream();
  }

  @Override
  public long count(OutboundMessagingCriteria criteria) {
    val qMls2Message = QOutboundMessagingEntity.outboundMessagingEntity;
    val query = selectFrom(qMls2Message, entityManager);

    configurePredicate(query, createSearchPredicate(criteria, qMls2Message));

    return query.fetchCount();
  }

  private Predicate createSearchPredicate(OutboundMessagingCriteria criteria, QOutboundMessagingEntity qMls2Message) {
    if (StringUtils.isNotBlank(criteria.keyword())) return createSearchPredicateByKeyword(criteria, qMls2Message);
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qMls2Message.id, criteria.id(), expressions);
    match(qMls2Message.payloadMessageId, criteria.payloadMessageId(), expressions);
    match(qMls2Message.type, criteria.type(), expressions);
    match(qMls2Message.status, criteria.status(), expressions);
    match(qMls2Message.destinationSystemId, criteria.destinationSystemId(), expressions);
    goe(qMls2Message.dateQueued, criteria.dateQueuedStart(), expressions);
    loe(qMls2Message.dateQueued, criteria.dateQueuedEnd(), expressions);
    goe(qMls2Message.dateProcessed, criteria.dateProcessedStart(), expressions);
    loe(qMls2Message.dateProcessed, criteria.dateProcessedEnd(), expressions);
    goe(qMls2Message.dateSent, criteria.dateSentStart(), expressions);
    loe(qMls2Message.dateSent, criteria.dateSentEnd(), expressions);
    return expressions.build();
  }

  private Predicate createSearchPredicateByKeyword(OutboundMessagingCriteria criteria, QOutboundMessagingEntity qMls2Message) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    val keywords = criteria.keyword();
    val keywordExpressions = BooleanExpressionBuilder.of(SearchTypeEnum.OR);
    Arrays.stream(keywords.split(",")).filter(x -> !x.equalsIgnoreCase(ANY_WILDCARD)).map(String::trim).forEach(keyword -> {
      matchIgnoreCase(qMls2Message.statusMessage, keyword, keywordExpressions);

      match(qMls2Message.status, findMessageStatusesMatchingKeyword(keyword).map(OutboundMessagingStatus::id), keywordExpressions);
      match(qMls2Message.type, findMessageTypesMatchingKeyword(keyword).map(OutboundMessagingType::id), keywordExpressions);
    });
    expressions.append(keywordExpressions.build());
    return expressions.build();
  }

  @SuppressWarnings({"Duplicates", "unused"})
  private List<SortOrderPair> createSortPairs(OutboundMessagingCriteria criteria, QOutboundMessagingEntity qMls2Message) {
    val sorts = generateSortOrderPairs(criteria.sortColumn(), criteria.sortOrder(), qMls2Message);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.property();
      // EntityPathBase<?> qEntity = qMls2Message;

      sort = sort.toBuilder().path(qMls2Message).property(sortColumn).build();
      filteredSorts.add(sort);
    }

    return filteredSorts;
  }

  private Stream<OutboundMessagingType> findMessageTypesMatchingKeyword(String keyword) {
    if (StringUtils.isBlank(keyword)) return Stream.empty();
    return Arrays
        .stream(MessageType.values())
        .filter(x -> KeywordUtils.matchesIgnoreCaseKeyword(x.getLabel(), keyword))
        .map(OutboundMessagingType::valueOf)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private Stream<OutboundMessagingStatus> findMessageStatusesMatchingKeyword(String keyword) {
    if (StringUtils.isBlank(keyword)) return Stream.empty();
    return Arrays.stream(OutboundMessagingStatus.values()).filter(x -> KeywordUtils.matchesIgnoreCaseKeyword(x.label(), keyword));
  }
}