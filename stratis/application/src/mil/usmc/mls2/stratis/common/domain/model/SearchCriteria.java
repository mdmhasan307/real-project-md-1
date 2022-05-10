package mil.usmc.mls2.stratis.common.domain.model;

public interface SearchCriteria extends Sorting, CriteriaBounds {

  SearchType searchType();
}
