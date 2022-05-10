package mil.usmc.mls2.stratis.common.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

// FUTURE Add SuperBuilder INNOV MikeL/ChrisW Backlog Replace usage of this with the new SearchCriteria class (from MIGS/MIPS).  Also look at SortOrder / SortOrderPair, etc.
@ToString
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class BaseSearchCriteria implements Serializable {

  /**
   * properties:
   * <p/>
   * sortColumn:              the name of the column to be sorted
   * sortOrder:               the expected order of the sorted results (ASC or DESC).
   * offset:                  the offset from zero from which to begin results.
   * maxItems:                the maximum number of results to return.
   * message:                 a generic message field that may be used for communicating details about
   * search conditions or results.
   */

  private String sortColumn;
  private SortOrder sortOrder = SortOrder.ASC;
  private int offset = 0;
  private int maxItems = 0;  //defaulted to all records for now.
  private String message;
  private boolean supportsSorting = true;

  protected void setSupportsSorting(boolean supportsSorting) {
    this.supportsSorting = supportsSorting;
  }

  public final String getSortColumn() {
    return sortColumn;
  }

  public void setSortColumn(String sortString) {
    // ignore if whitespace, accept if null
    if (StringUtils.isWhitespace(sortString)) {
      return;
    }

    checkSortSupport();
    parseSortColumnAndSortOrder(sortString, true, false);
  }

  @JsonIgnore
  public final void setSort(String sortColumn, SortOrder sortOrder) {
    checkSortSupport();
    this.sortColumn = sortColumn;
    this.sortOrder = sortOrder;
  }

  public final SortOrder getSortOrder() {
    return this.sortOrder;
  }

  public final void setSortOrder(SortOrder sortOrder) {
    checkSortSupport();
    this.sortOrder = sortOrder != null ? sortOrder : SortOrder.ASC;
  }

  public final String getSortOrderString() {
    return sortOrder != null ? sortOrder.name().toLowerCase() : "";
  }

  public final int getOffset() {
    return offset;
  }

  public final void setOffset(int offset) {
    this.offset = offset;
  }

  public final int getMaxItems() {
    return maxItems;
  }

  public final void setMaxItems(int maxItems) {
    this.maxItems = maxItems;
  }

  public final String getMessage() {
    return message;
  }

  public final void setMessage(String message) {
    this.message = message;
  }

  private void checkSortSupport() {
    if (!supportsSorting) {
      throw new NotImplementedException("Sorting not supported by " + getClass().getSimpleName());
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void parseSortColumnAndSortOrder(String sortString, boolean overrideSortColumnIfSortStringIsEmpty, boolean overrideSortOrderIfSortStringEmpty) {
    if (StringUtils.isBlank(sortString)) {
      if (overrideSortColumnIfSortStringIsEmpty) sortColumn = null;
      if (overrideSortOrderIfSortStringEmpty) sortOrder = SortOrder.ASC;
      return;
    }

    // parse
    sortString = sortString.trim();
    String sortColumnString = sortString;
    String lowerSortString = StringUtils.lowerCase(sortString);

    // assign sort order (conditional)
    int sortIndex = calcSortIndex(lowerSortString);

    if (sortIndex >= 0) {
      String sortOrderString = lowerSortString.substring(sortIndex).trim();

      // assign sort order
      sortOrder = "desc".equals(sortOrderString) ? SortOrder.DESC : SortOrder.ASC;

      // reset the sortColumnString by removing the sort order content
      if (sortIndex > 0) {
        sortColumnString = sortString.substring(0, sortIndex).trim();
      }
      else {
        sortColumnString = null;
      }
    }

    // assign sort column (conditional)
    if (StringUtils.isNotBlank(sortColumnString)) {
      this.sortColumn = filterSortColumn(sortColumnString);
    }
  }

  private int calcSortIndex(String lowerSortString) {
    int sortIndex = -1;
    if (StringUtils.endsWith(lowerSortString, " asc")) {
      sortIndex = StringUtils.indexOf(lowerSortString, " asc");
    }
    else if (StringUtils.endsWith(lowerSortString, " desc")) {
      sortIndex = StringUtils.indexOf(lowerSortString, " desc");
    }
    else if (StringUtils.containsOnly(lowerSortString, "asc")) {
      sortIndex = StringUtils.indexOf(lowerSortString, "asc");
    }
    else if (StringUtils.containsOnly(lowerSortString, "desc")) {
      sortIndex = StringUtils.indexOf(lowerSortString, "desc");
    }

    return sortIndex;
  }

  /**
   * Subclasses should override as needed. Provides ability to filter sort column prior to setting
   *
   * @param sortColumnString sortColumnString
   * @return string
   */
  private String filterSortColumn(String sortColumnString) {
    // do nothing
    return sortColumnString;
  }
}
