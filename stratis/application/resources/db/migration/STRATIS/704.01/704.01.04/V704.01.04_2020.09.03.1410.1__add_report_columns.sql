--1. Workload Detail (Picking Packing Shipping)
CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_WORKLOAD_DETAIL_PICKPACKSHIP" ("PACKED_DATE", "TIME_OF_PICK", "PICKED_BY", "PACKED_BY", "AAC", "EQUIP_NAME", "DOCUMENT_NUMBER", "SUPPLEMENTARY_ADDRESS", "PRIORITY", "LOCATION_LABEL", "NIIN", "NOMENCLATURE", "UI", "PACK_COLUMN", "PACK_LEVEL", "SCN", "PIN", "STATUS", "PICK_QTY", "WAC_NUMBER", "CURRENT_DATE") AS
  SELECT /*******************************************************************************
--------------------------------------------------------------------------------
-- Module Name    : v_workload_detail_pickpackship
-- Purpose        : view supports report workload_detail_pickpackship
--
--------------------------------------------------------------------------------
Modification History
--------------------------------------------------------------------------------
Name           Version       Description
--------------------------------------------------------------------------------
Kane Suresh   20130821.001  Moved SQL from disco to DB
Kane Suresh   20170912.001  Added a col issue.issue_priority_designator
                             for reports re-write into App server
Gary Weible   704.01.04     Added Nomenclature and Supplementary Address
*******************************************************************************/
             p.packed_date,
             p.time_of_pick,
             pkg_strat_func.f_get_user (p.picked_by),
             pkg_strat_func.f_get_user (pc.packed_by),
             c.aac,
             e.name,
             issue.document_number,
             issue.SUPPLEMENTARY_ADDRESS,
             issue.issue_priority_designator , --20170912.001
             l.location_label,
             ni.niin,
             ni.nomenclature,
             ni.ui,
             pc.pack_column,
             pc.pack_level,
             p.scn,
             p.pin,
             p.status,
             p.pick_qty,
             w.wac_number,
             SYSDATE
        FROM customer             c,
             equip                e,
             location             l,
             niin_info            ni,
             niin_location        nl,
             packing_consolidation pc,
             packing_station      ps,
             picking              p,
             wac                  w,
             issue                issue
       WHERE     (    (c.customer_id(+) = issue.customer_id)
                  AND (w.wac_id = l.wac_id)
                  AND (l.location_id = nl.location_id)
                  AND (ni.niin_id = issue.niin_id(+))
                  AND (ps.packing_station_id(+) = pc.packing_station_id)
                  AND (e.equipment_number(+) = ps.equipment_number)
                  AND (pc.packing_consolidation_id(+) =
                       p.packing_consolidation_id)
                  AND (issue.scn = p.scn)
                  AND (nl.niin_loc_id = p.niin_loc_id))
             AND (NVL ('Issue type', 'X') NOT IN ('W', 'R'))
             --and (ni.niin like :niin)
             --and (upper (p.status) like upper ( :"Status Parameter 1"))
             --and (upper (w.wac_number) like upper ( :"Wac number Parameter 1"))
             AND (((   (   issue.issue_type IN ('B',
                                                'R',
                                                'W',
                                                '')
                        OR issue.issue_type IS NULL)
                    OR issue.issue_type IS NULL)))
    ORDER BY w.wac_number ASC, e.name ASC, ni.ui ASC;


UPDATE (SELECT sqls.SQL_TEXT
        FROM RPT_SQLS sqls, RPT_NAMES names
        WHERE sqls.RPT_ID = names.RPT_ID and names.rpt_name = 'Workload Detail (Picking, Packing, Shipping)') ilv
SET ilv.SQL_TEXT = 'SELECT O225003.AAC, O225003.DOCUMENT_NUMBER, O225003.priority, O225003.EQUIP_NAME,
O225003.LOCATION_LABEL, O225003.NIIN, O225003.NOMENCLATURE, O225003.PACKED_BY, O225003.PACKED_DATE, O225003.PICK_QTY,
O225003.PICKED_BY, O225003.PIN, O225003.SCN, O225003.STATUS, O225003.TIME_OF_PICK, O225003.UI, O225003.SUPPLEMENTARY_ADDRESS,
O225003.WAC_NUMBER, SUM(O225003.PACK_LEVEL), SUM(O225003.PACK_COLUMN),
TO_CHAR(SYSDATE,''Day DD Mon rrrr''||'', ''||''hh24:mi:ss (ddd)'')
FROM SGA.V_WORKLOAD_DETAIL_PICKPACKSHIP O225003
WHERE ( UPPER(O225003.STATUS) LIKE UPPER(NVL(:"pm_Wkload_PPS_Status",''%'')) )
AND ( O225003.NIIN LIKE NVL(:"pm_filter_niin",''%'') )
AND ( UPPER(O225003.WAC_NUMBER) LIKE UPPER(NVL(:"pm_wac1",''%'')) )
AND ( UPPER(O225003.priority) LIKE UPPER(NVL(:"pm_priority",''%'')) )
AND ( UPPER(O225003.DOCUMENT_NUMBER) LIKE UPPER(NVL(:"pm_document_number",''%'')) )
GROUP BY O225003.AAC, O225003.DOCUMENT_NUMBER, O225003.priority, O225003.EQUIP_NAME, O225003.LOCATION_LABEL,
O225003.NIIN, O225003.NOMENCLATURE, O225003.PACKED_BY, O225003.PACKED_DATE, O225003.PICK_QTY, O225003.PICKED_BY, O225003.PIN,
O225003.SCN, O225003.STATUS, O225003.TIME_OF_PICK, O225003.UI,O225003.SUPPLEMENTARY_ADDRESS, O225003.WAC_NUMBER
ORDER BY O225003.WAC_NUMBER ASC, O225003.LOCATION_LABEL ASC, O225003.SCN ASC, O225003.NIIN ASC, O225003.AAC ASC, O225003.UI ASC;';


