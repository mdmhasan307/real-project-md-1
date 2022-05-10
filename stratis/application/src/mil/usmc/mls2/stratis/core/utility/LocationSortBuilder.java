package mil.usmc.mls2.stratis.core.utility;

public interface LocationSortBuilder {

  class Column {

    final String columnName;
    final String hibernateColumnName;

    public Column(String columnName, String hibernateColumnName) {
      this.columnName = columnName;
      this.hibernateColumnName = hibernateColumnName;
    }
  }

  //Reminder again: Stratis.BAY is pulled from the "Aisle" position of the location label.
  LocationSortBuilder.Column bay = new LocationSortBuilder.Column("BAY", "bay");
  LocationSortBuilder.Column side = new LocationSortBuilder.Column("SIDE", "side");

  //Reminder two: Aisle is set to the Loc_Level pulled from the Segment portion of the location label.
  LocationSortBuilder.Column aisle = new LocationSortBuilder.Column("AISLE", "aisle");
  LocationSortBuilder.Column locLevel = new LocationSortBuilder.Column("LOC_LEVEL", "locLevel");
  LocationSortBuilder.Column slot = new LocationSortBuilder.Column("SLOT", "slot");

  enum QueryType {
    HIBERNATE_LOCATION,
    HIBERNATE_NIIN_LOCATION,
    ADF,
    ADF_QUALIFIED
  }

  String getSortString(LocationSortBuilder.QueryType queryType);
}
