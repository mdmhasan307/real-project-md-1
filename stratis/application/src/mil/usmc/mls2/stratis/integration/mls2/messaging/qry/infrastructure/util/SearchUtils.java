package mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.util;

import com.querydsl.core.Query;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLBindings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.CriteriaBounds;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.common.domain.model.Sorting;
import mil.usmc.mls2.stratis.common.domain.model.StandardType;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;
import mil.usmc.mls2.stratis.integration.mls2.messaging.qry.application.model.SortOrderPair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"unused", "WeakerAccess"})
public class SearchUtils {

  public static <T> JPAQuery<T> selectFrom(EntityPathBase<T> qEntity, EntityManager entityManager) {
    return new JPAQuery<>(entityManager).select(qEntity).from(qEntity);
  }

  public static void match(BooleanPath qPath, Boolean input, BooleanExpressionBuilder builder) {
    if (input == null) return;
    builder.append(qPath.eq(input));
  }

  public static void match(StringExpression qPath, String input, BooleanExpressionBuilder builder) {
    match(qPath, input, builder, true, false);
  }

  public static void match(StringExpression qPath, String input, BooleanExpressionBuilder builder, boolean ignoreNull) {
    match(qPath, input, builder, ignoreNull, false);
  }

  public static void matchIgnoreCase(StringExpression qPath, String input, BooleanExpressionBuilder builder) {
    matchIgnoreCase(qPath, input, builder, true);
  }

  public static void matchIgnoreCase(StringExpression qPath, String input, BooleanExpressionBuilder builder, boolean ignoreNull) {
    match(qPath, input, builder, ignoreNull, true);
  }

  public static void match(ComparablePath<UUID> qPath, UUID criterion, BooleanExpressionBuilder builder) {
    match(qPath, criterion, builder, true);
  }

  public static <I extends Number & Comparable<?>> void match(NumberPath<I> qPath, StandardType<I> criterion, BooleanExpressionBuilder builder) {
    match(qPath, criterion, builder, true);
  }

  public static void match(ComparablePath<UUID> qPath, UUID criterion, BooleanExpressionBuilder builder, boolean ignoreNull) {
    if (ignoreNull && criterion == null) return;
    builder.append(criterion == null ? qPath.isNull() : qPath.eq(criterion));
  }

  public static <I extends Number & Comparable<?>> void match(NumberPath<I> qPath, StandardType<I> criterion, BooleanExpressionBuilder builder, boolean ignoreNull) {
    if (ignoreNull && criterion == null) return;
    builder.append(criterion == null ? qPath.isNull() : qPath.eq(criterion.id()));
  }

  public static void match(ComparablePath<String> qPath, String criterion, BooleanExpressionBuilder builder, boolean ignoreNull) {
    if (ignoreNull && criterion == null) return;
    builder.append(criterion == null ? qPath.isNull() : qPath.eq(criterion));
  }

  public static <T> void match(SimplePath<T> qPath, T criterion, BooleanExpressionBuilder builder) {
    match(qPath, criterion, builder, true);
  }

  public static void match(ComparablePath<String> qPath, String criterion, BooleanExpressionBuilder builder) {
    match(qPath, criterion, builder, true);
  }

  public static <T extends Number & Comparable<?>> void match(NumberPath<T> qPath, T criterion, BooleanExpressionBuilder builder) {
    match(qPath, criterion, builder, true);
  }

  public static <T extends Number & Comparable<?>> void match(NumberPath<T> qPath, T criterion, BooleanExpressionBuilder builder, boolean ignoreNull) {
    if (ignoreNull && criterion == null) return;
    builder.append(criterion == null ? qPath.isNull() : qPath.eq(criterion));
  }

  public static <T extends Enum<T>> void match(EnumPath<T> qPath, T criterion, BooleanExpressionBuilder builder) {
    match(qPath, criterion, builder, true);
  }

  public static <T extends Enum<T>> void match(EnumPath<T> qPath, T criterion, BooleanExpressionBuilder builder, boolean ignoreNull) {
    if (ignoreNull && criterion == null) return;
    builder.append(criterion == null ? qPath.isNull() : qPath.eq(criterion));
  }

