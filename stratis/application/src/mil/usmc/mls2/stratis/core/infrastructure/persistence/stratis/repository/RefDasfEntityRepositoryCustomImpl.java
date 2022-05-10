package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.RefDasfSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QNiinInfoEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QRefDasfEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefDasfEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class RefDasfEntityRepositoryCustomImpl implements RefDasfEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Set<RefDasfEntity> search(RefDasfSearchCriteria criteria) {
    val qEntity = QRefDasfEntity.refDasfEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return new HashSet<>(query.fetch());
  }

  /**
   * Return a list of NIINs from the REF_DASF table that don't exist in NIIN Info
   */
  @Override
  public Set<String> findMissingNiins() {
    val qEntity = QRefDasfEntity.refDasfEntity;
    val niinInfoEntity = QNiinInfoEntity.niinInfoEntity;

    //Query to get Niins from RefDasf
    val query = new JPAQuery<>(entityManager).select(qEntity.niin).from(qEntity);

    //Query to get all NIINS in Niin_Info
    val niinSubQuery = new JPAQuery<>(entityManager).select(niinInfoEntity.niin).from(niinInfoEntity);

    query.where(qEntity.niin.notIn(niinSubQuery));

    val result = query.fetch();
    return new HashSet<>(result);
  }

  private Predicate createSearchPredicate(RefDasfSearchCriteria criteria, QRefDasfEntity qRefDasfEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qRefDasfEntity.niin, criteria.getNiin(), expressions);
    match(qRefDasfEntity.documentNumber, criteria.getDocumentNumber(), expressions);
    return expressions.getExpression();
  }
}
