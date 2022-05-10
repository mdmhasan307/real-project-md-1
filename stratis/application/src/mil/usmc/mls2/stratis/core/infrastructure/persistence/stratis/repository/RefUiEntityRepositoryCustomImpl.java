package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.RefUiSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QRefUiEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefUiEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class RefUiEntityRepositoryCustomImpl implements RefUiEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public long count(RefUiSearchCriteria criteria) {
    val qRefUi = QRefUiEntity.refUiEntity;

    val query = selectFrom(qRefUi, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qRefUi));
    return query.fetchCount();
  }

  @Override
  public List<RefUiEntity> search(RefUiSearchCriteria criteria) {
    val qRefUi = QRefUiEntity.refUiEntity;

    val query = selectFrom(qRefUi, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qRefUi));
    return query.fetch();
  }

  private Predicate createSearchPredicate(RefUiSearchCriteria criteria, QRefUiEntity qRefUi) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qRefUi.uiFrom, criteria.getUiFrom(), expressions);
    match(qRefUi.uiTo, criteria.getUiTo(), expressions);
    match(qRefUi.id, criteria.getId(), expressions);

    return expressions.getExpression();
  }
}