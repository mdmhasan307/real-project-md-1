package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.ErrorSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QErrorEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
public class ErrorEntityRepositoryCustomImpl implements ErrorEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ErrorEntity> search(ErrorSearchCriteria criteria) {
    val qEntity = QErrorEntity.errorEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(ErrorSearchCriteria criteria, QErrorEntity qErrorEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qErrorEntity.id, criteria.getId(), expressions);
    match(qErrorEntity.code, criteria.getCode(), expressions);
    return expressions.getExpression();
  }
}