package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.RefMhifSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QRefMhifEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefMhifEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.HashSet;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class RefMhifEntityRepositoryCustomImpl implements RefMhifEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Set<RefMhifEntity> search(RefMhifSearchCriteria criteria) {
    val qEntity = QRefMhifEntity.refMhifEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return new HashSet<>(query.fetch());
  }

  private Predicate createSearchPredicate(RefMhifSearchCriteria criteria, QRefMhifEntity qRefMhifEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qRefMhifEntity.recordNiin, criteria.getRecordNiin(), expressions);
    return expressions.getExpression();
  }

  @Override
  public void callProcessMhif() {
    StoredProcedureQuery query = entityManager
        .createStoredProcedureQuery("PKG_STRAT_PROCS.P_PROCESS_MHIF");
    query.execute();
  }
}