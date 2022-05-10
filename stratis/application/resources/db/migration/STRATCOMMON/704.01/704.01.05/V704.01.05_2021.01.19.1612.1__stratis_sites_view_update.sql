CREATE OR REPLACE FORCE EDITIONABLE VIEW "INNOV"."STRATIS_SITES" ("SITE_NAME", "DESCN", "DB_STATUS", "LOCAL_TIME", "STATUS_ID") AS
SELECT SITE_NAME,
       DESCN,
       DECODE(pkg_common.f_pdb_up(DB_LINK_NAME), 1, 'Up', 'Down')
           DB_Status,
       TO_CHAR(pkg_common.f_pdb_localtime(DB_LINK_NAME),
               'dd-Mon-YYYY hh:Mi:SS AM')
           local_time,
       status_id
FROM MLS2_SITES;

