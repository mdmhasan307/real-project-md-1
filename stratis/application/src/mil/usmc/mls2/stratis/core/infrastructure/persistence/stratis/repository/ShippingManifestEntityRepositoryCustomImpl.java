package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QShippingManifestEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingManifestEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class ShippingManifestEntityRepositoryCustomImpl implements ShippingManifestEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ShippingManifestEntity> search(ShippingManifestSearchCriteria criteria) {
    val qShipping = QShippingManifestEntity.shippingManifestEntity;
    val query = selectFrom(qShipping, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qShipping));
    return query.fetch();
  }

  @Override
  public Long count(ShippingManifestSearchCriteria criteria) {
    val qShipping = QShippingManifestEntity.shippingManifestEntity;
    val query = selectFrom(qShipping, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qShipping));
    return query.fetchCount();
  }

  private Predicate createSearchPredicate(ShippingManifestSearchCriteria criteria, QShippingManifestEntity qShipping) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qShipping.equipmentNumber, criteria.getEquipmentNumber(), expressions);
    if (criteria.isManifestDateNull()) {
      match(qShipping.manifestDate, null, expressions, false);
    }
    match(qShipping.floorLocation().floorLocation, criteria.getFloorLocation(), expressions);
    return expressions.getExpression();
  }
}