package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNumSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PickSerialLotNumEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QPickSerialLotNumEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class PickSerialLotNumEntityRepositoryCustomImpl implements PickSerialLotNumEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<PickSerialLotNumEntity> search(PickSerialLotNumSearchCriteria criteria) {
    val qPick = QPickSerialLotNumEntity.pickSerialLotNumEntity;
    val query = selectFrom(qPick, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qPick));
    return query.fetch();
  }

  private Predicate createSearchPredicate(PickSerialLotNumSearchCriteria criteria, QPickSerialLotNumEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.serialLotNumTrack().serialLotNumTrackId, criteria.getSerialLotNumTrackId(), expressions);
    match(qEntity.pid, criteria.getPid(), expressions);
    match(qEntity.scn, criteria.getScn(), expressions);
    return expressions.getExpression();
  }
}