create or replace PACKAGE BODY pkg_strat_procs
AS

    GCV_PKG_NAME            CONSTANT VARCHAR2 (30) := 'pkg_strat_procs';
    GCN_MAX_INACTIVE_DAYS   CONSTANT NUMBER := 35;

    --
    --
    -- retrieve all NIINs which exist in both gcss and stratis

    CURSOR CSR_get_recon_recs
        IS
        SELECT SYSDATE,
               ni.fsc,
               rg.record_fsc,
               ni.ui,
               rg.unit_of_issue,
               SUM (DECODE (nl.cc, 'A', NVL (nl.qty, 0), 0)) stratis_good_qty,
               NVL (TO_NUMBER (rg.on_hand_op_stock_serviceable), 0)
                                                             on_hand_op_stock_serviceable,
               ni.nomenclature,
               --rm.item_name_nomenclature,
               ni.scc,
               --rm.physical_security_code,
               ni.shelf_life_code stratis_slc,
               --rm.shelf_life_code sassy_slc,
               NVL (ni.ro_threshold, 0) ro_threshold,
               NVL (TO_NUMBER (rg.requisitioning_objective), 0)
                                                             requisitioning_objective,
               ni.price,
               --NVL (TO_NUMBER (rm.unit_price), 0) / 100 unit_price,
               'Y' in_sassy,
               --n.niin niin,
               rg.record_niin niin,
               SUM (DECODE (nl.cc, 'A', 0, NVL (nl.qty, 0))) stratis_bad_qty,
               NVL (TO_NUMBER (rg.on_hand_unserviceable), 0)
                                                             on_hand_unserviceable,
               --'Y' IN_STRATIS,
               CASE WHEN ni.niin IS NULL THEN 'N' ELSE 'Y' END in_stratis,
               rg.ref_gabf_id
        FROM niin_info ni, niin_location nl, ref_gabf rg                 --,
             --ref_mhif rm
        WHERE     ni.niin_id = nl.niin_id(+)
          AND ni.niin(+) = rg.record_niin
          AND NVL (rg.processed, 'N') = 'N'
          --AND rg.record_niin = rm.record_niin(+)
        GROUP BY ni.fsc,
                 rg.record_fsc,
                 ni.ui,
                 rg.unit_of_issue,
                 rg.on_hand_op_stock_serviceable,
                 ni.nomenclature,
                 --rm.item_name_nomenclature,
                 ni.scc,
                 --rm.physical_security_code,
                 ni.shelf_life_code,
                 --rm.shelf_life_code,
                 ni.ro_threshold,
                 rg.requisitioning_objective,
                 ni.price,
                 --rm.unit_price,
                 --n.niin,
                 rg.record_niin,
                 rg.on_hand_unserviceable,
                 ni.niin,
                 rg.ref_gabf_id;

    vn_records_read                  NUMBER;
    vd_run_date                      DATE;
    vn_count_lot                     NUMBER;
    vn_count_serial                  NUMBER;
    vn_ret_val                       NUMBER;
    vn_total_gabf_rowcount           NUMBER;
    vv_mhif_item_name_nomenclature   REF_MHIF.ITEM_NAME_NOMENCLATURE%TYPE;
    vv_mhif_physical_security_code   REF_MHIF.PHYSICAL_SECURITY_CODE%TYPE;
    vv_mhif_sassy_slc                REF_MHIF.SHELF_LIFE_CODE%TYPE;
    vn_mhif_unit_price               REF_MHIF.UNIT_PRICE%TYPE;

    /*******************************************************************************
        * f_get_niinid -
        *****************************************************************************/
    FUNCTION f_get_niinid (pmv_niin_in IN niin_info.niin%TYPE)
        RETURN NUMBER
        IS
        vn_niinid   niin_info.niin_id%TYPE;
    BEGIN
        SELECT niin_id
        INTO vn_niinid
        FROM niin_info
        WHERE niin = pmv_niin_in AND ROWNUM < 2;

        RETURN (vn_niinid);
    EXCEPTION
        WHEN OTHERS
            THEN
                RETURN -1;
        --No RAISE handle ret val in caller
    END f_get_niinid;

    /******************************************************************************
   * CLEAR_GCSSMC_HIST -  run periodically to clear the gcssmc_imports_data_hist table
   ******************************************************************************/
    PROCEDURE p_clear_gcssmc_hist
    AS
    BEGIN


        DELETE FROM gcssmc_imports_data_hist
        WHERE created_date < ADD_MONTHS (SYSDATE, -1);

        COMMIT;
        pkg_stratis_common.p_event_log (
                    'clear_gcssmc_hist - delete from gcssmc_imports_data_hist; rows ='
                    || TO_CHAR (SQL%ROWCOUNT));
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'clear_gcssmc_hist -' || SQLERRM (SQLCODE));
                ROLLBACK;
                RAISE;
    END p_clear_gcssmc_hist;

    /****************************************************************************
   * CLEAUNP_GCSSMC_IMPORTS_DATA -  cleans up the gcssmc_imports_data based in the interface name and status
    ******************************************************************************/
    PROCEDURE p_cleaunp_gcssmc_imports_data
    AS
    BEGIN
        DELETE FROM gcssmc_imports_data
        WHERE interface_name = 'MHIF' AND status = 'COMPLETED'; --Kane when do they delete MHIF not complete

        DELETE FROM gcssmc_imports_data
        WHERE     interface_name IN ('DASF', 'GABF', 'GBOF')
          AND TRUNC (SYSDATE) - TRUNC (created_date) > 3;

        COMMIT;
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'cleaunp_gcssmc_imports_data -' || SQLERRM (SQLCODE));
                ROLLBACK;
                RAISE;
    END p_cleaunp_gcssmc_imports_data;

    /******************************************************************************
   -- HITS_HIST_BY_NIIN  (Procedure)
   ******************************************************************************/
    PROCEDURE p_hits_hist_by_niin (pmv_NIIN_IN   IN     VARCHAR2,
                                   CURSOR_A         OUT SYS_REFCURSOR)
    AS
        vn_niinid   niin_info.niin_id%TYPE;
    BEGIN
        IF REPLACE (pmv_niin_in, ' ') IS NULL
        THEN
            raise_application_error (-20001, 'NIIN can not be blank or NULL');
        ELSE
            vn_niinid := f_get_niinid (pmv_niin_in => pmv_niin_in);

            IF NVL (vn_niinid, 0) > 0
            THEN
                DBMS_OUTPUT.put_line ('b4 open in pkg NIIN_ID = ' || vn_niinid);

                OPEN CURSOR_A FOR
                    SELECT 'Qtr ' || ROWNUM sl#,
                           start_dt || ' THROUGH ' || end_dt range, --||' RECEIPTS: '
                           hits,
                           qty_rvcd_a,
                           qty_rvcd_f
                    FROM (WITH last5_qtrs
                                   AS (SELECT qtr,
                                              ADD_MONTHS (dt, (qtr - 1) * 3)
                                                  qtr_start_dt,
                                              LAST_DAY (
                                                      ADD_MONTHS (dt, (qtr - 1) * 3 + 2))
                                                  qtr_end_dt
                                       FROM (SELECT ADD_MONTHS (
                                                            TRUNC (SYSDATE, 'Q'),
                                                            -12)
                                                           dt,
                                                    ROWNUM qtr -- from what date param
                                             FROM all_users
                                             WHERE ROWNUM <= 5)), --how many qtrs param
                               issues1
                                   AS (SELECT TRUNC (ih.created_date, 'Q')
                                                               qtr_start_dt,
                                              ih.created_date,
                                              NVL (ih.CC, 'A') CC,
                                              ih.qty
                                       FROM issue_hist ih
                                       WHERE ih.niin_id = vn_niinid)
                          SELECT lq.qtr qtr,
                                 lq.qtr_start_dt start_dt,
                                 lq.qtr_end_dt end_dt,
                                 NVL (ih.CC, 'A') CC,
                                 COUNT (ih.created_date) hits,
                                 SUM (DECODE (ih.cc, 'A', ih.qty, 0)) qty_rvcd_a,
                                 SUM (DECODE (ih.cc, 'F', ih.qty, 0)) qty_rvcd_f
                          FROM issues1 ih
                                   RIGHT OUTER JOIN last5_qtrs lq
                                                    ON ih.qtr_start_dt = lq.qtr_start_dt
                          GROUP BY lq.qtr,
                                   lq.qtr_start_dt,
                                   lq.qtr_end_dt,
                                   NVL (ih.CC, 'A')
                          ORDER BY qtr DESC);


                DBMS_OUTPUT.put_line ('after OPEN CSR in pkg');
            ELSE
                DBMS_OUTPUT.put_line ('niin_id must be > 0 ; enter a valid niin');
            END IF;
        END IF;
    EXCEPTION
        WHEN OTHERS
            THEN
                DBMS_OUTPUT.put_line (SQLERRM (SQLCODE));
                RAISE;
    END p_hits_hist_by_niin;

    /*****************************************************************************
   * p_MONITOR_OBJECTS - monitor any modifications made to db applications and
                   set the flag accordingly. admin will then be able query which
                   applications have been modified and determine whether the changes
                   are authorized
   ******************************************************************************/
    PROCEDURE p_monitor_objects
    AS
        CURSOR get_db_objects
            IS
            SELECT DISTINCT
                us.TYPE,
                pkg_strat_func.f_compute_md5 (us.name) new_checksum,
                us.name
            FROM user_source us, db_modifications dbm
            WHERE     us.TYPE = dbm.TYPE
              AND us.name = dbm.name
              AND pkg_strat_func.f_compute_md5 (us.name) <>
                  dbm.orig_checksum;

        vn_upd_count   NUMBER := 0.0;
    BEGIN
        FOR rec IN get_db_objects
            LOOP
                UPDATE db_modifications
                SET modified = 'Y'
                WHERE TYPE = rec.TYPE AND name = rec.name;

                vn_upd_count := vn_upd_count + SQL%ROWCOUNT;
            END LOOP;

        COMMIT;
        pkg_stratis_common.p_event_log (
                    'monitor_objects - updated db_modifications; rows ='
                    || TO_CHAR (vn_upd_count));
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'monitor_objects -' || SQLERRM (SQLCODE));
                RAISE;
    END p_monitor_objects;

    /******************************************************************************
      * f_cre_dormant_usr_audit_log
      ******************************************************************************/
    FUNCTION f_cre_dormant_usr_audit_log
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        FOR rec
            IN (SELECT u.user_id, u.username, e.equipment_number
                FROM users u, equip e
                WHERE     UPPER (e.name) = 'DUMMY'
                  AND SYSDATE - NVL (u.last_login_hh, u.eff_start_dt) >=
                      GCN_MAX_INACTIVE_DAYS                          --35
                  AND SYSDATE - NVL (u.last_login, u.eff_start_dt) >=
                      GCN_MAX_INACTIVE_DAYS)                        --35)
            LOOP
                INSERT INTO audit_log (audit_log_id,
                                       source,
                                       TYPE,
                                       timestamp,
                                       category,
                                       event,
                                       user_id,
                                       equipment_number,
                                       description)
                VALUES (NULL,
                        'SECURITY',
                        'DORMANT',
                        SYSDATE,
                        'DORMANT ACCOUNT',
                        NULL,
                        rec.user_id,
                        rec.equipment_number,
                        rec.username || ' ACCOUNT OVER 35 DAYS INACTIVE');

                vi_ret_val := vi_ret_val + SQL%ROWCOUNT;
            END LOOP;

        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.f_cre_dormant_usr_audit_log - '
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_cre_dormant_usr_audit_log;

    /******************************************************************************
   * f_lock_InActive_user
   ******************************************************************************/
    FUNCTION f_lock_InActive_user
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        UPDATE users
        SET locked = 'Y', status = 'NON-ACTIVE'
        WHERE     SYSDATE - NVL (last_login_hh, eff_start_dt) >=
                  GCN_MAX_INACTIVE_DAYS                                 --35
          AND SYSDATE - NVL (last_login, eff_start_dt) >=
              GCN_MAX_INACTIVE_DAYS;                               --35;

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME || '.f_lock_InActive_user -' || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_lock_InActive_user;

    /******************************************************************************
   * f_reset_current_user
   ******************************************************************************/
    FUNCTION f_reset_current_user
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        UPDATE equip
        SET current_user_id = NULL
        WHERE current_user_id IN (SELECT user_id
                                  FROM users
                                  WHERE     logged_in = 'Y'
                                    AND   (  SYSDATE
                                      - NVL (
                                                     last_login,
                                                     TO_DATE ('01-jan-0001',
                                                              'dd-mon-rrrr')))
                                              * 24 >= 5.00);

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME || '.f_reset_current_user -' || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_reset_current_user;

    /******************************************************************************
   * f_reset_picking_assign_to
   ******************************************************************************/
    FUNCTION f_reset_picking_assign_to
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        UPDATE picking
        SET assign_to_user = NULL
        WHERE assign_to_user IN (SELECT user_id
                                 FROM users
                                 WHERE     logged_in = 'Y'
                                   AND   (  SYSDATE
                                     - NVL (
                                                    last_login,
                                                    TO_DATE ('01-jan-0001',
                                                             'dd-mon-rrrr')))
                                             * 24 >= 5.00
                                 UNION
                                 SELECT user_id
                                 FROM users
                                 WHERE     logged_in_hh = 'Y'
                                   AND   (  SYSDATE
                                     - NVL (
                                                    last_login_hh,
                                                    TO_DATE ('01-jan-0001',
                                                             'dd-mon-rrrr')))
                                             * 24 >= 5.00);

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.f_reset_picking_assign_to -'
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_reset_picking_assign_to;

    /******************************************************************************
   * f_reset_inventory_assign_to
   ******************************************************************************/
    FUNCTION f_reset_inventory_assign_to
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        UPDATE inventory_item
        SET assign_to_user = NULL
        WHERE assign_to_user IN (SELECT user_id
                                 FROM users
                                 WHERE     logged_in = 'Y'
                                   AND   (  SYSDATE
                                     - NVL (
                                                    last_login,
                                                    TO_DATE ('01-jan-0001',
                                                             'dd-mon-rrrr')))
                                             * 24 >= 5.00
                                 UNION
                                 SELECT user_id
                                 FROM users
                                 WHERE     logged_in_hh = 'Y'
                                   AND   (  SYSDATE
                                     - NVL (
                                                    last_login_hh,
                                                    TO_DATE ('01-jan-0001',
                                                             'dd-mon-rrrr')))
                                             * 24 >= 5.00);

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.f_reset_inventory_assign_to -'
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_reset_inventory_assign_to;

    /******************************************************************************
   * f_reset_stow_assign_to
   ******************************************************************************/
    FUNCTION f_reset_stow_assign_to
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        UPDATE stow
        SET assign_to_user = NULL
        WHERE assign_to_user IN (SELECT user_id
                                 FROM users
                                 WHERE     logged_in = 'Y'
                                   AND   (  SYSDATE
                                     - NVL (
                                                    last_login,
                                                    TO_DATE ('01-jan-0001',
                                                             'dd-mon-rrrr')))
                                             * 24 >= 5.00
                                 UNION
                                 SELECT user_id
                                 FROM users
                                 WHERE     logged_in_hh = 'Y'
                                   AND   (  SYSDATE
                                     - NVL (
                                                    last_login_hh,
                                                    TO_DATE ('01-jan-0001',
                                                             'dd-mon-rrrr')))
                                             * 24 >= 5.00);

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME || '.f_reset_stow_assign_to -' || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_reset_stow_assign_to;

    /******************************************************************************
   * f_reset_user_logged_in_fl
   ******************************************************************************/
    FUNCTION f_reset_user_logged_in_fl
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        UPDATE users
        SET logged_in = 'N', temp_key = NULL
        WHERE     logged_in = 'Y'
          AND   (  SYSDATE
            - NVL (last_login,
                   TO_DATE ('01-jan-0001', 'dd-mon-rrrr')))
                    * 24 >= 5.00;

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.f_reset_user_logged_in_fl -'
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_reset_user_logged_in_fl;

    /******************************************************************************
   * f_reset_user_logged_in_hh_fl
   ******************************************************************************/
    FUNCTION f_reset_user_logged_in_hh_fl
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        UPDATE users
        SET logged_in_hh = 'N', temp_key_hh = NULL
        WHERE     logged_in_hh = 'Y'
          AND   (  SYSDATE
            - NVL (last_login_hh,
                   TO_DATE ('01-jan-0001', 'dd-mon-rrrr')))
                    * 24 >= 5.00;

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.f_reset_user_logged_in_hh_fl -'
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_reset_user_logged_in_hh_fl;

    /******************************************************************************
   * f_reset_floor_location_in_use
   ******************************************************************************/
    FUNCTION f_reset_floor_location_in_use
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        UPDATE floor_location x
        SET x.in_use = 'N'
        WHERE NOT EXISTS
            (SELECT NULL
             FROM shipping_manifest
             WHERE floor_location_id = x.floor_location_id);

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.f_reset_floor_location_in_use -'
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_reset_floor_location_in_use;

    /******************************************************************************
   * f_set_expiration
   ******************************************************************************/
    FUNCTION f_set_expiration
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        UPDATE niin_Location x
        SET exp_remark = 'Y'
        WHERE     NVL (num_extents, 0) = 0
          AND TO_CHAR (expiration_date, 'MMYYYY') =
              TO_CHAR (SYSDATE, 'MMYYYY')
          AND EXISTS
            (SELECT NULL
             FROM niin_info
             WHERE     shelf_life_code BETWEEN '1' AND '9'
               AND niin_id = x.niin_id)
          AND exp_remark <> 'Y';

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME || '.f_set_expiration -' || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_set_expiration;

    /******************************************************************************
   * f_del_spool_status_complete
   ******************************************************************************/
    FUNCTION f_del_spool_status_complete
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        DELETE FROM spool
        WHERE status = 'COMPLETE';

        vi_ret_val := SQL%ROWCOUNT;
        COMMIT;
        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.f_del_spool_status_complete -'
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_del_spool_status_complete;

    /******************************************************************************
   * f_del_serial_lot_num_track
   ******************************************************************************/
    FUNCTION f_del_serial_lot_num_track
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        DELETE FROM serial_lot_num_track
        WHERE niin_id NOT IN (SELECT niin_id
                              FROM niin_info
                              WHERE    lot_control_flag = 'Y'
                                 OR serial_control_flag = 'Y');

        COMMIT;

        vi_ret_val := SQL%ROWCOUNT;

        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.f_del_serial_lot_num_track -'
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_del_serial_lot_num_track;

    /******************************************************************************
   * f_del_picked -- DO NOT DELETE if pending STOWs exist. --20140403.001
   ******************************************************************************/
    FUNCTION f_del_picked
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        DELETE FROM picking
        WHERE scn IN (SELECT scn
                      FROM issue
                      WHERE (   (    document_id <> 'YLL'
                          AND created_date < SYSDATE - 30)
                          OR (    document_id = 'YLL'
                              AND status = 'PICKED'

                              AND EXISTS
                                      (SELECT *
                                       FROM stow
                                       WHERE     pid IN (SELECT pid
                                                         FROM picking
                                                         WHERE scn IN (SELECT scn
                                                                       FROM issue
                                                                       WHERE (   (    document_id <>
                                                                                      'YLL'
                                                                           AND created_date <
                                                                               SYSDATE
                                                                                   - 30)
                                                                           OR (    document_id =
                                                                                   'YLL'
                                                                               AND status =
                                                                                   'PICKED'))))
                                         AND status = 'STOWED'))));

        COMMIT;

        vi_ret_val := SQL%ROWCOUNT;

        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME || '.f_del_picked -' || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_del_picked;


