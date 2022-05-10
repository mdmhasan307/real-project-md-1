package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.ShippingSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QShippingEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
public class ShippingEntityRepositoryCustomImpl implements ShippingEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ShippingEntity> search(ShippingSearchCriteria criteria) {
    val qShipping = QShippingEntity.shippingEntity;
    val query = selectFrom(qShipping, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qShipping));
    return query.fetch();
  }

  @Override
  public Long count(ShippingSearchCriteria criteria) {
    val qShipping = QShippingEntity.shippingEntity;
    val query = selectFrom(qShipping, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qShipping));
    return query.fetchCount();
  }

  private Predicate createSearchPredicate(ShippingSearchCriteria criteria, QShippingEntity qShippingEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qShippingEntity.shippingManifest().floorLocation().floorLocationId, criteria.getFloorLocationId(), expressions);
    match(qShippingEntity.scn, criteria.getScn(), expressions);
    return expressions.getExpression();
  }
}