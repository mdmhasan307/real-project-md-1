package mil.usmc.mls2.stratis.core.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.repository.common.Mls2SitesRepository;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
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
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class MultiTenantLocationSortBuilder implements LocationSortBuilder {

  private final Mls2SitesRepository mls2SitesRepository;

  private ArrayList<Column> jndiOrder;

  private void config() {
    val siteName = UserSiteSelectionTracker.getUserSiteSelection();

    val site = mls2SitesRepository.getBySiteName(siteName);
    val sort = site.isPresent() ? site.get().getSortOrder() : "DEFAULT";

    switch (sort) {
      case "LEGACY_DEFAULT":
        jndiOrder = new ArrayList<>(Arrays.asList(aisle, bay, locLevel, slot)); //pre 704.01.02 default sort
        break;
      case "ALTERNATE":
        jndiOrder = new ArrayList<>(Arrays.asList(bay, side, aisle, locLevel, slot));
        break;
      case "ALTERNATE2":
        jndiOrder = new ArrayList<>(Arrays.asList(bay, aisle, locLevel, slot)); //variation of DEFAULT that includes Aisle
        break;
      default:
        jndiOrder = new ArrayList<>(Arrays.asList(bay, locLevel, slot)); //defaults to DEFAULT order
        break;
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
    config();
    StringJoiner sj = new StringJoiner(", ");
    for (Column col : jndiOrder) {
      sj.add(getStringForQuery(queryType, col));
    }

    return sj.toString();
  }
}
