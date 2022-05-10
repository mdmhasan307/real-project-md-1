package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLogSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QRefDataloadLogEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefDataloadLogEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class RefDataloadLogEntityRepositoryCustomImpl implements RefDataloadLogEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Set<RefDataloadLogEntity> search(RefDataloadLogSearchCriteria criteria) {
    val qEntity = QRefDataloadLogEntity.refDataloadLogEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return new HashSet(query.fetch());
  }

  private Predicate createSearchPredicate(RefDataloadLogSearchCriteria criteria, QRefDataloadLogEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.refDataloadLogId, criteria.getId(), expressions);
    match(qEntity.interfaceName, criteria.getInterfaceName(), expressions);
    if (criteria.getCreatedBefore() != null)
      expressions.append(qEntity.createdDate.lt(criteria.getCreatedBefore()));

    return expressions.getExpression();
  }
}