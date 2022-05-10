package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.EquipmentSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.EquipmentEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QEquipmentEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class EquipmentEntityRepositoryCustomImpl implements EquipmentEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Long count(EquipmentSearchCriteria criteria) {
    val qEntity = QEquipmentEntity.equipmentEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetchCount();
  }

  @Override
  public Optional<EquipmentEntity> findByCurrentUserId(Integer currentUserId) {
    val qEquipment = QEquipmentEntity.equipmentEntity;
    val query = new JPAQuery<>(entityManager).select(qEquipment).from(qEquipment).where(qEquipment.currentUserId.eq(currentUserId));

    return Optional.ofNullable(query.fetchFirst());
  }

  @Override
  public List<EquipmentEntity> search(EquipmentSearchCriteria criteria) {
    val qEntity = QEquipmentEntity.equipmentEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    List<EquipmentEntity> list = query.fetch();
    return list;
  }

  private Predicate createSearchPredicate(EquipmentSearchCriteria criteria, QEquipmentEntity qEquipmentEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    if (!criteria.isIncludeDummy()) {
      notmatchIgnoreCase(qEquipmentEntity.name, "DUMMY", expressions, false);
    }
    match(qEquipmentEntity.wac().wacNumber, criteria.getWacNumber(), expressions);
    match(qEquipmentEntity.name, criteria.getName(), expressions);
    match(qEquipmentEntity.packingGroup, criteria.getPackingGroup(), expressions);
    match(qEquipmentEntity.description, criteria.getDescription(), expressions);
    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(EquipmentSearchCriteria criteria, QEquipmentEntity qEquipmentEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qEquipmentEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();
      EntityPathBase<?> qEntity = qEquipmentEntity;

      sort.setPath(qEntity);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}
