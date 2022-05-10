alter table mls2_sites
    add (
        pdb_name varchar2(10),
        pdb_username varchar2(50),
        pdb_password varchar2(50),
        flyway_username varchar2(50),
        flyway_password varchar2(50),
        flyway_schema varchar2(15) DEFAULT 'SGA',
        sort_order varchar(15),
        status_id number
        );