CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_IMPORTS_FILE_LOG_DASF_RPT"
            ("ID", "DATALOAD_STATUS", "INTERFACE_NAME", "DESCRIPTION", "LINE_NO", "CREATED_DATE") AS
SELECT rdl.ref_dataload_log_id,
       pkg_strat_func.f_get_data_row(rdl.ref_dataload_log_id),
       rdl.interface_name,
       rdl.description,
       rdl.line_no,
       rdl.created_date
FROM ref_dataload_log rdl
WHERE (rdl.interface_name = 'DASF')
ORDER BY rdl.interface_name ASC, (TRUNC((rdl.created_date))) DESC;


GRANT SELECT ON "SGA"."V_IMPORTS_FILE_LOG_DASF_RPT" TO "STRATIS_USER";
