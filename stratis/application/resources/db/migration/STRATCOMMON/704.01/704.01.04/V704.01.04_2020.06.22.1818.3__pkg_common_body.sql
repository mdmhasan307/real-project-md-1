create or replace PACKAGE BODY INNOV.pkg_common
AS
    FUNCTION f_pdb_up (Pmv_db_link_name_In IN VARCHAR2)
        RETURN PLS_INTEGER
    IS
        vi_retval   PLS_INTEGER := 0;
    BEGIN
        EXECUTE IMMEDIATE 'select 1 from dual@' || Pmv_db_link_name_In
            INTO vi_retval;

        DBMS_OUTPUT.put_line (
               'select 1 from dual@'
            || Pmv_db_link_name_In
            || ' retval ='
            || vi_retval);
        RETURN (vi_retval);
    EXCEPTION
        WHEN OTHERS
        THEN
            RETURN (-1);
    END f_pdb_up;

    FUNCTION f_pdb_localtime (Pmv_db_link_name_In IN VARCHAR2)
        RETURN date
    IS
        vd_retval   DATE;
    BEGIN
           EXECUTE IMMEDIATE 'select systimestamp from dual@' || Pmv_db_link_name_In
            INTO vd_retval;

        DBMS_OUTPUT.put_line (
              'select systimestamp from dual@'
            || Pmv_db_link_name_In
            || ' retval ='
            || vd_retval);
        RETURN (vd_retval);
    EXCEPTION
        WHEN OTHERS
        THEN
            RETURN (null);
    END f_pdb_localtime;

    FUNCTION fp_get_all_users (p_rows IN NUMBER DEFAULT 100)
        RETURN UserCollection
        PIPELINED
    IS
        vRec       pkg_common.UserCollection;
        vv_stmt    VARCHAR2 (4000);
        vA_users   pkg_common.UserCollection;
    BEGIN
        FOR rec IN (SELECT site_name, db_link_name FROM mls2_sites)
        LOOP
            -- if link pdb accessible
            CASE
                WHEN f_pdb_up (rec.db_link_name) = 1
                THEN
                    CASE
                        WHEN vv_stmt IS NULL
                        THEN                      -- first site just a selecet
                            vv_stmt :=
                                   ' SELECT '''
                                || rec.site_name
                                || ''' site_name,'
                                || rec.site_name
                                || '.* FROM users@'
                                || rec.db_link_name
                                || ' '
                                || rec.site_name;
                        ELSE                  -- addtl sites with union clause
                            vv_stmt :=
                                   vv_stmt
                                || '  UNION ALL SELECT '''
                                || rec.site_name
                                || ''' site_name,'
                                || rec.site_name
                                || '.* FROM users@'
                                || rec.db_link_name
                                || ' '
                                || rec.site_name;
                    END CASE;
                ELSE
                    NULL;
            END CASE;
        END LOOP;

        DBMS_OUTPUT.put_line ('vv_stmt =' || vv_stmt);

        --
        --Bulk all rows for the above select from dblink. users
        --
        EXECUTE IMMEDIATE vv_stmt
            BULK COLLECT INTO vA_users;

        FOR ctr IN 1 .. vA_users.COUNT
        LOOP
            DBMS_OUTPUT.put_line (vA_users (ctr).username);
            PIPE ROW (vA_users (ctr));
        END LOOP;

        RETURN;
    END fp_get_all_users;
END pkg_common;
/
--GRANT EXECUTE ON INNOV.pkg_common TO STRAT_COMMON_APP_ROLE;