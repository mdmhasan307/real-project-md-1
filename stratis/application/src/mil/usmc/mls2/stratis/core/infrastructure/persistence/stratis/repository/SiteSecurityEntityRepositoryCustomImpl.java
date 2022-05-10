package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.SiteSecuritySearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QSiteSecurityEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteSecurityEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

public class SiteSecurityEntityRepositoryCustomImpl implements SiteSecurityEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<SiteSecurityEntity> search(SiteSecuritySearchCriteria criteria) {
    val qEntity = QSiteSecurityEntity.siteSecurityEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetch();
  }

  @Override
  public Long count(SiteSecuritySearchCriteria criteria) {
    val qEntity = QSiteSecurityEntity.siteSecurityEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetchCount();
  }

  private Predicate createSearchPredicate(SiteSecuritySearchCriteria criteria, QSiteSecurityEntity qSiteSecurityEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qSiteSecurityEntity.codeName, criteria.getCodeName(), expressions);

    match(qSiteSecurityEntity.codeValue, criteria.getCodeValue(), expressions);
    return expressions.getExpression();
  }
}