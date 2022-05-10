package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.CommonUserSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.CommonUserEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.QCommonUserEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.apache.commons.collections4.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
public class CommonUserEntityRepositoryCustomImpl implements CommonUserEntityRepositoryCustom {

  @PersistenceContext(unitName = GlobalConstants.COMMON_PERSISTENCE_UNIT)
  private EntityManager entityManager;

  @Override
  public List<CommonUserEntity> search(CommonUserSearchCriteria criteria) {
    val qEntity = QCommonUserEntity.commonUserEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetch();
  }

  private Predicate createSearchPredicate(CommonUserSearchCriteria criteria, QCommonUserEntity qCommonUserEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qCommonUserEntity.id().cacNumber, criteria.getCacNumber(), expressions);
    if (CollectionUtils.isNotEmpty(criteria.getSitesAvailable()))
      expressions.append(qCommonUserEntity.id().siteName.in(criteria.getSitesAvailable()));
    return expressions.getExpression();
  }
}
