This is the docker bundle for STRATIS tomcat server, containing the basic configuration
for the application server with the basic locked down configuration.

------------------------
The STRATIS stratis.war and gcss.war files under sources/application/ are 0-byte files.

To build a docker image manually, copy in the desired stratis.war and gcss.war files into sources/application/

When the stratis source repository triggers a build, it will use freshly built war files for the container.

------------------------

To build this container run the following command:

docker build -t stratis_tomcat:ubi8 -f Dockerfile.ubi8 .

or

docker build -t stratis_tomcat:ubi8_minimal -f Dockerfile.ubi8_minimal .

Note that the tag could be something else as required, however this works for the purposes
of this file, and should be an actual release version when used in production.

The build process should show files being downloaded and updated, ultimately resulting
in an output file. This may be verified with:

docker inspect stratis_tomcat:ubi8_minimal

This build does not expose any port, as this container is not expected to be accessed
from outside the docker network itself. This container will run the AJP service on
port 8009, and the apache httpd container will be able to connect to this host.

The following command will start the container in the foreground.

docker run --rm --name stratis_tomcat stratis_tomcat:ubi8

If desired to run the command in the background, the usual -d flag should be given:

docker run --rm --name stratis_tomcat stratis_tomcat:ubi8 -d

Configuration Settings
----------------------

For starting the STRATIS tomcat container, some settings must be provided as environment
variables so the contanier is able to access it's resources. Most settings have a default value
that is suitable for normal circumstances, however these may require tuning in some environments.

Database Configurations:

DB_URL (REQUIRED / jdbc:oracle:thin:@//127.0.0.1:1521/STRATTEST) - This environment variable is
required as the default value is likely invalid in all circumstances.

DB_PASSWORD (REQUIRED / ENC(XXXXX)) - This value must be provided and must be an encrypted
value with the ENC() border around the actual encrypted password value.

DB_USER (NOT REQUIRED / stratis) - The default value for this user setting is often suitable
for most configurations and does not require alteration normally.

DB_LOGIN_TIMEOUT (NOT REQUIRED / 10) - The timeout in seconds for the connection to the
STRATIS database.

DB_POOL_NAME (NOT REQUIRED / STRATISPool) - The configuration value for the database connection pool
configured for the STRATIS web application.

DB_GCSS_POOL_NAME (NOT REQUIRED / STRATISwsPool) - The configuration value for the database
connection pool for the GCSS web application.

DB_POOL_INITIAL_SIZE (NOT REQUIRED / 3) - The initial size to be established when connecting
to the STRATIS database in this connection pool.

DB_POOL_MAX_SIZE (NOT REQUIRED / 50) - The maximum size allowed for this database connection
pool before additional connections will not be added or established.

DB_POOL_MIN_SIZE (NOT REQUIRED / 2) - The minimum size for the database connection pool
to be reduced to, as connections are idle and freed.

DB_POOL_INACTIVE_TIMEOUT (NOT REQUIRED / 120) - The timeout used for governing when a connection
should be released and closed, and removed from the connection pool.

DB_POOL_MAX_CX_REUSE_TIME (NOT REQUIRED / 300) - The configuration for the duration on how
long a database connection may be reused before being released, closed, and removed from the pool.

DB_POOL_VALIDATE_CX_ON_BORROW (NOT REQUIRED / true) - This is a boolean configuration to determine
if the database pool should check the connection status before the connection is made available
to the application.

DB_POOL_MAX_STATEMENTS (NOT REQUIRED / 10) - This is a count of the number of statements
that should configured in the connection pool.

DB_STATS_FREQUENCY (NOT REQUIRED / 30) - This configures the number of seconds to use
as an interval between statistics logging for the database state, and is used in debugging
or tracing the server status.

Tomcat Configurations:

TOMCAT_SERVER_PORT (NOT REQUIRED / 9021) - The configuration for the server management
port for tomcat, typically bound to localhost to avoid external connections.

TOMCAT_SERVER_ADDRESS (NOT REQUIRED / 127.0.0.1) - The server address IP for the binding
for the administration service for tomcat, typically on localhost.

TOMCAT_CONNECTOR_PORT (NOT REQUIRED / 8009) - The configuration for the AJP listener port
for the tomcat server container, used for connections from Apache HTTPD.

STRATIS Configurations:

STRATIS_PICKING_ORDER (NOT REQUIRED / Default) - This is set to either Default or Alternate
and controls how the STRATIS application generates items for picking.