/******************************************************************************
   * f_del_issued -- DO NOT DELETE if pending STOWs exist.--20140403.001
   ******************************************************************************/

    FUNCTION f_del_issued
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        DELETE --      FROM issue
            --            WHERE (   (document_id <> 'YLL' AND created_date < SYSDATE - 30)
            --                   OR (document_id = 'YLL' AND status = 'PICKED'));
        FROM  issue
        WHERE (   (document_id <> 'YLL' AND created_date < SYSDATE - 30)
            OR (    (document_id = 'YLL' AND status = 'PICKED')
                AND EXISTS
                        (SELECT *
                         FROM stow
                         WHERE     pid IN (SELECT pid
                                           FROM picking
                                           WHERE scn IN (SELECT scn
                                                         FROM issue
                                                         WHERE (   (    document_id <>
                                                                        'YLL'
                                                             AND created_date <
                                                                 SYSDATE
                                                                     - 30)
                                                             OR (    document_id =
                                                                     'YLL'
                                                                 AND status =
                                                                     'PICKED'))))
                           AND status = 'STOWED')));

        COMMIT;

        vi_ret_val := SQL%ROWCOUNT;

        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME || '.f_del_issued -' || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_del_issued;

    /******************************************************************************
   * f_del_packing_consolidation
   ******************************************************************************/
    FUNCTION f_del_packing_consolidation
        RETURN NUMBER
    AS
        vi_ret_val   PLS_INTEGER := 0;
    BEGIN
        DELETE FROM packing_consolidation
        WHERE packing_consolidation_id NOT IN (SELECT pc1.packing_consolidation_id
                                               FROM packing_consolidation pc1,
                                                    issue i
                                               WHERE pc1.packing_consolidation_id =
                                                     i.packing_consolidation_id
                                               UNION
                                               SELECT pc2.packing_consolidation_id
                                               FROM packing_consolidation pc2,
                                                    picking p
                                               WHERE pc2.packing_consolidation_id =
                                                     p.packing_consolidation_id);

        COMMIT;

        vi_ret_val := SQL%ROWCOUNT;

        RETURN (vi_ret_val);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.f_del_packing_consolidation -'
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_del_packing_consolidation;

    /****************************************************************************
   * p_nightly_updates -
   ******************************************************************************/
    PROCEDURE p_nightly_updates
    AS
        vn_ret_val   NUMBER := 0.0;
    BEGIN
        vn_ret_val := f_cre_dormant_usr_audit_log;
        --kane display retval???

        vn_ret_val := f_lock_InActive_user;

        vn_ret_val := f_reset_current_user;

        vn_ret_val := f_reset_picking_assign_to;

        vn_ret_val := f_reset_inventory_assign_to;

        vn_ret_val := f_reset_stow_assign_to;

        vn_ret_val := f_reset_user_logged_in_fl;

        vn_ret_val := f_reset_user_logged_in_hh_fl;

        vn_ret_val := f_reset_floor_location_in_use;

        vn_ret_val := f_set_expiration;

        vn_ret_val := f_del_spool_status_complete;

        vn_ret_val := f_del_serial_lot_num_track;

        vn_ret_val := f_del_picked;

        vn_ret_val := f_del_issued;

        vn_ret_val := f_del_packing_consolidation;

        pkg_stratis_common.p_event_log (
                    GCV_PKG_NAME || '.p_nightly_updates - COMPLETED');
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.p_nightly_updates - Error: '
                            || SQLERRM (SQLCODE));
                RAISE;
    END p_nightly_updates;

    /************************************************************************************************************
   -- PARTITION_HIST_TABLES  (Procedure)
   ************************************************************************************************************/
    PROCEDURE p_partition_hist_tables (
        pmv_tab_name_in   IN USER_TAB_PARTITIONS.table_NAME%TYPE DEFAULT 'RECON_HIST',
        pmd_date_in       IN DATE DEFAULT SYSDATE)
    AS
        /************************************************************************************************************
            ------------------------------------------------------------------------------------------------
            -- Module Name    : Partition_hist_tables
            -- Purpose        : to create monthly partitions on timestamp col
                                and run from cron job 01st of each month; drops oldest partition
                                on FIFO basis retaining 12 monthly partitions.
            -- O/S File       :p_partition_hist_tables.prc
            --
            ------------------------------------------------------------------------------------------------
                Modification History
            ------------------------------------------------------------------------------------------------
             Name                  Version         Description
            ------------------------------------------------------------------------------------------------
            Kane Suresh           03072012.001 check if Partition Name exists b4 drop;
                                               Kane Create Interval Partition in 11g
            *************************************************************************************************************/

        vv_month        VARCHAR2 (3);
        vv_date_value   VARCHAR2 (4000);
        vv_ptn_name     USER_TAB_PARTITIONS.PARTITION_NAME%TYPE;
        vv_tab_name     USER_TAB_PARTITIONS.table_NAME%TYPE := pmv_tab_name_in;

        /**********************************************************************
         * f_partition_exists
         ************************************************************************/
        FUNCTION f_partition_exists (
            pmv_tab_name_in   IN USER_TAB_PARTITIONS.table_NAME%TYPE,
            pmv_ptn_name_in   IN USER_TAB_PARTITIONS.PARTITION_NAME%TYPE)
            RETURN BOOLEAN
            IS
            vn_ptn_count   PLS_INTEGER := 0;
            vb_retval      BOOLEAN;
        BEGIN
            SELECT COUNT (1)
            INTO vn_ptn_count
            FROM USER_TAB_PARTITIONS
            WHERE table_name = vv_tab_name AND partition_name = vv_ptn_name;

            IF vn_ptn_count > 0
            THEN
                vb_retval := TRUE;
            ELSE
                vb_retval := FALSE;
            END IF;

            RETURN (vb_retval);
        END f_partition_exists;
    BEGIN
        vv_month := TO_CHAR (pmd_date_in, 'MON');
        vv_ptn_name :=
                UPPER (
                            'timestamp_'
                            || vv_month
                            || '_'
                            || TO_CHAR (TO_NUMBER (TO_CHAR (pmd_date_in, 'RRRR')) - 1));

        IF f_partition_exists (pmv_tab_name_in   => vv_tab_name,
                               pmv_ptn_name_in   => vv_ptn_name)
        THEN
            DBMS_OUTPUT.put_line (
                        'Partition '
                        || vv_ptn_name
                        || ' exists on Table '
                        || vv_tab_name
                        || ' and will be dropped');

            EXECUTE IMMEDIATE
                    'ALTER TABLE '
                    || vv_tab_name
                    || ' DROP PARTITION '
                    || vv_ptn_name;
        ELSE
            DBMS_OUTPUT.put_line (
                        'Partition '
                        || vv_ptn_name
                        || ' does NOT exist  on Table '
                        || vv_tab_name);
        END IF;

        vv_ptn_name :=
                UPPER (
                            'timestamp_' || vv_month || '_' || TO_CHAR (pmd_date_in, 'RRRR'));

        IF vv_month = 'DEC'
        THEN
            --
            -- construct date value less then beginning of Next year
            --
            vv_date_value :=
                        'TO_DATE('
                        || ''''
                        || LPAD (MOD (TO_CHAR (pmd_date_in, 'MM') + 1, 12), 2, '0')
                        || '/01/'
                        || TO_CHAR (TO_CHAR (pmd_date_in, 'RRRR') + 1)
                        || ''''
                        || ','
                        || ''''
                        || 'MM/DD/RRRR'
                        || ''''
                        || ')';
        ELSE
            --
            -- construct date value less then beginning ofmonth incurrent year
            --
            vv_date_value :=
                        'to_date('
                        || ''''
                        || LPAD (MOD (TO_CHAR (pmd_date_in, 'MM') + 1, 13), 2, '0')
                        || '/01/'
                        || TO_CHAR (pmd_date_in, 'RRRR')
                        || ''''
                        || ','
                        || ''''
                        || 'MM/DD/RRRR'
                        || ''''
                        || ')';
        END IF;

        IF f_partition_exists (pmv_tab_name_in   => vv_tab_name,
                               pmv_ptn_name_in   => vv_ptn_name)
        THEN
            DBMS_OUTPUT.put_line (
                        'Partition '
                        || vv_ptn_name
                        || ' exists on Table '
                        || vv_tab_name
                        || ' and will NOT be created');
        ELSE
            DBMS_OUTPUT.put_line (
                        'Partition '
                        || vv_ptn_name
                        || ' does NOT exist  on Table '
                        || vv_tab_name
                        || ' and will  be created');

            EXECUTE IMMEDIATE
                    'ALTER TABLE '
                    || vv_tab_name
                    || ' ADD PARTITION '
                    || vv_ptn_name
                    || ' VALUES LESS THAN('
                    || vv_date_value
                    || ')';
        END IF;
    END p_partition_hist_tables;

    /***********************************************************************
   * f_get_ref_mhif_detail
   ************************************************************************/
    FUNCTION f_get_ref_mhif_detail (
        pmv_niin_in                      IN     niin_info.niin%TYPE,
        pmv_item_name_nomenclature_out      OUT REF_MHIF.ITEM_NAME_NOMENCLATURE%TYPE,
        pmv_physical_security_code_out      OUT REF_MHIF.physical_security_code%TYPE,
        pmv_sassy_slc_out                   OUT REF_MHIF.SHELF_LIFE_CODE%TYPE,
        pmn_unit_price_out                  OUT REF_MHIF.UNIT_PRICE%TYPE)
        RETURN NUMBER
        IS
    BEGIN
        SELECT item_name_nomenclature,
               physical_security_code,
               shelf_life_code,
               NVL (TO_NUMBER (unit_price), 0) / 100 unit_price
        INTO pmv_item_name_nomenclature_out,
            pmv_physical_security_code_out,
            pmv_sassy_slc_out,
            pmn_unit_price_out
        FROM (  SELECT rm.item_name_nomenclature,
                       rm.physical_security_code,
                       rm.shelf_life_code,
                       NVL (TO_NUMBER (rm.unit_price), 0) / 100 unit_price
                FROM ref_mhif rm
                WHERE rm.record_niin = pmv_niin_in
                ORDER BY timestamp, ref_mhif_id DESC)
        WHERE ROWNUM < 2;                                         -- take top 1

        RETURN (0);
    EXCEPTION
        WHEN OTHERS
            THEN
                RETURN (-1);
    END f_get_ref_mhif_detail;

    /***********************************************************************
      * f_get_Niin_Info_detail
      ************************************************************************/
    FUNCTION f_get_Niin_Info_detail (
        pmv_niin_in                      IN     niin_info.niin%TYPE,
        pmv_item_name_nomenclature_out      OUT NIIN_INFO.NOMENCLATURE%TYPE,
        pmv_physical_security_code_out      OUT NIIN_INFO.scc%TYPE,
        pmv_sassy_slc_out                   OUT NIIN_INFO.SHELF_LIFE_CODE%TYPE,
        pmn_unit_price_out                  OUT NIIN_INFO.PRICE%TYPE)
        RETURN NUMBER
        IS
    BEGIN
        SELECT nomenclature,
               scc,
               shelf_life_code,
               NVL (price, 0) / 100 unit_price
        INTO pmv_item_name_nomenclature_out,
            pmv_physical_security_code_out,
            pmv_sassy_slc_out,
            pmn_unit_price_out
        FROM NIIN_INFO
        WHERE ROWNUM < 2;                                      -- take 1 is PK?

        RETURN (0);
    EXCEPTION
        WHEN OTHERS
            THEN
                RETURN (-1);
    END f_get_Niin_Info_detail;

    /***********************************************************************
     * f_cre_reconsrllot_NOT_in_sTRAT
     ************************************************************************/
    FUNCTION f_cre_reconsrllot_NOT_in_sTRAT (pmd_run_date_in IN DATE)
        RETURN NUMBER
    AS
    BEGIN
        INSERT INTO recon_serial_lot_num (niin,
                                          lot_con_num,
                                          serial_number,
                                          timestamp,
                                          location_label,
                                          qty,
                                          HOST)
        SELECT rg.record_niin,
               rgs.lot_con_Num,
               rgs.serial_number,
               vd_run_date,
               NULL,
               rgs.qty,
               'HOST'
        FROM ref_gabf rg, ref_gabf_serial rgs, recon_hist rh
        WHERE     rg.ref_gabf_id = rgs.ref_gabf_id
          AND rg.record_niin = rh.niin
          AND rh.in_sassy = 'Y'
          AND rh.in_stratis = 'N'
          AND rh.timestamp = pmd_run_date_in;

        RETURN (SQL%ROWCOUNT);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'populate_gcss_recon_hist.f_cre_reconsrllot_NOT_in_sTRAT - Error: '
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_cre_reconsrllot_NOT_in_sTRAT;

    /***********************************************************************
        * f_cre_recon_hist_not_in_STRAT
        ************************************************************************/
    FUNCTION f_cre_recon_hist_not_in_STRAT (pmd_run_date_in    IN DATE,
                                            pmn_rec_count_in   IN NUMBER)
        RETURN NUMBER
    AS
    BEGIN
        INSERT INTO recon_hist (TIMESTAMP,
                                sassy_FSC,
                                sassy_UI,
                                sassy_BALANCE_SERVICABLE,
                                sassy_NOMEN,
                                sassy_SCC,
                                sassy_SLC,
                                sassy_RO,
                                sassy_PRICE,
                                IN_SASSY,
                                NIIN,
                                sassy_BALANCE_UNSERVICABLE,
                                in_stratis,
                                records_read)
        SELECT pmd_run_date_in,
               rg.record_fsc,
               rg.unit_of_issue,
               rg.on_hand_op_stock_serviceable,
               ni.nomenclature,
               ni.scc,
               ni.shelf_life_code,
               rg.requisitioning_objective,
               rg.unit_price,
               'Y',
               rg.record_niin,
               SUBSTR (rg.on_hand_unserviceable, 2),
               'N',
               pmn_rec_count_in
        FROM ref_gabf rg, niin_info ni
        WHERE     NVL (rg.processed, 'N') = 'N'
          AND rg.on_hand_op_stock_serviceable > 0
          AND rg.record_niin = ni.niin(+)
          AND NOT EXISTS
            (SELECT NULL
             FROM niin_location nl, niin_info ni1
             WHERE     ni1.niin_id = nl.niin_id
               AND ni1.niin = ni.niin
               AND nl.qty > 0);

        RETURN (SQL%ROWCOUNT);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'populate_gcss_recon_hist.f_cre_recon_hist_not_in_STRAT - Error: '
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_cre_recon_hist_not_in_STRAT;

    /***********************************************************************
        * f_cre_rsln_in_strat_no_in_gcss
        ************************************************************************/
    FUNCTION f_cre_rsln_in_strat_notin_gcss (pmd_run_date_in IN DATE)
        RETURN NUMBER
    AS
    BEGIN
        INSERT INTO recon_serial_lot_Num (niin,
                                          lot_con_num,
                                          serial_number,
                                          timestamp,
                                          location_label,
                                          qty,
                                          HOST)
        SELECT x.niin,
               y.lot_con_num,
               y.serial_number,
               vd_run_date,
               z.location_label,
               y.qty,
               'STRATIS'
        FROM niin_Info x,
             serial_lot_num_track y,
             location z,
             recon_hist a
        WHERE     x.niin_id = y.niin_Id
          AND y.location_id = z.location_id(+)
          AND x.niin = a.niin
          AND y.issued_flag = 'N'
          AND y.qty > 0
          AND a.in_sassy = 'N'
          AND a.in_stratis = 'Y'
          AND a.timestamp = pmd_run_date_in;

        RETURN (SQL%ROWCOUNT);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'populate_gcss_recon_hist.f_cre_rsln_in_strat_notin_gcss - Error: '
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_cre_rsln_in_strat_notin_gcss;

    /***********************************************************************
        * f_cre_rh_in_strat_not_in_rg
        ************************************************************************/
    FUNCTION f_cre_rh_in_strat_not_in_rg (pmd_run_date_in    IN DATE,
                                          pmn_rec_count_in   IN NUMBER)
        RETURN NUMBER
    AS
    BEGIN
        INSERT INTO recon_hist (timestamp,
                                stratis_fsc,
                                stratis_ui,
                                stratis_balance_servicable,
                                stratis_nomen,
                                stratis_scc,
                                stratis_slc,
                                stratis_ro,
                                stratis_price,
                                in_sassy,
                                niin,
                                stratis_balance_unservicable,
                                in_stratis,
                                records_read)
        SELECT vd_run_date,
               ni.fsc,
               ni.ui,
               SUM (DECODE (nl.cc, 'A', NVL (nl.qty, 0), 0)),
               ni.nomenclature,
               ni.scc,
               ni.shelf_life_code,
               ni.ro_threshold,
               ni.price,
               'N',
               ni.niin,
               SUM (DECODE (nl.cc, 'A', 0, NVL (nl.qty, 0))),
               'Y',
               vn_records_read
        FROM niin_info ni, niin_location nl
        WHERE     ni.niin_id = nl.niin_id(+)
          AND NOT EXISTS
            (SELECT NULL
             FROM ref_gabf
             WHERE     record_niin = ni.niin
               AND NVL (processed, 'N') = 'N')
        GROUP BY ni.fsc,
                 ni.ui,
                 ni.nomenclature,
                 ni.scc,
                 ni.shelf_life_code,
                 ni.ro_threshold,
                 ni.price,
                 ni.niin;

        RETURN (SQL%ROWCOUNT);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'populate_gcss_recon_hist.f_cre_rh_in_strat_not_in_rg - Error: '
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_cre_rh_in_strat_not_in_rg;

    /***********************************************************************
        * f_cre_rsln_slrlot_diff_in_gcss
        ************************************************************************/
    FUNCTION f_cre_rsln_slrlot_diff_in_gcss (
        pmd_run_date_in            IN     DATE,
        pmc_CSR_GET_RECON_REC_io   IN OUT CSR_GET_RECON_RECS%ROWTYPE)
        RETURN NUMBER
    AS
    BEGIN
        INSERT INTO recon_serial_Lot_num (niin,
                                          lot_con_num,
                                          serial_number,
                                          timestamp,
                                          location_label,
                                          qty,
                                          HOST)
        SELECT ni.niin,
               slnt.lot_con_Num,
               slnt.serial_Number,
               pmd_run_date_in,
               l.location_label,
               slnt.qty,
               'STRATIS'
        FROM niin_Info ni, serial_lot_num_track slnt, location l
        WHERE     ni.niin = pmc_CSR_GET_RECON_REC_io.niin
          AND ni.niin_id = slnt.niin_id
          AND slnt.location_id = l.location_id(+)
          AND slnt.issued_flag = 'N'
          AND slnt.qty > 0
          AND NOT EXISTS
            (SELECT NULL
             FROM ref_gabf_serial
             WHERE     ref_gabf_id =
                       pmc_CSR_GET_RECON_REC_io.ref_gabf_id
               AND NVL (serial_number, 'X') =
                   NVL (slnt.serial_number, 'X')
               AND NVL (lot_con_Num, 'X') =
                   NVL (slnt.lot_con_num, 'X'));

        RETURN (SQL%ROWCOUNT);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'populate_gcss_recon_hist.f_cre_rsln_slrlot_diff_in_gcss - Error: '
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_cre_rsln_slrlot_diff_in_gcss;

    /***********************************************************************
    * f_cre_rsln_slrlot_dif_in_strat
    ************************************************************************/
    FUNCTION f_cre_rsln_slrlot_dif_in_strat (
        pmd_run_date_in            IN     DATE,
        pmc_CSR_GET_RECON_REC_io   IN OUT CSR_GET_RECON_RECS%ROWTYPE)
        RETURN NUMBER
    AS
    BEGIN
        INSERT INTO recon_serial_Lot_num (niin,
                                          lot_con_num,
                                          serial_number,
                                          timestamp,
                                          location_label,
                                          qty,
                                          HOST)
        SELECT rg.record_niin,
               rgs.lot_con_num,
               rgs.SERIAL_NUMBER,
               vd_run_date,
               NULL,
               rgs.qty,
               'HOST'
        FROM ref_gabf rg, ref_gabf_serial rgs
        WHERE     rg.ref_gabf_id = rgs.ref_gabf_id
          AND rg.ref_gabf_id = pmc_CSR_GET_RECON_REC_io.ref_gabf_id
          AND NOT EXISTS
            (SELECT NULL
             FROM serial_lot_num_track slt, niin_info n
             WHERE     n.niin_id = slt.niin_id
               AND n.niin = pmc_CSR_GET_RECON_REC_io.niin
               AND NVL (slt.serial_number, 'X') =
                   NVL (rgs.serial_number, 'X')
               AND NVL (slt.lot_con_Num, 'X') =
                   NVL (rgs.lot_con_num, 'X'));

        RETURN (SQL%ROWCOUNT);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'populate_gcss_recon_hist.f_cre_rsln_slrlot_dif_in_strat - Error: '
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_cre_rsln_slrlot_dif_in_strat;

    /***********************************************************************
    * f_cre_rh_for_niin_prop_change
    ************************************************************************/
    FUNCTION f_cre_rh_for_niin_prop_change (
        pmd_run_date_in                 IN     DATE,
        pmc_CSR_GET_RECON_REC_io        IN OUT CSR_GET_RECON_RECS%ROWTYPE,
        pmv_item_name_nomenclature_in   IN     REF_MHIF.ITEM_NAME_NOMENCLATURE%TYPE,
        pmv_physical_security_code_in   IN     REF_MHIF.PHYSICAL_SECURITY_CODE%TYPE,
        pmv_sassy_slc_in                IN     REF_MHIF.SHELF_LIFE_CODE%TYPE,
        pmn_unit_price_in               IN     REF_MHIF.UNIT_PRICE%TYPE,
        pmn_records_read_in             IN     NUMBER)
        RETURN NUMBER
    AS
    BEGIN
        INSERT INTO recon_hist (timestamp,
                                stratis_fsc,
                                sassy_fsc,
                                stratis_ui,
                                sassy_ui,
                                stratis_balance_servicable,
                                sassy_balance_servicable,
                                stratis_nomen,
                                sassy_nomen,
                                stratis_scc,
                                sassy_scc,
                                stratis_slc,
                                sassy_slc,
                                stratis_ro,
                                sassy_ro,
                                stratis_price,
                                sassy_price,
                                in_sassy,
                                niin,
                                stratis_balance_unservicable,
                                sassy_balance_unservicable,
                                in_stratis,
                                records_read,
                                war_reserve_qty,
                                opcode_reserve_qty,
                                forecast_qty,
                                stage_qty,
                                dock_qty,
                                ro_qty)
        VALUES (pmd_run_date_in,
                pmc_CSR_GET_RECON_REC_io.fsc,
                RTRIM (LTRIM (pmc_CSR_GET_RECON_REC_io.record_fsc)),
                pmc_CSR_GET_RECON_REC_io.ui,
                RTRIM (LTRIM (pmc_CSR_GET_RECON_REC_io.unit_of_issue)),
                pmc_CSR_GET_RECON_REC_io.stratis_good_qty,
                pmc_CSR_GET_RECON_REC_io.on_hand_op_stock_serviceable,
                pmc_CSR_GET_RECON_REC_io.nomenclature,
                RTRIM (LTRIM (pmv_item_name_nomenclature_in)),
                pmc_CSR_GET_RECON_REC_io.scc,
                RTRIM (LTRIM (pmv_physical_security_code_in)),
                pmc_CSR_GET_RECON_REC_io.stratis_slc,
                RTRIM (LTRIM (pmv_sassy_slc_in)),
                pmc_CSR_GET_RECON_REC_io.ro_threshold,
                pmc_CSR_GET_RECON_REC_io.requisitioning_objective,
                pmc_CSR_GET_RECON_REC_io.price,
                pmn_unit_price_in,
                pmc_CSR_GET_RECON_REC_io.in_sassy,
                pmc_CSR_GET_RECON_REC_io.niin,
                pmc_CSR_GET_RECON_REC_io.stratis_bad_qty,
                pmc_CSR_GET_RECON_REC_io.on_hand_unserviceable,
                pmc_CSR_GET_RECON_REC_io.in_stratis,
                vn_records_read,
                NULL,
                NULL,
                NULL,
                NULL,
                NULL,
                NULL);

        RETURN (SQL%ROWCOUNT);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'populate_gcss_recon_hist.f_cre_rh_for_niin_prop_change - Error: '
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_cre_rh_for_niin_prop_change;

    /***********************************************************************
    * f_cre_rh_for_srl_lot_niin
    ************************************************************************/
    FUNCTION f_cre_rh_for_srl_lot_niin (
        pmd_run_date_in            IN     DATE,
        pmc_CSR_GET_RECON_REC_io   IN OUT CSR_GET_RECON_RECS%ROWTYPE,
        pmn_records_read_in        IN     NUMBER)
        RETURN NUMBER
    AS
    BEGIN
        INSERT INTO recon_hist (timestamp,
                                in_sassy,
                                in_stratis,
                                niin,
                                records_read,
                                stratis_balance_servicable,
                                sassy_balance_servicable,
                                stratis_balance_unservicable,
                                sassy_balance_unservicable)
        VALUES (pmd_run_date_in,
                pmc_CSR_GET_RECON_REC_io.in_sassy,
                pmc_CSR_GET_RECON_REC_io.in_stratis,
                pmc_CSR_GET_RECON_REC_io.niin,
                pmn_records_read_in,
                pmc_CSR_GET_RECON_REC_io.stratis_good_qty,
                pmc_CSR_GET_RECON_REC_io.on_hand_op_stock_serviceable,
                pmc_CSR_GET_RECON_REC_io.stratis_bad_qty,
                pmc_CSR_GET_RECON_REC_io.on_hand_unserviceable);

        RETURN (SQL%ROWCOUNT);
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'populate_gcss_recon_hist.f_cre_rh_for_srl_lot_niin - Error: '
                            || SQLERRM (SQLCODE));
                RETURN (-1);
    END f_cre_rh_for_srl_lot_niin;

    /***********************************************************************
    * f_gabf_niin_diff_from_strat
    ************************************************************************/
    FUNCTION f_gabf_niin_diff_from_strat (
        pmc_csr_rec_in                  IN csr_get_recon_recs%ROWTYPE,
        pmv_item_name_nomenclature_in   IN ref_mhif.item_name_nomenclature%TYPE,
        pmv_physical_security_code_in   IN ref_mhif.physical_security_code%TYPE,
        pmv_sassy_slc_in                IN ref_mhif.shelf_life_code%TYPE,
        pmn_unit_price_in               IN niin_info.price%TYPE)
        RETURN BOOLEAN
        IS
        vb_ret_val   BOOLEAN;
    BEGIN
        -- if NIIN properties changed between GCSS (GABF) and STRATIS
        IF    NVL (pmc_csr_rec_in.fsc, 'x') <>
              NVL (RTRIM (LTRIM (pmc_csr_rec_in.record_fsc)), 'x')
            OR NVL (pmc_csr_rec_in.ui, 'x') <>
               NVL (RTRIM (LTRIM (pmc_csr_rec_in.unit_of_issue)), 'x')
            OR NVL (pmc_csr_rec_in.stratis_good_qty, 0) <>
               NVL (pmc_csr_rec_in.on_hand_op_stock_serviceable, 0)
            OR NVL (pmc_csr_rec_in.nomenclature, 'x') <>
               NVL (RTRIM (LTRIM (pmv_item_name_nomenclature_in)), 'x')
            OR NVL (pmc_csr_rec_in.scc, 'x') <>
               NVL (RTRIM (LTRIM (pmv_physical_security_code_in)), 'x')
            OR NVL (pmc_csr_rec_in.stratis_slc, 'x') <>
               NVL (RTRIM (LTRIM (pmv_sassy_slc_in)), 'x')
            OR NVL (pmc_csr_rec_in.ro_threshold, 0) <>
               NVL (pmc_csr_rec_in.requisitioning_objective, 0)
            OR NVL (pmc_csr_rec_in.price, 0) <> NVL (pmn_unit_price_in, 0)
            OR NVL (pmc_csr_rec_in.stratis_bad_qty, 0) <>
               NVL (pmc_csr_rec_in.on_hand_unserviceable, 0)
        THEN
            vb_ret_val := TRUE;
        ELSE
            vb_ret_val := FALSE;
        END IF;

        RETURN (vb_ret_val);
    END f_gabf_niin_diff_from_strat;

    /***********************************************************************
    * f_gabf_found_in_STRATIS_count
    ************************************************************************/
    FUNCTION f_gabf_found_in_STRATIS_count
        RETURN NUMBER
    AS
        vn_records_read   NUMBER := 0.0;
    BEGIN
        SELECT COUNT (*)
        INTO vn_records_read
        FROM niin_info ni, ref_gabf rg
        WHERE ni.niin = rg.record_niin AND NVL (rg.processed, 'N') = 'N';

        RETURN (vn_records_read);
    END f_gabf_found_in_STRATIS_count;

    /***********************************************************************
       * f_get_gabf_total_rowcount
       ************************************************************************/
    FUNCTION f_get_gabf_total_rowcount
        RETURN NUMBER
    AS
        vn_records_read   NUMBER := 0.0;
    BEGIN
        SELECT COUNT (*)
        INTO vn_records_read
        FROM ref_gabf rg
        WHERE NVL (rg.processed, 'N') = 'N';

        RETURN (vn_records_read);
    END f_get_gabf_total_rowcount;

    /***********************************************************************
    * p_set_recon_run_date
    ************************************************************************/
    PROCEDURE p_set_recon_run_date (pmd_run_date_in IN DATE)
    AS
    BEGIN
        INSERT INTO RECON_run_date (timestamp)
        VALUES (pmd_run_date_in);
    END p_set_recon_run_date;

    /***********************************************************************
    * p_set_gabf_processed
    ************************************************************************/
    PROCEDURE p_set_gabf_processed (
        pmn_ref_gabf_id_in   IN ref_gabf.ref_gabf_id%TYPE)
    AS
        --pragma autonomous_transaction;
    BEGIN
        UPDATE ref_gabf
        SET processed = 'Y'
        WHERE ref_gabf_id = pmn_ref_gabf_id_in;
        --PKG_STRATIS_COMMON.p_commit;
    END p_set_gabf_processed;

    /**************************************************************
    * f_create_ref_dataload_log -
    ***************************************************************/
    FUNCTION f_create_ref_dataload_log (
        pmn_ref_dataload_log_id_in      IN ref_dataload_log.ref_dataload_log_id%TYPE,
        pmv_interface_name_in           IN ref_dataload_log.interface_name%TYPE,
        pmd_created_date_in             IN ref_dataload_log.created_date%TYPE,
        pmv_created_by_in               IN ref_dataload_log.created_by%TYPE,
        pmcl_data_row_in                IN ref_dataload_log.data_row%TYPE,
        pmv_description_in              IN ref_dataload_log.description%TYPE,
        pmi_line_no_in                  IN ref_dataload_log.line_no%TYPE,
        pmi_gcssmc_imports_data_id_in   IN ref_dataload_log.gcssmc_imports_data_id%TYPE)
        RETURN NUMBER
        IS
        PRAGMA AUTONOMOUS_TRANSACTION;
    BEGIN
        INSERT INTO ref_dataload_log (ref_dataload_log_id,
                                      interface_name,
                                      created_date,
                                      created_by,
                                      data_row,
                                      description,
                                      line_no,
                                      gcssmc_imports_data_id)
        VALUES (pmn_ref_dataload_log_id_in,
                pmv_interface_name_in,
                pmd_created_date_in,
                pmv_created_by_in,
                pmcl_data_row_in,
                pmv_description_in,
                pmi_line_no_in,
                pmi_gcssmc_imports_data_id_in);


        --PKG_STRATIS_COMMON.p_commit;
        RETURN (SQL%ROWCOUNT);
    EXCEPTION
        WHEN OTHERS
            THEN
                COMMIT;
                RETURN (0);
    END f_create_ref_dataload_log;

    /*******************************************************************************
    * POPULATE_GCSS_RECON_HIST  (Procedure)
    *****************************************************************************/
    PROCEDURE p_populate_gcss_recon_hist
    AS
        /************************************
       * p_init_MHIF_vars
       ************************************/
        PROCEDURE p_init_MHIF_vars
        AS
        BEGIN
            vv_mhif_item_name_nomenclature := NULL;
            vv_mhif_physical_security_code := NULL;
            vv_mhif_sassy_slc := NULL;
            vn_mhif_unit_price := NULL;
        END p_init_MHIF_vars;
    BEGIN
        SAVEPOINT sp_gabf1;
        vd_run_date := SYSDATE;

        -- total rows to be processed in GABF
        vn_total_gabf_rowcount := f_get_gabf_total_rowcount;
        PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                    'total rows to be processed in GABF =' || vn_total_gabf_rowcount);

        -- get gabf niin found in STRATIS count
        vn_records_read := f_gabf_found_in_STRATIS_count;
        PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                    'gabf niin found in STRATIS count =' || vn_total_gabf_rowcount);

        -- Set sysdate as run date in table
        p_set_recon_run_date (pmd_run_date_in => vd_run_date);

        FOR csr_rec IN CSR_get_recon_recs
            LOOP
                BEGIN
                    --Initialize to NULL
                    p_init_MHIF_vars;
                    PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                                'GABF Niin ='
                                || csr_rec.niin
                                || 'in_stratis flag ='
                                || csr_rec.in_stratis);

                    --why check always --only when NIIN_INFO not found??/Kane
                    IF csr_rec.in_stratis = 'N'
                    THEN
                        vn_ret_val :=
                                f_get_ref_mhif_detail (
                                        pmv_niin_in                      => csr_rec.niin,
                                        pmv_item_name_nomenclature_out   => vv_mhif_item_name_nomenclature,
                                        pmv_physical_security_code_out   => vv_mhif_physical_security_code,
                                        pmv_sassy_slc_out                => vv_mhif_sassy_slc,
                                        pmn_unit_price_out               => vn_mhif_unit_price);
                        vn_ret_val :=
                                f_create_ref_dataload_log (
                                        pmn_ref_dataload_log_id_in      => NULL,
                                        pmv_interface_name_in           => 'GABF',
                                        pmd_created_date_in             => SYSDATE,
                                        pmv_created_by_in               => 1,
                                        pmcl_data_row_in                =>    'ERROR - Niin NOT in STRATIS '
                                            || ' REF_GABF_ID = '
                                            || TO_CHAR (
                                                                                      csr_rec.ref_gabf_id),
                                        pmv_description_in              => ' NIIN: ' || csr_rec.niin,
                                        pmi_line_no_in                  => 0,
                                        pmi_gcssmc_imports_data_id_in   => NULL);
                    ELSIF csr_rec.in_stratis = 'Y'
                    THEN
                        vn_ret_val :=
                                f_get_NIIN_INFO_detail (
                                        pmv_niin_in                      => csr_rec.niin,
                                        pmv_item_name_nomenclature_out   => vv_mhif_item_name_nomenclature, --here it is NIIN_INFO detail
                                        pmv_physical_security_code_out   => vv_mhif_physical_security_code,
                                        pmv_sassy_slc_out                => vv_mhif_sassy_slc,
                                        pmn_unit_price_out               => vn_mhif_unit_price);
                    END IF;

                    --
                    -- populate recon_stratis_serial_Lot_num with the STRATIS serial number/lot control number combinations which differ
                    -- from the corresponding NIIN in GCSS.
                    --
                    vn_ret_val :=
                            f_cre_rsln_slrlot_diff_in_gcss (
                                    pmd_run_date_in            => vd_run_date,
                                    pmc_CSR_GET_RECON_REC_io   => csr_rec);

                    vn_count_serial := vn_ret_val; --SQL%ROWCOUNT;# of rows inserted by fn f_cre_rsln_slrlot_diff_in_gcss
                    --Kane --how to handle above -1 error retval
                    PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                                'Serial Count =' || vn_count_serial);
                    --
                    -- populate recon_stratis_sassy_Lot_num with the GCSS serial number/lot control number combinations which differ
                    -- from the corresponding NIIN in stratis.
                    --
                    vn_ret_val :=
                            f_cre_rsln_slrlot_dif_in_strat (
                                    pmd_run_date_in            => vd_run_date,
                                    pmc_CSR_GET_RECON_REC_io   => csr_rec);
                    vn_count_lot := vn_ret_val;         ---SQL%ROWCOUNT; chk ret val??
                    PKG_STRATIS_COMMON.P_SHOW_MESSAGE ('Lot Count =' || vn_count_lot);

                    --if NIIN in GABF has different properties than in STRATIS
                    IF f_gabf_niin_diff_from_strat (
                            pmc_csr_rec_in                  => csr_rec,
                            pmv_item_name_nomenclature_in   => vv_mhif_item_name_nomenclature,
                            pmv_physical_security_code_in   => vv_mhif_physical_security_code,
                            pmv_sassy_slc_in                => vv_mhif_sassy_slc,
                            pmn_unit_price_in               => vn_mhif_unit_price)
                    THEN
                        PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                                ' NIIN in GABF has different properties than in STRATIS');
                        -- Kane why only part of the cols below checked for niin property change?????

                        -- store those items where the following fields differ between mhif and stratis:
                        --
                        --  fsc
                        --  ui
                        --  quantity (servicable)
                        --  nomenclature
                        --  security code
                        --  shelf life code
                        --  ro threshold
                        --  price
                        --  quantity (unserviceable)
                        --  serial number
                        --  lot control number
                        --

                        vn_ret_val :=
                                f_cre_rh_for_niin_prop_change (
                                        pmd_run_date_in                 => vd_run_date,
                                        pmc_CSR_GET_RECON_REC_io        => csr_rec,
                                        pmv_item_name_nomenclature_in   => vv_mhif_item_name_nomenclature,
                                        pmv_physical_security_code_in   => vv_mhif_physical_security_code,
                                        pmv_sassy_slc_in                => vv_mhif_sassy_slc,
                                        pmn_unit_price_in               => vn_mhif_unit_price,
                                        pmn_records_read_in             => vn_records_read);
                        PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                                    ' f_cre_rh_for_niin_prop_change inserted rows ='
                                    || vn_ret_val);
                    ELSIF vn_count_lot > 0 OR vn_count_serial > 0
                    THEN
                        -- if serial and lot niin cre recon hist
                        vn_ret_val :=
                                f_cre_rh_for_srl_lot_niin (
                                        pmd_run_date_in            => vd_run_date,
                                        pmc_csr_get_recon_rec_io   => csr_rec,
                                        pmn_records_read_in        => vn_records_read);
                        PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                                    ' f_cre_rh_for_srl_lot_niin inserted rows =' || vn_ret_val);
                    END IF;

                    -- update the processed flag to Yes
                    p_set_gabf_processed (pmn_ref_gabf_id_in => 'Y');
                    PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                            ' p_set_gabf_processed flag set to Y');
                EXCEPTION
                    WHEN OTHERS
                        THEN
                            vn_ret_val :=
                                    f_create_ref_dataload_log (
                                            pmn_ref_dataload_log_id_in      => NULL,
                                            pmv_interface_name_in           => 'GABF',
                                            pmd_created_date_in             => SYSDATE,
                                            pmv_created_by_in               => 1,
                                            pmcl_data_row_in                =>    'ERROR - '
                                                || SQLERRM (SQLCODE)
                                                || ' REF_GABF_ID = '
                                                || TO_CHAR (
                                                                                          csr_rec.ref_gabf_id),
                                            pmv_description_in              => ' NIIN: ' || csr_rec.niin,
                                            pmi_line_no_in                  => 0,
                                            pmi_gcssmc_imports_data_id_in   => NULL);
                            PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                                        ' f_create_ref_dataload_log  logged ERROR - '
                                        || SQLERRM (SQLCODE)
                                        || ' REF_GABF_ID = '
                                        || TO_CHAR (csr_rec.ref_gabf_id)
                                        || ' NIIN: '
                                        || csr_rec.niin);
                END;
            END LOOP;

        --
        -- store those items which exist in stratis but not on the gabf
        --
        vn_ret_val :=
                f_cre_rh_in_strat_not_in_rg (pmd_run_date_in    => vd_run_date,
                                             pmn_rec_count_in   => vn_records_read);
        PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                    ' f_cre_rh_in_strat_not_in_rg inserted rows =' || vn_ret_val);

        --
        -- store serial and lot control numbers, if they exist, for those NIIN which exist in stratis
        -- but not on the gabf (GCSS).
        --
        vn_ret_val :=
                f_cre_rsln_in_strat_notin_gcss (pmd_run_date_in => vd_run_date);
        PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                    ' f_cre_rsln_in_strat_notin_gcss inserted rows =' || vn_ret_val);

        --
        -- store those items which exist on the gabf but not in stratis
        --
        vn_ret_val :=
                f_cre_recon_hist_not_in_STRAT (pmd_run_date_in    => vd_run_date,
                                               pmn_rec_count_in   => vn_records_read);
        PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                    ' f_cre_recon_hist_not_in_STRAT inserted rows =' || vn_ret_val);

        --
        -- store serial and lot control numbers, if they exist, for those NIINs which exist in GCSS
        -- but not in stratis.
        --
        vn_ret_val :=
                f_cre_reconsrllot_NOT_in_strat (pmd_run_date_in => vd_run_date);
        PKG_STRATIS_COMMON.P_SHOW_MESSAGE (
                    ' f_cre_reconsrllot_NOT_in_strat inserted rows =' || vn_ret_val);
        -- partial commit is ok; log error in gabf and notify in ref_dataload_log. set interface_name='GABF'
        --pkg_stratis_common.p_commit;
    EXCEPTION
        WHEN OTHERS
            THEN
                PKG_STRATIS_COMMON.P_EVENT_LOG (
                            'populate_gcss_recon_hist - Error: ' || SQLERRM (SQLCODE));
                ROLLBACK TO sp_gabf1;
        -- Rollback and raise application error ????Kane
    END p_populate_gcss_recon_hist;

    /*****************************************************************************
    *- p_POPULATE_RECON_HIST  (Procedure)
    *****************************************************************************/
    PROCEDURE p_populate_recon_hist
    AS
        --
        -- retrieve all NIINs which exist in both sassy and stratis
        --
        -- modified to populate recon_report_date table, r.ehlen, 12/17/2010
        --
        -- modified to include pending picks when determining the on-hand quantity
        records_read   NUMBER;
        run_date       DATE;

        CURSOR get_recon_recs
            IS
            SELECT SYSDATE,
                   x.fsc,
                   z.record_fsc,
                   x.ui,
                   z.unit_of_issue,
                   SUM (DECODE (y.cc, 'A', NVL (y.qty, 0), 0))
                       - NVL (a.stratis_good_qty, 0)
                                                           stratis_good_qty,
                   NVL (TO_NUMBER (z.on_hand_op_stock_serviceable), 0)
                                                           on_hand_op_stock_serviceable,
                   x.nomenclature,
                   w.item_name_nomenclature,
                   x.scc,
                   w.physical_security_code,
                   x.shelf_life_code stratis_slc,
                   w.shelf_life_code sassy_slc,
                   NVL (x.ro_threshold, 0) ro_threshold,
                   NVL (TO_NUMBER (z.requisitioning_objective), 0)
                                                           requisitioning_objective,
                   x.price,
                   NVL (TO_NUMBER (w.unit_price), 0) / 100 unit_price,
                   'Y' in_sassy,
                   x.niin,
                   SUM (DECODE (y.cc, 'A', 0, NVL (y.qty, 0)))
                       - NVL (a.stratis_bad_qty, 0)
                                                           stratis_bad_qty,
                   NVL (TO_NUMBER (z.on_hand_unserviceable), 0)
                                                           on_hand_unserviceable,
                   --          decode(x.serial_control_flag||x.lot_control_flag,'NN','X'
                   --                                                           ,'Y') in_stratis,
                   'Y' IN_STRATIS,
                   z.ref_gabf_id,
                   records_read
            FROM niin_info x,
                 niin_location y,
                 ref_gabf z,
                 ref_mhif w,
                 (  SELECT niin_id,
                           SUM (
                                   DECODE (
                                           cc,
                                           'A', DECODE (issue_type, 'B', 0, NVL (qty, 0)),
                                           0))
                               stratis_good_qty,
                           SUM (
                                   DECODE (cc,
                                           'A', 0,
                                           DECODE (issue_type, 'B', 0, NVL (qty, 0))))
                               stratis_bad_qty
                    FROM issue
                    WHERE SUBSTR (scn, 2, 3) = TO_CHAR (SYSDATE, 'DDD')
                    GROUP BY niin_id) a
            WHERE     x.niin_Id = y.niin_id(+)
              AND x.niin = z.record_niin
              AND NVL (z.processed, 'N') = 'N'
              AND            --     z.on_hand_op_stock_serviceable > 0 and
                    z.record_niin = w.record_niin(+)
              AND x.niin_Id = a.niin_Id(+)
            GROUP BY a.stratis_good_qty,
                     a.stratis_bad_qty,
                     x.fsc,
                     z.record_fsc,
                     x.ui,
                     z.unit_of_issue,
                     z.on_hand_op_stock_serviceable,
                     x.nomenclature,
                     w.item_name_nomenclature,
                     x.scc,
                     w.physical_security_code,
                     x.shelf_life_code,
                     w.shelf_life_code,
                     x.ro_threshold,
                     z.requisitioning_objective,
                     x.price,
                     w.unit_price,
                     x.niin,
                     z.on_hand_unserviceable,
                     --          x.serial_control_flag,
                     --         x.lot_control_flag,
                     z.ref_gabf_id;

        count_lot      NUMBER;
        count_serial   NUMBER;
    BEGIN
        run_date := SYSDATE;

        SELECT COUNT (*)
        INTO records_read
        FROM niin_info x, ref_gabf y
        WHERE x.niin = y.record_niin AND NVL (y.processed, 'N') = 'N';

        INSERT INTO recon_run_date
        VALUES (run_date);

        FOR i IN get_recon_recs
            LOOP
            --
            -- populate recon_stratis_serial_Lot_num with the STRATIS serial number/lot control number combinations which differ
            -- from the corresponding NIIN in SASSY.
            --
                INSERT INTO recon_serial_Lot_num
                SELECT x.niin,
                       y.lot_con_Num,
                       y.serial_Number,
                       run_date,
                       z.location_label,
                       y.qty,
                       'STRATIS'
                FROM niin_Info x, serial_lot_num_track y, location z
                WHERE     x.niin = i.niin
                  AND x.niin_id = y.niin_id
                  AND y.location_id = z.location_id(+)
                  AND NOT EXISTS
                    (SELECT NULL
                     FROM ref_gabf_serial
                     WHERE     ref_gabf_id = i.ref_gabf_id
                       AND NVL (serial_number, 'X') =
                           NVL (y.serial_number, 'X')
                       AND NVL (lot_con_Num, 'X') =
                           NVL (y.lot_con_num, 'X'));

                count_serial := SQL%ROWCOUNT;

                --
                -- populate recon_stratis_sassy_Lot_num with the SASSY serial number/lot control number combinations which differ
                -- from the corresponding NIIN in
                --
                INSERT INTO recon_serial_Lot_num
                SELECT x.record_niin,
                       y.LOT_CON_NUM,
                       y.SERIAL_NUMBER,
                       run_date,
                       NULL,
                       y.qty,
                       'HOST'
                FROM ref_gabf x, ref_gabf_serial y
                WHERE     x.ref_gabf_id = y.ref_gabf_id
                  AND x.ref_gabf_id = i.ref_gabf_id
                  AND NOT EXISTS
                    (SELECT NULL
                     FROM serial_lot_num_track z, niin_info w
                     WHERE     w.niin_id = z.niin_id
                       AND w.niin = i.niin
                       AND NVL (z.serial_number, 'X') =
                           NVL (y.serial_number, 'X')
                       AND NVL (z.lot_con_Num, 'X') =
                           NVL (y.lot_con_num, 'X'));

                count_lot := SQL%ROWCOUNT;

                IF    NVL (i.fsc, 'x') <> NVL (RTRIM (LTRIM (i.record_fsc)), 'x')
                    OR NVL (i.ui, 'x') <> NVL (RTRIM (LTRIM (i.unit_of_issue)), 'x')
                    OR NVL (i.stratis_good_qty, 0) <>
                       NVL (i.on_hand_op_stock_serviceable, 0)
                    OR NVL (i.nomenclature, 'x') <>
                       NVL (RTRIM (LTRIM (i.item_name_nomenclature)), 'x')
                    OR NVL (i.scc, 'x') <>
                       NVL (RTRIM (LTRIM (i.physical_security_code)), 'x')
                    OR NVL (i.stratis_slc, 'x') <>
                       NVL (RTRIM (LTRIM (i.sassy_slc)), 'x')
                    OR NVL (i.ro_threshold, 0) <> NVL (i.requisitioning_objective, 0)
                    OR NVL (i.price, 0) <> NVL (i.unit_price, 0)
                    OR NVL (i.stratis_bad_qty, 0) <> NVL (i.on_hand_unserviceable, 0)
                THEN
                    --
                    -- store those items where the following fields differ between mhif and stratis:
                    --
                    --  fsc
                    --  ui
                    --  quantity (servicable)
                    --  nomenclature
                    --  security code
                    --  shelf life code
                    --  ro threshold
                    --  price
                    --  quantity (unserviceable)
                    --  serial number
                    --  lot control number
                    --
                    INSERT INTO recon_hist
                    VALUES (run_date,
                            i.fsc,
                            RTRIM (LTRIM (i.record_fsc)),
                            i.ui,
                            RTRIM (LTRIM (i.unit_of_issue)),
                            i.stratis_good_qty,
                            i.on_hand_op_stock_serviceable,
                            i.nomenclature,
                            RTRIM (LTRIM (i.item_name_nomenclature)),
                            i.scc,
                            RTRIM (LTRIM (i.physical_security_code)),
                            i.stratis_slc,
                            RTRIM (LTRIM (i.sassy_slc)),
                            i.ro_threshold,
                            i.requisitioning_objective,
                            i.price,
                            i.unit_price,
                            i.in_sassy,
                            i.niin,
                            i.stratis_bad_qty,
                            i.on_hand_unserviceable,
                            i.in_stratis,
                            records_read,
                            NULL,
                            NULL,
                            NULL,
                            NULL,
                            NULL,
                            NULL);
                ELSIF count_lot > 0 OR count_serial > 0
                THEN
                    INSERT INTO recon_hist (timestamp,
                                            in_sassy,
                                            in_stratis,
                                            niin,
                                            records_read,
                                            stratis_balance_servicable,
                                            sassy_balance_servicable,
                                            stratis_balance_unservicable,
                                            sassy_balance_unservicable)
                    VALUES (run_date,
                            i.in_sassy,
                            i.in_stratis,
                            i.niin,
                            i.records_read,
                            i.stratis_good_qty,
                            i.on_hand_op_stock_serviceable,
                            i.stratis_bad_qty,
                            i.on_hand_unserviceable);
                END IF;
            END LOOP;

        --
        -- store those items which exist in stratis but not on the mhif
        --
        INSERT INTO recon_hist (TIMESTAMP,
                                STRATIS_FSC,
                                STRATIS_UI,
                                STRATIS_BALANCE_SERVICABLE,
                                STRATIS_NOMEN,
                                STRATIS_SCC,
                                STRATIS_SLC,
                                STRATIS_RO,
                                STRATIS_PRICE,
                                IN_SASSY,
                                NIIN,
                                STRATIS_BALANCE_UNSERVICABLE,
                                in_stratis,
                                records_read)
        SELECT run_date,
               x.fsc,
               x.ui,
               SUM (DECODE (y.cc, 'A', NVL (y.qty, 0), 0)),
               x.nomenclature,
               x.scc,
               x.shelf_life_code,
               x.ro_threshold,
               x.price,
               'N',
               x.niin,
               SUM (DECODE (y.cc, 'A', 0, NVL (y.qty, 0))),
               'Y',
               records_read
        FROM niin_info x, niin_location y
        WHERE     x.niin_id = y.niin_id(+)
          AND NOT EXISTS
            (SELECT NULL
             FROM ref_gabf
             WHERE     record_niin = x.niin
               AND NVL (processed, 'N') = 'N')
        GROUP BY x.fsc,
                 x.ui,
                 x.nomenclature,
                 x.scc,
                 x.shelf_life_code,
                 x.ro_threshold,
                 x.price,
                 x.niin;

        --
        -- store serial and lot control numbers, if they exist, for those NIINSs which exist in stratis
        -- but not on the mhif (SASSY).
        --
        INSERT INTO recon_serial_lot_Num
        SELECT x.niin,
               y.lot_con_num,
               y.serial_number,
               run_date,
               z.location_label,
               y.qty,
               'STRATIS'
        FROM niin_Info x,
             serial_lot_num_track y,
             location z,
             recon_hist a
        WHERE     x.niin_id = y.niin_Id
          AND y.location_id = z.location_id(+)
          AND x.niin = a.niin
          AND a.in_sassy = 'N'
          AND a.in_stratis = 'Y'
          AND a.timestamp = run_date;

        --
        -- store those items which exist on the mhif but not in stratis
        --
        INSERT INTO recon_hist (TIMESTAMP,
                                sassy_FSC,
                                sassy_UI,
                                sassy_BALANCE_SERVICABLE,
                                sassy_NOMEN,
                                sassy_SCC,
                                sassy_SLC,
                                sassy_RO,
                                sassy_PRICE,
                                IN_SASSY,
                                NIIN,
                                sassy_BALANCE_UNSERVICABLE,
                                in_stratis,
                                records_read)
        SELECT run_date,
               z.record_fsc,
               z.unit_of_issue,
               z.on_hand_op_stock_serviceable,
               w.item_name_nomenclature,
               w.physical_security_code,
               w.shelf_life_code,
               z.requisitioning_objective,
               z.unit_price,
               'Y',
               z.record_niin,
               SUBSTR (z.on_hand_unserviceable, 2),
               'N',
               records_read
        FROM ref_gabf z, ref_mhif w
        WHERE     NVL (z.processed, 'N') = 'N'
          AND z.on_hand_op_stock_serviceable > 0
          AND z.record_niin = w.record_niin(+)
          AND NOT EXISTS
            (SELECT NULL
             FROM niin_info
             WHERE niin = z.record_niin);

        --
        -- store serial and lot control numbers, if they exist, for those NIINSs which exist in sassy
        -- but not in
        --
        INSERT INTO recon_serial_lot_num
        SELECT z.record_niin,
               w.lot_con_Num,
               w.serial_number,
               run_date,
               NULL,
               w.qty,
               'HOST'
        FROM ref_gabf z, ref_gabf_serial w, recon_hist a
        WHERE     z.ref_gabf_id = w.ref_gabf_id
          AND z.record_niin = a.niin
          AND a.in_sassy = 'Y'
          AND a.in_stratis = 'N'
          AND a.timestamp = run_date;

        COMMIT;
    END p_populate_recon_hist; --Kane chk with Mfan and reuse fns above from gcss imp proc


    /*******************************************************************************
    * p_POP_EXP_NIIN  (Procedure)
    *****************************************************************************/
    PROCEDURE p_pop_exp_niin
    AS
    BEGIN
        INSERT INTO error_queue (error_queue_id,
                                 status,
                                 created_by,
                                 created_date,
                                 eid,
                                 key_type,
                                 key_num,
                                 modified_by,
                                 modified_date,
                                 notes)
        SELECT NULL,
               'OPEN',
               1,
               SYSDATE,
               e.eid,
               'NIIN_LOC_ID',
               nl.niin_loc_id,
               NULL,
               NULL,
               'NIIN: '
                   || ni.niin
                   || ' Location: '
                   || l.location_label
                   || ' EXP Date: '
                   || nl.expiration_date
        FROM niin_location nl,
             error e,
             niin_Info ni,
             location l
        WHERE     TRUNC (nl.expiration_date) - TRUNC (SYSDATE) <= 10
          AND nl.cc = 'A'
          AND nl.niin_id = ni.niin_id
          AND nl.location_Id = l.location_id
          AND e.ERROR_CODE = 'EXPIRATION1'
          AND NOT EXISTS
            (SELECT NULL
             FROM error_queue
             WHERE     key_type = 'NIIN_LOC_ID'
               AND TO_NUMBER (key_num) = nl.niin_loc_Id);

        COMMIT;
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'p_pop_exp_niin -' || SQLERRM (SQLCODE));
                RAISE;
    END p_pop_exp_niin;

    /*****************************************************************************
   * -p_PROCESS_MHIF  (Procedure)
   *****************************************************************************/
    PROCEDURE p_process_mhif
    AS
    BEGIN
        pkg_stratis_app.p_process_mhif;
    END p_process_MHIF;


    /*******************************************************************************
   * p_PROCESS_PENDING_STOWS  (Procedure)
   *****************************************************************************/
    PROCEDURE p_process_pending_stows
    AS
        CURSOR get_pending_stows
            IS
            SELECT r.rcn, e.eid
            FROM receipt r, error e, site_info si
            WHERE     e.ERROR_CODE = 'STOW_DELAY'
              AND EXISTS
                (SELECT NULL
                 FROM stow s
                 WHERE     SUBSTR (s.status, 1, 11) IN ('STOW BYPASS',
                                                        'STOW READY',
                                                        'STOW REFUSE')
                   AND s.rcn = r.rcn
                   AND (SYSDATE - s.created_date) * 24 >=
                       si.pending_stow_time)
              AND NOT EXISTS
                (SELECT NULL
                 FROM error_queue
                 WHERE     key_type = 'RCN'
                   AND TO_NUMBER (key_num) = r.rcn
                   AND eid = e.eid);
    BEGIN
        FOR rec IN get_pending_stows
            LOOP
                INSERT INTO error_queue (error_queue_id,
                                         status,
                                         created_by,
                                         created_date,
                                         eid,
                                         key_type,
                                         key_num,
                                         modified_by,
                                         modified_date,
                                         notes)
                VALUES (NULL,
                        'OPEN',
                        1,
                        SYSDATE,
                        rec.eid,
                        'RCN',
                        rec.rcn,
                        NULL,
                        NULL,
                        NULL);
            END LOOP;

        COMMIT;
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            'p_process_pending_stows -' || SQLERRM (SQLCODE));
                RAISE;
    END p_process_pending_stows;


    /************************************************************************************************************
   * P_drop_partitions- This Procedure is created to drop partitions that are older than 6 months
   *************************************************************************************************************/
    PROCEDURE P_drop_partitions (
        pmd_#_Months_back_in   IN DATE DEFAULT LAST_DAY (
                ADD_MONTHS (SYSDATE, -6)),
        pmv_table_name_in      IN USER_TABLES.table_name%TYPE DEFAULT 'RECON_HIST')
        IS
    BEGIN
        FOR rec
            IN (  SELECT table_name,
                         partition_name,
                         TO_DATE (REPLACE (partition_name, 'TIMESTAMP', '01'),
                                  'dd_MON_YYYY')
                             PTN_date
                  FROM user_tab_partitions
                  WHERE table_name = pmv_table_name_in
                  ORDER BY PTN_date DESC)
            LOOP
                IF rec.PTN_date > pmd_#_Months_back_in
                THEN
                    DBMS_OUTPUT.put_line (
                                ' -- Ptn NOT DELETED = ' || rec.partition_name);
                ELSE
                    DBMS_OUTPUT.put_line (
                                ' alter table  '
                                || rec.table_name
                                || ' drop partition '
                                || rec.partition_name
                                || ';');

                    EXECUTE IMMEDIATE
                            ' alter table  '
                            || rec.table_name
                            || ' drop partition '
                            || rec.partition_name;
                END IF;
            END LOOP;
    END p_drop_partitions;

    /*******************************************************************************
   -- P_QUARTERLY_USAGE_BY_NIIN  (Procedure)
    *****************************************************************************/
    PROCEDURE p_quarterly_usage_by_niin (
        pmv_niin_in       IN     VARCHAR2,
        pmd_run_date_in   IN     DATE DEFAULT SYSDATE,
        cursor_a             OUT SYS_REFCURSOR)
    AS
        vn_niinid     niin_info.niin_id%TYPE;
        vd_run_date   DATE := SYSDATE;
    BEGIN
        IF REPLACE (pmv_niin_in, ' ') IS NULL
        THEN
            raise_application_error (-20001, 'NIIN can not be blank or NULL');
        ELSE
            vn_niinid := f_get_niinid (pmv_niin_in => pmv_niin_in);

            IF NVL (vn_niinid, 0) > 0
            THEN
                DBMS_OUTPUT.put_line ('b4 open in pkg NIIN_ID = ' || vn_niinid);

                vd_run_date := NVL (vd_run_date, SYSDATE);

                OPEN CURSOR_A FOR
                    SELECT    'Qtr '
                                  || ROW_NUMBER () OVER (ORDER BY qtr DESC)
                                  || ': '
                                  || range,
                              Receipts,
                              qty_rvcd_a,
                              qty_rvcd_f,
                              qty_issud_a,
                              qty_issud_f
                    FROM (WITH last5_qtrs
                                   AS (SELECT qtr,
                                              ADD_MONTHS (dt, (qtr - 1) * 3)
                                                  qtr_start_dt,
                                              LAST_DAY (
                                                      ADD_MONTHS (dt, (qtr - 1) * 3 + 2))
                                                  qtr_end_dt
                                       FROM (SELECT ADD_MONTHS (
                                                            TRUNC (vd_run_date, 'Q'),
                                                            -12)
                                                           dt,
                                                    ROWNUM qtr -- from what date param
                                             FROM all_users
                                             WHERE ROWNUM <= 5)), --how many qtrs param
                               receipts
                                   AS (SELECT TRUNC (rh.created_date, 'Q')
                                                                   qtr_start_dt,
                                              rh.created_date,
                                              NVL (rh.CC, 'A') CC,
                                              rh.quantity_inducted qty_received,
                                              0 qty_issued
                                       FROM receipt_hist rh
                                       WHERE rh.niin_id = vn_niinid
                                       UNION ALL
                                       SELECT TRUNC (ih.created_date, 'Q')
                                                               qtr_start_dt,
                                              ih.created_date,
                                              NVL (ih.CC, 'A') CC,
                                              0 qty_received,
                                              ih.qty qty_issued
                                       FROM issue_hist ih
                                       WHERE     ih.niin_id = vn_niinid
                                         AND ih.status = 'SHIPPED')
                          SELECT lq.qtr,
                                 TO_CHAR (lq.qtr_start_dt, 'MON')
                                     || '-'
                                     || TO_CHAR (lq.qtr_end_dt, 'MON-YYYY')
                                                         Range,
                                 COUNT (rh.created_date) Receipts,
                                 SUM (DECODE (rh.cc, 'A', rh.qty_received, 0))
                                                         qty_rvcd_a,
                                 SUM (DECODE (rh.cc, 'F', rh.qty_received, 0))
                                                         qty_rvcd_f,
                                 SUM (DECODE (rh.cc, 'A', rh.qty_issued, 0))
                                                         qty_issud_a,
                                 SUM (DECODE (rh.cc, 'F', rh.qty_issued, 0))
                                                         qty_issud_f
                          FROM receipts rh
                                   RIGHT OUTER JOIN last5_qtrs lq
                                                    ON rh.qtr_start_dt = lq.qtr_start_dt
                          GROUP BY lq.qtr,
                                   lq.qtr_start_dt,
                                   lq.qtr_end_dt,
                                   NVL (rh.CC, 'A')
                          ORDER BY qtr);

                DBMS_OUTPUT.put_line ('after OPEN CSR in pkg');
            ELSE
                DBMS_OUTPUT.put_line ('niin_id must be > 0 ; enter a valid niin');
            END IF;
        END IF;
    EXCEPTION
        WHEN OTHERS
            THEN
                DBMS_OUTPUT.put_line (SQLERRM (SQLCODE));
                RAISE;
    END p_quarterly_usage_by_niin;

    /******************************************************************************
   * p_RECVD_HIST_BY_NIIN  (Procedure)
   ******************************************************************************/
    PROCEDURE p_recvd_hist_by_niin (pmv_NIIN_IN   IN     VARCHAR2,
                                    CURSOR_A         OUT SYS_REFCURSOR)
    AS
        vn_niinid   niin_info.niin_id%TYPE;
    BEGIN
        IF REPLACE (pmv_niin_in, ' ') IS NULL
        THEN
            raise_application_error (-20001, 'NIIN can not be blank or NULL');
        ELSE
            vn_niinid := f_get_niinid (pmv_niin_in => pmv_niin_in);

            IF NVL (vn_niinid, 0) > 0
            THEN
                DBMS_OUTPUT.put_line ('b4 open in pkg NIIN_ID = ' || vn_niinid);

                OPEN CURSOR_A FOR
                    SELECT 'Qtr ' || ROWNUM sl#,
                           start_dt || ' THROUGH ' || end_dt range,
                           Receipts,
                           qty_rvcd_a,
                           qty_rvcd_f
                    FROM (WITH last5_qtrs
                                   AS (SELECT qtr,
                                              ADD_MONTHS (dt, (qtr - 1) * 3)
                                                  qtr_start_dt,
                                              LAST_DAY (
                                                      ADD_MONTHS (dt, (qtr - 1) * 3 + 2))
                                                  qtr_end_dt
                                       FROM (SELECT ADD_MONTHS (
                                                            TRUNC (SYSDATE, 'Q'),
                                                            -12)
                                                           dt,
                                                    ROWNUM qtr -- from what date param
                                             FROM all_users
                                             WHERE ROWNUM <= 5)), --how many qtrs param
                               receipts
                                   AS (SELECT TRUNC (rh.created_date, 'Q')
                                                               qtr_start_dt,
                                              rh.created_date,
                                              NVL (rh.CC, 'A') CC,
                                              rh.quantity_inducted
                                       FROM receipt_hist rh
                                       WHERE rh.niin_id = vn_niinid)
                          SELECT lq.qtr qtr,
                                 lq.qtr_start_dt start_dt,
                                 lq.qtr_end_dt end_dt,
                                 NVL (rh.CC, 'A') CC,
                                 COUNT (rh.created_date) Receipts,
                                 SUM (
                                         DECODE (rh.cc, 'A', rh.quantity_inducted, 0))
                                     qty_rvcd_a,
                                 SUM (
                                         DECODE (rh.cc, 'F', rh.quantity_inducted, 0))
                                     qty_rvcd_f
                                 --sum(rh.quantity_inducted) --over(partition by lq.q) qty
                          FROM receipts rh
                                   RIGHT OUTER JOIN last5_qtrs lq
                                                    ON rh.qtr_start_dt = lq.qtr_start_dt
                          GROUP BY lq.qtr,
                                   lq.qtr_start_dt,
                                   lq.qtr_end_dt,
                                   NVL (rh.CC, 'A')
                          ORDER BY qtr DESC);

                DBMS_OUTPUT.put_line ('after OPEN CSR in pkg');
            ELSE
                DBMS_OUTPUT.put_line ('niin_id must be > 0 ; enter a valid niin');
            END IF;
        END IF;
    EXCEPTION
        WHEN OTHERS
            THEN
                DBMS_OUTPUT.put_line (SQLERRM (SQLCODE));
                RAISE;
    END p_recvd_hist_by_niin;

    /*******************************************************************************
   *  p_REMOVE_HC_WALK_THRU  (Procedure)
   *****************************************************************************/
    PROCEDURE p_remove_hc_walk_thru
    AS
        CURSOR get_walk_thrus
            IS
            SELECT DISTINCT scn
            FROM picking
            WHERE status = 'WALKTHRU';

        CURSOR get_packing_consolidations
            IS
            SELECT DISTINCT packing_consolidation_id
            FROM picking
            WHERE status = 'WALKTHRU';

        vn_num_issues             NUMBER;
        vn_num_packing            NUMBER;
        vn_num_issues_walk_thru   NUMBER;
    BEGIN
        SAVEPOINT SP1;

        FOR rec IN get_walk_thrus
            LOOP
                SELECT COUNT (*)
                INTO vn_num_issues
                FROM picking
                WHERE scn = rec.scn;

                SELECT COUNT (*)
                INTO vn_num_issues_walk_thru
                FROM picking
                WHERE scn = rec.scn AND status = 'WALKTHRU';

                IF vn_num_issues = vn_num_issues_walk_thru
                THEN
                    DELETE FROM issue
                    WHERE scn = rec.scn;
                ELSE
                    DELETE FROM picking
                    WHERE scn = rec.scn AND status = 'WALKTHRU';
                END IF;
            END LOOP;

        FOR rec IN get_packing_consolidations
            LOOP
                SELECT COUNT (*)
                INTO vn_num_packing
                FROM picking
                WHERE packing_consolidation_id = rec.packing_consolidation_id;

                IF vn_num_packing = 0
                THEN
                    DELETE FROM packing_consolidation
                    WHERE packing_consolidation_id =
                          rec.packing_consolidation_id;
                END IF;
            END LOOP;

        DELETE FROM serial_lot_num_track slnt
        WHERE     slnt.qty = 0
          AND slnt.issued_flag = 'Y'
          AND NOT EXISTS
            (SELECT NULL
             FROM pick_serial_lot_Num
             WHERE serial_lot_num_track_id =
                   slnt.serial_lot_num_track_id);

        COMMIT;
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME || '.p_remove_hc_walk_thru -' || SQLERRM (SQLCODE));
                ROLLBACK TO SP1;
                RAISE;
    END p_remove_hc_walk_thru;

    /*******************************************************************************
    * p_RESET_SEQUENCES  (Procedure)
    *****************************************************************************/
    PROCEDURE p_reset_sequences
    AS
        vn_max_seq_val   NUMBER := 0.0;
        vv_seq_name      VARCHAR2 (30);
    BEGIN
        pkg_stratis_common.p_event_log (
                    '====>procedure p_reset_sequences started at '
                    || TO_CHAR (SYSDATE, 'dd-mon-yyyy hh24:mi:ss'));
        vv_seq_name := 'ISSUE_SCN_SEQ';

        EXECUTE IMMEDIATE 'select ' || vv_seq_name || '.nextval from dual'
            INTO vn_max_seq_val;

        EXECUTE IMMEDIATE
                'alter sequence '
                || vv_seq_name
                || ' increment by -'
                || vn_max_seq_val
                || ' minvalue 0';

        EXECUTE IMMEDIATE 'select ' || vv_seq_name || '.nextval from dual'
            INTO vn_max_seq_val;

        EXECUTE IMMEDIATE
                'alter sequence ' || vv_seq_name || ' increment by 1 minvalue 0';

        pkg_stratis_common.p_event_log (
                    'After alter sequence '
                    || vv_seq_name
                    || ' increment by -'
                    || vn_max_seq_val
                    || ' minvalue 0');

        vv_seq_name := 'STOW_SID_SEQ';

        EXECUTE IMMEDIATE 'select ' || vv_seq_name || '.nextval from dual'
            INTO vn_max_seq_val;

        EXECUTE IMMEDIATE
                'alter sequence '
                || vv_seq_name
                || ' increment by -'
                || vn_max_seq_val
                || ' minvalue 0';

        EXECUTE IMMEDIATE 'select ' || vv_seq_name || '.nextval from dual'
            INTO vn_max_seq_val;

        EXECUTE IMMEDIATE
                'alter sequence ' || vv_seq_name || ' increment by 1 minvalue 0';

        pkg_stratis_common.p_event_log (
                    'After alter sequence '
                    || vv_seq_name
                    || ' increment by 1 minvalue 0');

        vv_seq_name := 'CONSOL_SEQ';

        EXECUTE IMMEDIATE 'select ' || vv_seq_name || '.nextval from dual'
            INTO vn_max_seq_val;

        EXECUTE IMMEDIATE
                'alter sequence '
                || vv_seq_name
                || ' increment by -'
                || vn_max_seq_val
                || ' minvalue 0';

        EXECUTE IMMEDIATE 'select ' || vv_seq_name || '.nextval from dual'
            INTO vn_max_seq_val;

        EXECUTE IMMEDIATE
                'alter sequence ' || vv_seq_name || ' increment by 1 minvalue 0';

        pkg_stratis_common.p_event_log (
                    'After alter sequence '
                    || vv_seq_name
                    || ' increment by 1 minvalue 0');

        vv_seq_name := 'SPOOL_BATCH_NUM_SEQ';

        EXECUTE IMMEDIATE 'select ' || vv_seq_name || '.nextval from dual'
            INTO vn_max_seq_val;

        EXECUTE IMMEDIATE
                'alter sequence '
                || vv_seq_name
                || ' increment by -'
                || vn_max_seq_val
                || ' minvalue 0';

        EXECUTE IMMEDIATE 'select ' || vv_seq_name || '.nextval from dual'
            INTO vn_max_seq_val;

        EXECUTE IMMEDIATE
                'alter sequence ' || vv_seq_name || ' increment by 1 minvalue 0';

        pkg_stratis_common.p_event_log (
                    'After alter sequence '
                    || vv_seq_name
                    || ' increment by 1 minvalue 0');
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME || '.p_reset_sequences -' || SQLERRM (SQLCODE));
                RAISE;
    END p_reset_sequences;


    /*******************************************************************************
    * p_UPDATE_LOCATION_AVAIL_FLAG  (Procedure)
    *****************************************************************************/
    PROCEDURE p_update_location_avail_flag
    AS
        CURSOR csr_get_0_qty
            IS
            SELECT nl.location_id, nl.niin_loc_id
            FROM NIIN_LOCATION nl
            WHERE     NOT EXISTS
                (SELECT NULL
                 FROM stow
                 WHERE location_id = nl.location_id
                 UNION
                 SELECT NULL
                 FROM picking a
                 WHERE a.niin_Loc_Id = nl.niin_loc_Id
                 UNION
                 SELECT NULL
                 FROM inventory_item a
                 WHERE a.niin_Loc_id = nl.niin_Loc_id)
              AND EXISTS
                (SELECT location_id
                 FROM niin_location
                 WHERE     location_id = nl.location_id
                   AND niin_id = nl.niin_id
                   AND qty = 0);

        vn_count   NUMBER;
    BEGIN
        FOR rec IN csr_get_0_qty
            LOOP
                DELETE FROM niin_Location
                WHERE niin_loc_Id = rec.niin_loc_id;

                UPDATE niin_Location_hist
                SET user_id = 1
                WHERE niin_loc_Id = rec.niin_loc_id;

                SELECT COUNT (*)
                INTO vn_count
                FROM niin_location
                WHERE location_id = rec.location_id;

                IF vn_count = 0
                THEN
                    UPDATE location
                    SET availability_flag = 'A'
                    WHERE location_id = rec.location_id;
                END IF;
            END LOOP;
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.p_update_location_avail_flag -'
                            || SQLERRM (SQLCODE));
    END p_update_location_avail_flag;


    /*******************************************************************************
   -- p_UPD_NSN_CC  (Procedure)--20140215.001
    *****************************************************************************/
    PROCEDURE p_upd_nsn_cc
    AS
        CURSOR csr_get_failed_niins
            IS
            SELECT nl.niin_loc_id,
                   ni.niin,
                   ni.ui,
                   nl.qty,
                   ni.fsc,
                   nl.pc,
                   si.gcss_mc,
                   si.routing_id,
                   si.aac,
                   NVL (ni.lot_control_flag, 'x') lot_control_flag,
                   NVL (ni.serial_control_flag, 'x') serial_control_flag,
                   nl.cc
            FROM niin_location nl, niin_info ni, site_info si
            WHERE     nl.niin_id = ni.niin_id
              AND nl.cc = 'A'
              AND nl.num_extents = 0
              AND TRUNC (nl.expiration_date) <= TRUNC (SYSDATE)
              AND nl.qty > 0;

        vn_new_spool_id   NUMBER;
        vn_site_number    NUMBER;
        vn_ref_spool_id   spool.ref_spool_id%type;
    BEGIN
        FOR rec IN csr_get_failed_niins
            LOOP
                SAVEPOINT SP1;
                BEGIN

                    IF rec.lot_control_flag <> 'Y' AND rec.serial_control_flag <> 'Y'
                    THEN
                        UPDATE niin_Location
                        SET cc = 'F'
                        WHERE niin_loc_id = rec.niin_loc_id;

                        IF rec.gcss_mc = 'Y'
                        THEN
                            --EXECUTE IMMEDIATE  'alter trigger spool_transactions disable';
                            EXECUTE IMMEDIATE  'alter trigger tbidr_spool_transactions  disable';
                            vn_new_spool_id := spool_id_seq.NEXTVAL;
                            pkg_stratis_common.p_show_message('vn_new_spool_id = '||vn_new_spool_id);

                            vn_site_number := pkg_stratis_common.f_get_site_number;
                            pkg_stratis_common.p_show_message('vn_site_number ='||vn_site_number);
                            pkg_stratis_common.p_show_message(' TO_CHAR (SYSDATE, YDDD) ='||TO_CHAR (SYSDATE, 'YDDD'));

                            vn_ref_spool_id := vn_site_number|| TO_CHAR (SYSDATE, 'YDDD')|| vn_new_spool_id;
                            pkg_stratis_common.p_show_message('vn_ref_spool_id ='||vn_ref_spool_id);


                            INSERT INTO spool (spool_id,
                                               spool_def_mode,
                                               status,
                                               timestamp,
                                               transaction_type,
                                               gcssmc_xml,
                                               ref_spool_id)
                            VALUES (
                                       vn_new_spool_id,
                                       'G',
                                       'READY',
                                       SYSDATE,
                                       'DAC',
                                       '<?xml version = '
                                           || ''''
                                           || '1.0'
                                           || ''''
                                           || ' encoding='
                                           || ''''
                                           || 'UTF-8'
                                           || ''''
                                           || '?><shipmentReceiptsInCollection xmlns="http://www.usmc.mil/schemas/1/if/stratis">'
                                           || '<mRec><dIC>DAC</dIC>'
                                           || '<rIC>'
                                           || rec.routing_id
                                           || '</rIC>'
                                           || '<iPAAC>'
                                           || rec.aac
                                           || '</iPAAC>'
                                           || '<sDN>'
                                           || rec.aac
                                           || TO_CHAR (SYSDATE, 'YDDD')
                                           || CHR (
                                                       DOC_SER_NUM_GCSSMC_SEQ.NEXTVAL
                                                       / 1000
                                                   + 64)
                                           || LPAD (
                                               CAST (
                                                       MOD (
                                                               DOC_SER_NUM_GCSSMC_SEQ.NEXTVAL,
                                                               1000) AS VARCHAR2 (4)),
                                               3,
                                               '0')
                                           || '</sDN>'
                                           || '<nIIN>'
                                           || rec.niin
                                           || '</nIIN>'
                                           || '<uOI>'
                                           || rec.ui
                                           || '</uOI>'
                                           || '<fCC>'
                                           || rec.cc
                                           || '</fCC>'
                                           || '<qCC'
                                           || rec.cc
                                           || '>'
                                           || rec.qty
                                           || '</qCC'
                                           || rec.cc
                                           || '>'
                                           || '<keyD>'
                                           || TO_CHAR (
                                                       SYSTIMESTAMP,
                                                       'YYYY-MM-DD"T"HH24:MI:SS.FF3TZH:TZM')
                                           || '</keyD>'
                                           || '<txnDate>'
                                           || TO_CHAR (
                                                       SYSTIMESTAMP,
                                                       'YYYY-MM-DD"T"HH24:MI:SS.FF3TZH:TZM')
                                           || '</txnDate>'
                                           || '<spoolID>'
                                           --|| vn_new_spool_id --20140215.001
                                           || to_char(vn_ref_spool_id)
                                           || '</spoolID>'
                                           || '</mRec></shipmentReceiptsInCollection>',
--                                       vn_site_number
--                                    || TO_CHAR (SYSDATE, 'YDDD')
--                                    || vn_new_spool_id
                                       vn_ref_spool_id
                                   );

                            --EXECUTE IMMEDIATE 'alter trigger spool_transactions enable';
                            EXECUTE IMMEDIATE 'alter trigger tbidr_spool_transactions enable';

                        ELSE
                            INSERT INTO spool (spool_def_mode,
                                               status,
                                               timestamp,
                                               priority,
                                               transaction_type,
                                               rec_data)
                            VALUES (
                                       'C',
                                       'READY',
                                       SYSDATE,
                                       '05',
                                       'DAC',
                                       'DAC'
                                           || rec.routing_id
                                           || ' '
                                           || rec.fsc
                                           || rec.niin
                                           || '  '
                                           || rec.ui
                                           || LPAD (rec.qty, 5, 0)
                                           || rec.aac
                                           || TO_CHAR (SYSDATE, 'YDDD')
                                           || LPAD (DOC_SER_NUM_SEQ.NEXTVAL, 4, 0)
                                           || '                     '
                                           || rec.pc
                                           || 'F   '
                                           || rec.pc
                                           || 'A            ');
                        END IF;
                    ELSE
                        --20140217.001
                        IF NVL(pkg_stratis_common.f_get_config_param('pkg_stratis_procs.p_upd_nsn_cc.lock_location'),'N') = 'Y' THEN
                            UPDATE niin_Location
                            SET locked = 'Y'
                            WHERE niin_loc_id = rec.niin_loc_id;
                        END IF;

                        INSERT INTO error_queue (error_queue_id,
                                                 status,
                                                 created_by,
                                                 created_date,
                                                 eid,
                                                 key_type,
                                                 key_num,
                                                 modified_by,
                                                 modified_date,
                                                 notes)
                        SELECT NULL,
                               'OPEN',
                               1,
                               SYSDATE,
                               eid,
                               'NIIN_LOC_ID',
                               rec.niin_loc_id,
                               NULL,
                               NULL,
                               NULL
                        FROM error
                        WHERE     ERROR_CODE = 'EXPIRATION1'
                          AND NOT EXISTS
                            (SELECT NULL
                             FROM error_queue
                             WHERE     key_type = 'NIIN_LOC_ID'
                               AND TO_NUMBER (key_num) =
                                   rec.niin_loc_Id);
                    END IF;

                    pkg_stratis_common.p_commit;
                EXCEPTION
                    WHEN OTHERS
                        THEN
                            pkg_stratis_common.p_event_log (
                                        GCV_PKG_NAME
                                        || '.p_upd_nsn_cc - rec.niin_loc_id='
                                        || rec.niin_loc_id
                                        || '-'
                                        || SQLERRM (SQLCODE));
                            ROLLBACK TO SP1;
                END;
            END LOOP;
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME || '.p_upd_nsn_cc -' || SQLERRM (SQLCODE));
                RAISE;
    END p_upd_nsn_cc;

    /*******************************************************************************
   * p_RECEIPT_HIST_LOC_AVAIL_PROC - -- this procedure runs nightly.
    This procedure checks all receipts for the day, and will move receipts and related tranactions
         to history if:  all items for the receipt have been stowed or the receipt is marked as
         transhipped or complete. it then calls the procedure UPDATE_LOCATION_AVAIL_FLAG.
          This procedure reviews all empty locations and will move the
         niins to history and mark the location as available if there are no pending stows,
         picks or inventories against those niins.
   *******************************************************************************/
    PROCEDURE p_receipt_hist_loc_avail_proc
    AS
        CURSOR csr_receipts
            IS
            SELECT rcn, status FROM receipt;

        vn_num_stows    NUMBER;
        vn_num_stowed   NUMBER;
    BEGIN
        FOR rec IN csr_receipts
            LOOP
                SELECT COUNT (*)
                INTO vn_num_stows
                FROM stow
                WHERE rcn = rec.rcn;

                IF vn_num_stows > 0
                THEN
                    SELECT COUNT (*)
                    INTO vn_num_stowed
                    FROM stow
                    WHERE rcn = rec.rcn AND status = 'STOWED';

                    IF vn_num_stows = vn_num_stowed
                    THEN
                        IF rec.status = 'REWAREHOUSE'
                        THEN
                            DELETE FROM issue
                            WHERE scn IN (SELECT scn
                                          FROM picking
                                          WHERE pid IN (SELECT pid
                                                        FROM stow
                                                        WHERE rcn = rec.rcn));
                        END IF;

                        DELETE FROM receipt
                        WHERE rcn = rec.rcn;
                    END IF;
                ELSIF rec.status IN ('TRANSSHIPPED',
                                     'RECEIPT DRAFT',
                                     'RECEIPT COMPLETE',
                                     'REWAREHOUSE')
                THEN
                    DELETE FROM receipt
                    WHERE rcn = rec.rcn;
                END IF;
            END LOOP;

        p_update_location_avail_flag;

        COMMIT;
    EXCEPTION
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME
                            || '.p_receipt_hist_loc_avail_proc -'
                            || SQLERRM (SQLCODE));
                RAISE;
    END p_receipt_hist_loc_avail_proc;

    /******************************************************************************
   *  p_synch_tbl_id_to_seq -
   ******************************************************************************/
    PROCEDURE p_synch_tbl_id_to_seq (
        vv_chk_schema        VARCHAR2 DEFAULT 'Y',
        vv_compile_schema    VARCHAR2 DEFAULT 'Y')
        IS
        CURSOR csr_seqlist
            IS
            SELECT tbl, id, seq
            FROM (SELECT 'AUDIT_LOG' Tbl,
                         'AUDIT_LOG_ID' ID,
                         'AUDIT_LOG_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'COM_PORT' Tbl,
                         'COM_PORT_ID' ID,
                         'COM_PORT_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'CUSTOMER' Tbl,
                         'CUSTOMER_ID' ID,
                         'CUSTOMER_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'CUST_FLOOR_LOCATION' Tbl,
                         'CUST_FLOOR_LOCATION_ID' ID,
                         'CUST_FLOOR_LOCATION_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'DIVIDER_SLOTS' Tbl,
                         'DIVIDER_SLOTS_ID' ID,
                         'DIVIDER_SLOTS_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'DIVIDER_TYPE' Tbl,
                         'DIVIDER_TYPE_ID' ID,
                         'DIVIDER_TYPE_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'EQUIP' Tbl,
                         'EQUIP.EQUIPMENT_NUMBER' ID,
                         'EQUIP_NUM_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'ERROR_QUEUE' Tbl,
                         'ERROR_QUEUE_ID' ID,
                         'ERROR_QUEUE_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'FLOOR_LOCATION' Tbl,
                         'FLOOR_LOCATION_ID' ID,
                         'FLOOR_LOCATION_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'GCSSMC_IMPORTS_DATA' Tbl,
                         'GCSSMC_IMPORTS_DATA_ID' ID,
                         'GCSSMC_IMPORTS_DATA_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'GROUPS' Tbl,
                         'GROUPS.GROUP_ID' ID,
                         'GROUP_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'INV_SERIAL_LOT_NUM' Tbl,
                         'INV_SERIAL_LOT_NUM_ID' ID,
                         'INV_SERIAL_LOT_NUM_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'INVENTORY' Tbl,
                         'INVENTORY_ID' ID,
                         'INVENTORY_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'INVENTORY_ITEM' Tbl,
                         'INVENTORY_ITEM_ID' ID,
                         'INVENTORY_ITEM_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'LOCATION' Tbl,
                         'LOCATION_ID' ID,
                         'LOCATION_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'LOCATION_CLASSIFICATION' Tbl,
                         'LOC_CLASSIFICATION_ID' ID,
                         'LOC_CLASSIFICATION_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'LOCATION_HEADER' Tbl,
                         'LOC_HEADER_ID' ID,
                         'LOC_HEADER_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'LOCATION_HEADER_BIN' Tbl,
                         'LOCATION_HEADER_BIN_ID' ID,
                         'LOC_HEADER_BIN_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'NIIN_INFO' Tbl,
                         'NIIN_INFO.NIIN_ID' ID,
                         'NIIN_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'NIIN_LOCATION' Tbl,
                         'NIIN_LOCATION.NIIN_LOC_ID' ID,
                         'NIIN_LOC_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'PACKING_CONSOLIDATION' Tbl,
                         'PACKING_CONSOLIDATION_ID' ID,
                         'PACKING_CONSOLIDATION_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'PACKING_STATION' Tbl,
                         'PACKING_STATION_ID' ID,
                         'PACKING_STATION_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'PICK_SERIAL_LOT_NUM' Tbl,
                         'PICK_SERIAL_LOT_NUM' ID,
                         'PICK_SERIAL_LOT_NUM_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'PICKING' Tbl, 'PID' ID, 'PID_SEQ' SEQ FROM DUAL
                  UNION
                  SELECT 'RECEIPT' Tbl, 'RCN' ID, 'RECEIPT_RCN_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_DAC' Tbl, 'REF_DAC_ID' ID, 'REF_DAC_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_DASF' Tbl,
                         'REF_DASF_ID' ID,
                         'REF_DASF_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_DATALOAD_LOG' Tbl,
                         'REF_DATALOAD_LOG_ID' ID,
                         'REF_DATALOAD_LOG_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_GABF' Tbl,
                         'REF_GABF_ID' ID,
                         'REF_GABF_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_GABF_SERIAL' Tbl,
                         'REF_GABF_SERIAL_ID' ID,
                         'REF_GABF_SERIAL_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_GBOF' Tbl,
                         'REF_GBOF_ID' ID,
                         'REF_GBOF_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_MATS' Tbl,
                         'REF_MATS_ID' ID,
                         'REF_MATS_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_MHIF' Tbl,
                         'REF_MHIF_ID' ID,
                         'REF_MHIF_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_RECALL' Tbl,
                         'REF_RECALL_ID' ID,
                         'REF_RECALL_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_UI' Tbl, 'REF_UI_ID' ID, 'REF_UI_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'REF_UI_VALID' Tbl,
                         'REF_UI_VALID_ID' ID,
                         'REF_UI_VALID_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'ROUTE' Tbl, 'ROUTE.ROUTE_ID' ID, 'ROUTE_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'SECURITY_QUESTION' Tbl,
                         'SECURITY_QUESTION_ID' ID,
                         'SECURITY_QUESTION_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'SERIAL_LOT_NUM_TRACK' Tbl,
                         'SERIAL_LOT_NUM_TRACK_ID' ID,
                         'SERIAL_LOT_NUM_TRACK_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'SHIPPING' Tbl,
                         'SHIPPING_ID' ID,
                         'SHIPPING_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'SHIPPING_MANIFEST' Tbl,
                         'SHIPPING_MANIFEST_ID' ID,
                         'SHIPPING_MANIFEST_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'SHIPPING_ROUTE' Tbl,
                         'SHIPPING_ROUTE.ROUTE_ID' ID,
                         'SHIPPING_ROUTE_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'SPOOL' Tbl, 'SPOOL_ID' ID, 'SPOOL_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'STOW' Tbl, 'STOW_ID' ID, 'STOW_ID_SEQ' SEQ FROM DUAL
                  UNION
                  SELECT 'USER_GROUPS' Tbl,
                         'USER_GROUPS.USER_GROUP_ID' ID,
                         'USER_GROUP_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'USER_PASSWORD_UPDATE' Tbl,
                         'USER_PW_UPDATE_ID' ID,
                         'USER_PW_UPDATE_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'USERS' Tbl, 'USERS.USER_ID' ID, 'USER_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'WAC' Tbl, 'WAC_ID' ID, 'WAC_ID_SEQ' SEQ FROM DUAL
                  UNION
                  SELECT 'WAREHOUSE' Tbl,
                         'WAREHOUSE_ID' ID,
                         'WAREHOUSE_ID_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'ISSUE' Tbl, 'ISSUE.SCN' ID, 'ISSUE_SCN_SEQ' SEQ
                  FROM DUAL
                  UNION
                  SELECT 'STOW' Tbl, 'STOW.STOW_SID' ID, 'STOW_SID_SEQ' SEQ
                  FROM DUAL);

        vn_max_tbl_id   NUMBER := 0;
        vn_max_seq_id   NUMBER := 0;
        --vv_seq_name   user_sequences.sequence_name%type;
        vv_sql          VARCHAR2 (32765);
    BEGIN
        IF UPPER (NVL (vv_chk_schema, 'N')) = 'Y'
        THEN
            IF USER != 'SGA'
            THEN
                raise_application_error (
                        -20001,
                        'Please connect as SGA user to run this utility....');
            END IF;
        END IF;

        FOR rec IN csr_seqlist
            LOOP
                BEGIN
                    -- set maxval limit
                    EXECUTE IMMEDIATE 'ALTER SEQUENCE ' || rec.seq || ' NOMAXVALUE';

                    --get max table ID
                    vv_sql :=
                                'SELECT max('
                                || rec.id
                                || ') INTO :vn_max_tbl_id FROM '
                                || rec.tbl;

                    EXECUTE IMMEDIATE vv_sql INTO vn_max_tbl_id;

                    PKG_STRATIS_COMMON.p_show_message (
                                rec.tbl || '.' || rec.id || ' has maxval =' || vn_max_tbl_id);
                    --get max seq id
                    vv_sql :=
                                'SELECT '
                                || rec.seq
                                || '.nextval INTO :vn_max_seq_id FROM DUAL';

                    EXECUTE IMMEDIATE vv_sql INTO vn_max_seq_id;

                    PKG_STRATIS_COMMON.p_show_message (
                                rec.seq || ' has nextval =' || vn_max_seq_id);

                    IF vn_max_seq_id < vn_max_tbl_id
                    THEN
                        IF rec.seq IN ('STOW_SID_SEQ', 'ISSUE_SCN_SEQ')
                        THEN
                            EXECUTE IMMEDIATE
                                    'select ' || rec.seq || '.nextval from dual'
                                INTO vn_max_seq_id;

                            EXECUTE IMMEDIATE
                                    'alter sequence '
                                    || rec.seq
                                    || ' increment by -'
                                    || vn_max_seq_id
                                    || ' minvalue 0';
                        ELSE
                            --increment seq to match table max
                            EXECUTE IMMEDIATE
                                    'ALTER SEQUENCE '
                                    || rec.seq
                                    || ' increment by '
                                    || vn_max_tbl_id;

                            vv_sql :=
                                        'SELECT '
                                        || rec.seq
                                        || '.nextval INTO :vn_max_seq_id FROM DUAL';

                            EXECUTE IMMEDIATE vv_sql INTO vn_max_seq_id;

                            EXECUTE IMMEDIATE
                                    'ALTER SEQUENCE ' || rec.seq || ' increment by 1';
                        END IF;

                        PKG_STRATIS_COMMON.p_show_message (
                                    rec.seq || ' is reset table MAX  =' || vn_max_tbl_id);
                    END IF;
                EXCEPTION
                    WHEN OTHERS
                        THEN
                            PKG_STRATIS_COMMON.p_show_message (
                                        'p_synch_tbl_id_to_seq -'
                                        || vv_sql
                                        || ' - '
                                        || SQLERRM (SQLCODE));
                END;
            END LOOP;

        --2013.003
        p_reset_sequences;

        IF UPPER (NVL (vv_compile_schema, 'N')) = 'Y'
        THEN
            DBMS_UTILITY.compile_schema (USER);
        END IF;
    EXCEPTION
        WHEN OTHERS
            THEN
                PKG_STRATIS_COMMON.p_show_message (
                            'p_synch_tbl_id_to_seq -' || SQLERRM (SQLCODE));
                RAISE;
    END p_synch_tbl_id_to_seq;

    /******************************************************************************
   *  p_delete_event_log -
   ******************************************************************************/
    PROCEDURE p_delete_event_log (
        pmn_num_months_to_retain_in   IN NUMBER DEFAULT 3)
        IS
        vn_how_many_months_to_retain   NUMBER := 0.0;
    BEGIN
        IF SIGN (NVL (pmn_num_months_to_retain_in, 3)) IN (0, -1)
        THEN
            vn_how_many_months_to_retain := pmn_num_months_to_retain_in; -- go back num months is negative
        ELSIF SIGN (NVL (pmn_num_months_to_retain_in, 3)) = 1
        THEN
            vn_how_many_months_to_retain :=
                        -1 * ABS (pmn_num_months_to_retain_in); -- go back num months is positive
        END IF;

        DELETE FROM event_log
        WHERE trans_ts <
              ADD_MONTHS (SYSDATE, vn_how_many_months_to_retain);

        PKG_STRATIS_COMMON.p_show_message (
                    'p_delete_event_log - # of rows deleted =' || SQL%ROWCOUNT);
        pkg_stratis_common.p_commit;
    EXCEPTION
        WHEN OTHERS
            THEN
                PKG_STRATIS_COMMON.p_show_message (
                            'p_delete_event_log -' || SQLERRM (SQLCODE));
                RAISE;
    END p_delete_event_log;

    /******************************************************************************
   *  p_drop_all_STRATIS_jobs -
   ******************************************************************************/
    PROCEDURE p_drop_all_STRATIS_jobs (
        pmv_job_owner_schema_in   IN VARCHAR2 DEFAULT 'SGA')
        IS
        CURSOR csr_jobs
            IS
            SELECT job_name
            FROM user_scheduler_Jobs
            WHERE job_creator = NVL (pmv_job_owner_schema_in, 'SGA')
            ORDER BY 1;
    BEGIN
        FOR rec IN csr_Jobs
            LOOP
                pkg_stratis_common.p_show_message (
                            pmv_job_owner_schema_in
                            || '.'
                            || rec.job_name
                            || ' job is being dropped');
                DBMS_SCHEDULER.drop_job (job_name => rec.job_name);
            END LOOP;
    END p_drop_all_STRATIS_jobs;

    /******************************************************************************
   *  p_cre_all_STRATIS_jobs -
   ******************************************************************************/
    PROCEDURE p_cre_all_STRATIS_jobs (
        pmv_job_owner_schema_in   IN VARCHAR2 DEFAULT 'SGA')
        IS
    BEGIN
        p_drop_all_STRATIS_jobs (
                pmv_job_owner_schema_in   => pmv_job_owner_schema_in);

        pkg_stratis_common.p_show_message (
                'job bc4j_cleanup1 is being created to run pkg BC4J_CLEANUP.session_state_min');

        DBMS_SCHEDULER.create_job (
                job_name              => 'bc4j_cleanup1',
                job_type              => 'STORED_PROCEDURE',
            --job_action            => 'bc4j_cleanup.session_state_min', --04292015.001 FMW pkg func
                job_action            => 'bc4j_cleanup.session_state',
                start_date            => TRUNC (SYSDATE + 1) + (2 / 24),
                repeat_interval       => 'FREQ=daily',
                number_of_arguments   => 1,
                enabled               => FALSE);
        DBMS_SCHEDULER.set_job_argument_value (job_name            => 'bc4j_cleanup1',
                                               argument_position   => 1,
                                               argument_value      => 1440);
        DBMS_SCHEDULER.enable ('bc4j_cleanup1');


        pkg_stratis_common.p_show_message (
                'job cleanup_gcssmc_imports_data1 is being created to run PKG_STRAT_PROCS.p_cleaunp_gcssmc_imports_data');
        DBMS_SCHEDULER.create_job (
                job_name          => 'cleanup_gcssmc_imports_data1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_cleaunp_gcssmc_imports_data',
                start_date        => TRUNC (SYSDATE + 1),
                repeat_interval   => 'FREQ=daily',
                enabled           => TRUE);
        pkg_stratis_common.p_show_message (
                'job clear_gcssmc_hist1 is being created to run PKG_STRAT_PROCS.p_clear_gcssmc_hist');

        DBMS_SCHEDULER.create_job (
                job_name          => 'clear_gcssmc_hist1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_clear_gcssmc_hist',
                start_date        => SYSDATE,
                repeat_interval   => 'FREQ=weekly',
                enabled           => TRUE);

        pkg_stratis_common.p_show_message (
                'job monitor_objects1 is being created to run PKG_STRAT_PROCS.p_monitor_objects');
        DBMS_SCHEDULER.create_job (
                job_name          => 'monitor_objects1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_monitor_objects',
                start_date        => SYSDATE,
                repeat_interval   => 'FREQ=weekly',
                enabled           => TRUE);
        --
        pkg_stratis_common.p_show_message (
                'job nightly_updates1 is being created to run PKG_STRAT_PROCS.p_nightly_updates');
        DBMS_SCHEDULER.create_job (
                job_name          => 'nightly_updates1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_nightly_updates',
                start_date        => TRUNC (SYSDATE + 1),
                repeat_interval   => 'FREQ=daily',
                enabled           => TRUE);

        --  partitions are created by INTERVAL partition; this job is not needed any more

        --dbms_scheduler.create_job(job_name=>'partition_hist_tables1',
        --                             job_type=>'STORED_PROCEDURE',
        --                             job_action=>'partition_hist_tables',
        --                             start_date=> TO_DATE(lpad(TO_CHAR(SYSDATE,'MM'),2,'0')||'01'||TO_CHAR(SYSDATE,'YY'),'MMDDYY') ,
        --                             repeat_interval=>'FREQ=monthly;bymonthday=1',
        --                             enabled=>TRUE);
        --
        pkg_stratis_common.p_show_message (
                'job pending_stows1 is being created to run PKG_STRAT_PROCS.p_process_pending_stows');
        DBMS_SCHEDULER.create_job (
                job_name          => 'pending_stows1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_process_pending_stows',
                start_date        => SYSDATE,
                repeat_interval   => 'FREQ=hourly',
                enabled           => TRUE);

        pkg_stratis_common.p_show_message (
                'job pop_exp_niin1 is being created to run PKG_STRAT_PROCS.p_pop_exp_niin');
        DBMS_SCHEDULER.create_job (
                job_name          => 'pop_exp_niin1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_pop_exp_niin',
                start_date        => TRUNC (SYSDATE + 1) + (5 / 1440),
                repeat_interval   => 'FREQ=daily',
                enabled           => TRUE);
        --
        pkg_stratis_common.p_show_message (
                'job receipt_hist_loc_avail_proc1 is being created to run PKG_STRAT_PROCS.p_receipt_hist_loc_avail_proc');
        DBMS_SCHEDULER.create_job (
                job_name          => 'receipt_hist_loc_avail_proc1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_receipt_hist_loc_avail_proc',
                start_date        => TRUNC (SYSDATE + 1) + (30 / 1440),
                repeat_interval   => 'FREQ=daily',
                enabled           => TRUE);
        --
        pkg_stratis_common.p_show_message (
                'job remove_hc_walk_thru1 is being created to run PKG_STRAT_PROCS.p_remove_hc_walk_thru');
        DBMS_SCHEDULER.create_job (
                job_name          => 'remove_hc_walk_thru1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_remove_hc_walk_thru',
                start_date        => TRUNC (SYSDATE + 1) + (15 / 1440),
                repeat_interval   => 'FREQ=daily',
                enabled           => TRUE);
        --
        pkg_stratis_common.p_show_message (
                'job reset_sequences1 is being created to run PKG_STRAT_PROCS.p_reset_sequences');
        DBMS_SCHEDULER.create_job (
                job_name          => 'reset_sequences1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_reset_sequences',
                start_date        => TRUNC (SYSDATE + 1),
                repeat_interval   => 'FREQ=daily',
                enabled           => TRUE);

        pkg_stratis_common.p_show_message (
                'job upd_nsn_cc1 is being created to run PKG_STRAT_PROCS.p_upd_nsn_cc');
        DBMS_SCHEDULER.create_job (
                job_name          => 'upd_nsn_cc1',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_upd_nsn_cc',
                start_date        => TRUNC (SYSDATE + 1) + (10 / 1440),
                repeat_interval   => 'FREQ=daily',
                enabled           => TRUE);

        pkg_stratis_common.p_show_message (
                'job Job_delete_event_log is being created to run PKG_STRAT_PROCS.p_delete_event_log');
        DBMS_SCHEDULER.create_job (
                job_name          => 'Job_delete_event_log',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_delete_event_log',
                start_date        => TRUNC (SYSDATE + 1) + (10 / 1440),
                repeat_interval   => 'FREQ=monthly',
                enabled           => TRUE);
        -- 20140624.001
        pkg_stratis_common.p_show_message (
                'job Job_Write_Monitor_log is being created to run PKG_STRAT_PROCS.p_write_monitor_log');
        DBMS_SCHEDULER.create_job (
                job_name          => 'Job_write_monitor_log',
                job_type          => 'STORED_PROCEDURE',
                job_action        => 'PKG_STRAT_PROCS.p_write_monitor_log',
                start_date        => TRUNC (SYSDATE + 1) + (10 / 1440),
                repeat_interval   => 'FREQ=daily',
                enabled           => TRUE);

    END p_cre_all_STRATIS_jobs;
