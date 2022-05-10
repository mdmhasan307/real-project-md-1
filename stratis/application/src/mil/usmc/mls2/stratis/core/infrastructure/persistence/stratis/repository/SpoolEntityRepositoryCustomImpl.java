package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.SpoolSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QSpoolEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SpoolEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@SuppressWarnings("Duplicates")
public class SpoolEntityRepositoryCustomImpl implements SpoolEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Set<SpoolEntity> search(SpoolSearchCriteria criteria) {
    val qSpool = QSpoolEntity.spoolEntity;
    val query = selectFrom(qSpool, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qSpool));
    configureOrderBy(query, createSortPairs(criteria, qSpool));
    return new HashSet<>(query.fetch());
  }

  @Override
  public Optional<SpoolEntity> searchSingle(SpoolSearchCriteria criteria) {
    val qSpool = QSpoolEntity.spoolEntity;
    val query = selectFrom(qSpool, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qSpool));
    configureOrderBy(query, createSortPairs(criteria, qSpool));
    return Optional.ofNullable(query.fetchFirst());
  }

  @Override
  public Long count(SpoolSearchCriteria criteria) {
    val qSpool = QSpoolEntity.spoolEntity;
    val query = selectFrom(qSpool, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qSpool));
    return query.fetchCount();
  }

  private Predicate createSearchPredicate(SpoolSearchCriteria criteria, QSpoolEntity qSpoolEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);

    match(qSpoolEntity.spoolId, criteria.getSpoolId(), expressions);
    match(qSpoolEntity.spoolDefMode, criteria.getSpoolDefMode(), expressions);

    if (!CollectionUtils.isEmpty(criteria.getStatusList())) {
      expressions.append(qSpoolEntity.status.in(criteria.getStatusList()));
    }
    if (!criteria.isAllowNullXml()) {
      expressions.append(qSpoolEntity.xml.isNotNull());
    }

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(SpoolSearchCriteria criteria, QSpoolEntity qSpoolEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qSpoolEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();

      sort.setPath(qSpoolEntity);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}