package mil.usmc.mls2.stratis.common.domain.model;

public enum SearchType {

  SEARCH_ONLY,
  COUNT_ONLY,
  FULL;

  public boolean includesSearch() {
    return SearchType.COUNT_ONLY != this;
  }

  public boolean includesCount() {
    return SEARCH_ONLY != this;
  }

  public boolean isFull() {
    return SearchType.FULL == this;
  }
}