--2. Workload Detail (Stowing)
 CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_WORKLOAD_DETAIL_STOWING" ("LOCATION_LABEL", "SID", "QTY_TO_BE_STOWED", "STATUS", "NIIN", "NOMENCLATURE", "WAC_NUMBER", "STOW_QTY", "STOWED_BY", "PACK_AREA", "CURRENT_DATE") AS
  SELECT /*******************************************************************************
              --------------------------------------------------------------------------------
              -- Module Name    : v_workload_detail_stowing
              -- Purpose        : view supports report workload_detail_stowing
              --
              --------------------------------------------------------------------------------
              Modification History
              --------------------------------------------------------------------------------
              Name           Version       Description
              --------------------------------------------------------------------------------
              Kane Suresh   20130821.001  Moved SQL from disco to DB
              Kane Suresh   20170919.001  Added new col stowed_by, pack_area for reports
              Kane Suresh   20171010.001  stow_by (user_id ) replaced with user name
              Gary Weible   704.01.04     Added Nomenclature
              *******************************************************************************/
             l.location_label,
             s.sid,
             s.qty_to_be_stowed,
             s.status,
             i.niin,
             i.nomenclature,
             w.wac_number,
             s.stow_qty,
             pkg_strat_func.f_get_user (s.stowed_by),
             w.pack_area,
             SYSDATE
        FROM location l, stow s, wac w, receipt r, niin_info i
       WHERE     ((w.wac_id = l.wac_id) AND (l.location_id = s.location_id))
             AND (s.RCN = r.RCN) and (r.NIIN_ID = i.niin_id)
             AND (s.status NOT IN ('STOWED', 'STOW CANCEL'))
    --AND (UPPER (w.WAC_NUMBER) LIKE UPPER ( :"Wac number Parameter 1"))
    ORDER BY w.wac_number ASC,
             l.location_label ASC,
             s.sid ASC,
             s.qty_to_be_stowed ASC,
             s.status ASC,
             s.stow_qty ASC;


UPDATE (SELECT sqls.SQL_TEXT
        FROM RPT_SQLS sqls, RPT_NAMES names
        WHERE sqls.RPT_ID = names.RPT_ID and names.rpt_name = 'Worklod Detail (Stowing)') ilv
SET ilv.SQL_TEXT = 'SELECT O225004.LOCATION_LABEL, O225004.SID, O225004.STATUS, O225004.WAC_NUMBER, O225004.stowed_by, O225004.pack_area, O225004.NIIN, O225004.NOMENCLATURE,
SUM(O225004.STOW_QTY), SUM(O225004.QTY_TO_BE_STOWED), TO_CHAR(SYSDATE,''Day DD Mon rrrr''||'', ''||''hh24:mi:ss (ddd)'')
FROM SGA.V_WORKLOAD_DETAIL_STOWING O225004
WHERE ( UPPER(O225004.WAC_NUMBER) like UPPER(NVL(:"pm_Stow_WAC",''%'')) )
 AND ( UPPER(O225004.PACK_AREA) like UPPER(NVL(:"pm_pack_area",''%'')) )
GROUP BY O225004.LOCATION_LABEL, O225004.SID, O225004.STATUS, O225004.WAC_NUMBER,O225004.stowed_by, O225004.pack_area, O225004.NIIN, O225004.NOMENCLATURE
ORDER BY O225004.WAC_NUMBER ASC, O225004.LOCATION_LABEL ASC, O225004.SID ASC, O225004.STATUS ASC;';


