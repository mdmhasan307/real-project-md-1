--------------------------------------------------------
--  DDL for Table MLS2_INBOUND_MSG
--------------------------------------------------------
CREATE TABLE "INNOV"."MLS2_INBOUND_MSG"
(	"ID" RAW(16) NOT NULL ENABLE,
     "SOURCE_SYSTEM_ID" RAW(16) NOT NULL ENABLE,
     "TYPE_ID" NUMBER(*,0) NOT NULL ENABLE,
     "STATUS_ID" NUMBER(*,0) NOT NULL ENABLE,
     "RECEIVED_COUNT" NUMBER(6,0) NOT NULL ENABLE,
     "DATE_RECEIVED" TIMESTAMP (6) NOT NULL ENABLE,
     "DATE_PROCESSED" TIMESTAMP (6),
     "PAYLOAD" CLOB,
     "PAYLOAD_MESSAGE_ID" RAW(16) NOT NULL ENABLE,
     "PAYLOAD_CLASS" VARCHAR2(250 BYTE) NOT NULL ENABLE,
     "PAYLOAD_CLASS_VERSION" NUMBER(6,0) NOT NULL ENABLE,
     "PROCESSED_COUNT" NUMBER(6,0) DEFAULT 0 NOT NULL ENABLE,
     "STATUS_MESSAGE" VARCHAR2(4000 BYTE),
     "SITE_IDENTIFIER" VARCHAR2(10)
) ;
alter table "INNOV"."MLS2_INBOUND_MSG" add constraint MLS2_INB_MSG_PK primary key("ID");
alter table "INNOV"."MLS2_INBOUND_MSG" add constraint MLS2_INB_MSG_SITE_IDENT_FK foreign key("SITE_IDENTIFIER")
    REFERENCES "INNOV"."MLS2_SITES" ("SITE_NAME");

--GRANT SELECT, INSERT, UPDATE, DELETE ON "INNOV"."MLS2_INBOUND_MSG" TO "STRAT_COMMON_APP_ROLE";

--------------------------------------------------------
--  DDL for Table MLS2_OUTBOUND_MSG
--------------------------------------------------------
CREATE TABLE "INNOV"."MLS2_OUTBOUND_MSG"
(	"ID" RAW(16) NOT NULL ENABLE,
     "DESTINATION_SYSTEM_ID" RAW(16),
     "TYPE_ID" NUMBER(*,0) NOT NULL ENABLE,
     "STATUS_ID" NUMBER(*,0) NOT NULL ENABLE,
     "RELATED_INBOUND_MSG_ID" RAW(16),
     "SENT_COUNT" NUMBER(6,0) NOT NULL ENABLE,
     "DATE_QUEUED" TIMESTAMP (6) NOT NULL ENABLE,
     "DATE_SENT" TIMESTAMP (6),
     "DATE_PROCESSED" TIMESTAMP (6),
     "PAYLOAD" CLOB,
     "PAYLOAD_MESSAGE_ID" RAW(16) NOT NULL ENABLE,
     "PAYLOAD_CLASS" VARCHAR2(250 BYTE) NOT NULL ENABLE,
     "PAYLOAD_CLASS_VERSION" NUMBER(6,0) NOT NULL ENABLE,
     "PROCESSED_COUNT" NUMBER(6,0) DEFAULT 0 NOT NULL ENABLE,
     "STATUS_MESSAGE" VARCHAR2(4000 BYTE),
     "SITE_IDENTIFIER" VARCHAR2(10)
     );
alter table "INNOV"."MLS2_OUTBOUND_MSG" add constraint MLS2_OUTB_MSG_PK primary key("ID");
alter table "INNOV"."MLS2_OUTBOUND_MSG" add constraint MLS2_OUTB_MSG_RIM unique("RELATED_INBOUND_MSG_ID");
alter table "INNOV"."MLS2_OUTBOUND_MSG" add constraint MLS2_OUTB_MSG_SITE_IDENT_FK foreign key("SITE_IDENTIFIER")
    REFERENCES "INNOV"."MLS2_SITES" ("SITE_NAME");


--GRANT SELECT, INSERT, UPDATE, DELETE ON "INNOV"."MLS2_OUTBOUND_MSG" TO "STRAT_COMMON_APP_ROLE";