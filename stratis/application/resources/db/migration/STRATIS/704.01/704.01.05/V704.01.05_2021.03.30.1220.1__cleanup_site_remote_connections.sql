update SITE_INTERFACES
set connection_id = null
where connection_id = 2;

delete
from site_remote_connections
where HOST_NAME = 'SASSY'
   or HOST_NAME = 'AMS-CMOS';