  public static <T extends Enum<T>> void match(EnumPath<T> qPath, List<T> criteria, BooleanExpressionBuilder builder) {
    match(qPath, criteria, builder, true);
  }

  public static <T extends Enum<T>> void match(EnumPath<T> qPath, List<T> criteria, BooleanExpressionBuilder builder, boolean ignoreNull) {
    if (ignoreNull && criteria == null) return;

    if (criteria == null || criteria.isEmpty()) return;
    if (criteria.size() == 1) {
      T criterion = criteria.iterator().next();
      builder.append(qPath.eq(criterion));
    }
    else {
      builder.append(qPath.in(criteria));
    }
  }

  public static <T> void match(SimplePath<T> qPath, T criterion, BooleanExpressionBuilder builder, boolean ignoreNull) {
    if (ignoreNull && criterion == null) return;
    builder.append(criterion == null ? qPath.isNull() : qPath.eq(criterion));
  }

  public static <I extends Number & Comparable<?>> void match(NumberPath<I> qPath, Stream<I> $criteria, BooleanExpressionBuilder builder) {
    if ($criteria == null) return;
    val criteriaList = $criteria.collect(Collectors.toList());

    if (criteriaList.size() == 1) {
      I criterion = criteriaList.iterator().next();
      builder.append(qPath.eq(criterion));
    }
    else {
      builder.append(qPath.in(criteriaList));
    }
  }

  public static <T> void match(SimplePath<T> qPath, Collection<T> criteria, BooleanExpressionBuilder builder) {
    if (criteria == null || criteria.isEmpty()) return;
    if (criteria.size() == 1) {
      T criterion = criteria.iterator().next();
      builder.append(qPath.eq(criterion));
    }
    else {
      builder.append(qPath.in(criteria));
    }
  }

  public static void match(DateTimePath<OffsetDateTime> qPath, OffsetDateTime criterion, BooleanExpressionBuilder builder) {
    match(qPath, criterion, builder, true);
  }

  public static void match(DateTimePath<OffsetDateTime> qPath, OffsetDateTime criterion, BooleanExpressionBuilder builder, boolean ignoreNull) {
    if (ignoreNull && criterion == null) return;
    builder.append(criterion == null ? qPath.isNull() : qPath.eq(criterion));
  }

  public static void goe(DateTimePath<OffsetDateTime> qPath, OffsetDateTime criterion, BooleanExpressionBuilder builder) {
    if (criterion == null) return;
    builder.append(qPath.goe(criterion));
  }

  public static void loe(DateTimePath<OffsetDateTime> qPath, OffsetDateTime criterion, BooleanExpressionBuilder builder) {
    if (criterion == null) return;
    builder.append(qPath.loe(criterion));
  }

  public static void match(DatePath<LocalDate> qPath, LocalDate criterion, BooleanExpressionBuilder builder) {
    match(qPath, criterion, builder, true);
  }

  public static void match(DatePath<LocalDate> qPath, LocalDate criterion, BooleanExpressionBuilder builder, boolean ignoreNull) {
    if (ignoreNull && criterion == null) return;
    builder.append(criterion == null ? qPath.isNull() : qPath.eq(criterion));
  }

  public static void goe(DatePath<LocalDate> qPath, LocalDate criterion, BooleanExpressionBuilder builder) {
    if (criterion == null) return;
    builder.append(qPath.goe(criterion));
  }

  public static void loe(DatePath<LocalDate> qPath, LocalDate criterion, BooleanExpressionBuilder builder) {
    if (criterion == null) return;
    builder.append(qPath.loe(criterion));
  }

  public static <T> T singleResult(List<T> list, String errorMessageIfMultipleFound) {
    if (CollectionUtils.isEmpty(list)) {
      return null;
    }

    if (list.size() > 1) {
      throw new IllegalStateException(errorMessageIfMultipleFound);
    }
    else {
      return list.get(0);
    }
  }

