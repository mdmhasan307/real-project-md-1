CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_RECEIPTS_ACTIVE_RECEIPTS_RPT"
            ("LOCATION_LABEL", "CREATED_DATE_CHAR", "NIIN", "RCN", "DOCUMENT_NUMBER", "STATUS", "SID", "TOT_QUANTITY_STOWED",
             "TOT_QUANTITY_INDUCTED") AS
SELECT DECODE(l.location_label, 'NONE', NULL, l.location_label),
       r.created_date,
       ni.niin,
       r.rcn,
       r.document_number,
       r.status,
       s.sid,
       SUM(r.quantity_stowed),
       SUM(r.quantity_inducted)
FROM location l,
     niin_info ni,
     receipt r,
     stow s
WHERE ((ni.niin_id = r.niin_id)
    AND (r.rcn = s.rcn(+))
    AND (l.location_id(+) = s.location_id))
GROUP BY DECODE(l.location_label, 'NONE', NULL, l.location_label),
         r.created_date,
         ni.niin,
         r.rcn,
         r.document_number,
         r.status,
         s.sid
ORDER BY r.rcn ASC,
         r.created_date ASC,
         r.document_number ASC,
         r.status ASC,
         s.sid ASC;


GRANT SELECT ON "SGA"."V_RECEIPTS_ACTIVE_RECEIPTS_RPT" TO "STRATIS_USER";
