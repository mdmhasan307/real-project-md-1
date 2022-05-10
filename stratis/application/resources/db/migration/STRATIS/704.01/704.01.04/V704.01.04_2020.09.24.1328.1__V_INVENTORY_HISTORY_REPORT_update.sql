CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_INVENTORY_HISTORY_REPORT"
            ("ID", "COMPLETED_DATE", "COMPLETED_BY", "STATUS", "NIIN", "NOMENCLATURE", "CC", "QTY", "LOCATION_LABEL", "CUM_NEG_ADJ",
             "CUM_POS_ADJ", "PRICE", "TOTAL_VALUE", "WAC")
AS
SELECT max(iih.rowid), --added to view to support hibernate having a unique identifier.
       iih.completed_date,
       pkg_strat_func.f_get_user(iih.completed_by) completedBy,
       iih.status,
       ni.niin,
       ni.nomenclature,
       nl_nlh.cc,
       nl_nlh.qty,
       l_w_l_wh.location_label,
       SUM(iih.cum_neg_adj),
       SUM(iih.cum_pos_adj),
       ni.price,
       SUM(ni.price * nl_nlh.qty),
       substr(l_w_l_wh.location_label, 0, 3)
FROM inventory_item_hist iih,
     niin_info ni,
     (SELECT niin_loc_id,
             niin_id,
             cc,
             qty,
             location_id
      FROM niin_location
      UNION
      SELECT niin_loc_id,
             niin_id,
             cc,
             qty,
             location_id
      FROM niin_location_hist) nl_nlh,
     (SELECT l.location_id, l.location_label, w.wac_number
      FROM location l,
           wac w
      WHERE l.wac_id = w.wac_id
      UNION
      SELECT lh.location_id, lh.location_label, w_wh.wac_number
      FROM location_hist lh,
           (SELECT wac_id, wac_number
            FROM wac_hist
            UNION
            SELECT wac_id, wac_number
            FROM wac) w_wh) l_w_l_wh
WHERE ((nl_nlh.niin_loc_id = iih.niin_loc_id)
    AND (ni.niin_id = nl_nlh.niin_id)
    AND (l_w_l_wh.location_id = nl_nlh.location_id))
GROUP BY iih.rowid,
         iih.completed_date,
         pkg_strat_func.f_get_user(iih.completed_by),
         iih.status,
         ni.niin,
         ni.nomenclature,
         nl_nlh.cc,
         nl_nlh.qty,
         ni.price,
         l_w_l_wh.location_label;


GRANT SELECT ON "SGA"."V_INVENTORY_HISTORY_REPORT" TO "STRATIS_USER";