  public static List<SortOrderPair> generateSortOrderPairs(String inputSortColumn, SortOrder inputSortOrder, RelationalPathBase<?> qEntity) {
    final List<SortOrderPair> sorts = new ArrayList<>();
    final String criteriaSortColumn = StringUtils.trim(inputSortColumn);
    if (StringUtils.isNotBlank(criteriaSortColumn)) {
      String[] allSortColumnTokens = criteriaSortColumn.split(",");

      if (allSortColumnTokens.length == 1) {
        String sortColumnToken = StringUtils.trim(allSortColumnTokens[0]);
        String[] sortColumnTokens = sortColumnToken.split(" ");
        String sortColumn = StringUtils.trim(sortColumnTokens[0]);
        SortOrder sortColumnSortOrder = sortColumnTokens.length > 1 ? SortOrder.valueOf(StringUtils.trim(sortColumnTokens[1])) : (inputSortOrder != null ? inputSortOrder : SortOrder.ASC);

        sorts.add(SortOrderPair.of(qEntity, sortColumn, sortColumnSortOrder));
      }
      else {
        for (String sortColumnToken : allSortColumnTokens) {
          sortColumnToken = StringUtils.trim(sortColumnToken);
          String[] sortColumnTokens = sortColumnToken.split(" ");
          String sortColumn = StringUtils.trim(sortColumnTokens[0]);
          SortOrder sortColumnSortOrder = sortColumnTokens.length > 1 ? SortOrder.valueOf(StringUtils.trim(sortColumnTokens[1])) : SortOrder.ASC;

          sorts.add(SortOrderPair.of(qEntity, sortColumn, sortColumnSortOrder));
        }
      }
    }

    return sorts;
  }

  public static List<SortOrderPair> generateSortOrderPairs(String inputSortColumn, SortOrder inputSortOrder, BeanPath<?> qEntity) {
    val sorts = new ArrayList<SortOrderPair>();
    val criteriaSortColumn = StringUtils.trim(inputSortColumn);
    if (StringUtils.isNotBlank(criteriaSortColumn)) {
      val allSortColumnTokens = criteriaSortColumn.split(",");

      if (allSortColumnTokens.length == 1) {
        String sortColumnToken = StringUtils.trim(allSortColumnTokens[0]);
        String[] sortColumnTokens = sortColumnToken.split(" ");
        String sortColumn = StringUtils.trim(sortColumnTokens[0]);
        SortOrder sortColumnSortOrder = sortColumnTokens.length > 1 ? SortOrder.valueOf(StringUtils.trim(sortColumnTokens[1])) : (inputSortOrder != null ? inputSortOrder : SortOrder.ASC);

        sorts.add(SortOrderPair.of(qEntity, sortColumn, sortColumnSortOrder));
      }
      else {
        for (String sortColumnToken : allSortColumnTokens) {
          sortColumnToken = StringUtils.trim(sortColumnToken);
          String[] sortColumnTokens = sortColumnToken.split(" ");
          String sortColumn = StringUtils.trim(sortColumnTokens[0]);
          SortOrder sortColumnSortOrder = sortColumnTokens.length > 1 ? SortOrder.valueOf(StringUtils.trim(sortColumnTokens[1])) : SortOrder.ASC;

          sorts.add(SortOrderPair.of(qEntity, sortColumn, sortColumnSortOrder));
        }
      }
    }

    return sorts;
  }

  public static void configureLimits(Query<?> query, CriteriaBounds criteriaBounds) {
    configureLimits(query, criteriaBounds.offset(), criteriaBounds.limit());
  }

  public static void configureOrderBy(JPAQuery<?> query, List<SortOrderPair> sortOrderPairs) {
    query.orderBy(generateOrderSpecifiers(sortOrderPairs));
  }

  /**
   * Returns the property path in bean notation.
   * Does not include root path
   * ex: "equipment.equipmentType().tamcn" returns "equipmentType.tamcn"
   * ex: "equipment.equipmentId" returns "equipmentId"
   */
  public static String getBeanProperty(Path<?> path) {
    List<String> pathChain = new ArrayList<>();

    Path<?> tempPath = path;
    while (tempPath != null && !tempPath.getMetadata().isRoot()) {
      tempPath = tempPath.getMetadata().getParent();
      if (tempPath != null) {
        if (!tempPath.getMetadata().isRoot()) {
          pathChain.add(tempPath.getMetadata().getName());
        }
      }
    }

    Collections.reverse(pathChain);

    StringBuilder sb = new StringBuilder();
    for (String pathLink : pathChain) {
      sb.append(pathLink).append('.');
    }

    if (path != null) {
      sb.append(path.getMetadata().getName());
    }

    return sb.toString();
  }

