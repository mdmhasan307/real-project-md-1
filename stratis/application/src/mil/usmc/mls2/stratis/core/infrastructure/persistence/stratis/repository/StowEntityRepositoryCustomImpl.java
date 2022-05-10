package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.StowSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QStowEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.StowEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class StowEntityRepositoryCustomImpl implements StowEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public long count(StowSearchCriteria criteria) {
    val qStow = QStowEntity.stowEntity;

    val query = selectFrom(qStow, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qStow));
    return query.fetchCount();
  }

  @Override
  public List<StowEntity> search(StowSearchCriteria criteria) {
    val qStow = QStowEntity.stowEntity;

    val query = selectFrom(qStow, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qStow));
    configureOrderBy(query, createSortPairs(criteria, qStow));
    return query.fetch();
  }

  private Predicate createSearchPredicate(StowSearchCriteria criteria, QStowEntity qStow) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);

    if (criteria.getReceipt() != null)
      match(qStow.receipt().rcn, criteria.getReceipt().getRcn(), expressions);

    match(qStow.location().wac().wacId, criteria.getWacId(), expressions);
    match(qStow.stowId, criteria.getStowId(), expressions);
    match(qStow.pickingEntity().pid, criteria.getPid(), expressions);
    match(qStow.sid, criteria.getSid(), expressions);
    match(qStow.assignToUser, criteria.getAssignedUserId(), expressions);
    match(qStow.scanInd, criteria.getScanInd(), expressions);
    match(qStow.serialNumber, criteria.getSerialNumber(), expressions);

    if (!CollectionUtils.isEmpty(criteria.getStatuses()))
      expressions.append((qStow.status.in(criteria.getStatuses())));

    if (!CollectionUtils.isEmpty(criteria.getSides()))
      expressions.append((qStow.location().side.in(criteria.getSides())));

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(StowSearchCriteria criteria, QStowEntity qStow) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qStow);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();

      sort.setPath(qStow);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}