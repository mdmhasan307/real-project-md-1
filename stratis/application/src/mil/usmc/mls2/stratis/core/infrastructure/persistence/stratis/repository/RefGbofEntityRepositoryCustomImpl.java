package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.RefGbofSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QRefGbofEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefGbofEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class RefGbofEntityRepositoryCustomImpl implements RefGbofEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Set<RefGbofEntity> search(RefGbofSearchCriteria criteria) {
    val qEntity = QRefGbofEntity.refGbofEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return new HashSet<>(query.fetch());
  }

  private Predicate createSearchPredicate(RefGbofSearchCriteria criteria, QRefGbofEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.primeNsn, criteria.getPrimeNsn(), expressions);
    return expressions.getExpression();
  }
}