--3. Workload Detail (Expiring NIINs)
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_WORKLOAD_DETAIL_EXPIRE_NIIN" ("LOCATION_LABEL", "NIIN", "NOMENCLATURE", "QTY", "EXPIRATION_DATE", "CURRENT_DATE") AS
  SELECT /*******************************************************************************
                 --------------------------------------------------------------------------------
                 -- Module Name    : v_workload_detail_expire_niin
                 -- Purpose        : view supports report workload_detail_expire_niin
                 --
                 --------------------------------------------------------------------------------
                 Modification History
                 --------------------------------------------------------------------------------
                 Name           Version       Description
                 --------------------------------------------------------------------------------
                 Kane Suresh   20130821.001  Moved SQL from disco to DB
                 Gary Weible   704.01.04     Added Nomenclature
                 *******************************************************************************/
           l.location_label,
            ni.niin,
            ni.nomenclature,
            nl.qty,
            nl.expiration_date,
            SYSDATE
       FROM location l,
            niin_info ni,
            niin_location nl,
            wac w
      WHERE     (    (w.wac_id = l.wac_id)
                 AND (l.location_id = nl.location_id)
                 AND (ni.niin_id = nl.niin_id))
            AND (TO_CHAR ( (nl.expiration_date), 'MON,RRRR') =
                    TO_CHAR (SYSDATE, 'MON,RRRR'))
   --AND (ni.NIIN LIKE :NIIN)
   --AND (UPPER (w.WAC_NUMBER) LIKE UPPER ( :"Wac number Parameter 1"))
   ORDER BY l.location_label ASC, ni.niin ASC, (nl.expiration_date) ASC;

UPDATE (SELECT sqls.SQL_TEXT
        FROM RPT_SQLS sqls, RPT_NAMES names
        WHERE sqls.RPT_ID = names.RPT_ID and names.rpt_name = 'Workload Detail (Expiring NIINs)') ilv
SET ilv.SQL_TEXT = 'SELECT O225000.EXPIRATION_DATE, O225000.LOCATION_LABEL, O225000.NIIN, O225000.NOMENCLATURE, O225000.QTY,
TO_CHAR(SYSDATE,''Day DD Mon rrrr''||'', ''||''hh24:mi:ss (ddd)'')
FROM SGA.V_WORKLOAD_DETAIL_EXPIRE_NIIN O225000
WHERE ( TO_CHAR(O225000.EXPIRATION_DATE,UPPER(''MON-YYYY'')) = NVL(:"pm_exp_niin_exp_dt",TO_CHAR(SYSDATE,UPPER(''MON-YYYY''))) )
AND ( O225000.NIIN LIKE :"pm_nsn_niin"||''%'' )
AND ( ( LPAD(O225000.LOCATION_LABEL,3,0) ) LIKE UPPER(:"pm_wac_number")||UPPER(''%'') )
ORDER BY O225000.LOCATION_LABEL ASC, O225000.NIIN ASC, O225000.EXPIRATION_DATE ASC;';



--4. Workload Detail (NSN Updates)
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_WORKLOAD_DETAIL_NSN_UPDATES" ("LOCATION_LABEL", "NIIN", "NOMENCLATURE", "EXPIRATION_DATE", "WAC_NUMBER", "NSN_REMARK", "CURRENT_DATE") AS
  SELECT /*******************************************************************************
        --------------------------------------------------------------------------------
        -- Module Name    : v_workload_detail_nsn_updates
        -- Purpose        : view supports report workload_detail_nsn_updates
        --
        --------------------------------------------------------------------------------
        Modification History
        --------------------------------------------------------------------------------
        Name           Version       Description
        --------------------------------------------------------------------------------
        Kane Suresh   20130821.001  Moved SQL from disco to DB
        Gary Weible   704.01.04     Added Nomenclature
        *******************************************************************************/
           l.location_label,
            ni.niin,
            ni.nomenclature,
            (nl.expiration_date),
            w.wac_number,
            nl.nsn_remark,
            SYSDATE
       FROM location l,
            niin_info ni,
            niin_location nl,
            wac w
      WHERE     (    (w.wac_id = l.wac_id)
                 AND (l.location_id = nl.location_id)
                 AND (ni.niin_id = nl.niin_id))
            AND (nl.nsn_remark = 'Y')
   --and (ni.niin like :niin)
   --and (upper (w.wac_number) like upper ( :"Wac number Parameter 1"))
   ORDER BY w.wac_number ASC,
            l.location_label ASC,
            ni.niin ASC,
            (nl.expiration_date) ASC;


UPDATE (SELECT sqls.SQL_TEXT
        FROM RPT_SQLS sqls, RPT_NAMES names
        WHERE sqls.RPT_ID = names.RPT_ID and names.rpt_name = 'Workload Detail (NSN Updates)') ilv
SET ilv.SQL_TEXT = 'SELECT O225001.EXPIRATION_DATE, O225001.LOCATION_LABEL, O225001.NIIN, O225001.NOMENCLATURE, O225001.NSN_REMARK, O225001.WAC_NUMBER,
   TO_CHAR(SYSDATE,''Day DD Mon rrrr''||'', ''||''hh24:mi:ss (ddd)'')
