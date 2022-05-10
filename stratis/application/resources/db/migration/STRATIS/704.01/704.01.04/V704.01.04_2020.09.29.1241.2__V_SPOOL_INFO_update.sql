CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_SPOOL_INFO"
            ("TIMESTAMP", "TRANSACTION_TYPE", "STATUS", "QTY", "DOCUMENT_NUMBER", "UI", "CC", "PRI", "SPOOL_ID", "NIIN_ID",
             "SPOOL_DEF_MODE", "TIMESTAMP_SENT", "SPOOL_BATCH_NUM", "RCN", "SCN")
AS
SELECT TIMESTAMP,
       NVL(transaction_type, 'n/a') transaction_type,
       NVL(pkg_spool_data.f_get_status(spool_id, spool_def_mode), 'n/a')
                                    status,
       pkg_spool_data.f_get_qty(spool_id,
                                spool_def_mode,
                                transaction_type)
                                    qty,
       pkg_spool_data.f_get_doc_number(spool_id,
                                       spool_def_mode,
                                       transaction_type)
                                    document_number,
       pkg_spool_data.f_get_ui(spool_id,
                               spool_def_mode,
                               transaction_type)
                                    ui,
       pkg_spool_data.f_get_cc(spool_id,
                               spool_def_mode,
                               transaction_type)
                                    cc,
       NVL(pkg_spool_data.f_get_pri(spool_id, spool_def_mode), 'n/a')
                                    pri,
       spool_id,
       niin_id,
       spool_def_mode,
       timestamp_sent,
       spool_batch_num,
       NVL(pkg_spool_data.f_get_rcn(spool_id, spool_def_mode), 'n/a')
                                    rcn,
       NVL(pkg_spool_data.f_get_scn(spool_id, spool_def_mode), 'n/a')
                                    scn
FROM spool
UNION ALL
SELECT TIMESTAMP,
       NVL(transaction_type, 'n/a') transaction_type,
       NVL(pkg_spool_data.f_get_status_hist(spool_id, spool_def_mode),
           'n/a')
                                    status,
       pkg_spool_data.f_get_qty_hist(spool_id,
                                     spool_def_mode,
                                     transaction_type)
                                    qty,
       pkg_spool_data.f_get_doc_number_hist(spool_id,
                                            spool_def_mode,
                                            transaction_type)
                                    document_number,
       pkg_spool_data.f_get_ui_hist(spool_id,
                                    spool_def_mode,
                                    transaction_type)
                                    ui,
       pkg_spool_data.f_get_cc_hist(spool_id,
                                    spool_def_mode,
                                    transaction_type)
                                    cc,
       NVL(pkg_spool_data.f_get_pri_hist(spool_id, spool_def_mode),
           'n/a')
                                    pri,
       spool_id,
       niin_id,
       spool_def_mode,
       timestamp_sent,
       spool_batch_num,
       NVL(pkg_spool_data.f_get_rcn_hist(spool_id, spool_def_mode),
           'n/a')
                                    rcn,
       NVL(pkg_spool_data.f_get_scn_hist(spool_id, spool_def_mode),
           'n/a')
                                    scn
FROM spool_hist
WHERE TRUNC(SYSDATE) - TRUNC(timestamp) <=
      pkg_hison.f_get_report_num_days('HISON');


GRANT SELECT ON "SGA"."V_SPOOL_INFO" TO "STRATIS_USER";
