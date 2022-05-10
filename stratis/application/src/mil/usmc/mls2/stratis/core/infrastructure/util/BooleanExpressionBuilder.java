package mil.usmc.mls2.stratis.core.infrastructure.util;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;

@RequiredArgsConstructor(staticName = "of")
public class BooleanExpressionBuilder {

  private BooleanExpression predicate;
  private final SearchTypeEnum searchType;

  public BooleanExpression getExpression() {
    return predicate;
  }

  public SearchTypeEnum getSearchType() {
    return searchType;
  }

  public BooleanExpression build() {
    return predicate;
  }

  public BooleanExpressionBuilder append(BooleanExpression expression) {

    if (predicate == null) {
      predicate = expression;
    }
    else {
      if (searchType == null || SearchTypeEnum.STRICT == searchType || SearchTypeEnum.AND == searchType) {
        predicate = predicate.and(expression);
      }
      else if (SearchTypeEnum.OR == searchType) {
        predicate = predicate.or(expression);
      }
      else {
        throw new IllegalArgumentException("SearchTypeEnum '" + searchType + "' not supported!");
      }
    }

    return this;
  }
}