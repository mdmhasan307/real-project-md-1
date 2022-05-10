package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QRefGabfEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefGabfEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.HashSet;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class RefGabfEntityRepositoryCustomImpl implements RefGabfEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public void callPopulateGcssReconHist() {
    StoredProcedureQuery query = entityManager
        .createStoredProcedureQuery("PKG_STRAT_PROCS.P_POPULATE_GCSS_RECON_HIST");
    query.execute();
  }

  @Override
  public Set<RefGabfEntity> search(RefGabfSearchCriteria criteria) {
    val qEntity = QRefGabfEntity.refGabfEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return new HashSet<>(query.fetch());
  }

  private Predicate createSearchPredicate(RefGabfSearchCriteria criteria, QRefGabfEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.recordNiin, criteria.getNiin(), expressions);
    return expressions.getExpression();
  }
}