FROM SGA.V_WORKLOAD_DETAIL_NSN_UPDATES O225001
WHERE (  O225001.NIIN like :"pm_nsn_niin"||''%''  ) AND ( UPPER(O225001.WAC_NUMBER) like UPPER(:"pm_wac_number")||''%'')
ORDER BY O225001.WAC_NUMBER ASC, O225001.LOCATION_LABEL ASC, O225001.NIIN ASC, O225001.EXPIRATION_DATE ASC, O225001.NSN_REMARK ASC;';

--5. Workload Detail (Pending Inventories)
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "SGA"."V_WORKLOAD_DETAIL_PENDING_INV" ("INV_TYPE", "STATUS", "LOCATION_LABEL", "NIIN", "NOMENCLATURE", "QTY", "WAC_NUMBER", "ASSIGN_TO_USER", "RELEASED_BY", "CUM_POS_ADJ", "CUM_NEG_ADJ", "PACK_AREA", "CURRENT_DATE") AS
  SELECT /*******************************************************************************
             --------------------------------------------------------------------------------
             -- Module Name    : v_workload_detail_Pending_inv
             -- Purpose        : view supports report workload_detail_Pending_inv
             --
             --------------------------------------------------------------------------------
             Modification History
             --------------------------------------------------------------------------------
             Name           Version       Description
             --------------------------------------------------------------------------------
             Kane Suresh   20130821.001  Moved SQL from disco to DB
             Kane Suresh   20170919.001  Added Addtl cols ii.Assign_to_user ,
                                          ii.Released_By, ii.Cum_Pos_Adj,  ii.Cum_Neg_Adj,
                                          w.PACK_AREA for new reports.
             Kane Suresh   20171011.001  ii.Assign_to_user modified to get user name instead of userid
             Gary Weible   704.01.04     Added Nomenclature
             *******************************************************************************/
             ii.inv_type,
             ii.status,
             l.location_label,
             ni.niin,
             ni.nomenclature,
             nl.qty,
             w.wac_number,
             --ii.Assign_to_user,
             pkg_strat_func.f_get_user (ii.Assign_to_user),
             ii.Released_By,
             ii.Cum_Pos_Adj,
             ii.Cum_Neg_Adj,
             w.PACK_AREA,
             SYSDATE
        FROM inventory_item ii,
             location      l,
             niin_info     ni,
             niin_location nl,
             wac           w
       WHERE     (    (nl.niin_loc_id = ii.niin_loc_id)
                  AND (w.wac_id = l.wac_id)
                  AND (l.location_id = nl.location_id)
                  AND (ni.niin_id = nl.niin_id))
             AND (ii.status IN ('INVENTORYPENDING', 'LOCSURVEYPENDING'))
    -- AND (ni.NIIN LIKE :NIIN)
    --AND (UPPER (w.WAC_NUMBER) LIKE  UPPER ( :"Wac number Parameter 1"))
    ORDER BY w.wac_number ASC,
             l.location_label ASC,
             ni.niin ASC,
             nl.qty ASC,
             ii.inv_type ASC,
             ii.status ASC;

UPDATE (SELECT sqls.SQL_TEXT
        FROM RPT_SQLS sqls, RPT_NAMES names
        WHERE sqls.RPT_ID = names.RPT_ID and names.rpt_name = 'Workload Detail (Pending Inventories)') ilv
SET ilv.SQL_TEXT = 'SELECT O225002.INV_TYPE, O225002.LOCATION_LABEL, O225002.NIIN, O225002.NOMENCLATURE, O225002.QTY, O225002.STATUS,
O225002.WAC_NUMBER, O225002.Assign_to_User,
    O225002.Released_By,
    O225002.Cum_Pos_Adj,
    O225002.Cum_Neg_Adj,
    O225002.PACK_AREA,
TO_CHAR(SYSDATE,''Day DD Mon rrrr''||'', ''||''hh24:mi:ss (ddd)'')
FROM SGA.V_WORKLOAD_DETAIL_PENDING_INV O225002
WHERE ( UPPER(O225002.NIIN) like UPPER(NVL(:"pm_InvPend_NIIN",''%'')) )
AND ( UPPER(O225002.WAC_NUMBER) like UPPER(NVL(:"pm_PendInv_WAC",''%'')) )
AND ( UPPER(O225002.PACK_AREA) like UPPER(NVL(:"pm_Pack_area",''%'')) )
ORDER BY O225002.WAC_NUMBER ASC, O225002.LOCATION_LABEL ASC, O225002.NIIN ASC, O225002.QTY ASC,
O225002.INV_TYPE ASC, O225002.STATUS ASC;';

COMMIT;
