package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.ErrorQueueCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorQueueEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QErrorQueueEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import org.apache.commons.collections4.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
public class ErrorQueueEntityRepositoryCustomImpl implements ErrorQueueEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ErrorQueueEntity> search(ErrorQueueCriteria criteria) {
    val qEntity = QErrorQueueEntity.errorQueueEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetch();
  }

  @Override
  public Long count(ErrorQueueCriteria criteria) {
    val qEntity = QErrorQueueEntity.errorQueueEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetchCount();
  }

  private Predicate createSearchPredicate(ErrorQueueCriteria criteria, QErrorQueueEntity qErrorQueueEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qErrorQueueEntity.keyType, criteria.getKeyType(), expressions);
    match(qErrorQueueEntity.keyNum, criteria.getKeyNum(), expressions);
    match(qErrorQueueEntity.eid, criteria.getEid(), expressions);

    if (CollectionUtils.isNotEmpty(criteria.getKeyNumsToNotMatch())) {
      expressions.append(qErrorQueueEntity.keyNum.notIn(criteria.getKeyNumsToNotMatch()));
    }

    return expressions.getExpression();
  }
}