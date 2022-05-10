CREATE TABLE "INNOV".SETTINGS_SYSTEM
(
    "ID" RAW(16) NOT NULL ENABLE,
    "SYSTEM_UUID" RAW(16) NOT NULL ENABLE
);

ALTER TABLE "INNOV"."SETTINGS_SYSTEM"
    ADD CONSTRAINT SETTINGS_SYSTEM_ID_PK PRIMARY KEY("ID");

ALTER TABLE "INNOV"."SETTINGS_SYSTEM" ADD CONSTRAINT SETTINGS_SYSTEM_SYSTEM_UUID_UQ UNIQUE ("SYSTEM_UUID");

COMMENT ON TABLE "INNOV"."SETTINGS_SYSTEM" IS 'Table contains a single record with general settings information on this STRATIS cluster.';

COMMENT ON COLUMN "INNOV"."SETTINGS_SYSTEM"."ID" IS 'The unique primary key for this table.';
COMMENT ON COLUMN "INNOV"."SETTINGS_SYSTEM"."SYSTEM_UUID" IS 'This value is the unique system identification UUID for this STRATIS cluster.  No other STRATIS clusters should contain the same value.';

--Commented out for this initial M7 where App Role Doesn't exist.
-- Only allow the application account to select and update these records.  Do not permit insert or delete.
--GRANT SELECT, UPDATE ON "INNOV"."SETTINGS_SYSTEM" TO "STRAT_COMMON_APP_ROLE";
