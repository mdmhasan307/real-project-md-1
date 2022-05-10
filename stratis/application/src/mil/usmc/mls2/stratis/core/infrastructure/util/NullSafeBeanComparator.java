package mil.usmc.mls2.stratis.core.infrastructure.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.Comparator;

/**
 * A port of Micah Mood's classic 2010 enhancement of Extreme Component's NullSafeBeanComparator
 */
@Slf4j
@SuppressWarnings({"rawtypes", "WeakerAccess", "unused"})
public class NullSafeBeanComparator<T> extends BeanComparator<T> {

  private final String property;
  private final boolean nullsAreHigh;

  @SuppressWarnings("squid:S1948")
  private final Comparator<T> comparator;

  public NullSafeBeanComparator(String property, Comparator<T> c) {
    this.comparator = c;
    this.property = property;
    this.nullsAreHigh = true;
  }

  public NullSafeBeanComparator(String property, Comparator<T> c, boolean nullAreHigh) {
    this.comparator = c;
    this.property = property;
    this.nullsAreHigh = nullAreHigh;
  }

  @Override
  public Comparator getComparator() {
    return comparator;
  }

  public boolean isNullsAreHigh() {
    return nullsAreHigh;
  }

  @Override
  public String getProperty() {
    return property;
  }

  /**
   * Compare beans safely. Handles NestedNullException thrown by PropertyUtils when the parent object is null
   */
  @SuppressWarnings("unchecked")
  @Override
  public int compare(T o1, T o2) {
    if (property == null) {
      // use the object's compare since no property is specified
      return this.comparator.compare(o1, o2);
    }

    T val1 = null;
    T val2 = null;

    try {
      val1 = (T) PropertyUtils.getProperty(o1, property);
    }
    catch (NestedNullException ignored) {
      // ignore
    }
    catch (Exception e) {
      log.warn("Failed to retrieve o1 property '{}' (aborting sort, trapping exception)", property, e);
      return 0;
    }

    try {
      val2 = (T) PropertyUtils.getProperty(o2, property);
    }
    catch (NestedNullException ignored) {
      // ignore
    }
    catch (Exception e) {
      log.warn("Failed to retrieve o2 property '{}' (aborting sort, trapping exception)", property, e);
      return 0;
    }

    try {
      //mjm 11/9/10: removed initial val1 == val2 as it
      //is incorrect and adversely affects multiple sorts.
      //also updated to return 0 if both are null, so that a multi-sort
      //will continue on to the next sort.
      if (val1 == null && val2 == null) {
        return 0;
      }

      if (val1 == null) {
        return this.nullsAreHigh ? 1 : -1;
      }

      if (val2 == null) {
        return this.nullsAreHigh ? -1 : 1;
      }

      return this.comparator.compare(val1, val2);
    }
    catch (Exception e) {
      log.warn("Failed to sort (trapping and returning 0)", e);
      return 0;
    }
  }

  @Override
  public boolean equals(Object x) {
    return super.equals(x);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}