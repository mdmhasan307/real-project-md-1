package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfoSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.NiinInfoEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QNiinInfoEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class NiinInfoEntityRepositoryCustomImpl implements NiinInfoEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public long count(NiinInfoSearchCriteria criteria) {
    val qNiinInfo = QNiinInfoEntity.niinInfoEntity;

    val query = selectFrom(qNiinInfo, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qNiinInfo));
    configureLimits(query, criteria);
    return query.fetchCount();
  }

  @Override
  public Set<NiinInfoEntity> search(NiinInfoSearchCriteria criteria) {
    val qNiinInfo = QNiinInfoEntity.niinInfoEntity;

    val query = selectFrom(qNiinInfo, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qNiinInfo));
    configureLimits(query, criteria);
    return new HashSet<>(query.fetch());
  }

  private Predicate createSearchPredicate(NiinInfoSearchCriteria criteria, QNiinInfoEntity qNiinInfo) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qNiinInfo.niin, criteria.getNiin(), expressions);

    return expressions.getExpression();
  }
}