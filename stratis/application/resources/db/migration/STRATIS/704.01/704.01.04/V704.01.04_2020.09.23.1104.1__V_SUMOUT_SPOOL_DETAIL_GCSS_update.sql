CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_SUMOUT_SPOOL_DETAIL_GCSS"
            ("ID", "TIMESTAMP", "GCSS_DOC_NUMBER", "GCSS_ROUTE", "GCSS_NIIN", "GCSS_UI", "GCSS_QTY", "GCSS_CC", "GCSS_DATE", "PRIORITY",
             "TRANSACTION_TYPE")
AS
SELECT s.spool_id,
       TO_CHAR((s.timestamp),
               'Day DD Mon rrrr' || ', ' || 'hh24:mi:ss (ddd)'),
       pkg_strat_func.f_get_gcss_doc_number(s.spool_id),
       pkg_strat_func.f_get_gcss_route(s.spool_id),
       pkg_strat_func.f_get_gcss_niin(s.spool_id),
       pkg_strat_func.f_get_gcss_ui(s.spool_id),
       pkg_strat_func.f_get_gcss_qty(s.spool_id),
       pkg_strat_func.f_get_gcss_cc(s.spool_id),
       TO_TIMESTAMP_TZ(REPLACE(pkg_strat_func.f_get_gcss_date(s.spool_id), 'T', ' '), 'YYYY-MM-DD HH:MI:SS.FFTZH:TZM'),
       s.priority,
       s.transaction_type
FROM spool s
WHERE (s.spool_def_mode = 'G')
ORDER BY TO_CHAR((s.timestamp),
                 'Day DD Mon rrrr' || ', ' || 'hh24:mi:ss (ddd)') DESC,
         s.transaction_type ASC,
         pkg_strat_func.f_get_gcss_doc_number(s.spool_id) ASC;

GRANT SELECT ON "SGA"."V_SUMOUT_SPOOL_DETAIL_GCSS" TO "STRATIS_USER";
