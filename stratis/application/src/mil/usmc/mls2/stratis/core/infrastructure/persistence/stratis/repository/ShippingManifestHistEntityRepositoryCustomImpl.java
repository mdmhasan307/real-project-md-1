package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestHistSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QShippingManifestHistEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingManifestHistEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class ShippingManifestHistEntityRepositoryCustomImpl implements ShippingManifestHistEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<ShippingManifestHistEntity> search(ShippingManifestHistSearchCriteria criteria) {
    val qShipping = QShippingManifestHistEntity.shippingManifestHistEntity;
    val query = selectFrom(qShipping, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qShipping));
    return query.fetch();
  }

  @Override
  public Long count(ShippingManifestHistSearchCriteria criteria) {
    val qShipping = QShippingManifestHistEntity.shippingManifestHistEntity;
    val query = selectFrom(qShipping, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qShipping));
    return query.fetchCount();
  }

  private Predicate createSearchPredicate(ShippingManifestHistSearchCriteria criteria, QShippingManifestHistEntity qShipping) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qShipping.manifest, criteria.getManifest(), expressions);
    if (criteria.isCheckPickedupOrDelivered()) {
      val delivered = BooleanExpressionBuilder.of(SearchTypeEnum.OR);
      match(qShipping.pickedUpFlag, "Y", delivered);
      match(qShipping.deliveredFlag, "Y", delivered);
      expressions.append(delivered.getExpression());
    }
    return expressions.getExpression();
  }
}