  public static void configurePredicate(Query<?> query, Predicate predicate) {
    if (predicate != null) {
      query.where(predicate);
    }
  }

  public static void configureLimits(Query<?> query, long offset, long maxItems) {
    // offset
    if (offset > 0) {
      log.debug("Setting offset to [{}].", offset);
      query.offset(offset);
    }
    else {
      log.debug("Setting offset to default of zero.");
      query.offset(0L);
    }

    // max items
    if (maxItems > 0) {
      log.debug("Setting maxItems limit to [{}].", maxItems);
      query.limit(maxItems);
    }
    else {
      log.debug("Not setting a max item limit.");
    }
  }

  // FUTURE MIKE 3.0.13.0 potential for dateUpdated and such - remove this model and convert calls to use SearchUtils.configureOrderBy(query, qEntity, createSortPairs(criteria, qEntity));
  public static void configureOrderBy(Query<?> query, Sorting sorting, BeanPath<?> qEntity) {
    if (org.apache.commons.lang.StringUtils.isNotBlank(sorting.sortColumn())) {
      query.orderBy(generateOrderSpecifiers(qEntity, SortOrderPair.of(qEntity, sorting.sortColumn(), sorting.sortOrder())));
    }
  }

  public static void configureOrderBy(Query<?> query, BeanPath<?> qEntity, String sortColumn) {
    if (org.apache.commons.lang.StringUtils.isNotBlank(sortColumn)) {
      query.orderBy(generateOrderSpecifiers(qEntity, SortOrderPair.of(qEntity, sortColumn, SortOrder.ASC)));
    }
  }

  public static void configureOrderBy(Query<?> query, BeanPath<?> qEntity, String sortColumn, SortOrder sortOrder) {
    if (org.apache.commons.lang.StringUtils.isNotBlank(sortColumn)) {
      query.orderBy(generateOrderSpecifiers(qEntity, SortOrderPair.of(qEntity, sortColumn, sortOrder)));
    }
  }

  public static void configureOrderBy(JPAQuery<?> query, BeanPath<?> qEntity, List<SortOrderPair> sortOrderPairs) {
    query.orderBy(generateOrderSpecifiers(qEntity, sortOrderPairs));
  }

  public static void configureOrderBy(JPAQuery<?> query, OrderSpecifier<?>[] orderSpecifiers) {
    if (orderSpecifiers != null) {
      query.orderBy(orderSpecifiers);
    }
  }

  public static <T> BooleanExpression buildInExpression(SimpleExpression<T> property, Collection<T> collection) {
    if (CollectionUtils.isEmpty(collection)) {
      return property.in(new ArrayList<>());
    }

    BooleanExpression exp = null;
    final List<T> values = new ArrayList<>(collection);
    final int parameterLimit = 999;
    final int listSize = values.size();
    for (int i = 0; i < listSize; i += parameterLimit) {
      List<T> subList;
      if (listSize > i + parameterLimit) {
        subList = values.subList(i, (i + parameterLimit));
      }
      else {
        subList = values.subList(i, listSize);
      }

      if (exp != null) {
        exp = exp.or(property.in(subList));
      }
      else {
        exp = property.in(subList);
      }
    }

    return exp;
  }

  public static OrderSpecifier<?>[] generateOrderSpecifiers(List<SortOrderPair> pairs) {
    return generateOrderSpecifiers(pairs.toArray(new SortOrderPair[0]));
  }

  public static OrderSpecifier<?>[] generateOrderSpecifiers(SortOrderPair sortOrderPair) {
    return generateOrderSpecifiers(new SortOrderPair[]{sortOrderPair});
  }

  public static OrderSpecifier<?>[] generateOrderSpecifiers(SortOrderPair[] pairs) {
    val specifiers = new ArrayList<OrderSpecifier<?>>();
    if (pairs != null) {
      for (SortOrderPair pair : pairs) {
        Order order = pair.isAscending() ? Order.ASC : Order.DESC;
        specifiers.add(new OrderSpecifier<>(order, Expressions.stringPath(pair.path(), dataTablesFilter(pair.property()))));
      }
    }

    return specifiers.toArray(new OrderSpecifier[0]);
  }

