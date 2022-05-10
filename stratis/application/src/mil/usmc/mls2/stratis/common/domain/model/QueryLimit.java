package mil.usmc.mls2.stratis.common.domain.model;

public class QueryLimit {

  public static final QueryLimit NONE = new QueryLimit(0, 0);

  private final long offset;
  private final long maxItems;

  public QueryLimit(long offset, long maxItems) {
    this.offset = offset;
    this.maxItems = maxItems;
  }

  public static QueryLimit asMaxItems(long maxItems) {
    return new QueryLimit(0, maxItems);
  }

  public static QueryLimit asOffset(long offset) {
    return new QueryLimit(offset, 0);
  }

  public long getOffset() {
    return offset;
  }

  public long getMaxItems() {
    return maxItems;
  }

  @Override
  public String toString() {
    return "QueryLimit{" +
        "offset=" + offset +
        ", maxItems=" + maxItems +
        '}';
  }
}
