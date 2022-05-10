package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.RefMatsSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QNiinInfoEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QRefMatsEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefMatsEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class RefMatsEntityRepositoryCustomImpl implements RefMatsEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Set<RefMatsEntity> search(RefMatsSearchCriteria criteria) {
    val qEntity = QRefMatsEntity.refMatsEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return new HashSet<>(query.fetch());
  }

  private Predicate createSearchPredicate(RefMatsSearchCriteria criteria, QRefMatsEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.niin, criteria.getNiin(), expressions);
    return expressions.getExpression();
  }

  /**
   * Return a list of NIINs from the REF_DASF table that don't exist in NIIN Info
   */
  @Override
  public Set<String> findMissingNiins() {
    val qEntity = QRefMatsEntity.refMatsEntity;
    val qNiinInfoEntity = QNiinInfoEntity.niinInfoEntity;

    //Query to get Niins from RefDasf
    val query = new JPAQuery<>(entityManager).select(qEntity.niin).from(qEntity);

    //Query to get all NIINS in Niin_Info
    val niinSubQuery = new JPAQuery<>(entityManager).select(qNiinInfoEntity.niin).from(qNiinInfoEntity);

    query.where(qEntity.niin.notIn(niinSubQuery));

    val result = query.fetch();
    return new HashSet<>(result);
  }
}