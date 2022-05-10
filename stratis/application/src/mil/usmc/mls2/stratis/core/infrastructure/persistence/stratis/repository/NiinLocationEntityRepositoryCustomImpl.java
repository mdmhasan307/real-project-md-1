package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.NiinLocationSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.NiinLocationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QNiinLocationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class NiinLocationEntityRepositoryCustomImpl implements NiinLocationEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public long count(NiinLocationSearchCriteria criteria) {
    val qNiinLocation = QNiinLocationEntity.niinLocationEntity;

    val query = selectFrom(qNiinLocation, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qNiinLocation));
    return query.fetchCount();
  }

  @Override
  public List<NiinLocationEntity> search(NiinLocationSearchCriteria criteria) {
    val qNiinLocation = QNiinLocationEntity.niinLocationEntity;

    val query = selectFrom(qNiinLocation, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qNiinLocation));
    configureOrderBy(query, createSortPairs(criteria, qNiinLocation));

    return query.fetch();
  }

  private Predicate createSearchPredicate(NiinLocationSearchCriteria criteria, QNiinLocationEntity qNiinLocation) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qNiinLocation.location().locationId, criteria.getLocationId(), expressions);
    if (criteria.getNiinIdMatch() == null || criteria.getNiinIdMatch()) {
      match(qNiinLocation.niinInfo().niinId, criteria.getNiinId(), expressions);
    }
    else {
      notmatch(qNiinLocation.niinInfo().niinId, criteria.getNiinId(), expressions);
    }
    match(qNiinLocation.cc, criteria.getCc(), expressions);
    match(qNiinLocation.location().wac().wacId, criteria.getWacId(), expressions);
    match(qNiinLocation.niinLocationId, criteria.getNiinInventoryId(), expressions);
    match(qNiinLocation.nsnRemark, criteria.getNsnRemark(), expressions);
    match(qNiinLocation.expRemark, criteria.getExpRemark(), expressions);
    match(qNiinLocation.niinInfo().niin, criteria.getNiin(), expressions);
    match(qNiinLocation.locked, criteria.getLocked(), expressions);

    if (!CollectionUtils.isEmpty(criteria.getSides())) {
      expressions.append(qNiinLocation.location().side.in(criteria.getSides()));
    }

    if (BooleanUtils.isTrue(criteria.getQtyGreaterThenZero())) {
      expressions.append(qNiinLocation.qty.gt(0));
    }

    loe(qNiinLocation.expirationDate, criteria.getExpirationDate(), expressions);

    if (criteria.getCheckNumExtentsNull() != null && criteria.getCheckNumExtentsNull()) {
      expressions.append(qNiinLocation.numExtents.isNull());
    }

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(NiinLocationSearchCriteria criteria, QNiinLocationEntity qNiinLocation) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qNiinLocation);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();

      sort.setPath(qNiinLocation);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}