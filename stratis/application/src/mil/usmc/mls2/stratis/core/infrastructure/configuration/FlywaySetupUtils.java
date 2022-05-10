package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.experimental.UtilityClass;
import org.flywaydb.core.Flyway;

@UtilityClass
public class FlywaySetupUtils {

  public void setupFlyway(boolean flywayEnabled, String flywayUrl, String flywayUsername, String flywayPassword, String flywayLocations, String flywayTable, String flywayDefaultSchema) {
    if (flywayEnabled)
      Flyway.configure()
          .dataSource(flywayUrl, flywayUsername, flywayPassword)
          .locations(flywayLocations)
          .table(flywayTable)
          .schemas(flywayDefaultSchema)
          .baselineOnMigrate(true)
          .baselineVersion("704.01.03")
          .validateOnMigrate(false) // This value checks checksums of previously run Flyway scripts, which should not be necessary.
          .load()
          .migrate();
  }
}
