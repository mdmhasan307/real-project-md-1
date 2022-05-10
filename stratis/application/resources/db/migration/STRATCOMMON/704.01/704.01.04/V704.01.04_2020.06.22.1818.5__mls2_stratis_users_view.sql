
  CREATE OR REPLACE FORCE EDITIONABLE VIEW INNOV.STRATIS_USERS ("SITE_NAME", "DESCN", "USERNAME", "FIRST_NAME", "MIDDLE_NAME", "LAST_NAME", "STATUS", "LOCKED", "LAST_LOGIN", "CAC_NUMBER", "EFF_START_DT") AS
  WITH
        sites AS (SELECT * FROM MLS2_SITES),
        usr AS (SELECT * FROM TABLE (pkg_common.fp_get_all_users))
    SELECT u.SITE_NAME,
           s.DESCN,
           u.USERNAME,
           u.FIRST_NAME,
           u.MIDDLE_NAME,
           u.LAST_NAME,
           u.STATUS,
           u.LOCKED,
           u.LAST_LOGIN,
           u.CAC_NUMBER,
           u.EFF_START_DT
      FROM usr u, sites s
     WHERE u.site_name = s.SITE_NAME;


--GRANT SELECT ON INNOV.STRATIS_USERS TO STRAT_COMMON_APP_ROLE;