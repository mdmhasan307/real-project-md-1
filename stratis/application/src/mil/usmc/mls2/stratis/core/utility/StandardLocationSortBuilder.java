package mil.usmc.mls2.stratis.core.utility;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.JNDIUtils;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * Reminder: in STRATIS, when locations are entered in the Admin section,
 * the 4th and 5th position of the location label ("Aisle" in the warehouse manual)
 * are stored as Location.BAY in the STRATIS database.
 * <p>
 * 1) The USMC Warehouse manual defines location label as
 * <p>
 * Area /First Position/Alpha.
 * Station /Second and Third Positions/Numeric.
 * Aisle or Row /Fourth and Fifth Positions/Numeric.
 * Segment /Sixth and Seventh Positions/Numeric.
 * Level /Eighth Position/Alpha.
 * Compartment /Ninth Position/Alpha
 * <p>
 * 2) When a STRATIS administrator enters a location via the STRATIS website, the location_label string is split up as
 * <p>
 * (WAC number should be derived from the WAC ID selected by the STRATIS user in a drop down)
 * <p>
 * Location.BAY / Fourth and Fifth Positions
 * <p>
 * Location.LOC_LEVEL / Sixth and Seven Positions
 * (This value is also reused for the Location.AISLE value as well, for locations entered via the STRATIS website)
 * <p>
 * Location.SLOT / Eighth and Ninth Positions
 */

@Slf4j
@Component
@Profile(Profiles.LEGACY)
public class StandardLocationSortBuilder implements LocationSortBuilder {

  @Value("${stratis.sortOrder}")
  private String sortOrder;

  private ArrayList<Column> config() {
    JNDIUtils lookup = JNDIUtils.getInstance();

    val sort = StringUtils.isEmpty(sortOrder) ? "DEFAULT" : sortOrder.toUpperCase(Locale.US);

    switch (sort) {
      case "LEGACY_DEFAULT":
        return new ArrayList<>(Arrays.asList(aisle, bay, locLevel, slot)); //pre 704.01.02 default sort
      case "ALTERNATE":
        return new ArrayList<>(Arrays.asList(bay, side, aisle, locLevel, slot));
      case "ALTERNATE2":
        return new ArrayList<>(Arrays.asList(bay, aisle, locLevel, slot)); //variation of DEFAULT that includes Aisle
      default:
        return new ArrayList<>(Arrays.asList(bay, locLevel, slot)); //defaults to DEFAULT order
    }
  }

  private String getStringForQuery(QueryType queryType, Column column) {
    switch (queryType) {
      case HIBERNATE_LOCATION:
        return "location." + column.hibernateColumnName;
      case HIBERNATE_NIIN_LOCATION:
        return "niinLocation.location." + column.hibernateColumnName;
      case ADF:
        return column.columnName.toUpperCase();
      case ADF_QUALIFIED:
        return "QRSLT." + column.columnName.toUpperCase();
    }

    return "unknown";
  }

  @Override
  public String getSortString(QueryType queryType) {
    val jndiOrder = config();
    StringJoiner sj = new StringJoiner(", ");
    for (Column col : jndiOrder) {
      sj.add(getStringForQuery(queryType, col));
    }

    return sj.toString();
  }
}
