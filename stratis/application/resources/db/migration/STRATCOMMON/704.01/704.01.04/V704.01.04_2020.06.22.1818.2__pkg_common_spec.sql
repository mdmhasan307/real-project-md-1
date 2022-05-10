create or replace PACKAGE INNOV.pkg_common
AS
    TYPE UserType IS RECORD
    (
        site_name        VARCHAR2 (10),
        USER_ID          NUMBER,
        FIRST_NAME       VARCHAR2 (50),
        MIDDLE_NAME      VARCHAR2 (20),
        LAST_NAME        VARCHAR2 (50),
        STATUS           VARCHAR2 (10),
        LAST_LOGIN       DATE,
        USERNAME         VARCHAR2 (50),
        VISIBLE_FLAG     VARCHAR2 (1),
        LOGGED_IN        VARCHAR2 (1),
        TEMP_KEY         VARCHAR2 (20),
        LOGGED_IN_HH     VARCHAR2 (1),
        TEMP_KEY_HH      VARCHAR2 (20),
        LAST_LOGIN_HH    DATE,
        LOCKED           VARCHAR2 (1),
        CAC_NUMBER       VARCHAR2 (50),
        EFF_START_DT     DATE,
        EFF_END_DT       DATE,
        TRANS_TS         DATE,
        MOD_BY_ID        NUMBER
    );

    TYPE UserCollection IS TABLE OF UserType;

    FUNCTION f_pdb_up (Pmv_db_link_name_In IN VARCHAR2)
        RETURN PLS_INTEGER;

    FUNCTION f_pdb_localtime (Pmv_db_link_name_In IN VARCHAR2)
        RETURN date;

    FUNCTION fp_get_all_users (p_rows IN NUMBER DEFAULT 100)
        RETURN UserCollection
        PIPELINED;
END pkg_common;