  public static OrderSpecifier<?>[] generateOrderSpecifiers(Path<?> entry, List<SortOrderPair> pairs) {
    return generateOrderSpecifiers(entry, pairs.toArray(new SortOrderPair[0]));
  }

  @SuppressWarnings("unchecked")
  public static <T extends Comparable<?>> OrderSpecifier<String>[] generateOrderSpecifiers(Path<?> entry, SortOrderPair[] pairs) {
    val specifiers = new ArrayList<OrderSpecifier<String>>();

    if (pairs != null) {
      for (SortOrderPair pair : pairs) {
        val order = pair.isAscending() ? Order.ASC : Order.DESC;
        StringExpression stringExpression = null;
        val splitProperty = pair.property().split(",");
        for (String prop : splitProperty) {
          if (stringExpression == null) {
            stringExpression = Expressions.stringPath(entry, prop);
          }
          else {
            val newPath = Expressions.stringPath(entry, prop);
            stringExpression = stringExpression.append(newPath);
          }
        }
        specifiers.add(new OrderSpecifier<>(order, stringExpression));
      }
    }

    return specifiers.toArray(new OrderSpecifier[0]);
  }

  public static OrderSpecifier<?>[] generateOrderSpecifiers(Path<?> entry, SortOrderPair sortOrderPair) {
    return generateOrderSpecifiers(entry, new SortOrderPair[]{sortOrderPair});
  }

  @SuppressWarnings("deprecation")
  public static void applyBindings(javax.persistence.Query query, SQLBindings sqlBindings) {
    int i = 1;
    for (Object binding : sqlBindings.getBindings()) {
      query.setParameter(i, binding);
      i++;
    }
  }

  public static String toSortColumn(String sortString) {
    if (StringUtils.isBlank(sortString)) return null;
    val parts = sortString.split(" ");
    return parts[0];
  }

  public static SortOrder toSortOrder(String sortString) {
    if (StringUtils.isBlank(sortString)) return null;
    val parts = sortString.split(" ");
    return SortOrder.safeValueOf(parts[1]);
  }

  /**
   * This will treat the input with a wildcard at the end if the input exists, and always ignoring case.
   */
  public static void like(StringExpression qPath, String input, BooleanExpressionBuilder builder) {
    if (StringUtils.isEmpty(input)) return;
    match(qPath, input + "*", builder, true, true);
  }

  private static void match(StringExpression qPath, String input, BooleanExpressionBuilder builder, boolean ignoreNull, boolean ignoreCase) {
    if (ignoreNull && input == null) return;
    val criterion = StringUtils.trim(input);
    val value = StringUtils.replace(criterion, "*", "");

    if (StringUtils.isNotBlank(value)) {
      if (criterion.startsWith("*") && criterion.endsWith("*")) {
        builder.append(ignoreCase ? qPath.containsIgnoreCase(value) : qPath.contains(value));
      }
      else if (criterion.startsWith("*")) {
        builder.append(ignoreCase ? qPath.endsWithIgnoreCase(value) : qPath.endsWith(value));
      }
      else if (criterion.endsWith("*")) {
        builder.append(ignoreCase ? qPath.startsWithIgnoreCase(value) : qPath.startsWith(value));
      }
      else {
        builder.append(ignoreCase ? qPath.equalsIgnoreCase(value) : qPath.eq(value));
      }
    }
  }

  private static List<Field> getFilteredFields(BeanPath<?> qEntity) {
    Field[] allFields = qEntity.getClass().getDeclaredFields();
    List<Field> fields = new ArrayList<>();
    for (Field field : allFields) {
      if (Modifier.isPublic(field.getModifiers())) {
        if (StringExpression.class.isAssignableFrom(field.getType()) || NumberPath.class.isAssignableFrom(field.getType()) || DateTimePath.class.isAssignableFrom(field.getType())) {
          fields.add(field);
        }
      }
    }

    return fields;
  }

  private static String dataTablesFilter(String property) {
    if (property == null) return null;
    // added to strip the .label subvalue for enums.
    return property.replace(".label", "");
  }
}
