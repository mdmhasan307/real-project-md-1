package mil.usmc.mls2.stratis.core.infrastructure.util;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.collections4.comparators.NullComparator;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Sort Utility Class.
 * <p>
 * "sortString" parameters may take any of the following forms:
 * - single property name
 * - single property name with space separated sort order of "asc" or "desc"
 * - combination of the above, separated by a ","
 * <p>
 * sort order will default to asc for a property if not specified
 * <p>
 * examples:
 * - "x"
 * - "x asc"
 * - "x,y"
 * - "x asc, y"
 * - "x asc, y desc"
 * - "x, y desc"
 * - "x,y,z"
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SortUtils {

  public static <T> void sort(List<T> list, String sortString) {
    sort(list, sortString, false);
  }

  public static <T> void sort(List<T> list, String sortString, boolean caseSensitive) {
    if (list == null) {
      return;
    }

    val sortOrderPairs = createSortOrderPairs(sortString);
    val comparator = createComparator(sortOrderPairs, caseSensitive);

    list.sort(comparator);
  }

  @SuppressWarnings("unchecked")
  public static <T> void sort(T[] array, String sortString) {
    if (array == null) {
      return;
    }

    val sortOrderPairs = createSortOrderPairs(sortString);
    val comparator = createComparator(sortOrderPairs, false);
    Arrays.sort(array, comparator);
  }

  @SuppressWarnings("unchecked")
  public static <T> void reverseSort(List<T> list, String sortString) {
    if (list == null) {
      return;
    }

    sort(list, sortString);
    Collections.reverse(list);
  }

  @SuppressWarnings("unchecked unused")
  public static <T> void reverseSort(T[] array, String sortString) {
    if (array == null) {
      return;
    }

    sort(array, sortString);
    ArrayUtils.reverse(array);
  }

  /* first-order sort */
  public static <T> void sort(T[] array) {
    if (array == null) {
      return;
    }

    Arrays.sort(array);
  }

  /* first-order sort */
  public static <T extends Comparable<? super T>> void sort(List<T> list) {
    if (list == null) {
      return;
    }

    Collections.sort(list);
  }

  @SuppressWarnings("unchecked")
  public static <T> void sort(List<T> list, SortOrderPair... sortOrderPairs) {
    if ((list == null || list.isEmpty()) || (sortOrderPairs == null || sortOrderPairs.length == 0)) {
      return;
    }

    list.sort(createComparator(Arrays.asList(sortOrderPairs), false));
  }

  @SuppressWarnings("Duplicates")
  private static List<SortOrderPair> createSortOrderPairs(String inputString) {
    final List<SortOrderPair> sorts = new ArrayList<>();
    final String criteriaSortColumn = StringUtils.trim(inputString);
    if (StringUtils.isNotBlank(criteriaSortColumn)) {
      String[] allSortColumnTokens = criteriaSortColumn.split(",");

      if (allSortColumnTokens.length == 1) {
        String sortColumnToken = StringUtils.trim(allSortColumnTokens[0]);
        String[] sortColumnTokens = sortColumnToken.split(" ");
        String sortColumn = StringUtils.trim(sortColumnTokens[0]);
        SortOrder sortColumnSortOrder = sortColumnTokens.length > 1 ? SortOrder.valueOf(StringUtils.trim(sortColumnTokens[1])) : SortOrder.ASC;

        sorts.add(new SortOrderPair(sortColumn, sortColumnSortOrder));
      }
      else {
        for (String sortColumnToken : allSortColumnTokens) {
          sortColumnToken = StringUtils.trim(sortColumnToken);
          String[] sortColumnTokens = sortColumnToken.split(" ");
          String sortColumn = StringUtils.trim(sortColumnTokens[0]);
          SortOrder sortColumnSortOrder = sortColumnTokens.length > 1 ? SortOrder.valueOf(StringUtils.trim(sortColumnTokens[1])) : SortOrder.ASC;

          sorts.add(new SortOrderPair(sortColumn, sortColumnSortOrder));
        }
      }
    }

    return sorts;
  }

  private static Transformer caseInsensitiveTransformer = x -> x instanceof String ? ((String) x).toLowerCase() : x;

  @SuppressWarnings("unchecked")
  private static <T> Comparator<T> createComparator(Collection<SortOrderPair> sortOrderPairs, boolean caseSensitive) {
    val chain = new ComparatorChain();
    val caseInsensitiveComparator = new TransformingComparator(caseInsensitiveTransformer);

    for (val sortOrderPair : sortOrderPairs) {
      final Comparator<?> comparatorToUse;
      if (sortOrderPair.isOrderAscending()) {
        if (caseSensitive) {
          comparatorToUse = new NullSafeBeanComparator(sortOrderPair.getProperty(), new NullComparator());
        }
        else {
          comparatorToUse = new NullSafeBeanComparator(sortOrderPair.getProperty(), new NullComparator(caseInsensitiveComparator));
        }
      }
      else {
        if (caseSensitive) {
          comparatorToUse = new NullSafeBeanComparator(sortOrderPair.getProperty(), new ReverseComparator(new NullComparator()));
        }
        else {
          comparatorToUse = new NullSafeBeanComparator(sortOrderPair.getProperty(), new ReverseComparator(new NullComparator(caseInsensitiveComparator)));
        }
      }

      chain.addComparator(comparatorToUse);
    }

    return chain;
  }

  @Getter
  @Setter
  @ToString
  private static class SortOrderPair {

    private String property;
    private SortOrder order;

    public SortOrderPair(String property, SortOrder order) {
      setProperty(property);
      setOrder(order);
    }

    public boolean isOrderAscending() {
      return SortOrder.ASC == order;
    }

    public void setOrder(SortOrder order) {
      this.order = order;
    }

    public void setOrder(String order) {
      this.order = order != null ? (order.toLowerCase().contains("desc") ? SortOrder.DESC : SortOrder.ASC) : SortOrder.ASC;
    }
  }
}