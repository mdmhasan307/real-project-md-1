package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.LocationSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.LocationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QLocationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class LocationEntityRepositoryCustomImpl implements LocationEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  public long count(LocationSearchCriteria criteria) {
    val qLocation = QLocationEntity.locationEntity;

    val query = selectFrom(qLocation, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qLocation));
    return query.fetchCount();
  }

  public List<LocationEntity> search(LocationSearchCriteria criteria) {
    val qLocation = QLocationEntity.locationEntity;

    val query = selectFrom(qLocation, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qLocation));
    return query.fetch();
  }

  private Predicate createSearchPredicate(LocationSearchCriteria criteria, QLocationEntity qLocationEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qLocationEntity.wac().wacId, criteria.getWacId(), expressions);
    match(qLocationEntity.wac().warehouseId, criteria.getWarehouseId(), expressions);
    match(qLocationEntity.locationLabel, criteria.getLocationLabel(), expressions);
    match(qLocationEntity.availabilityFlag, criteria.getAvailabilityFlag(), expressions);

    return expressions.getExpression();
  }
}