/******************************************************************************
   *  p_write_monitor_log -DG0080 proc called from nightly DB Sched job
   *    that writes USERS table rows for a diff shell script
   *  20140624.001
   ******************************************************************************/
    PROCEDURE p_write_monitor_log
        IS
        vfptr     UTL_FILE.file_type;
        vv_dir    VARCHAR2 (50) := 'MONITOR_LOG_DIR';
        vv_name   VARCHAR2 (30) := 'user_priv_audit'||to_char(sysdate,'mmddyyyy')||'.txt';
    BEGIN
        vfptr := UTL_FILE.fopen (vv_dir, vv_name, 'W',32000);

        FOR rec IN (  SELECT USER_ID,
                             STATUS,
                             EFF_START_DT,
                             EFF_END_DT,
                             LOCKED,
                             ACCT_TYPES,
                             GROUPS,
                             GROUP_PRIVS,
                             ACCT_PRIVS
                      FROM v_user_profile
                      ORDER BY user_id DESC)
            LOOP
                dbms_output.put_line('line length ='||length(rec.USER_ID
                    || ','
                    || rec.STATUS
                    || ','
                    || TO_CHAR (rec.EFF_START_DT, 'dd-mon-yyyy hh24:mi:ss')
                    || ','
                    || TO_CHAR (rec.EFF_END_DT, 'dd-mon-yyyy hh24:mi:ss')
                    || ','
                    || rec.LOCKED
                    || ','
                    || rec.ACCT_TYPES
                    || ','
                    || rec.GROUPS
                    || ','
                    || rec.GROUP_PRIVS
                    || ','
                    || rec.ACCT_PRIVS));
                UTL_FILE.put_line (
                        vfptr,
                        rec.USER_ID
                            || ','
                            || rec.STATUS
                            || ','
                            || TO_CHAR (rec.EFF_START_DT, 'dd-mon-yyyy hh24:mi:ss')
                            || ','
                            || TO_CHAR (rec.EFF_END_DT, 'dd-mon-yyyy hh24:mi:ss')
                            || ','
                            || rec.LOCKED
                            || ','
                            || rec.ACCT_TYPES
                            || ','
                            || rec.GROUPS
                            || ','
                            || rec.GROUP_PRIVS
                            || ','
                            || rec.ACCT_PRIVS);
            END LOOP;

        UTL_FILE.fclose (vfptr);
    EXCEPTION
        WHEN UTL_FILE.INVALID_PATH
            THEN
                DBMS_OUTPUT.PUT_LINE ('invalid_path');
                RAISE;
        WHEN UTL_FILE.INVALID_MODE
            THEN
                DBMS_OUTPUT.PUT_LINE ('invalid_mode');
                RAISE;
        WHEN UTL_FILE.INVALID_FILEHANDLE
            THEN
                DBMS_OUTPUT.PUT_LINE ('invalid_filehandle');
                RAISE;
        WHEN UTL_FILE.INVALID_OPERATION
            THEN
                DBMS_OUTPUT.PUT_LINE ('invalid_operation');
                RAISE;
        WHEN UTL_FILE.READ_ERROR
            THEN
                DBMS_OUTPUT.PUT_LINE ('read_error');
                RAISE;
        WHEN UTL_FILE.WRITE_ERROR
            THEN
                DBMS_OUTPUT.PUT_LINE ('write_error');
                RAISE;
        WHEN UTL_FILE.INTERNAL_ERROR
            THEN
                DBMS_OUTPUT.PUT_LINE ('internal_error');
                RAISE;
        WHEN OTHERS
            THEN
                pkg_stratis_common.p_event_log (
                            GCV_PKG_NAME||' - p_write_monitor_log' || SQLERRM (SQLCODE));
    END p_write_monitor_log;
END pkg_strat_procs;
/

