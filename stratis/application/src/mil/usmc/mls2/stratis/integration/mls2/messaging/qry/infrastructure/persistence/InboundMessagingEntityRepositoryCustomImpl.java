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
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingCriteria;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingStatus;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.InboundMessagingType;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Stream;

import static mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused")
@CommonDbTransaction(propagation = Propagation.MANDATORY)
class InboundMessagingEntityRepositoryCustomImpl implements InboundMessagingEntityRepositoryCustom {

  private static final String ANY_WILDCARD = "*";

  @PersistenceContext(unitName = GlobalConstants.COMMON_PERSISTENCE_UNIT)
  private EntityManager entityManager;

  @Override
  public Stream<InboundMessagingEntity> findByMessageStatus(InboundMessagingStatus status) {
    val qMls2Message = QInboundMessagingEntity.inboundMessagingEntity;
    val query = selectFrom(qMls2Message, entityManager);
    query.where(qMls2Message.status.eq(status.id()));
    return query.fetch().stream();
  }

  @Override
  public Optional<InboundMessagingEntity> findByPayloadMessageId(UUID payloadMessageId) {
    val qMls2Message = QInboundMessagingEntity.inboundMessagingEntity;
    val query = selectFrom(qMls2Message, entityManager);
    query.where(qMls2Message.payloadMessageId.eq(payloadMessageId));
    return Optional.ofNullable(query.fetchOne());
  }

  @Override
  public Stream<InboundMessagingEntity> search(InboundMessagingCriteria criteria) {
    val qInboundMessaging = QInboundMessagingEntity.inboundMessagingEntity;
    val query = selectFrom(qInboundMessaging, entityManager);

    configurePredicate(query, createSearchPredicate(criteria, qInboundMessaging));
    configureOrderBy(query, createSortPairs(criteria, qInboundMessaging));
    configureLimits(query, criteria);

    return query.fetch().stream();
  }

  @Override
  public long count(InboundMessagingCriteria criteria) {
    val qMls2Message = QInboundMessagingEntity.inboundMessagingEntity;
    val query = selectFrom(qMls2Message, entityManager);

    configurePredicate(query, createSearchPredicate(criteria, qMls2Message));

    return query.fetchCount();
  }

  private Predicate createSearchPredicate(InboundMessagingCriteria criteria, QInboundMessagingEntity qMls2Message) {
    if (StringUtils.isNotBlank(criteria.keyword())) return createSearchPredicateByKeyword(criteria, qMls2Message);
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qMls2Message.id, criteria.id(), expressions);
    match(qMls2Message.payloadMessageId, criteria.payloadMessageId(), expressions);
    match(qMls2Message.type, criteria.type(), expressions);
    match(qMls2Message.status, criteria.status(), expressions);
    match(qMls2Message.sourceSystemId, criteria.sourceSystemId(), expressions);
    goe(qMls2Message.dateReceived, criteria.dateReceivedStart(), expressions);
    loe(qMls2Message.dateReceived, criteria.dateReceivedEnd(), expressions);
    return expressions.build();
  }

  private Predicate createSearchPredicateByKeyword(InboundMessagingCriteria criteria, QInboundMessagingEntity qMls2Message) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    val keywords = criteria.keyword();
    val keywordExpressions = BooleanExpressionBuilder.of(SearchTypeEnum.OR);
    Arrays.stream(keywords.split(",")).filter(x -> !x.equalsIgnoreCase(ANY_WILDCARD)).map(String::trim).forEach(keyword -> {
      matchIgnoreCase(qMls2Message.statusMessage, keyword, keywordExpressions);

      match(qMls2Message.status, findMessageStatusesMatchingKeyword(keyword).map(InboundMessagingStatus::id), keywordExpressions);
      match(qMls2Message.type, findMessageTypesMatchingKeyword(keyword).map(InboundMessagingType::id), keywordExpressions);
    });
    expressions.append(keywordExpressions.build());
    return expressions.build();
  }

  @SuppressWarnings({"Duplicates", "unused"})
  private List<SortOrderPair> createSortPairs(InboundMessagingCriteria criteria, QInboundMessagingEntity qMls2Message) {
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

  private Stream<InboundMessagingType> findMessageTypesMatchingKeyword(String keyword) {
    if (StringUtils.isBlank(keyword)) return Stream.empty();
    return Arrays
        .stream(MessageType.values())
        .filter(x -> KeywordUtils.matchesIgnoreCaseKeyword(x.getLabel(), keyword))
        .map(InboundMessagingType::valueOf)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private Stream<InboundMessagingStatus> findMessageStatusesMatchingKeyword(String keyword) {
    if (StringUtils.isBlank(keyword)) return Stream.empty();
    return Arrays.stream(InboundMessagingStatus.values()).filter(x -> KeywordUtils.matchesIgnoreCaseKeyword(x.label(), keyword));
  }
}