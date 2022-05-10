package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsDataSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.GcssMcImportsDataEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QGcssMcImportsDataEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class GcssMcImportsDataEntityRepositoryCustomImpl implements GcssMcImportsDataEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Set<GcssMcImportsDataEntity> search(GcssMcImportsDataSearchCriteria criteria) {
    val qGcssMcImportsDataEntity = QGcssMcImportsDataEntity.gcssMcImportsDataEntity;

    val query = selectFrom(qGcssMcImportsDataEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qGcssMcImportsDataEntity));
    return new HashSet<>(query.fetch());
  }

  @Override
  public Optional<GcssMcImportsDataEntity> getMostRecentRecordForProcessing(String interfaceName) {
    return getRecordForProcessing(interfaceName, SortOrder.DESC);
  }

  @Override
  public Optional<GcssMcImportsDataEntity> getOldestRecordForProcessing(String interfaceName) {
    return getRecordForProcessing(interfaceName, SortOrder.ASC);
  }

  private Optional<GcssMcImportsDataEntity> getRecordForProcessing(String interfaceName, SortOrder order) {
    val qGcssMcImportsDataEntity = QGcssMcImportsDataEntity.gcssMcImportsDataEntity;
    val query = selectFrom(qGcssMcImportsDataEntity, entityManager);

    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    expressions.append(qGcssMcImportsDataEntity.interfaceName.eq(interfaceName));

    val modifiedDateCheck = OffsetDateTime.now().minusDays(1);
    val statusExpression = BooleanExpressionBuilder.of(SearchTypeEnum.OR);
    statusExpression.append(qGcssMcImportsDataEntity.status.eq("READY"));
    statusExpression.append(qGcssMcImportsDataEntity.status.eq("FAILED").and(qGcssMcImportsDataEntity.modifiedDate.gt(modifiedDateCheck)));

    expressions.append(statusExpression.getExpression());

    configurePredicate(query, expressions.getExpression());
    configureOrderBy(query, generateSortOrderPairs("id", order, qGcssMcImportsDataEntity));
    query.limit(1);
    return Optional.ofNullable(query.fetchOne());
  }

  private Predicate createSearchPredicate(GcssMcImportsDataSearchCriteria criteria, QGcssMcImportsDataEntity qGcssMcImportsDataEntity) {

    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qGcssMcImportsDataEntity.id, criteria.getId(), expressions);
    match(qGcssMcImportsDataEntity.interfaceName, criteria.getInterfaceName(), expressions);
    if (criteria.getCreatedBefore() != null)
      expressions.append(qGcssMcImportsDataEntity.createdDate.lt(criteria.getCreatedBefore()));

    if (!CollectionUtils.isEmpty(criteria.getStatuses())) {
      expressions.append(qGcssMcImportsDataEntity.status.in(criteria.getStatuses()));
    }

    if (!CollectionUtils.isEmpty(criteria.getStatusesNotEqual())) {
      expressions.append(qGcssMcImportsDataEntity.status.notIn(criteria.getStatusesNotEqual()));
    }
    return expressions.getExpression();
  }
}