package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerialSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QRefGabfSerialEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefGabfSerialEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class RefGabfSerialEntityRepositoryCustomImpl implements RefGabfSerialEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Set<RefGabfSerialEntity> search(RefGabfSerialSearchCriteria criteria) {
    val qEntity = QRefGabfSerialEntity.refGabfSerialEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return new HashSet(query.fetch());
  }

  private Predicate createSearchPredicate(RefGabfSerialSearchCriteria criteria, QRefGabfSerialEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.refGabfSerialId, criteria.getId(), expressions);
    return expressions.getExpression();
  }
}