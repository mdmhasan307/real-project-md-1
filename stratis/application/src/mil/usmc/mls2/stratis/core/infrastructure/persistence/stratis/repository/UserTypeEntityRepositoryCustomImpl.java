package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.UserTypeSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QUserTypeEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.UserTypeEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class UserTypeEntityRepositoryCustomImpl implements UserTypeEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<UserTypeEntity> search(UserTypeSearchCriteria criteria) {
    val qUserType = QUserTypeEntity.userTypeEntity;

    val query = selectFrom(qUserType, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qUserType));
    return query.fetch();
  }

  private Predicate createSearchPredicate(UserTypeSearchCriteria criteria, QUserTypeEntity qUserType) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);

    match(qUserType.type, criteria.getType(), expressions);
    return expressions.getExpression();
  }
}